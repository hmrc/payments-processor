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

package pp.services.chargref

import java.time.{Clock, LocalDateTime, ZoneId}

import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.Logger
import pp.config.ChargeRefQueueConfig
import pp.connectors.des.DesConnector
import pp.model.chargeref.{ChargeRefNotificationDesRequest, ChargeRefNotificationRequest, ChargeRefNotificationWorkItem}
import pp.scheduling.chargeref.ChargeRefNotificationMongoRepo
import pp.services.WorkItemService
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.workitem.{Failed, WorkItem}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChargeRefService @Inject() (
    desConnector:                   DesConnector,
    chargeRefNotificationMongoRepo: ChargeRefNotificationMongoRepo,
    clock:                          Clock,
    queueConfig:                    ChargeRefQueueConfig
)(implicit executionContext: ExecutionContext) extends WorkItemService[ChargeRefNotificationWorkItem] {

  private val logger: Logger = Logger(this.getClass.getSimpleName)

  //Need to have an implementation of this.  Nuts and Bolts of getting items from the queue ... needs to satisfy interface WorkItemService
  def retrieveWorkItems: Future[Seq[WorkItem[ChargeRefNotificationWorkItem]]] = {

      @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
      def sendNotificationIfFound(count: Int, sentWorkItems: Seq[WorkItem[ChargeRefNotificationWorkItem]]): Future[Seq[WorkItem[ChargeRefNotificationWorkItem]]] = {

          def retrieveWorkItem(count: Int): Future[Option[WorkItem[ChargeRefNotificationWorkItem]]] = {
            if (count == queueConfig.pollLimit) Future successful None
            else chargeRefNotificationMongoRepo.pullOutstanding
          }

        retrieveWorkItem(count).flatMap {
          case None => Future successful sentWorkItems
          case Some(workItem) =>
            sendNotificationMarkAsComplete(sentWorkItems, workItem).flatMap { workItems =>
              sendNotificationIfFound(count + 1, workItems)
            }
        }
      }

    sendNotificationIfFound(0, Seq.empty)
  }

  //These are all specific to charge reference processing

  def sendCardPaymentsNotificationSync(chargeRefNotificationPciPalRequest: ChargeRefNotificationRequest): Future[HttpResponse] = {
    logger.debug("inside sendCardPaymentsNotificationSync")

    val desChargeRef = ChargeRefNotificationDesRequest(chargeRefNotificationPciPalRequest.taxType,
                                                       chargeRefNotificationPciPalRequest.chargeRefNumber,
                                                       chargeRefNotificationPciPalRequest.amountPaid)

    desConnector.sendCardPaymentsNotification(desChargeRef)
  }

  def sendCardPaymentsNotificationToWorkItemRepo(chargeRefNotificationPciPalRequest: ChargeRefNotificationRequest): Future[WorkItem[ChargeRefNotificationWorkItem]] = {
    logger.debug("inside sendCardPaymentsNotificationAsync")
    val time = LocalDateTime.now(clock)

    val jodaLocalDateTime = new DateTime(time.atZone(ZoneId.systemDefault).toInstant.toEpochMilli)
    val workItem = ChargeRefNotificationWorkItem(time, chargeRefNotificationPciPalRequest.taxType,
                                                 chargeRefNotificationPciPalRequest.chargeRefNumber,
                                                 chargeRefNotificationPciPalRequest.amountPaid, chargeRefNotificationPciPalRequest.origin)

    chargeRefNotificationMongoRepo.pushNew(workItem, jodaLocalDateTime)

  }

  private def sendWorkItemToDes(chargeRefNotificationWorkItem: WorkItem[ChargeRefNotificationWorkItem]) = {

    logger.debug("inside sendWorkItemToDes")
    val desChargeRef = ChargeRefNotificationDesRequest(chargeRefNotificationWorkItem.item.taxType,
                                                       chargeRefNotificationWorkItem.item.chargeRefNumber,
                                                       chargeRefNotificationWorkItem.item.amountPaid)
    for {
      _ <- desConnector.sendCardPaymentsNotification(desChargeRef)
    } yield ()

  }

  private def sendNotificationMarkAsComplete(acc: Seq[WorkItem[ChargeRefNotificationWorkItem]], workItem: WorkItem[ChargeRefNotificationWorkItem]): Future[Seq[WorkItem[ChargeRefNotificationWorkItem]]] = {
    logger.debug("inside sendNotificationMarkAsComplete")
    sendWorkItemToDes(workItem)
      .map(_ => chargeRefNotificationMongoRepo.complete(workItem.id))
      .map(_ => acc :+ workItem)
      .recoverWith {
        case _ =>
          chargeRefNotificationMongoRepo.markAs(workItem.id, Failed).map(_ => acc)
      }
  }
}
