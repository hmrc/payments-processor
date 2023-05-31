package pp.controllers.cdsops

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{postRequestedFor, urlEqualTo, verify}
import org.scalatest.Assertion
import play.api.libs.json.Json
import pp.scheduling.cds.CdsOpsMongoRepo
import support.PaymentsProcessData.cdsStatusUpdateRequest
import support.{Cds, ItSpec}
import uk.gov.hmrc.http.{HttpResponse, UpstreamErrorResponse}

trait CdsControllerSpec extends ItSpec {
  private lazy val repo = injector.instanceOf[CdsOpsMongoRepo]

  override def beforeEach(): Unit = {
    repo.removeAll().futureValue
    WireMock.reset()
    super.beforeEach()
  }

  protected def numberOfQueuedNotifications: Long = repo.countAll().futureValue

  def aSynchronousEndpointWhenTheCdsPaymentUpdateSucceeds(): Unit = {
      def verifySuccess(response: HttpResponse): Assertion = {
        response.status shouldBe 200
        verify(1, postRequestedFor(urlEqualTo(Cds.endpoint)))
        numberOfQueuedNotifications shouldBe 0
      }

    s"return Ok for a POST to the internal endpoint /cds/send-notification" when {
      "the Cds call succeeds with OK, status=Successful" in {
        Cds.statusUpdateSucceeds()
        verifySuccess(testConnector.sendStatusUpdateToCds(cdsStatusUpdateRequest).futureValue)
      }

    }
  }

  def aSynchronousEndpointWhenTheCdsPaymentUpdateReturns4xx(): Unit = {
    "fail without persisting to the queue for a POST to the internal endpoint /cds/send-notification" when {
      "the Cds call returns 400" in {
        Cds.statusUpdateRespondsWith(400, "")

        val response = testConnector.sendStatusUpdateToCds(cdsStatusUpdateRequest).failed.futureValue
        response.isInstanceOf[UpstreamErrorResponse] shouldBe true

        numberOfQueuedNotifications shouldBe 0
      }

      "the Cds returns 404" in {
        Cds.statusUpdateRespondsWith(404, "")

        val response = testConnector.sendStatusUpdateToCds(cdsStatusUpdateRequest).failed.futureValue

        response.asInstanceOf[UpstreamErrorResponse].reportAs shouldBe 502

        numberOfQueuedNotifications shouldBe 0
      }
    }
  }

}
