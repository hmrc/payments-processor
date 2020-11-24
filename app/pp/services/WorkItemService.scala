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

package pp.services

import java.time.{Clock, LocalDateTime}

import play.api.Logger
import pp.config.QueueConfig
import pp.model.WorkItemFields
import pp.scheduling.NotificationRepo
import uk.gov.hmrc.workitem.{Failed, PermanentlyFailed, WorkItem}

import scala.concurrent.{ExecutionContext, Future}

trait WorkItemService[P <: WorkItemFields] {

  val clock: Clock

  val repo: NotificationRepo[P]

  implicit val executionContext: ExecutionContext

  val logger: Logger

  val queueConfig: QueueConfig

  def sendWorkItem(workItem: WorkItem[P]): Future[Unit]

  def isAvailable(workItem: WorkItemFields): Boolean = {
    val time = LocalDateTime.now(clock)
    time.isBefore(workItem.availableUntil)
  }

  def showWarning(workItem: WorkItemFields): Boolean = {
    val time = LocalDateTime.now(clock)
    time.isAfter(workItem.warningAt)
  }

  def availableUntil(time: LocalDateTime): LocalDateTime = time.plus(queueConfig.ttlMinusBufferMarkFailed)
  def warningAt(time: LocalDateTime): LocalDateTime = time.plus(queueConfig.ttlMinusBufferWarning)

  def markAsPermFailed(acc: Seq[WorkItem[P]], workItem: WorkItem[P]): Future[Seq[WorkItem[P]]] = {
    logger.warn(s"payments-processor: Failed to process workitem ${workItem.item.toString}")
    repo.markAs(workItem.id, PermanentlyFailed)
      .map(_ => acc :+ workItem)
  }

  def processThenMarkAsComplete(acc: Seq[WorkItem[P]], workItem: WorkItem[P]): Future[Seq[WorkItem[P]]] = {
    logger.debug("inside processThenMarkAsComplete")

    sendWorkItem(workItem)
      .map(_ => repo.complete(workItem.id))
      .map(_ => acc :+ workItem)
      .recoverWith {
        case _ => {
          if (showWarning(workItem.item)) {
            logger.warn("payments-processor: Nearly out if time to process workitem ${workItem.item.toString}")
          }
          repo.markAs(workItem.id, Failed).map(_ => acc)
        }
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

