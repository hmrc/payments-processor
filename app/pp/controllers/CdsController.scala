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

package pp.controllers

import javax.inject.Inject
import play.api.mvc.{Action, ControllerComponents}
import play.api.{Configuration, Logger}
import pp.config.CdsOpsQueueConfig
import pp.connectors.CdsConnector
import pp.controllers.retries.CdsRetries
import pp.model.cds.NotificationCds
import pp.services.CdsOpsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

class CdsController @Inject() (
    cc:                    ControllerComponents,
    val cdsOpsQueueConfig: CdsOpsQueueConfig,
    val configuration:     Configuration,
    val cdsOpsService:     CdsOpsService,
    val cdsConnector:      CdsConnector

)
  (implicit val executionContext: ExecutionContext) extends BackendController(cc) with CdsRetries with HeaderValidator {

  val logger: Logger = Logger(this.getClass.getSimpleName)

  def sendPaymentUpdateToCds(): Action[NotificationCds] = Action.async(parse.json[NotificationCds]) { implicit request =>
    logger.debug("sendStatusUpdateToCds")
    sendPaymentUpdateToCds(request.body)
  }
}

