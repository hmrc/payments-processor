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

import java.time.{Clock, LocalDateTime, ZoneId}

import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.iteratee.{Enumerator, Iteratee}
import pp.connectors.des.DesConnector
import pp.model.{ChargeRefNotificationDesRequest, ChargeRefNotificationRequest, ChargeRefNotificationWorkItem}
import pp.scheduling.ChargeRefNotificationMongoRepo
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.workitem.{Failed, WorkItem}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChargeRefService @Inject() (
    desConnector:                   DesConnector,
    chargeRefNotificationMongoRepo: ChargeRefNotificationMongoRepo,
    clock:                          Clock
)(implicit executionContext: ExecutionContext) {

  def sendCardPaymentsNotificationSync(chargeRefNotificationPciPalRequest: ChargeRefNotificationRequest): Future[HttpResponse] = {
    Logger.debug("inside sendCardPaymentsNotificationSync")

    val desChargeRef = ChargeRefNotificationDesRequest(chargeRefNotificationPciPalRequest.taxType,
                                                       chargeRefNotificationPciPalRequest.chargeRefNumber,
                                                       chargeRefNotificationPciPalRequest.amountPaid)

    desConnector.sendCardPaymentsNotification(desChargeRef)
  }

  def sendCardPaymentsNotificationToWorkItemRepo(chargeRefNotificationPciPalRequest: ChargeRefNotificationRequest): Future[WorkItem[ChargeRefNotificationWorkItem]] = {
    Logger.debug("inside sendCardPaymentsNotificationAsync")
    val time = LocalDateTime.now(clock)

    val jodaLocalDateTime = new DateTime(time.atZone(ZoneId.systemDefault).toInstant.toEpochMilli)
    val workItem = ChargeRefNotificationWorkItem(time, chargeRefNotificationPciPalRequest.taxType,
                                                 chargeRefNotificationPciPalRequest.chargeRefNumber,
                                                 chargeRefNotificationPciPalRequest.amountPaid, chargeRefNotificationPciPalRequest.origin)

    chargeRefNotificationMongoRepo.pushNew(workItem, jodaLocalDateTime)

  }

  private def sendWorkItemToDes(chargeRefNotificationWorkItem: WorkItem[ChargeRefNotificationWorkItem]) = {

    Logger.debug("inside sendWorkItemToDes")
    val desChargeRef = ChargeRefNotificationDesRequest(chargeRefNotificationWorkItem.item.taxType,
                                                       chargeRefNotificationWorkItem.item.chargeRefNumber,
                                                       chargeRefNotificationWorkItem.item.amountPaid)
    for {
      _ <- desConnector.sendCardPaymentsNotification(desChargeRef)
    } yield ()

  }

  def retrieveWorkItems: Future[Seq[WorkItem[ChargeRefNotificationWorkItem]]] = {

    Logger.debug("inside retrieveWorkItems")
    val pullWorkItems: Enumerator[WorkItem[ChargeRefNotificationWorkItem]] =
      Enumerator.generateM(chargeRefNotificationMongoRepo.pullOutstanding)

    val processWorkItems = Iteratee.foldM(Seq.empty[WorkItem[ChargeRefNotificationWorkItem]]) {
      sendNotificationMarkAsComplete
    }

    pullWorkItems.run(processWorkItems)
  }

  def sendNotificationMarkAsComplete(acc: Seq[WorkItem[ChargeRefNotificationWorkItem]], workItem: WorkItem[ChargeRefNotificationWorkItem]): Future[Seq[WorkItem[ChargeRefNotificationWorkItem]]] = {

    Logger.debug("inside sendNotificationMarkAsComplete")
    sendWorkItemToDes(workItem)
      .map(_ => chargeRefNotificationMongoRepo.complete(workItem.id))
      .map(_ => acc :+ workItem)
      .recoverWith {
        case _ =>
          chargeRefNotificationMongoRepo.markAs(workItem.id, Failed).map(_ => acc)
      }

  }

}
