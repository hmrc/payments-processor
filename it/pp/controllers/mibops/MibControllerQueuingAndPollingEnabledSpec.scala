package pp.controllers.mibops

import com.github.tomakehurst.wiremock.client.WireMock.{postRequestedFor, urlEqualTo, verify}
import play.api.http.Status
import support.Mib
import support.PaymentsProcessData.modsPaymentCallBackRequestWithAmendmentRef

class MibControllerQueuingAndPollingEnabledSpec extends MibControllerSpec {

  override def configMap: Map[String, Any] = super.configMap
    .updated("mibops.poller.enabled", "true")
    .updated("mibops.queue.enabled", "true")

  "the MibController" when {
    "polling is enabled and queuing is disabled" should {
      behave like aSynchronousEndpointWhenTheMibPaymentUpdateSucceeds()
      behave like aSynchronousEndpointWhenTheMibPaymentUpdateReturns4xx()

      "return Ok and then asynchronously process the notification" when {
        "the Mib call fails with an internal server error" in {
          val delayInMilliSeconds = 10

          Mib.statusUpdateFailsWithAnInternalServerError(delayInMilliSeconds)
          Mib.statusUpdateFailsWithAnInternalServerError(delayInMilliSeconds, 1)
          Mib.statusUpdateSucceeds(delayInMilliSeconds, 2)

          numberOfQueuedNotifications shouldBe 0

          val response = testConnector.mibPaymentCallBack(modsPaymentCallBackRequestWithAmendmentRef).futureValue
          response.status shouldBe Status.OK
          numberOfQueuedNotifications shouldBe 1

          eventually {
            numberOfQueuedNotifications shouldBe 0
          }

          verify(3, postRequestedFor(urlEqualTo("/declare-commercial-goods/payment-callback")))
        }
      }
    }
  }

}
