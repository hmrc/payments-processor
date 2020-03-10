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
import com.github.tomakehurst.wiremock.client.WireMock.{postRequestedFor, urlEqualTo, verify, patchRequestedFor}
import org.scalatest.Assertion
import play.api.libs.json.Json
import pp.model.StatusTypes
import pp.scheduling.ChargeRefNotificationMongoRepo
import support.PaymentsProcessData.{chargeRefNotificationPciPalRequest, chargeRefNotificationRequest}
import support.{Des, ItSpec, TpsPaymentsBackend}
import uk.gov.hmrc.http.{BadRequestException, HttpResponse, Upstream5xxResponse}

trait ChargeRefControllerSpec extends ItSpec {
  private lazy val repo = injector.instanceOf[ChargeRefNotificationMongoRepo]

  override def beforeEach(): Unit = {
    repo.removeAll().futureValue
    WireMock.reset()
  }

  protected def numberOfQueuedNotifications: Integer = repo.count(Json.obj()).futureValue

  def aSynchronousEndpointWhenTheDesNotificationSucceeds(): Unit = {
      def verifySuccess(response: HttpResponse,
                        checkDes: Boolean = true,
                        checkTpsBackend: Boolean = false
                       ): Assertion = {
        response.status shouldBe 200
        if (checkDes) verify(1, postRequestedFor(urlEqualTo(Des.endpoint)))
        if (checkTpsBackend) verify(1, patchRequestedFor(urlEqualTo(TpsPaymentsBackend.endpoint)))
        numberOfQueuedNotifications shouldBe 0
      }


    "return Ok for a POST to the internal endpoint /send-card-payments-notification" when {
      "the Des call succeeds with OK" in {
        Des.cardPaymentsNotificationSucceeds()
        verifySuccess(testConnector.sendCardPaymentsNotification(chargeRefNotificationRequest).futureValue)
      }

      "the Des call succeeds with NO_CONTENT" in {
        Des.cardPaymentsNotificationSucceedsWithNoContent()
        verifySuccess(testConnector.sendCardPaymentsNotification(chargeRefNotificationRequest).futureValue)
      }
    }

    "return Ok for a POST to the public api /send-card-payments" when {
      "the Des call succeeds with OK, status=complete" in {
        Des.cardPaymentsNotificationSucceeds()
        TpsPaymentsBackend.tpsBackendOk
        verifySuccess(testConnector.sendCardPayments(chargeRefNotificationPciPalRequest).futureValue, checkTpsBackend= true)
      }

      "the Des call succeeds with NO_CONTENT,status=complete" in {
        Des.cardPaymentsNotificationSucceedsWithNoContent()
        TpsPaymentsBackend.tpsBackendOk
        verifySuccess(testConnector.sendCardPayments(chargeRefNotificationPciPalRequest).futureValue, checkTpsBackend = true)
      }

      "the Des call succeeds with OK, status=failed" in {
        Des.cardPaymentsNotificationSucceeds()
        TpsPaymentsBackend.tpsBackendOk
        verifySuccess(testConnector.sendCardPayments(chargeRefNotificationPciPalRequest.copy(Status = StatusTypes.failed)).futureValue, false, true)
      }
    }
  }

  def aSynchronousEndpointWhenTheDesNotificationReturns4xx(): Unit = {
    "fail without persisting to the queue for a POST to the internal endpoint /send-card-payments-notification" when {
      "the Des call returns 400" in {
        Des.cardPaymentsNotificationRespondsWith(400, "")

        val response = testConnector.sendCardPaymentsNotification(chargeRefNotificationRequest).failed.futureValue
        response.isInstanceOf[BadRequestException] shouldBe true

        numberOfQueuedNotifications shouldBe 0
      }

      "the Des call returns 404" in {
        Des.cardPaymentsNotificationRespondsWith(404, "")

        val response = testConnector.sendCardPaymentsNotification(chargeRefNotificationRequest).failed.futureValue
        response.asInstanceOf[Upstream5xxResponse].reportAs shouldBe 502

        numberOfQueuedNotifications shouldBe 0
      }

      "the Des call returns 409" in {
        Des.cardPaymentsNotificationRespondsWith(409, "")

        val response = testConnector.sendCardPaymentsNotification(chargeRefNotificationRequest).failed.futureValue
        response.asInstanceOf[Upstream5xxResponse].reportAs shouldBe 502

        numberOfQueuedNotifications shouldBe 0
      }
    }
  }

  def aSynchronousEndpointWhenTheDesNotificationFailsWithAnInternalError(): Unit = {
    "fail without persisting to the queue" when {
      "the des call fails with an error" in {
        Des.cardPaymentsNotificationFailsWithAnInternalServerError()

        val failure = testConnector.sendCardPaymentsNotification(chargeRefNotificationRequest).failed.futureValue

        failure.getMessage should include(Des.errorMessage)
        numberOfQueuedNotifications shouldBe 0

        verify(1, postRequestedFor(urlEqualTo("/cross-regime/payments/card/notification")))
      }
    }
  }


  def aSynchronousEndpointWhenTheTpsBackendFailsWithAnInternalError(): Unit = {
    "fail without persisting to the queue" when {
      "the tps-backend call fails with an error" in {
        Des.cardPaymentsNotificationSucceedsWithNoContent()
        TpsPaymentsBackend.tpsBackendFailed

        val failure = testConnector.sendCardPayments(chargeRefNotificationPciPalRequest).failed.futureValue

        failure.getMessage should include(TpsPaymentsBackend.errorMessage)
        numberOfQueuedNotifications shouldBe 0

        verify(0, postRequestedFor(urlEqualTo("/cross-regime/payments/card/notification")))
        verify(1, patchRequestedFor(urlEqualTo(TpsPaymentsBackend.endpoint)))
      }
    }
  }


}
