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

package pp.connectors

import play.api.Logger
import play.api.libs.json.Json
import pp.connectors.ResponseReadsThrowingException.readResponse
import pp.model.pngrs.PngrStatusUpdateRequest
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PngrConnector @Inject() (httpClient: HttpClientV2, servicesConfig: ServicesConfig)(implicit ec: ExecutionContext) {

  private val serviceURL: String = s"${servicesConfig.baseUrl("bc-passengers-declarations")}/bc-passengers-declarations"

  private val logger: Logger = Logger(this.getClass.getSimpleName)

  def updateWithStatus(statusUpdate: PngrStatusUpdateRequest): Future[HttpResponse] = {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val url: String = s"$serviceURL/update-payment"
    logger.debug(s"""c $url""")

    httpClient
      .post(url"$url")
      .withBody(Json.toJson(statusUpdate))
      .execute[HttpResponse]
  }

}
