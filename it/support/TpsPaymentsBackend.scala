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

object TpsPaymentsBackend {

  val endpoint = s"/tps-payments-backend/update-with-pcipal-data"
  val errorMessage = "tps failed"
  val successMessage = "ok"

  def tpsBackendOk = updateTps(200,successMessage)

  def tpsBackendFailed = updateTps(500, errorMessage)

  def updateTps(status: Int, responseBody: String) = {

    stubFor(
      patch(urlEqualTo(endpoint))
        .willReturn(aResponse()
          .withStatus(status)
          .withBody(responseBody)))
  }

}
