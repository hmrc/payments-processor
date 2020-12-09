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

package pp.controllers.pngrs

import com.github.tomakehurst.wiremock.client.WireMock.{postRequestedFor, urlEqualTo, verify}
import play.api.http.Status
import support.PaymentsProcessData.pngrStatusUpdateRequest
import support.Pngr

class PngrUpdateControllerQueuingAndPollingEnabledSpec extends PngrUpdateControllerSpec {

  override def configMap: Map[String, Any] =
    super
      .configMap
      .updated("pngr.queue.enabled", "true")
      .updated("pngr.poller.enabled", "true")

  "the PngrUpdateController" when {
    "polling is enabled and queuing is enabled" should {
      behave like aSynchronousEndpointWhenThePngrStatusUpdateSucceeds()
      behave like aSynchronousEndpointWhenThePngrStatusUpdateReturns4xx()
    }
  }

  "return Ok and then asynchronously process the notification" when {
    "the Pngr call fails with an internal server error" in {
      val delayInMilliSeconds = 10

      Pngr.statusUpdateFailsWithAnInternalServerError(delayInMilliSeconds)
      Pngr.statusUpdateFailsWithAnInternalServerError(delayInMilliSeconds, 1)
      Pngr.statusUpdateSucceeds(delayInMilliSeconds, 2)

      numberOfQueuedNotifications shouldBe 0

      val response = testConnector.sendStatusUpdateToPngr(pngrStatusUpdateRequest).futureValue
      response.status shouldBe Status.OK
      numberOfQueuedNotifications shouldBe 1

      eventually {
        numberOfQueuedNotifications shouldBe 0
      }

      verify(3, postRequestedFor(urlEqualTo("/bc-passengers-declarations/update-payment")))
    }
  }
}
