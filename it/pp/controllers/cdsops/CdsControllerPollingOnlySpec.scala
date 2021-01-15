package pp.controllers.cdsops

import com.github.tomakehurst.wiremock.client.WireMock.{getRequestedFor, urlEqualTo, verify}
import pp.services.CdsOpsService
import support.Cds
import support.PaymentsProcessData.cdsStatusUpdateRequest

class CdsControllerPollingOnlySpec extends CdsControllerSpec {

  private lazy val cdsService = injector.instanceOf[CdsOpsService]

  override def configMap: Map[String, Any] = super.configMap.updated("cds.poller.enabled", "true")

  "the CdsController" when {
    "polling is enabled and queuing is disabled" should {
      behave like aSynchronousEndpointWhenTheCdsPaymentUpdateSucceeds()
      behave like aSynchronousEndpointWhenTheCdsPaymentUpdateReturns4xx()

      "asynchronously process pre-existing queued notifications" in {
        val delayInMilliSeconds = 10

        Cds.statusUpdateFailsWithAnInternalServerError(delayInMilliSeconds)
        Cds.statusUpdateFailsWithAnInternalServerError(delayInMilliSeconds, 1)
        Cds.statusUpdateSucceeds(delayInMilliSeconds, 2)

        cdsService.sendCdsOpsToWorkItemRepo(cdsStatusUpdateRequest).futureValue
        numberOfQueuedNotifications shouldBe 1

        eventually {
          numberOfQueuedNotifications shouldBe 0
        }

        verify(3, getRequestedFor(urlEqualTo("/accounts/notifyimmediatepayment/v1")))
      }
    }
  }


}
