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

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{postRequestedFor, urlEqualTo, verify}
import org.scalatest.Assertion
import pp.scheduling.mib.MibOpsMongoRepo
import support.PaymentsProcessData.modsPaymentCallBackRequestWithAmendmentRef
import support.{ItSpec, Mib}
import uk.gov.hmrc.http.{HttpResponse, UpstreamErrorResponse}

trait MibControllerSpec extends ItSpec {

  private lazy val repo = injector.instanceOf[MibOpsMongoRepo]

  override def beforeEach(): Unit = {
    repo.removeAll().futureValue
    WireMock.reset()
    super.beforeEach()
  }

  protected def numberOfQueuedNotifications: Long = repo.countAll().futureValue

  def aSynchronousEndpointWhenTheMibPaymentUpdateSucceeds(): Unit = {
      def verifySuccess(response: HttpResponse): Assertion = {
        response.status shouldBe 200
        verify(1, postRequestedFor(urlEqualTo(Mib.endpoint)))
        numberOfQueuedNotifications shouldBe 0
      }

    s"return Ok for a POST to the internal endpoint /mib/payment-callback/:reference" when {
      "the Mib call succeeds with OK, status=Successful" in {
        Mib.statusUpdateSucceeds()
        verifySuccess(testConnector.mibPaymentCallBack(modsPaymentCallBackRequestWithAmendmentRef).futureValue)
      }
    }
  }

  def aSynchronousEndpointWhenTheMibPaymentUpdateReturns4xx(): Unit = {
    "fail without persisting to the queue for a POST to the internal endpoint /mib/payment-callback" when {
      "the Mib call returns 400" in {
        Mib.statusUpdateRespondsWith(400, "")

        val response = testConnector.mibPaymentCallBack(modsPaymentCallBackRequestWithAmendmentRef).failed.futureValue
        response.isInstanceOf[UpstreamErrorResponse] shouldBe true

        numberOfQueuedNotifications shouldBe 0
      }

      "the Mib returns 404" in {
        Mib.statusUpdateRespondsWith(404, "")

        val response = testConnector.mibPaymentCallBack(modsPaymentCallBackRequestWithAmendmentRef).failed.futureValue

        response.asInstanceOf[UpstreamErrorResponse].reportAs shouldBe 502

        numberOfQueuedNotifications shouldBe 0
      }
    }
  }

}
