package pp.controllers.mibops

import com.github.tomakehurst.wiremock.client.WireMock.{postRequestedFor, urlEqualTo, verify}
import play.api.http.Status
import support.Mib
import support.PaymentsProcessData.modsPaymentCallBackRequestWithAmendmentRef

class MibControllerQueuingOnlySpec extends MibControllerSpec {
  override def configMap: Map[String, Any] = super.configMap.updated("mibops.queue.enabled", "true")

  "the MibController" when {
    "polling is enabled and queuing is disabled" should {
      behave like aSynchronousEndpointWhenTheMibPaymentUpdateSucceeds()
      behave like aSynchronousEndpointWhenTheMibPaymentUpdateReturns4xx()

      "return OK and persist to the queue but not process asynchronously" when {
        "the Mib call fails with an internal server error" in {
          Mib.statusUpdateFailsWithAnInternalServerError()

          numberOfQueuedNotifications shouldBe 0

          val response = testConnector.mibPaymentCallBack(modsPaymentCallBackRequestWithAmendmentRef).futureValue
          response.status shouldBe Status.OK

          numberOfQueuedNotifications shouldBe 1

          verify(1, postRequestedFor(urlEqualTo("/declare-commercial-goods/payment-callback")))
        }
      }
    }
  }
}
