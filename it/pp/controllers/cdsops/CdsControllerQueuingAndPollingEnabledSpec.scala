package pp.controllers.cdsops

import com.github.tomakehurst.wiremock.client.WireMock.{postRequestedFor, urlEqualTo, verify}
import play.api.http.Status
import support.Cds
import support.PaymentsProcessData.cdsStatusUpdateRequest

class CdsControllerQueuingAndPollingEnabledSpec extends CdsControllerSpec {

  override def configMap: Map[String, Any] = super.configMap
    .updated("cds.poller.enabled", "true")
    .updated("cds.queue.enabled", "true")

  "the CdsController" when {
    "polling is enabled and queuing is enabled" should {
      behave like aSynchronousEndpointWhenTheCdsPaymentUpdateSucceeds()
      behave like aSynchronousEndpointWhenTheCdsPaymentUpdateReturns4xx()
    }
  }

  "return Ok and then asynchronously process the notification" when {
    "the Cds call fails with an internal server error" in {
      val delayInMilliSeconds = 10

      Cds.statusUpdateFailsWithAnInternalServerError(delayInMilliSeconds)
      Cds.statusUpdateFailsWithAnInternalServerError(delayInMilliSeconds, 1)
      Cds.statusUpdateSucceeds(delayInMilliSeconds, 2)

      numberOfQueuedNotifications shouldBe 0

      val response = testConnector.sendStatusUpdateToCds(cdsStatusUpdateRequest).futureValue
      response.status shouldBe Status.OK
      numberOfQueuedNotifications shouldBe 1

      eventually {
        numberOfQueuedNotifications shouldBe 0
      }

      verify(3, postRequestedFor(urlEqualTo("/accounts/notifyimmediatepayment/v1")))
    }
  }


}
