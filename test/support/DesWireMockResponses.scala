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
import com.github.tomakehurst.wiremock.stubbing.Scenario

object DesWireMockResponses {

  def sendCardPaymentsNotification = {
    stubFor(post(urlMatching("/cross-regime/repayment/VATC/new-api"))
      .willReturn(aResponse()
        .withStatus(200)))

  }

  def sendCardPaymentsNotificationFailure = {
    stubFor(post(urlMatching("/cross-regime/repayment/VATC/new-api"))
      .willReturn(aResponse()
        .withStatus(500)
        .withBody("des failed")))
  }

  def sendCardPaymentsNotificationFailurePersistent = {

    stubFor(post(urlMatching("/cross-regime/repayment/VATC/new-api"))
      .inScenario("JMS Test")
      .whenScenarioStateIs(Scenario.STARTED)
      .willReturn(aResponse()
        .withStatus(500)
        .withBody("des failed"))
      .willSetStateTo("Success"))

  }

  def sendCardPaymentsNotificationSuccessPersistent = {

    stubFor(post(urlMatching("/cross-regime/repayment/VATC/new-api"))
      .inScenario("JMS Test")
      .whenScenarioStateIs("Success")
      .willReturn(aResponse()
        .withStatus(200)))

  }

}
