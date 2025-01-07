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
import play.api.http.Status

object Des {
  val endpoint = "/cross-regime/payments/card/notification"
  val errorMessage = "des failed"
  val successMessage = "ok"

  def cardPaymentsNotificationSucceeds(delayMillis: Int = 0, sequence: Int = 0): StubMapping =
    cardPaymentsNotificationRespondsWith(status       = Status.OK, responseBody = successMessage, delayMillis = delayMillis, sequence = sequence)

  // the Des specification defines both 200 and 204 as valid return codes
  def cardPaymentsNotificationSucceedsWithNoContent(delayMillis: Int = 0, sequence: Int = 0): StubMapping =
    cardPaymentsNotificationRespondsWith(Status.NO_CONTENT, successMessage, delayMillis, sequence)

  def cardPaymentsNotificationFailsWithAnInternalServerError(delayMillis: Int = 0, sequence: Int = 0): StubMapping =
    cardPaymentsNotificationRespondsWith(Status.INTERNAL_SERVER_ERROR, errorMessage, delayMillis, sequence)

  def cardPaymentsNotificationRespondsWith(status: Int, responseBody: String, delayMillis: Int = 0, sequence: Int = 0): StubMapping = {
    stubFor(
      post(urlEqualTo(endpoint))
        .inScenario("des")
        .whenScenarioStateIs(WireMockStub.state(sequence))
        .willReturn(aResponse()
          .withStatus(status)
          .withBody(responseBody)
          .withFixedDelay(delayMillis))
        .willSetStateTo(WireMockStub.nextState(sequence)))
  }

}
