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

package support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.Json.toJson
import pp.model.{PaymentItemId, TaxType}

object TpsPaymentsBackend {

  private val basePath = "/tps-payments-backend"

  val updateEndpoint = s"$basePath/update-with-pcipal-data"
  val updateErrorMessage = "tps failed"
  val notFoundErrorMessage = "404"
  val successMessage = "ok"

  def getTaxTypeEndpoint(paymentItemId: PaymentItemId) = s"$basePath/payment-items/${paymentItemId.value}/tax-type"

  def tpsUpdateOk: StubMapping = updateTps(200, successMessage)

  def tpsUpdateFailed: StubMapping = updateTps(500, updateErrorMessage)

  def updateTps(status: Int, responseBody: String): StubMapping = stubFor(
    patch(urlEqualTo(updateEndpoint))
      .willReturn(aResponse()
        .withStatus(status)
        .withBody(responseBody)))

  def getTaxTypeOk(paymentItemId: PaymentItemId, taxType: TaxType): StubMapping = stubFor(
    get(urlEqualTo(getTaxTypeEndpoint(paymentItemId)))
      .willReturn(aResponse()
        .withStatus(200)
        .withBody(toJson(taxType).toString())))

  def getTaxTypeNotFound(paymentItemId: PaymentItemId): StubMapping =
    stubFor(get(urlEqualTo(getTaxTypeEndpoint(paymentItemId))).willReturn(aResponse().withStatus(404)))
}
