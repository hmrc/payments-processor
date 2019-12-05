/*
 * Copyright 2019 HM Revenue & Customs
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
import com.github.tomakehurst.wiremock.stubbing.{StubMapping}

object DesResponses {

  def sendCardPaymentsNotification(status: Int, delayMillis: Int, responseBody: String, sequence: Int = 0): StubMapping = {
    stubFor(
      post(urlEqualTo(s"/cross-regime/repayment/VATC/new-api"))
        .inScenario("des")
        .whenScenarioStateIs(WiremockStub.state(sequence))
        .willReturn(aResponse()
          .withStatus(status)
          .withBody(responseBody)
          .withFixedDelay(delayMillis))
        .willSetStateTo(WiremockStub.nextState(sequence))

    )
  }

}
