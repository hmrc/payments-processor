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
import pp.config.PngrsQueueConfig
import pp.connectors.PngrConnector
import pp.controllers.retries.PngrRetries
import pp.model.pngrs.PngrStatusUpdateRequest
import pp.services.PngrService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton
class PngrUpdateController @Inject() (
    cc:                  ControllerComponents,
    val pngrQueueConfig: PngrsQueueConfig,
    val configuration:   Configuration,
    val pngrService:     PngrService,
    val pngrConnector:   PngrConnector

)
  (implicit val executionContext: ExecutionContext) extends BackendController(cc) with HeaderValidator with PngrRetries {

  val logger: Logger = Logger(this.getClass.getSimpleName)

  def sendStatusUpdateToPngr(): Action[PngrStatusUpdateRequest] = Action.async(parse.json[PngrStatusUpdateRequest]) { implicit request =>
    logger.debug("sendStatusUpdateToPngr")
    sendStatusUpdateToPngr(request.body)
  }
}

