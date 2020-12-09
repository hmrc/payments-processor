package pp.controllers.mibops

import com.github.tomakehurst.wiremock.client.WireMock.{getRequestedFor, urlEqualTo, verify}
import play.api.http.Status
import support.Mib
import support.PaymentsProcessData.mibReference

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

          Mib.statusUpdateFailsWithAnInternalServerError(delayInMilliSeconds, reference = mibReference)
          Mib.statusUpdateFailsWithAnInternalServerError(delayInMilliSeconds, 1, reference = mibReference)
          Mib.statusUpdateSucceeds(delayInMilliSeconds, 2, reference = mibReference)

          numberOfQueuedNotifications shouldBe 0

          val response = testConnector.mibPaymentCallBack(mibReference).futureValue
          response.status shouldBe Status.OK
          numberOfQueuedNotifications shouldBe 1

          eventually {
            numberOfQueuedNotifications shouldBe 0
          }

          verify(3, getRequestedFor(urlEqualTo(s"/declare-commercial-goods/payment-callback/$mibReference")))
        }
      }
    }
  }


}
