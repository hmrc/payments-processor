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

package pp.services

import java.time.{Clock, LocalDateTime}

import javax.inject.{Inject, Singleton}
import pp.connectors.des.DesConnector
import pp.model.{ChargeRefNotificationDesRequest, ChargeRefNotificationPciPalRequest, ChargeRefNotificationWorkItem, TaxTypes}
import pp.scheduling.ChargeRefNotificationMongoRepo
import uk.gov.hmrc.http.HttpResponse
import java.time.ZoneId

import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.iteratee.{Enumerator, Iteratee}
import uk.gov.hmrc.workitem.WorkItem

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChargeRefService @Inject() (
    desConnector:                   DesConnector,
    chargeRefNotificationMongoRepo: ChargeRefNotificationMongoRepo,
    clock:                          Clock
)(implicit executionContext: ExecutionContext) {

  def sendCardPaymentsNotificationSync(chargeRefNotificationPciPalRequest: ChargeRefNotificationPciPalRequest): Future[HttpResponse] = {
    Logger.debug("inside sendCardPaymentsNotificationSync")

    val desChargeRef = ChargeRefNotificationDesRequest(chargeRefNotificationPciPalRequest.taxType,
                                                       chargeRefNotificationPciPalRequest.chargeRefNumber,

                                                       chargeRefNotificationPciPalRequest.amountDue,
                                                       chargeRefNotificationPciPalRequest.amountPaid)

    desConnector.sendCardPaymentsNotification(desChargeRef)
  }

  def sendCardPaymentsNotificationToWorkItemRepo(chargeRefNotificationPciPalRequest: ChargeRefNotificationPciPalRequest): Future[WorkItem[ChargeRefNotificationWorkItem]] = {
    Logger.debug("inside sendCardPaymentsNotificationAsync")
    val time = LocalDateTime.now(clock)

    val jodaLocalDateTime = new DateTime(time.atZone(ZoneId.systemDefault).toInstant.toEpochMilli)
    val workItem = ChargeRefNotificationWorkItem(time, chargeRefNotificationPciPalRequest.taxType,
                                                 chargeRefNotificationPciPalRequest.chargeRefNumber,

                                                 chargeRefNotificationPciPalRequest.amountDue,
                                                 chargeRefNotificationPciPalRequest.amountPaid)

    chargeRefNotificationMongoRepo.pushNew(workItem, jodaLocalDateTime)

  }

  private def sendWorkItemToDes(chargeRefNotificationWorkItem: WorkItem[ChargeRefNotificationWorkItem]) = {

    Logger.debug("inside sendWorkItemToDes")
    val desChargeRef = ChargeRefNotificationDesRequest(chargeRefNotificationWorkItem.item.taxType,
                                                       chargeRefNotificationWorkItem.item.chargeRefNumber,
                                                       chargeRefNotificationWorkItem.item.amountDue,
                                                       chargeRefNotificationWorkItem.item.amountPaid)
    for {
      _ <- desConnector.sendCardPaymentsNotification(desChargeRef)
    } yield ()

  }

  def processOneWorkItem() = {
    chargeRefNotificationMongoRepo.pullOutstanding flatMap {
      case Some(outstandingItem) => {
        sendWorkItemToDes(outstandingItem).map { _ => true }
      }
      case None => Future.successful(false)
    }

  }

  //def scanOneItemAndMarkAsComplete (acc: Seq[ChargeRefNotificationWorkItem],workItem: WorkItem[ChargeRefNotificationWorkItem]): Future[Seq[ChargeRefNotificationWorkItem]] =

  def retrieveWorkItems = {
    val pullWorkItems: Enumerator[WorkItem[ChargeRefNotificationWorkItem]] =
      Enumerator.generateM(chargeRefNotificationMongoRepo.pullOutstanding)

    val processWorkItems: Iteratee[WorkItem[ChargeRefNotificationWorkItem], Unit] = {
      Iteratee.foreach {
        workItem =>
          sendWorkItemToDes(workItem)
          ()
      }
    }

    pullWorkItems.run(processWorkItems)
  }

}
