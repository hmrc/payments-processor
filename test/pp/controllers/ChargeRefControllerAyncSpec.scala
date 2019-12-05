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

package pp.controllers

import com.github.tomakehurst.wiremock.client.WireMock
import play.api.http.Status
import play.api.libs.json.Json
import pp.scheduling.ChargeRefNotificationMongoRepo
import support._

class ChargeRefControllerAyncSpec extends ItSpec {

  val testConnector = injector.instanceOf[TestConnector]

  val repo = injector.instanceOf[ChargeRefNotificationMongoRepo]

  override def beforeEach(): Unit = {
    val remove = repo.removeAll().futureValue
    WireMock.reset()

  }

  override def configMap =
    super
      .configMap
      .updated("queue.enabled", "true")
      .updated("poller.enabled", "true")

  "call sendCardPaymentsNotification expect ok" in {

    DesResponses.sendCardPaymentsNotification(200, 100, "", 0)
    val response = testConnector.sendCardPaymentsNotification(PaymentsProcessData.chargeRefNotificationDesRequest).futureValue
    response.status shouldBe Status.OK
    collectionSize shouldBe 0
    WireMock.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo("/cross-regime/repayment/VATC/new-api")))

  }

  "call sendCardPaymentsNotification expect ok with a message on the queue" in {

    DesResponses.sendCardPaymentsNotification(500, 100, "des failed", 0)
    val response = testConnector.sendCardPaymentsNotification(PaymentsProcessData.chargeRefNotificationDesRequest).futureValue
    response.status shouldBe Status.OK
    collectionSize shouldBe 1

  }

  "call sendCardPaymentsNotification expect ok with no messages on the queue" in {

    DesResponses.sendCardPaymentsNotification(500, 10, "des failed", 0)
    DesResponses.sendCardPaymentsNotification(500, 10, "des failed", 1)
    DesResponses.sendCardPaymentsNotification(200, 10, "ok", 2)
    val response = testConnector.sendCardPaymentsNotification(PaymentsProcessData.chargeRefNotificationDesRequest).futureValue
    Thread.sleep(2000)
    response.status shouldBe Status.OK
    WireMock.verify(3, WireMock.postRequestedFor(WireMock.urlEqualTo("/cross-regime/repayment/VATC/new-api")))
    collectionSize shouldBe 0

  }

  private def collectionSize: Int = {
    repo.count(Json.obj()).futureValue

  }

}