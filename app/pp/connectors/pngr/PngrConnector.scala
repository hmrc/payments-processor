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

package pp.connectors.pngr

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.mvc.Request
import pp.connectors.ResponseReadsThrowingException
import pp.model.pngr.PngrStatusUpdateRequest
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PngrConnector @Inject() (httpClient: HttpClient, servicesConfig: ServicesConfig)(implicit ec: ExecutionContext) {

  private val serviceURL: String = s"${servicesConfig.baseUrl("bc-passengers-declarations")}/bc-passengers-declarations"

  private val logger: Logger = Logger(this.getClass.getSimpleName)

  implicit val readRaw: HttpReads[HttpResponse] = ResponseReadsThrowingException.readResponse

  def updateWithStatus(statusUpdate: PngrStatusUpdateRequest): Future[HttpResponse] = {
    val url: String = s"$serviceURL/update-payment"
    logger.debug(s"""c $url""")
    implicit val hc: HeaderCarrier = HeaderCarrier()
    httpClient.POST[PngrStatusUpdateRequest, HttpResponse](url, statusUpdate)
  }

}
