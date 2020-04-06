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
import play.api.{Configuration, Logger}
import play.api.mvc.{Action, ControllerComponents}
import pp.config.QueueConfig
import pp.connectors.tps.TpsPaymentsBackendConnector
import pp.model.{ChargeRefNotificationRequest, HeadOfDutyIndicators, StatusTypes, TaxType, TaxTypes}
import pp.model.pcipal.ChargeRefNotificationPcipalRequest
import pp.services.ChargeRefService
import uk.gov.hmrc.http.{BadGatewayException, BadRequestException, NotFoundException, Upstream4xxResponse}
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import uk.gov.hmrc.workitem.ToDo

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChargeRefController @Inject() (
    cc:                          ControllerComponents,
    chargeRefService:            ChargeRefService,
    queueConfig:                 QueueConfig,
    tpsPaymentsBackendConnector: TpsPaymentsBackendConnector,
    configuration:               Configuration
)
  (implicit executionContext: ExecutionContext) extends BackendController(cc) with HeaderValidator {

  def sendCardPaymentsNotificationPciPal(): Action[ChargeRefNotificationPcipalRequest] = Action.async(parse.json[ChargeRefNotificationPcipalRequest]) { implicit request =>

    val sendChargeRef = shouldProcess(HeadOfDutyIndicators.toTaxcode(request.body.HoD))

    if (request.body.Status == StatusTypes.complete && sendChargeRef) {
      Logger.debug("sendCardPaymentsNotificationPciPal ... sending to DES")
      for {
        _ <- tpsPaymentsBackendConnector.updateWithPcipalData(request.body)
        _ <- processChargeRefNotificationRequest(ChargeRefNotificationPcipalRequest.toChargeRefNotificationRequest(request.body))
      } yield Ok
    } else {
      Logger.debug(s"sendCardPaymentsNotificationPciPal ... not sending to DES, as status was ${request.body.Status.toString}, ignoreSendChargeRef was $sendChargeRef")
      for {
        _ <- tpsPaymentsBackendConnector.updateWithPcipalData(request.body)
      } yield Ok
    }
  }

  def sendCardPaymentsNotification(): Action[ChargeRefNotificationRequest] = Action.async(parse.json[ChargeRefNotificationRequest]) { implicit request =>
    Logger.debug("sendCardPaymentsNotification")
    val shouldSendChargeRef: Boolean = shouldProcess(request.body.taxType)
    if (shouldSendChargeRef) {
      processChargeRefNotificationRequest(request.body)
    } else {
      Logger.debug(s"Not sending des notification for ${request.body.taxType}, ignoreSendChargeRef was $shouldSendChargeRef")
      Future.successful(Ok)
    }

  }

  private def shouldProcess(taxType: TaxType) = {
    import scala.collection.JavaConverters._

    val ignoreList = configuration.underlying.getStringList("taxTypes.chargeref.ignore")
      .asScala.toList.map(m => TaxTypes.forCode(m).getOrElse(throw new RuntimeException(s"No TaxType for $m")))

    !ignoreList.contains(taxType)

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

