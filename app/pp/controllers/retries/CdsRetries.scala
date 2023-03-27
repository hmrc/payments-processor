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

package pp.controllers.retries

import play.api.Logger
import play.api.mvc.Results
import pp.config.CdsOpsQueueConfig
import pp.connectors.CdsConnector
import pp.model.cds.NotificationCds
import pp.services.CdsOpsService
import uk.gov.hmrc.http.{BadGatewayException, BadRequestException, UpstreamErrorResponse}
import uk.gov.hmrc.mongo.workitem.ProcessingStatus

import scala.concurrent.{ExecutionContext, Future}

trait CdsRetries extends Results {

  val logger: Logger
  val cdsOpsQueueConfig: CdsOpsQueueConfig
  val cdsConnector: CdsConnector
  val cdsOpsService: CdsOpsService

  implicit val executionContext: ExecutionContext

  def sendPaymentUpdateToCds(notification: NotificationCds): Future[Status] = {
    logger.debug("sendPaymentUpdateToCds")
    cdsConnector
      .paymentCallback(notification)
      .map(_ => Ok)
      .recoverWith {
        case e: UpstreamErrorResponse if e.statusCode == 400 =>
          Future.failed(new BadRequestException(e.getMessage()))
        case e: UpstreamErrorResponse if e.statusCode == 404 =>
          Future.failed(new BadGatewayException(e.message))
        case e =>
          if (cdsOpsQueueConfig.queueEnabled) {
            logger.debug("Queue enabled")
            cdsOpsService.sendCdsOpsToWorkItemRepo(notification)
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

}
