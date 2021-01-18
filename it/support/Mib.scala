package support


import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status


object Mib {

  def endpoint(reference: String) = s"/declare-commercial-goods/payment-callback/$reference"

  val errorMessage = "mib failed"
  val successMessage = "ok"

  def statusUpdateSucceeds(delayMillis: Int = 0, sequence: Int = 0, reference: String): StubMapping =
    statusUpdateRespondsWith(status = Status.OK, responseBody = successMessage, delayMillis = delayMillis, sequence = sequence, reference)

  def statusUpdateFailsWithAnInternalServerError(delayMillis: Int = 0, sequence: Int = 0, reference: String): StubMapping =
    statusUpdateRespondsWith(Status.INTERNAL_SERVER_ERROR, errorMessage, delayMillis, sequence, reference)

  def statusUpdateRespondsWith(status: Int, responseBody: String, delayMillis: Int = 0, sequence: Int = 0, reference: String): StubMapping = {
    stubFor(
      get(urlEqualTo(endpoint(reference)))
        .inScenario("mib")
        .whenScenarioStateIs(WireMockStub.state(sequence))
        .willReturn(aResponse()
          .withStatus(status)
          .withBody(responseBody)
          .withFixedDelay(delayMillis))
        .willSetStateTo(WireMockStub.nextState(sequence))

    )
  }

}
