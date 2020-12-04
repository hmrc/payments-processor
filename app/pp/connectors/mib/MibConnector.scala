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

package pp.connectors.mib

import javax.inject.{Inject, Singleton}
import play.api.Logger
import pp.connectors.ResponseReadsThrowingException
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import ResponseReadsThrowingException.readResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MibConnector @Inject() (httpClient: HttpClient, servicesConfig: ServicesConfig)(implicit ec: ExecutionContext) {

  private def serviceURL(reference: String): String = s"${servicesConfig.baseUrl("merchandise-in-baggage")}/declare-commercial-goods/payment-callback/$reference"

  private val logger: Logger = Logger(this.getClass.getSimpleName)

  def paymentCallback(reference: String): Future[HttpResponse] = {
    logger.debug("paymentCallback")
    implicit val hc: HeaderCarrier = HeaderCarrier()
    httpClient.GET[HttpResponse](serviceURL(reference))
  }

}
