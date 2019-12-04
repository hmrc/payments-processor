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

import play.api.Application
import play.api.http.Status
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.Json
import pp.scheduling.ChargeRefNotificationMongoRepo
import support._

class ChargeRefControllerAyncSpec extends ItSpec {

  val testConnector = injector.instanceOf[TestConnector]

  val repo = injector.instanceOf[ChargeRefNotificationMongoRepo]

  override def beforeEach(): Unit = {
    val remove = repo.removeAll().futureValue
  }

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .overrides(GuiceableModule.fromGuiceModules(Seq(overridingsModule)))
    .configure(configMap).build()

  override def configMap = Map[String, Any](
    "mongodb.uri " -> "mongodb://localhost:27017/payments-processor-it",
    "queue.retryAfter" -> "1 seconds",
    "microservice.services.des.port" -> WireMockSupport.port,
    "queue.enabled" -> true,
    "poller.enabled" -> true,
    "queue.retryAfter" -> "1 seconds",
    "poller.initialDelay" -> "1 seconds",
    "poller.interval" -> "1 seconds"
  )

  "call sendCardPaymentsNotification expect ok" in {

    DesWireMockResponses.sendCardPaymentsNotification
    val response = testConnector.sendCardPaymentsNotification(PaymentsProcessData.chargeRefNotificationDesRequest).futureValue
    response.status shouldBe Status.OK
    Thread.sleep(1000)
    collectionSize shouldBe 0

  }

  "call sendCardPaymentsNotification expect ok with a message on the queue" in {

    DesWireMockResponses.sendCardPaymentsNotificationFailure
    val response = testConnector.sendCardPaymentsNotification(PaymentsProcessData.chargeRefNotificationDesRequest).futureValue
    response.status shouldBe Status.OK
    Thread.sleep(1000)
    collectionSize shouldBe 1

  }

  "call sendCardPaymentsNotification expect ok with no messages on the queue" in {

    DesWireMockResponses.sendCardPaymentsNotificationFailurePersistent
    DesWireMockResponses.sendCardPaymentsNotificationSuccessPersistent
    val response = testConnector.sendCardPaymentsNotification(PaymentsProcessData.chargeRefNotificationDesRequest).futureValue
    response.status shouldBe Status.OK
    Thread.sleep(1000)
    collectionSize shouldBe 0

  }

  private def collectionSize: Int = {
    repo.count(Json.obj()).futureValue

  }

}
