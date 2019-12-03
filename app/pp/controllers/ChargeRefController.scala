/*
 * Copyright 2019 HM Revenue & Customs
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
import play.api.Logger
import play.api.mvc.{Action, ControllerComponents, Result}
import pp.config.AppConfig
import pp.model.ChargeRefNotificationPciPalRequest
import pp.services.ChargeRefService
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import uk.gov.hmrc.workitem.{InProgress, ResultStatus, ToDo}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChargeRefController @Inject() (
    cc:               ControllerComponents,
    chargeRefService: ChargeRefService,
    appConfig:        AppConfig
)
  (implicit executionContext: ExecutionContext) extends BackendController(cc) with HeaderValidator {

  def sendCardPaymentsNotification(): Action[ChargeRefNotificationPciPalRequest] = Action.async(parse.json[ChargeRefNotificationPciPalRequest]) { implicit request =>
    chargeRefService
      .sendCardPaymentsNotificationSync(request.body)
      .map(_ => Ok)
      .recoverWith{
        case _ => {
          chargeRefService
            .sendCardPaymentsNotificationToWorkItemRepo(request.body)
            .map(
              res => res.status match {
                case ToDo => Ok
                case _ => {
                  Logger.error("Could not add message to work item repo")
                  InternalServerError
                }
              }
            )
        }
      }
  }

}

