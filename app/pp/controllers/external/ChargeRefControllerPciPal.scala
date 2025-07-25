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

package pp.controllers.external

import cats.implicits.catsSyntaxEq
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.api.{Configuration, Logger}
import pp.config.{ChargeRefQueueConfig, MibOpsQueueConfig, PngrsQueueConfig}
import pp.connectors.{MibConnector, PngrConnector, TpsPaymentsBackendConnector}
import pp.controllers.HeaderValidator
import pp.controllers.retries.{ChargeRefDesRetries, MibRetries, PngrRetries}
import pp.model.StatusTypes.validated
import pp.model.mods.ModsPaymentCallBackRequest
import pp.model.pcipal.ChargeRefNotificationPcipalRequest
import pp.model.pcipal.ChargeRefNotificationPcipalRequest.{toChargeRefNotificationRequest, toPngrStatusUpdateRequest}
import pp.model.{TaxType, TaxTypes}
import pp.services.{AuditService, ChargeRefService, MibOpsService, PngrService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class ChargeRefControllerPciPal @Inject() (
    cc:                          ControllerComponents,
    val chargeRefService:        ChargeRefService,
    val chargeRefQueueConfig:    ChargeRefQueueConfig,
    val pngrQueueConfig:         PngrsQueueConfig,
    tpsPaymentsBackendConnector: TpsPaymentsBackendConnector,
    val configuration:           Configuration,
    val pngrService:             PngrService,
    val pngrConnector:           PngrConnector,
    val mibOpsService:           MibOpsService,
    val mibOpsQueueConfig:       MibOpsQueueConfig,
    val mibConnector:            MibConnector,
    auditService:                AuditService
)
  (implicit val executionContext: ExecutionContext) extends BackendController(cc) with HeaderValidator with ChargeRefDesRetries with PngrRetries with MibRetries {

  val logger: Logger = Logger(this.getClass.getSimpleName)

  def sendCardPaymentsNotificationPciPal(): Action[AnyContent] = Action.async { implicit request =>

    logger.debug(s"sendCardPaymentsNotificationPciPal request ${request.body.asJson.toString}")

    val notification = Try {
      request.body.asJson.map(_.as[ChargeRefNotificationPcipalRequest])
    } match {
      case Success(Some(chargeRefNotificationPcipalRequest)) =>
        logger.debug(s"sendCardPaymentsNotificationPciPal for ${chargeRefNotificationPcipalRequest.toString}")
        logger.info(s"Notification received from Pcipal, payment status: [ ${chargeRefNotificationPcipalRequest.Status.toString} ]")
        auditService.auditPcipalNotificationEvent(chargeRefNotificationPcipalRequest)
        chargeRefNotificationPcipalRequest
      case Success(None) =>
        logger.error(s"Received notification from PciPal but could not parse as json")
        throw new RuntimeException(s"Received notification from PciPal but could not parse as json")
      case Failure(exception) =>
        logger.error(s"Received notification from PciPal but could not read body. Exception ${exception.toString}")
        throw new RuntimeException("Received notification from PciPal but could not read body ", exception)
    }

      def sendToDesIfValidatedAndConfigured(taxType: TaxType): Future[Status] = {
        if (notification.Status === validated && (sendAllToDes || taxType.sendToDes)) {
          processChargeRefNotificationRequest(toChargeRefNotificationRequest(notification, taxType))
        } else Future successful Ok
      }

      def sendStatusUpdateToPngrIfConfigured(taxType: TaxType): Future[Status] =
        if (taxType === TaxTypes.pngr) {
          sendStatusUpdateToPngr(toPngrStatusUpdateRequest(notification))
        } else Future successful Ok

      def sendStatusUpdateToMibIfConfigured(taxType: TaxType): Future[Status] =
        if (taxType === TaxTypes.mib && notification.Status === validated) {
          for {
            amendmentRef <- tpsPaymentsBackendConnector.getModsAmendmentReference(notification.paymentItemId)
            modsPayload = ModsPaymentCallBackRequest(notification.ChargeReference, amendmentRef.amendmentReference)
            statusFromPaymentUpdate <- sendPaymentUpdateToMib(modsPayload)
          } yield statusFromPaymentUpdate
        } else Future successful Ok

    for {
      taxType <- tpsPaymentsBackendConnector.getTaxType(notification.paymentItemId)
      _ <- tpsPaymentsBackendConnector.updateWithPcipalData(notification)
      _ <- sendToDesIfValidatedAndConfigured(taxType)
      _ <- sendStatusUpdateToPngrIfConfigured(taxType)
      _ <- sendStatusUpdateToMibIfConfigured(taxType)
    } yield Ok
  }
}
