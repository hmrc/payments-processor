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

package pp.controllers.retries

import play.api.mvc.Results
import play.api.{Configuration, Logger}
import pp.config.ChargeRefQueueConfig
import pp.model.chargeref.ChargeRefNotificationRequest
import pp.services.ChargeRefService
import uk.gov.hmrc.http.{BadGatewayException, BadRequestException, UpstreamErrorResponse}
import uk.gov.hmrc.mongo.workitem.ProcessingStatus

import scala.concurrent.{ExecutionContext, Future}

trait ChargeRefDesRetries extends Results {

  val logger: Logger
  val chargeRefService: ChargeRefService
  val chargeRefQueueConfig: ChargeRefQueueConfig
  val configuration: Configuration
  implicit val executionContext: ExecutionContext

  val sendAllToDes: Boolean = configuration.underlying.getBoolean("sendAllToDes")

  def processChargeRefNotificationRequest(chargeRefNotificationRequest: ChargeRefNotificationRequest): Future[Status] = {
    logger.debug("processChargeRefNotificationRequest")
    chargeRefService
      .sendCardPaymentsNotificationSync(chargeRefNotificationRequest)
      .map(_ => Ok)
      .recoverWith {
        case e: UpstreamErrorResponse if e.statusCode == 400 =>
          Future.failed(new BadRequestException(e.getMessage()))
        case e: UpstreamErrorResponse if e.statusCode == 404 =>
          Future.failed(new BadGatewayException(e.message))
        case e: UpstreamErrorResponse if e.statusCode == 409 =>
          Future.failed(e)
        case e =>
          if (chargeRefQueueConfig.queueEnabled) {
            logger.debug("Queue enabled")
            chargeRefService
              .sendCardPaymentsNotificationToWorkItemRepo(chargeRefNotificationRequest)
              .map(
                res => res.status match {
                  case ProcessingStatus.ToDo => Ok
                  case _ =>
                    logger.error("Could not add message to work item repo")
                    InternalServerError
                }
              )
          } else {
            logger.warn("Queue disabled")
            Future.failed(e)
          }
      }
  }

  def sendCardPaymentsNotification(chargeRefNotificationRequest: ChargeRefNotificationRequest): Future[Status] = {
    logger.debug("sendCardPaymentsNotification")

    val sendChargeRef = sendAllToDes || chargeRefNotificationRequest.taxType.sendToDes
    if (sendChargeRef) {
      processChargeRefNotificationRequest(chargeRefNotificationRequest)
    } else {
      logger.debug(s"Not sending des notification for ${chargeRefNotificationRequest.taxType}, ignoreSendChargeRef was $sendChargeRef")
      Future.successful(Ok)
    }

  }

}
