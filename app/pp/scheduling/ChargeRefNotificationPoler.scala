/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pp.scheduling

import java.util.concurrent.{Executors, TimeUnit}

import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import pp.config.AppConfig
import pp.services.ChargeRefService

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

@Singleton
class ChargeRefNotificationPoler @Inject() (
    actorSystem:      ActorSystem,
    appConfig:        AppConfig,
    chargeRefService: ChargeRefService
)(implicit executionContext: ExecutionContext,
  applicationLifecycle: ApplicationLifecycle) {

  case object Poll

  class ContinuousPollingActor extends Actor {

    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    override def receive: Receive = {

      case Poll =>
        Logger.debug("Inside receive")
        chargeRefService.processOneWorkItem() andThen {
          case Success(true) =>
            Logger.debug("received Success(true)")
            self ! Poll
          case Success(false) =>
            Logger.debug("recevied success(false)")
            context.system.scheduler.scheduleOnce(appConfig.pollerRetryAfterFailureIntervalFinate, self, Poll)
          case Failure(e) =>
            Logger.warn("Queue processing failed", e)
            context.system.scheduler.scheduleOnce(appConfig.pollerIntervalFinate, self, Poll)

          //context.system.scheduler.scheduleOnce(appConfig.pollerRetryAfterFailureIntervalFinate, self, Poll)
        }
        ()

    }

  }

  private lazy val pollingActors = List.fill(appConfig.pollerInstances)(actorSystem.actorOf(Props(new ContinuousPollingActor())))

  pollingActors.foreach(e => Logger.debug("AAAAAAAA " + e.path.toString))

  actorSystem.actorSelection("/user/*") ! PoisonPill
  private val bootstrap = new Runnable {
    override def run(): Unit = {
      pollingActors.foreach { pollingActor =>
        pollingActor ! Poll
      }
    }
  }

  private def shutDown(): Unit = {
    pollingActors.foreach { pollingActor =>
      pollingActor ! PoisonPill
    }
  }

  if (appConfig.pollerEnabled) {
    Logger.debug("about to start ChargeRefNotificationPoler")

    // Start the poller after a delay.
    Executors.newScheduledThreadPool(1).schedule(
      bootstrap, appConfig.pollerIntervalFinate.toMillis, TimeUnit.MILLISECONDS)

    applicationLifecycle.addStopHook { () =>
      shutDown()
      Future.successful(())
    }
  } else {
    Logger.debug("ChargeRefNotificationPoler is disabled")
  }
}

