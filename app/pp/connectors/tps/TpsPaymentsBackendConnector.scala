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

package pp.connectors.tps

import javax.inject.{Inject, Singleton}
import play.api.mvc.Request
import play.api.{Configuration, Logger}
import pp.model.pcipal.ChargeRefNotificationPcipalRequest
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TpsPaymentsBackendConnector @Inject() (
    httpClient:     HttpClient,
    configuration:  Configuration,
    servicesConfig: ServicesConfig
)(implicit ec: ExecutionContext) {

  private val serviceURL: String = servicesConfig.baseUrl("tps-payments-backend")

  def updateWithPcipalData(chargeRefNotificationPciPalRequest: ChargeRefNotificationPcipalRequest)(implicit request: Request[_], hc: HeaderCarrier): Future[HttpResponse] = {
    val update: String = s"$serviceURL/tps-payments-backend/update-with-pcipal-data"
    Logger.debug(s"""calling tps-payments-updateWithPcipalSessionId find with url $update""")
    httpClient.PATCH[ChargeRefNotificationPcipalRequest, HttpResponse](update, chargeRefNotificationPciPalRequest)
  }
}