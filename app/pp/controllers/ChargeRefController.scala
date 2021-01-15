/*
 * Copyright 2021 HM Revenue & Customs
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

package pp.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, ControllerComponents}
import play.api.{Configuration, Logger}
import pp.config.{ChargeRefQueueConfig, PngrsQueueConfig}
import pp.connectors.{PngrConnector, TpsPaymentsBackendConnector}
import pp.controllers.retries.{ChargeRefDesRetries, PngrRetries}
import pp.model.StatusTypes.validated
import pp.model.chargeref.ChargeRefNotificationRequest
import pp.model.pcipal.ChargeRefNotificationPcipalRequest
import pp.model.pcipal.ChargeRefNotificationPcipalRequest.{toChargeRefNotificationRequest, toPngrStatusUpdateRequest}
import pp.model.{TaxType, TaxTypes}
import pp.services.{ChargeRefService, PngrService}
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChargeRefController @Inject() (
    cc:                          ControllerComponents,
    val chargeRefService:        ChargeRefService,
    val chargeRefQueueConfig:    ChargeRefQueueConfig,
    val pngrQueueConfig:         PngrsQueueConfig,
    tpsPaymentsBackendConnector: TpsPaymentsBackendConnector,
    val configuration:           Configuration,
    val pngrService:             PngrService,
    val pngrConnector:           PngrConnector

)
  (implicit val executionContext: ExecutionContext) extends BackendController(cc) with HeaderValidator with ChargeRefDesRetries with PngrRetries {

  val logger: Logger = Logger(this.getClass.getSimpleName)

  def sendCardPaymentsNotificationPciPal(): Action[ChargeRefNotificationPcipalRequest] = Action.async(parse.json[ChargeRefNotificationPcipalRequest]) { implicit request =>
    logger.debug("sendCardPaymentsNotificationPciPal")

    val notification = request.body

      def sendToDesIfValidatedAndConfigured(taxType: TaxType): Future[Status] = {
        if (notification.Status == validated && (sendAllToDes || taxType.sendToDes)) {
          processChargeRefNotificationRequest(toChargeRefNotificationRequest(notification, taxType))
        } else Future successful Ok
      }

      def sendStatusUpdateToPngrIfConfigured(taxType: TaxType): Future[Status] =
        if (taxType == TaxTypes.pngr) {
          sendStatusUpdateToPngr(toPngrStatusUpdateRequest(notification))
        } else Future successful Ok

    for {
      taxType <- tpsPaymentsBackendConnector.getTaxType(notification.paymentItemId)
      _ <- tpsPaymentsBackendConnector.updateWithPcipalData(notification)
      _ <- sendToDesIfValidatedAndConfigured(taxType)
      _ <- sendStatusUpdateToPngrIfConfigured(taxType)
    } yield Ok
  }

  def sendCardPaymentsNotification(): Action[ChargeRefNotificationRequest] = Action.async(parse.json[ChargeRefNotificationRequest]) { implicit request =>
    logger.debug("sendCardPaymentsNotification")
    sendCardPaymentsNotification(request.body)
  }

}

