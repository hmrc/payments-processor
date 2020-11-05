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

package pp.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, ControllerComponents}
import play.api.{Configuration, Logger}
import pp.config.ChargeRefQueueConfig
import pp.connectors.tps.TpsPaymentsBackendConnector
import pp.model.StatusTypes.validated
import pp.model.pcipal.ChargeRefNotificationPcipalRequest
import pp.model.pcipal.ChargeRefNotificationPcipalRequest.toChargeRefNotificationRequest
import pp.model.{ChargeRefNotificationRequest, TaxType}
import pp.services.ChargeRefService
import uk.gov.hmrc.http.{BadGatewayException, BadRequestException, NotFoundException, Upstream4xxResponse}
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import uk.gov.hmrc.workitem.ToDo

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChargeRefController @Inject() (
    cc:                          ControllerComponents,
    chargeRefService:            ChargeRefService,
    queueConfig:                 ChargeRefQueueConfig,
    tpsPaymentsBackendConnector: TpsPaymentsBackendConnector,
    configuration:               Configuration
)
  (implicit executionContext: ExecutionContext) extends BackendController(cc) with HeaderValidator {

  val sendAllToDes: Boolean = configuration.underlying.getBoolean("sendAllToDes")

  def sendCardPaymentsNotificationPciPal(): Action[ChargeRefNotificationPcipalRequest] = Action.async(parse.json[ChargeRefNotificationPcipalRequest]) { implicit request =>
    Logger.debug("sendCardPaymentsNotificationPciPal")

    val notification = request.body

      def sendToDesIfValidatedAndConfigured(taxType: TaxType) =
        if (notification.Status == validated && (sendAllToDes || taxType.sendToDes))
          processChargeRefNotificationRequest(toChargeRefNotificationRequest(notification, taxType))
        else Future successful Ok

    for {
      taxType <- tpsPaymentsBackendConnector.getTaxType(notification.paymentItemId)
      _ <- tpsPaymentsBackendConnector.updateWithPcipalData(notification)
      _ <- sendToDesIfValidatedAndConfigured(taxType)
    } yield Ok
  }

  def sendCardPaymentsNotification(): Action[ChargeRefNotificationRequest] = Action.async(parse.json[ChargeRefNotificationRequest]) { implicit request =>
    Logger.debug("sendCardPaymentsNotification")

    val sendChargeRef = sendAllToDes || request.body.taxType.sendToDes
    if (sendChargeRef) {
      processChargeRefNotificationRequest(request.body)
    } else {
      Logger.debug(s"Not sending des notification for ${request.body.taxType}, ignoreSendChargeRef was $sendChargeRef")
      Future.successful(Ok)
    }

  }

  private def processChargeRefNotificationRequest(chargeRefNotificationRequest: ChargeRefNotificationRequest) = {
    Logger.debug("processChargeRefNotificationRequest")
    chargeRefService
      .sendCardPaymentsNotificationSync(chargeRefNotificationRequest)
      .map(_ => Ok)
      .recoverWith {
        case e: BadRequestException                                  => Future.failed(e)
        case e: NotFoundException                                    => Future.failed(new BadGatewayException(e.message))
        case e: Upstream4xxResponse if e.upstreamResponseCode == 409 => Future.failed(e)
        case e =>
          if (queueConfig.queueEnabled) {
            Logger.debug("Queue enabled")
            chargeRefService
              .sendCardPaymentsNotificationToWorkItemRepo(chargeRefNotificationRequest)
              .map(
                res => res.status match {
                  case ToDo => Ok
                  case _ =>
                    Logger.error("Could not add message to work item repo")
                    InternalServerError
                }
              )
          } else {
            Logger.warn("Queue disabled")
            Future.failed(e)
          }
      }
  }

}

