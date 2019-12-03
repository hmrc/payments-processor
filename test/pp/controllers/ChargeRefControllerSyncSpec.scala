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
import support._
import play.api.http.Status
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}

class ChargeRefControllerSyncSpec extends ItSpec {

  val testConnector = injector.instanceOf[TestConnector]

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .overrides(GuiceableModule.fromGuiceModules(Seq(overridingsModule)))
    .configure(configMap).build()

  override def configMap = Map[String, Any](
    "mongodb.uri " -> "mongodb://localhost:27017/payments-processor-it",
    "queue.retryAfter" -> "1 seconds",
    "queue.enabled" -> false,
    "microservice.services.des.port" -> WireMockSupport.port
  )

  "call sendCardPaymentsNotification expect ok" in {

    DesWireMockResponses.sendCardPaymentsNotification
    val response = testConnector.sendCardPaymentsNotification(PaymentsProcessData.chargeRefNotificationDesRequest).futureValue
    response.status shouldBe Status.OK

  }

}
