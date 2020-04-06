package pp.controllers

import com.github.tomakehurst.wiremock.client.WireMock.{patchRequestedFor, postRequestedFor, urlEqualTo, verify}
import support.PaymentsProcessData.{chargeRefNotificationPciPalRequest, chargeRefNotificationRequest}
import support.{Des, TestSettings, TpsPaymentsBackend}
import uk.gov.hmrc.http.HttpResponse

class ChargeRefControllerTaxtypeCheckSpec extends ChargeRefControllerSpec {


  override def configMap: Map[String, Any] =
    super
      .configMap
      .updated("queue.enabled", "true")
      .updated("poller.enabled", "true")
      .updated("taxTypes.chargeref.ignore", Seq("p800"))


  private def verifySuccess(response: HttpResponse,
                            checkTpsBackend: Boolean = false,
                            backendCount: Int = 1
                           ): Unit = {
    response.status shouldBe 200
    verify(0, postRequestedFor(urlEqualTo(Des.endpoint)))
    if (checkTpsBackend) verify(backendCount, patchRequestedFor(urlEqualTo(TpsPaymentsBackend.endpoint)))
  }

  if (TestSettings.ChargeRefControllerTaxtypeCheckEnabled) {
    "return Ok for a POST to the public api /send-card-payments" when {
      "status=complete, des should not be called" in {
        TpsPaymentsBackend.tpsBackendOk
        verifySuccess(testConnector.sendCardPayments(chargeRefNotificationPciPalRequest).futureValue, checkTpsBackend = true)
      }

    }

    "return Ok for a POST to the synchronous api" when {
      "status=complete, des should not be called" in {
        TpsPaymentsBackend.tpsBackendOk
        verifySuccess(
          response = testConnector.sendCardPaymentsNotification(chargeRefNotificationRequest).futureValue,
          backendCount = 0
        )
      }

    }
  }


}
