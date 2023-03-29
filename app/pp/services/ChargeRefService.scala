/*
 * Copyright 2023 HM Revenue & Customs
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
import pp.config.ChargeRefQueueConfig
import pp.connectors.DesConnector
import pp.model.chargeref.{ChargeRefNotificationDesRequest, ChargeRefNotificationRequest}
import pp.model.wokitems.ChargeRefNotificationMyWorkItem
import pp.scheduling.chargeref.ChargeRefNotificationMongoRepo
import uk.gov.hmrc.mongo.workitem.WorkItem

import java.time.{Clock, LocalDateTime}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChargeRefService @Inject() (
    desConnector:    DesConnector,
    val repo:        ChargeRefNotificationMongoRepo,
    val clock:       Clock,
    val queueConfig: ChargeRefQueueConfig
)(implicit val executionContext: ExecutionContext) extends WorkItemService[ChargeRefNotificationMyWorkItem] {

  val logger: Logger = Logger(this.getClass.getSimpleName)

  //These are all specific to charge reference processing

  def sendWorkItem(chargeRefNotificationWorkItem: WorkItem[ChargeRefNotificationMyWorkItem]): Future[Unit] = {

    logger.debug("inside sendWorkItemToDes")
    val desChargeRef = ChargeRefNotificationDesRequest(chargeRefNotificationWorkItem.item.taxType,
                                                       chargeRefNotificationWorkItem.item.chargeRefNumber,
                                                       chargeRefNotificationWorkItem.item.amountPaid)
    for {
      _ <- desConnector.sendCardPaymentsNotification(desChargeRef)
    } yield ()

  }

  def sendCardPaymentsNotificationSync(chargeRefNotificationPciPalRequest: ChargeRefNotificationRequest): Future[Unit] = {
    logger.debug("inside sendCardPaymentsNotificationSync")

    val desChargeRef = ChargeRefNotificationDesRequest(chargeRefNotificationPciPalRequest.taxType,
                                                       chargeRefNotificationPciPalRequest.chargeRefNumber,
                                                       chargeRefNotificationPciPalRequest.amountPaid)

    desConnector.sendCardPaymentsNotification(desChargeRef)
  }

  def sendCardPaymentsNotificationToWorkItemRepo(chargeRefNotificationPciPalRequest: ChargeRefNotificationRequest): Future[WorkItem[ChargeRefNotificationMyWorkItem]] = {
    logger.debug("inside sendCardPaymentsNotificationAsync")
    val time = LocalDateTime.now(clock)

    val localDateTime = repo.now()
    val workItem = ChargeRefNotificationMyWorkItem(time, availableUntil(time), chargeRefNotificationPciPalRequest.taxType,
                                                   chargeRefNotificationPciPalRequest.chargeRefNumber,
                                                   chargeRefNotificationPciPalRequest.amountPaid, chargeRefNotificationPciPalRequest.origin)

    repo.pushNew(workItem, localDateTime)

  }

}
