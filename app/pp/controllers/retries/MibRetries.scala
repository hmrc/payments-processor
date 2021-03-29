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

package pp.controllers.retries

import play.api.Logger
import play.api.mvc.Results
import pp.config.MibOpsQueueConfig
import pp.connectors.MibConnector
import pp.model.mods.ModsPaymentCallBackRequest
import pp.services.MibOpsService
import uk.gov.hmrc.http.{BadGatewayException, BadRequestException, UpstreamErrorResponse}
import uk.gov.hmrc.workitem.ToDo

import scala.concurrent.{ExecutionContext, Future}

trait MibRetries extends Results {

  val logger: Logger
  val mibOpsQueueConfig: MibOpsQueueConfig
  val mibConnector: MibConnector
  val mibOpsService: MibOpsService

  implicit val executionContext: ExecutionContext

  def sendPaymentUpdateToMib(modsPaymentCallBackRequest: ModsPaymentCallBackRequest): Future[Status] = {
    logger.debug("sendPaymentUpdateToMib")
    mibConnector
      .paymentCallback(modsPaymentCallBackRequest)
      .map(_ => Ok)
      .recoverWith {
        case e: UpstreamErrorResponse if e.statusCode == 400 =>
          Future.failed(new BadRequestException(e.getMessage()))
        case e: UpstreamErrorResponse if e.statusCode == 404 =>
          Future.failed(new BadGatewayException(e.message))
        case e =>
          if (mibOpsQueueConfig.queueEnabled) {
            logger.debug("Queue enabled")
            mibOpsService.sendMibOpsToWorkItemRepo(modsPaymentCallBackRequest)
              .map(
                res => res.status match {
                  case ToDo => Ok
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

}
