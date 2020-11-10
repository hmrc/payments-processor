/*
 * Copyright 2020 HM Revenue & Customs
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

import akka.actor.{ActorSystem, Cancellable}
import play.api.Logger
import pp.config.QueueConfig
import pp.model.WorkItemFields
import pp.services.WorkItemService
import uk.gov.hmrc.play.scheduling.ExclusiveScheduledJob

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

abstract class PollingService[P <: WorkItemFields](val actorSystem:     ActorSystem,
                                                   val queueConfig:     QueueConfig,
                                                   val workItemService: WorkItemService[P])(
    implicit
    ec: ExecutionContext) extends ExclusiveScheduledJob {

  lazy val initialDelay: FiniteDuration = queueConfig.pollerInitialDelay
  lazy val interval: FiniteDuration = queueConfig.pollerInterval
  private val logger: Logger = Logger(this.getClass.getSimpleName)

  logger.debug(s"Starting $name, Initial delay: $initialDelay, Polling interval: $interval")

  callExecutor(name)

  override def executeInMutex(implicit ec: ExecutionContext): Future[Result] =
    workItemService.retrieveWorkItems.map(items => Result(s"$name: Processed ${items.size} items"))

  def callExecutor(name: String)(implicit ec: ExecutionContext): Cancellable = {
    actorSystem.scheduler.schedule(initialDelay, interval) {
      if (queueConfig.pollerEnabled) {
        executor(name)
      } else {
        logger.warn(s"$name: Poller enabled is false")
      }
    }
  }

  def executor(name: String)(implicit ec: ExecutionContext): Unit = {
    execute.onComplete({
      case Success(Result(res)) =>
        logger.debug(res)
      case Failure(throwable) =>
        logger.error(s"$name: Exception completing work item", throwable)
    })
  }
}
