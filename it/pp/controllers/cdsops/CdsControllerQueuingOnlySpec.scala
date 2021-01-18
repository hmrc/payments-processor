package pp.controllers.cdsops

import com.github.tomakehurst.wiremock.client.WireMock.{postRequestedFor, urlEqualTo, verify}
import play.api.http.Status
import support.Cds
import support.PaymentsProcessData.cdsStatusUpdateRequest

class CdsControllerQueuingOnlySpec extends CdsControllerSpec {
  override def configMap: Map[String, Any] = super.configMap.updated("cds.queue.enabled", "true")

  "the CdsController" when {
    "polling is enabled and queuing is disabled" should {
      behave like aSynchronousEndpointWhenTheCdsPaymentUpdateSucceeds()
      behave like aSynchronousEndpointWhenTheCdsPaymentUpdateReturns4xx()

      "return OK and persist to the queue but not process asynchronously" when {
        "the Cds call fails with an internal server error" in {
          Cds.statusUpdateFailsWithAnInternalServerError()

          numberOfQueuedNotifications shouldBe 0

          val response = testConnector.sendStatusUpdateToCds(cdsStatusUpdateRequest).futureValue
          response.status shouldBe Status.OK

          numberOfQueuedNotifications shouldBe 1

          verify(1, postRequestedFor(urlEqualTo("/accounts/notifyimmediatepayment/v1")))
        }
      }
    }
  }
}
