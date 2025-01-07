/*
 * Copyright 2024 HM Revenue & Customs
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

package pp.controllers.mibops

import com.github.tomakehurst.wiremock.client.WireMock.{postRequestedFor, urlEqualTo, verify}
import pp.services.MibOpsService
import support.Mib
import support.PaymentsProcessData.modsPaymentCallBackRequestWithAmendmentRef

class MibControllerPollingOnlySpec extends MibControllerSpec {

  private lazy val mibService = injector.instanceOf[MibOpsService]

  override def configMap: Map[String, Any] = super.configMap
    .updated("mibops.poller.enabled", "true")

  "the MibController" when {
    "polling is enabled and queuing is disabled" should {
      behave like aSynchronousEndpointWhenTheMibPaymentUpdateSucceeds()
      behave like aSynchronousEndpointWhenTheMibPaymentUpdateReturns4xx()

      "asynchronously process pre-existing queued notifications" in {
        val delayInMilliSeconds = 10

        Mib.statusUpdateFailsWithAnInternalServerError(delayInMilliSeconds)
        Mib.statusUpdateFailsWithAnInternalServerError(delayInMilliSeconds, 1)
        Mib.statusUpdateSucceeds(delayInMilliSeconds, 2)

        mibService.sendMibOpsToWorkItemRepo(modsPaymentCallBackRequestWithAmendmentRef).futureValue
        numberOfQueuedNotifications shouldBe 1

        eventually {
          numberOfQueuedNotifications shouldBe 0
        }

        verify(3, postRequestedFor(urlEqualTo(s"/declare-commercial-goods/payment-callback")))
      }
    }
  }

}
