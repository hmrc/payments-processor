package pp.controllers.cdsops

class CdsControllerQueuingAndPollingDisabledSpec extends CdsControllerSpec {

  "the CdsController" when {
    "polling is enabled and queuing is disabled" should {
      behave like aSynchronousEndpointWhenTheCdsPaymentUpdateSucceeds()
      behave like aSynchronousEndpointWhenTheCdsPaymentUpdateReturns4xx()
    }

  }
}
