/*
 * Copyright 2024 HM Revenue & Customs
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
import play.api.http.Status

object Mib {

  val endpoint = "/declare-commercial-goods/payment-callback"
  val errorMessage = "mib failed"
  val successMessage = "ok"

  def statusUpdateSucceeds(delayMillis: Int = 0, sequence: Int = 0): StubMapping =
    statusUpdateRespondsWith(status       = Status.OK, responseBody = successMessage, delayMillis = delayMillis, sequence = sequence)

  def statusUpdateFailsWithAnInternalServerError(delayMillis: Int = 0, sequence: Int = 0): StubMapping =
    statusUpdateRespondsWith(Status.INTERNAL_SERVER_ERROR, errorMessage, delayMillis, sequence)

  def statusUpdateRespondsWith(status: Int, responseBody: String, delayMillis: Int = 0, sequence: Int = 0): StubMapping = {
    stubFor(
      post(urlEqualTo(endpoint))
        .inScenario("mib")
        .whenScenarioStateIs(WireMockStub.state(sequence))
        .willReturn(aResponse()
          .withStatus(status)
          .withBody(responseBody)
          .withFixedDelay(delayMillis))
        .willSetStateTo(WireMockStub.nextState(sequence)))
  }

}
