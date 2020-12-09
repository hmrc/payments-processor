package pp.controllers.mibops

class MibControllerQueuingAndPollingDisabledSpec extends MibControllerSpec {

  "the MibController" when {
    "polling is enabled and queuing is disabled" should {
      behave like aSynchronousEndpointWhenTheMibPaymentUpdateSucceeds()
      behave like aSynchronousEndpointWhenTheMibPaymentUpdateReturns4xx()
    }

  }
}
