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

import com.github.tomakehurst.wiremock.client.WireMock.{postRequestedFor, urlEqualTo, verify}
import pp.services.ChargeRefService
import support.{Des, TestSettings}
import support.PaymentsProcessData.chargeRefNotificationRequest

class ChargeRefControllerPollingOnlySpec extends ChargeRefControllerSpec {
  override def configMap: Map[String, Any] = super.configMap.updated("poller.enabled", "true")

  private lazy val chargeRefService = injector.instanceOf[ChargeRefService]

  if (TestSettings.ChargeRefControllerPollingOnlySpecEnabled) {
    "the ChargeRefController" when {
      "polling is enabled and queuing is disabled" should {
        behave like aSynchronousEndpointWhenTheDesNotificationSucceeds()
        behave like aSynchronousEndpointWhenTheDesNotificationReturns4xx()
        behave like aSynchronousEndpointWhenTheDesNotificationFailsWithAnInternalError()
        behave like aSynchronousEndpointWhenTheTpsBackendFailsWithAnInternalError()

        "asynchronously process pre-existing queued notifications" in {
          val delayInMilliSeconds = 10

          Des.cardPaymentsNotificationFailsWithAnInternalServerError(delayInMilliSeconds, 0)
          Des.cardPaymentsNotificationFailsWithAnInternalServerError(delayInMilliSeconds, 1)
          Des.cardPaymentsNotificationSucceeds(delayInMilliSeconds, 2)

          chargeRefService.sendCardPaymentsNotificationToWorkItemRepo(chargeRefNotificationRequest).futureValue
          numberOfQueuedNotifications shouldBe 1

          eventually {
            numberOfQueuedNotifications shouldBe 0
          }

          verify(3, postRequestedFor(urlEqualTo("/cross-regime/payments/card/notification")))
        }
      }
    }
  }
}
