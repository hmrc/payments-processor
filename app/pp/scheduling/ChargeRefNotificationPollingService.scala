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

import akka.actor.ActorSystem
import pp.config.QueueConfig
import com.google.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.play.scheduling.ExclusiveScheduledJob

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import pp.services.ChargeRefService

@Singleton
class ChargeRefNotificationPollingService @Inject() (actorSystem:      ActorSystem,
                                                     queueConfig:      QueueConfig,
                                                     chargeRefService: ChargeRefService)(
    implicit
    ec: ExecutionContext) extends ExclusiveScheduledJob {

  override def name: String = "ChargeRefNotificationPollingService"

  override def executeInMutex(implicit ec: ExecutionContext): Future[Result] =
    chargeRefService.retrieveWorkItems.map(items => Result(s"Processed ${items.size} charge references"))

  lazy val initialDelay: FiniteDuration = queueConfig.pollerInitialDelay
  lazy val interval: FiniteDuration = queueConfig.pollerInterval

  Logger.debug(s"Starting ChargeRefNotificationPollingService, Initial delay: ${initialDelay}, Polling interval: ${interval}")

  def executor()(implicit ec: ExecutionContext): Unit = {
    execute.onComplete({
      case Success(Result(res)) =>
        Logger.debug(res)
      case Failure(throwable) =>
        Logger.warn(s"Exception completing work item", throwable)
    })
  }

  actorSystem.scheduler.schedule(initialDelay, interval) {
    if (queueConfig.pollerEnabled) {
      executor()
    } else {
      Logger.debug("Polling is enabled is false")
    }
  }
}
