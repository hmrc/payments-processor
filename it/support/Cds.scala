package support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status

object Cds {

  val endpoint = "/accounts/notifyimmediatepayment/v1"
  val errorMessage = "cds failed"
  val successMessage = "ok"

  def statusUpdateSucceeds(delayMillis: Int = 0, sequence: Int = 0): StubMapping =
    statusUpdateRespondsWith(status = Status.OK, responseBody = successMessage, delayMillis = delayMillis, sequence = sequence)

  def statusUpdateFailsWithAnInternalServerError(delayMillis: Int = 0, sequence: Int = 0): StubMapping =
    statusUpdateRespondsWith(Status.INTERNAL_SERVER_ERROR, errorMessage, delayMillis, sequence)

  def statusUpdateRespondsWith(status: Int, responseBody: String, delayMillis: Int = 0, sequence: Int = 0): StubMapping = {
    stubFor(
      post(urlEqualTo(endpoint))
        .inScenario("cds")
        .whenScenarioStateIs(WireMockStub.state(sequence))
        .willReturn(aResponse()
          .withStatus(status)
          .withBody(responseBody)
          .withFixedDelay(delayMillis))
        .willSetStateTo(WireMockStub.nextState(sequence))

    )
  }
}
