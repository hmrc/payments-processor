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

package pp.connectors

import javax.inject.{Inject, Singleton}
import play.api.Logger
import pp.connectors.ResponseReadsThrowingException.readResponse
import pp.model.cds.NotificationCds
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CdsConnector @Inject() (httpClient: HttpClient, servicesConfig: ServicesConfig)(implicit ec: ExecutionContext) {

  private val logger: Logger = Logger(this.getClass.getSimpleName)
  private val serviceURL: String = s"${servicesConfig.baseUrl("cds")}"
  private val authorizationToken: String = servicesConfig.getString("microservice.services.cds.authToken")

  def paymentCallback(notificationCds: NotificationCds): Future[HttpResponse] = {
    val url: String = s"$serviceURL/accounts/notifyimmediatepayment/v1"
    logger.debug(s"""sending cds notification: $url""")
    implicit val desHc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(s"Bearer $authorizationToken")))
    httpClient.POST[NotificationCds, HttpResponse](url, notificationCds)
  }
}
