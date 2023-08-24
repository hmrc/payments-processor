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
import pp.connectors.ResponseReadsThrowingException.readResponse
import pp.model.mods.ModsPaymentCallBackRequest
import pp.model.pcipal.ChargeRefNotificationPcipalRequest
import pp.model.{PaymentItemId}
import tps.model.{TaxType, TaxTypes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TpsPaymentsBackendConnector @Inject() (httpClient: HttpClient, servicesConfig: ServicesConfig)(implicit ec: ExecutionContext) {

  private val serviceURL: String = s"${servicesConfig.baseUrl("tps-payments-backend")}/tps-payments-backend"

  private val logger: Logger = Logger(this.getClass.getSimpleName)

  def updateWithPcipalData(chargeRefNotificationPciPalRequest: ChargeRefNotificationPcipalRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val url: String = s"$serviceURL/update-with-pcipal-data"
    logger.debug(s"""calling tps-payments-updateWithPcipalSessionId find with url $url""")
    httpClient.PATCH[ChargeRefNotificationPcipalRequest, HttpResponse](url, chargeRefNotificationPciPalRequest)
  }

  def getTaxType(paymentItemId: PaymentItemId)(implicit hc: HeaderCarrier): Future[TaxType] =
    httpClient.GET[HttpResponse](s"$serviceURL/payment-items/${paymentItemId.value}/tax-type")
      .map(_.json.as[String])
      .map { taxTypeUpperCase =>
        TaxTypes.withNameInsensitiveOption(taxTypeUpperCase).getOrElse(throw new RuntimeException(s"Unknown tax type $taxTypeUpperCase"))
      }

  def getModsAmendmentReference(paymentItemId: PaymentItemId)(implicit hc: HeaderCarrier): Future[ModsPaymentCallBackRequest] = {
    val url: String = s"$serviceURL/payment-items/${paymentItemId.value}/mods-amendment-ref"
    logger.debug(s"""calling tps-payments-modsAmendmentRef with url $url""")
    httpClient.GET[HttpResponse](url)
      .map(_.json.as[ModsPaymentCallBackRequest])
  }

}
