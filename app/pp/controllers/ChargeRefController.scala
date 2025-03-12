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

import play.api.mvc.{Action, ControllerComponents}
import play.api.{Configuration, Logger}
import pp.config.{ChargeRefQueueConfig, MibOpsQueueConfig, PngrsQueueConfig}
import pp.connectors.{MibConnector, PngrConnector}
import pp.controllers.retries.{ChargeRefDesRetries, MibRetries, PngrRetries}
import pp.model.chargeref.ChargeRefNotificationRequest
import pp.services.{ChargeRefService, MibOpsService, PngrService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ChargeRefController @Inject() (
    cc:                       ControllerComponents,
    val chargeRefService:     ChargeRefService,
    val chargeRefQueueConfig: ChargeRefQueueConfig,
    val pngrQueueConfig:      PngrsQueueConfig,
    val configuration:        Configuration,
    val pngrService:          PngrService,
    val pngrConnector:        PngrConnector,
    val mibOpsService:        MibOpsService,
    val mibOpsQueueConfig:    MibOpsQueueConfig,
    val mibConnector:         MibConnector
)
  (implicit val executionContext: ExecutionContext) extends BackendController(cc) with HeaderValidator with ChargeRefDesRetries with PngrRetries with MibRetries {

  val logger: Logger = Logger(this.getClass.getSimpleName)

  def sendCardPaymentsNotification(): Action[ChargeRefNotificationRequest] = Action.async(parse.json[ChargeRefNotificationRequest]) { implicit request =>
    logger.debug("sendCardPaymentsNotification")
    sendCardPaymentsNotification(request.body)
  }
}
