/*
 * Copyright 2022 HM Revenue & Customs
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

package pp.services

import play.api.Logger
import pp.config.QueueConfig
import pp.model.MyWorkItemFields
import pp.scheduling.NotificationRepo
import uk.gov.hmrc.mongo.workitem.{ProcessingStatus, WorkItem}

import java.time.{Clock, LocalDateTime}
import scala.concurrent.{ExecutionContext, Future}

trait WorkItemService[P <: MyWorkItemFields] {

  val clock: Clock

  val repo: NotificationRepo[P]

  implicit val executionContext: ExecutionContext

  val logger: Logger

  val queueConfig: QueueConfig

  def sendWorkItem(workItem: WorkItem[P]): Future[Unit]

  def isAvailable(workItem: MyWorkItemFields): Boolean = {
    val time = LocalDateTime.now(clock)
    time.isBefore(workItem.availableUntil)
  }

  def availableUntil(time: LocalDateTime): LocalDateTime = time.plus(queueConfig.queueAvailableFor)

  def markAsPermFailed(acc: Seq[WorkItem[P]], workItem: WorkItem[P]): Future[Seq[WorkItem[P]]] = {
    logger.warn(s"payments-processor: Failed to process workitem ${workItem.item.toString}")
    repo.markAs(workItem.id, ProcessingStatus.PermanentlyFailed)
      .map(_ => acc :+ workItem)
  }

  def processThenMarkAsComplete(acc: Seq[WorkItem[P]], workItem: WorkItem[P]): Future[Seq[WorkItem[P]]] = {
    logger.debug("inside processThenMarkAsComplete")

    sendWorkItem(workItem)
      .map(_ => repo.completeAndDelete(workItem.id))
      .map(_ => acc :+ workItem)
      .recoverWith {
        case _ =>
          repo.markAs(workItem.id, ProcessingStatus.Failed).map(_ => acc)
      }
  }

  def retrieveWorkItems: Future[Seq[WorkItem[P]]] = {

      @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
      def sendNotificationIfFound(count: Int, sentWorkItems: Seq[WorkItem[P]]): Future[Seq[WorkItem[P]]] = {

          def retrieveWorkItem(count: Int): Future[Option[WorkItem[P]]] = {
            if (count == queueConfig.pollLimit) Future successful None
            else repo.pullOutstanding
          }

        retrieveWorkItem(count).flatMap {
          case None => Future successful sentWorkItems
          case Some(workItem) =>
            if (isAvailable(workItem.item)) {
              processThenMarkAsComplete(sentWorkItems, workItem).flatMap { workItems =>
                sendNotificationIfFound(count + 1, workItems)
              }
            } else {
              markAsPermFailed(sentWorkItems, workItem).flatMap { workItems =>
                sendNotificationIfFound(count + 1, workItems)
              }
            }
        }
      }

    sendNotificationIfFound(0, Seq.empty)
  }
}

