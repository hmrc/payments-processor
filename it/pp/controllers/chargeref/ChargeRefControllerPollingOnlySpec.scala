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

package pp.controllers.chargeref

import com.github.tomakehurst.wiremock.client.WireMock.{postRequestedFor, urlEqualTo, verify}
import pp.services.ChargeRefService
import support.Des
import support.PaymentsProcessData.p800ChargeRefNotificationRequest

class ChargeRefControllerPollingOnlySpec extends ChargeRefControllerSpec {
  private lazy val chargeRefService = injector.instanceOf[ChargeRefService]

  override def configMap: Map[String, Any] = super.configMap
    .updated("chargeref.poller.enabled", "true")
    .updated("pngr.poller.enabled", "true")

  "the ChargeRefController" when {
    "polling is enabled and queuing is disabled" should {
      behave like aSynchronousEndpointWhenTheDesNotificationSucceeds()
      behave like aSynchronousEndpointWhenTheDesNotificationReturns4xx()
      behave like aSynchronousEndpointWhenTheDesNotificationFailsWithAnInternalError()
      behave like aSynchronousEndpointWhenTheTpsUodateFailsWithAnInternalError()
      behave like aSynchronousEndpointWhenTpsGetTaxTypeFailsWith404()
      behave like aSynchronousEndpointWhenTheDesNotificationFailsWithIncorrectJsonCall()

      "asynchronously process pre-existing queued notifications" in {
        val delayInMilliSeconds = 10

        Des.cardPaymentsNotificationFailsWithAnInternalServerError(delayInMilliSeconds)
        Des.cardPaymentsNotificationFailsWithAnInternalServerError(delayInMilliSeconds, 1)
        Des.cardPaymentsNotificationSucceeds(delayInMilliSeconds, 2)

        chargeRefService.sendCardPaymentsNotificationToWorkItemRepo(p800ChargeRefNotificationRequest).futureValue
        numberOfQueuedNotifications shouldBe 1

        eventually {
          numberOfQueuedNotifications shouldBe 0
        }

        verify(3, postRequestedFor(urlEqualTo("/cross-regime/payments/card/notification")))
      }
    }
  }

}
