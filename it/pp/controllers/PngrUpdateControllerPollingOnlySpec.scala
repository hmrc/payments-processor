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
import pp.services.pngr.PngrService
import support.PaymentsProcessData.pngrStatusUpdateRequest
import support.Pngr

class PngrUpdateControllerPollingOnlySpec extends PngrUpdateControllerSpec {
  private lazy val pngrService = injector.instanceOf[PngrService]

  override def configMap: Map[String, Any] = super.configMap.updated("pngr.poller.enabled", "true")

  "the PngrUpdateController" when {
    "polling is enabled and queuing is disabled" should {
      behave like aSynchronousEndpointWhenThePngrStatusUpdateSucceeds()
      behave like aSynchronousEndpointWhenThePngrStatusUpdateReturns4xx()

      "asynchronously process pre-existing queued notifications" in {
        val delayInMilliSeconds = 10

        Pngr.statusUpdateFailsWithAnInternalServerError(delayInMilliSeconds)
        Pngr.statusUpdateFailsWithAnInternalServerError(delayInMilliSeconds, 1)
        Pngr.statusUpdateSucceeds(delayInMilliSeconds, 2)

        pngrService.sendPngrToWorkItemRepo(pngrStatusUpdateRequest).futureValue
        numberOfQueuedNotifications shouldBe 1

        eventually {
          numberOfQueuedNotifications shouldBe 0
        }

        verify(3, postRequestedFor(urlEqualTo("/bc-passengers-declarations/update-payment")))
      }
    }
  }
}
