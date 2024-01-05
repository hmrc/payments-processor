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

import controllers.Assets
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import pp.config.AppContext
import pp.model.api.{Access, Api, ApiDefinition, Version}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class ApiDocumentationController @Inject() (
    cc:            ControllerComponents,
    assets:        Assets,
    appContext:    AppContext,
    configuration: Configuration
) extends BackendController(cc) {

  // NOTE: This must follow DocumentationController from play-hmrc-api
  // See https://github.com/hmrc/play-hmrc-api/blob/main/src/main/play-28/uk/gov/hmrc/api/controllers/DocumentationController.scala
  def definition(): Action[AnyContent] = Action.async {
    val accessIn: Access = Access(whitelistedApplicationIds = appContext.whiteListedAppIds.getOrElse(Seq.empty[String]))
    val version: Version = Version(access           = accessIn,
                                   endpointsEnabled = configuration.underlying.getBoolean("api.enabled"), status = configuration.underlying.getString("api.status"))
    val apiIn: Api = Api(context  = appContext.apiContext, versions = Seq(version))
    val apiDefinition: ApiDefinition = ApiDefinition(api = apiIn)

    Future.successful(Ok(Json.toJson(apiDefinition)))
  }

  def conf(
      version: String,
      file:    String
  ): Action[AnyContent] =
    assets.at(s"/public/api/conf/$version", file)

}
