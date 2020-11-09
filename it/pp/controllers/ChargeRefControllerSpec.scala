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

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{patchRequestedFor, postRequestedFor, urlEqualTo, verify}
import org.scalatest.Assertion
import play.api.libs.json.Json
import pp.model.StatusTypes.failed
import pp.model.TaxTypes.p800
import pp.scheduling.chargeref.ChargeRefNotificationMongoRepo
import support.PaymentsProcessData._
import support.{Des, ItSpec, TpsPaymentsBackend}
import uk.gov.hmrc.http.{HttpResponse, UpstreamErrorResponse}

trait ChargeRefControllerSpec extends ItSpec {
  private lazy val repo = injector.instanceOf[ChargeRefNotificationMongoRepo]

  override def beforeEach(): Unit = {
    repo.removeAll().futureValue
    WireMock.reset()
    super.beforeEach()
  }

  protected def numberOfQueuedNotifications: Integer = repo.count(Json.obj()).futureValue


  def aSynchronousEndpointWhenTheDesNotificationSucceeds(): Unit = {
    def verifySuccess(response: HttpResponse,
                      checkDes: Boolean = true,
                      checkTpsBackend: Boolean = false
                     ): Assertion = {
      response.status shouldBe 200
      if (checkDes) verify(1, postRequestedFor(urlEqualTo(Des.endpoint)))
      if (checkTpsBackend) verify(1, patchRequestedFor(urlEqualTo(TpsPaymentsBackend.updateEndpoint)))
      numberOfQueuedNotifications shouldBe 0
    }


    "return Ok for a POST to the internal endpoint /send-card-payments-notification" when {
      "the Des call succeeds with OK" in {
        Des.cardPaymentsNotificationSucceeds()
        verifySuccess(testConnector.sendCardPaymentsNotification(p800ChargeRefNotificationRequest).futureValue)
      }

      "the Des call succeeds with NO_CONTENT" in {
        Des.cardPaymentsNotificationSucceedsWithNoContent()
        verifySuccess(testConnector.sendCardPaymentsNotification(p800ChargeRefNotificationRequest).futureValue)
      }
    }

    s"return Ok for a POST to the public api /send-card-payments" when {
      "the Des call succeeds with OK, status=complete" in {
        Des.cardPaymentsNotificationSucceeds()
        TpsPaymentsBackend.getTaxTypeOk(p800PaymentItemId, p800)
        TpsPaymentsBackend.tpsUpdateOk
        verifySuccess(testConnector.sendCardPayments(p800PcipalNotification).futureValue, checkTpsBackend = true)
      }

      "the Des call succeeds with NO_CONTENT,status=complete" in {
        Des.cardPaymentsNotificationSucceedsWithNoContent()
        TpsPaymentsBackend.getTaxTypeOk(p800PaymentItemId, p800)
        TpsPaymentsBackend.tpsUpdateOk
        verifySuccess(testConnector.sendCardPayments(p800PcipalNotification).futureValue, checkTpsBackend = true)
      }

      "the Des call succeeds with OK, status=failed" in {
        Des.cardPaymentsNotificationSucceeds()
        TpsPaymentsBackend.getTaxTypeOk(p800PaymentItemId, p800)
        TpsPaymentsBackend.tpsUpdateOk
        verifySuccess(testConnector.sendCardPayments(p800PcipalNotification.copy(Status = failed)).futureValue, checkDes = false, checkTpsBackend = true)
      }
    }
  }

  def aSynchronousEndpointWhenTheDesNotificationReturns4xx(): Unit = {
    "fail without persisting to the queue for a POST to the internal endpoint /send-card-payments-notification" when {
      "the Des call returns 400" in {
        Des.cardPaymentsNotificationRespondsWith(400, "")

        val response = testConnector.sendCardPaymentsNotification(p800ChargeRefNotificationRequest).failed.futureValue
        response.isInstanceOf[UpstreamErrorResponse] shouldBe true

        numberOfQueuedNotifications shouldBe 0
      }

      "the Des call returns 404" in {
        Des.cardPaymentsNotificationRespondsWith(404, "")

        val response = testConnector.sendCardPaymentsNotification(p800ChargeRefNotificationRequest).failed.futureValue
        response.asInstanceOf[UpstreamErrorResponse].reportAs shouldBe 502

        numberOfQueuedNotifications shouldBe 0
      }

      "the Des call returns 409" in {
        Des.cardPaymentsNotificationRespondsWith(409, "")

        val response = testConnector.sendCardPaymentsNotification(p800ChargeRefNotificationRequest).failed.futureValue
        response.asInstanceOf[UpstreamErrorResponse].reportAs shouldBe 502

        numberOfQueuedNotifications shouldBe 0
      }
    }
  }

  def aSynchronousEndpointWhenTheDesNotificationFailsWithAnInternalError(): Unit = {
    "fail without persisting to the queue" when {
      "the des call fails with an error" in {
        Des.cardPaymentsNotificationFailsWithAnInternalServerError()

        val failure = testConnector.sendCardPaymentsNotification(p800ChargeRefNotificationRequest).failed.futureValue

        failure.getMessage should include(Des.errorMessage)
        numberOfQueuedNotifications shouldBe 0

        verify(1, postRequestedFor(urlEqualTo("/cross-regime/payments/card/notification")))
      }
    }
  }

  def aSynchronousEndpointWhenTheTpsUodateFailsWithAnInternalError(): Unit = {
    "fail without persisting to the queue" when {
      "the tps-backend update call fails with an error" in {
        Des.cardPaymentsNotificationSucceedsWithNoContent()
        TpsPaymentsBackend.getTaxTypeOk(p800PaymentItemId, p800)
        TpsPaymentsBackend.tpsUpdateFailed

        val failure = testConnector.sendCardPayments(p800PcipalNotification).failed.futureValue

        failure.getMessage should include(TpsPaymentsBackend.updateErrorMessage)
        numberOfQueuedNotifications shouldBe 0

        verify(0, postRequestedFor(urlEqualTo("/cross-regime/payments/card/notification")))
        verify(1, patchRequestedFor(urlEqualTo(TpsPaymentsBackend.updateEndpoint)))
      }
    }
  }

  def aSynchronousEndpointWhenTpsGetTaxTypeFailsWith404(): Unit = {
    "fail without persisting to the queue" when {
      "the tps-backend getTaxType call fails with an error" in {
        TpsPaymentsBackend.getTaxTypeNotFound(p800PaymentItemId)

        val failure = testConnector.sendCardPayments(p800PcipalNotification).failed.futureValue

        failure.getMessage should include(TpsPaymentsBackend.notFoundErrorMessage)
        numberOfQueuedNotifications shouldBe 0

        verify(0, postRequestedFor(urlEqualTo("/cross-regime/payments/card/notification")))
        verify(0, patchRequestedFor(urlEqualTo(TpsPaymentsBackend.updateEndpoint)))
      }
    }
  }
}
