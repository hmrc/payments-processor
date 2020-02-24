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
import com.github.tomakehurst.wiremock.client.WireMock.{postRequestedFor, urlEqualTo, verify}
import org.scalatest.Assertion
import play.api.libs.json.Json
import pp.scheduling.ChargeRefNotificationMongoRepo
import support.PaymentsProcessData.chargeRefNotificationRequest
import support.{Des, ItSpec}
import uk.gov.hmrc.http.{BadRequestException, HttpResponse, Upstream5xxResponse}

trait ChargeRefControllerSpec extends ItSpec {
  private lazy val repo = injector.instanceOf[ChargeRefNotificationMongoRepo]

  override def beforeEach(): Unit = {
    repo.removeAll().futureValue
    WireMock.reset()
  }

  protected def numberOfQueuedNotifications: Integer = repo.count(Json.obj()).futureValue

  def aSynchronousEndpointWhenTheDesNotificationSucceeds(): Unit = {
      def verifySuccess(response: HttpResponse): Assertion = {
        response.status shouldBe 200
        verify(1, postRequestedFor(urlEqualTo(Des.endpoint)))
        numberOfQueuedNotifications shouldBe 0
      }

    "return Ok for a POST to the internal endpoint /send-card-payments-notification" when {
      "the Des call succeeds with OK" in {
        Des.cardPaymentsNotificationSucceeds()
        verifySuccess(testConnector.sendCardPaymentsNotification(chargeRefNotificationRequest).futureValue)
      }

      "the Des call succeeds with NO_CONTENT" in {
        Des.cardPaymentsNotificationSucceedsWithNoContent()
        verifySuccess(testConnector.sendCardPayments(chargeRefNotificationRequest).futureValue)
      }
    }

    "return Ok for a POST to the public api /send-card-payments" when {
      "the Des call succeeds with OK" in {
        Des.cardPaymentsNotificationSucceeds()
        verifySuccess(testConnector.sendCardPayments(chargeRefNotificationRequest).futureValue)
      }

      "the Des call succeeds with NO_CONTENT" in {
        Des.cardPaymentsNotificationSucceedsWithNoContent()
        verifySuccess(testConnector.sendCardPayments(chargeRefNotificationRequest).futureValue)
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
}
