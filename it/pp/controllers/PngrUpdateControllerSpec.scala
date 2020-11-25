package pp.controllers


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

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{patchRequestedFor, postRequestedFor, urlEqualTo, verify}
import org.scalatest.Assertion
import play.api.libs.json.Json
import pp.model.TaxTypes.pngr
import pp.model.pngr.PngrStatusTypes
import pp.scheduling.pngr.PngrMongoRepo
import support.PaymentsProcessData._
import support.{ItSpec, Pngr, TpsPaymentsBackend}
import uk.gov.hmrc.http.{HttpResponse, UpstreamErrorResponse}

trait PngrUpdateControllerSpec extends ItSpec {
  private lazy val repo = injector.instanceOf[PngrMongoRepo]

  override def beforeEach(): Unit = {
    repo.removeAll().futureValue
    WireMock.reset()
    super.beforeEach()
  }

  protected def numberOfQueuedNotifications: Integer = repo.count(Json.obj()).futureValue


  def aSynchronousEndpointWhenThePngrStatusUpdateSucceeds(): Unit = {
    def verifySuccess(response: HttpResponse
                     ): Assertion = {
      response.status shouldBe 200
      verify(1, postRequestedFor(urlEqualTo(Pngr.endpoint)))
      numberOfQueuedNotifications shouldBe 0
    }



    s"return Ok for a POST to the internal endpoint /pngr/send-update" when {
      "the Pngr call succeeds with OK, status=Successful" in {
        Pngr.statusUpdateSucceeds()
        TpsPaymentsBackend.getTaxTypeOk(pngrPaymentItemId, pngr)
        verifySuccess(testConnector.sendStatusUpdateToPngr(pngrStatusUpdateRequest).futureValue)
      }

      "the Pngr call succeeds with OK, status=Failed" in {
        Pngr.statusUpdateSucceeds()
        TpsPaymentsBackend.getTaxTypeOk(pngrPaymentItemId, pngr)
        verifySuccess(testConnector.sendStatusUpdateToPngr(pngrStatusUpdateRequest.copy(status = PngrStatusTypes.Failed)).futureValue)
      }
    }
  }

  def aSynchronousEndpointWhenThePngrStatusUpdateReturns4xx(): Unit = {
    "fail without persisting to the queue for a POST to the internal endpoint /pngr/send-update" when {
      "the Pngr call returns 400" in {
        Pngr.statusUpdateRespondsWith(400, "")

        val response = testConnector.sendStatusUpdateToPngr(pngrStatusUpdateRequest).failed.futureValue
        response.isInstanceOf[UpstreamErrorResponse] shouldBe true

        numberOfQueuedNotifications shouldBe 0
      }

      "the Pngr returns 404" in {
        Pngr.statusUpdateRespondsWith(404, "")

        val response = testConnector.sendStatusUpdateToPngr(pngrStatusUpdateRequest).failed.futureValue
        response.asInstanceOf[UpstreamErrorResponse].reportAs shouldBe 502

        numberOfQueuedNotifications shouldBe 0
      }
    }
  }


}
