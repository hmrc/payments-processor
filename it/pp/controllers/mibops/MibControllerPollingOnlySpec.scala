package pp.controllers.mibops

import com.github.tomakehurst.wiremock.client.WireMock.{getRequestedFor, urlEqualTo, verify}
import pp.services.MibOpsService
import support.Mib
import support.PaymentsProcessData.mibReference

class MibControllerPollingOnlySpec extends MibControllerSpec {

  private lazy val mibService = injector.instanceOf[MibOpsService]

  override def configMap: Map[String, Any] = super.configMap
    .updated("mibops.poller.enabled", "true")


  "the MibController" when {
    "polling is enabled and queuing is disabled" should {
      behave like aSynchronousEndpointWhenTheMibPaymentUpdateSucceeds()
      behave like aSynchronousEndpointWhenTheMibPaymentUpdateReturns4xx()

      "asynchronously process pre-existing queued notifications" in {
        val delayInMilliSeconds = 10

        Mib.statusUpdateFailsWithAnInternalServerError(delayInMilliSeconds, reference = mibReference)
        Mib.statusUpdateFailsWithAnInternalServerError(delayInMilliSeconds, 1, reference = mibReference)
        Mib.statusUpdateSucceeds(delayInMilliSeconds, 2, reference = mibReference)

        mibService.sendMibOpsToWorkItemRepo(mibReference).futureValue
        numberOfQueuedNotifications shouldBe 1

        eventually {
          numberOfQueuedNotifications shouldBe 0
        }

        verify(3, getRequestedFor(urlEqualTo(s"/declare-commercial-goods/payment-callback/$mibReference")))
      }
    }
  }


}
