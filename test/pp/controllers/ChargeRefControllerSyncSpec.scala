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

package pp.controllers

import com.github.tomakehurst.wiremock.client.WireMock
import play.api.http.Status
import pp.scheduling.ChargeRefNotificationMongoRepo
import support._

class ChargeRefControllerSyncSpec extends ItSpec {

  val testConnector = injector.instanceOf[TestConnector]

  val repo = injector.instanceOf[ChargeRefNotificationMongoRepo]

  override def beforeEach(): Unit = {
    val remove = repo.removeAll().futureValue
    WireMock.reset()
  }

  "call sendCardPaymentsNotification expect ok" in {

    DesResponses.sendCardPaymentsNotification(200, 0, "ok", 0)
    val response = testConnector.sendCardPaymentsNotification(PaymentsProcessData.chargeRefNotificationDesRequest).futureValue
    response.status shouldBe Status.OK
    WireMock.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo("/cross-regime/payments/card/notification")))
  }

  "call sendCardPaymentsNotification expect failure if des fails" in {
    DesResponses.sendCardPaymentsNotification(500, 0, "des failed", 0)
    val response = testConnector.sendCardPaymentsNotification(PaymentsProcessData.chargeRefNotificationDesRequest).failed.futureValue
    response.getMessage should include ("des failed")

  }

  "call sendCardPaymentsNotification from root, expect ok" in {
    DesResponses.sendCardPaymentsNotification(200, 0, "ok", 0)
    val response = testConnector.sendCardPaymentsNotificationRoot(PaymentsProcessData.chargeRefNotificationDesRequest).futureValue
    response.status shouldBe Status.OK

  }

}
