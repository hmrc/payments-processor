package pp.controllers.pngrs

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{postRequestedFor, urlEqualTo, verify}
import org.scalatest.Assertion
import play.api.libs.json.Json
import pp.model.TaxTypes.pngr
import pp.model.pngrs.PngrStatusTypes
import pp.scheduling.pngrs.PngrMongoRepo
import support.PaymentsProcessData.{pngrPaymentItemId, pngrStatusUpdateRequest}
import support.{ItSpec, Pngr, TpsPaymentsBackend}
import uk.gov.hmrc.http.{HttpResponse, UpstreamErrorResponse}

trait PngrUpdateControllerSpec extends ItSpec {
  private lazy val repo = injector.instanceOf[PngrMongoRepo]

  override def beforeEach(): Unit = {
    repo.removeAll().futureValue
    WireMock.reset()
    super.beforeEach()
  }

  protected def numberOfQueuedNotifications: Integer = repo.count(Json.obj()).futureValue


  def aSynchronousEndpointWhenThePngrStatusUpdateSucceeds(): Unit = {
    def verifySuccess(response: HttpResponse
                     ): Assertion = {
      response.status shouldBe 200
      verify(1, postRequestedFor(urlEqualTo(Pngr.endpoint)))
      numberOfQueuedNotifications shouldBe 0
    }



    s"return Ok for a POST to the internal endpoint /pngr/send-update" when {
      "the Pngr call succeeds with OK, status=Successful" in {
        Pngr.statusUpdateSucceeds()
        TpsPaymentsBackend.getTaxTypeOk(pngrPaymentItemId, pngr)
        verifySuccess(testConnector.sendStatusUpdateToPngr(pngrStatusUpdateRequest).futureValue)
      }

      "the Pngr call succeeds with OK, status=Failed" in {
        Pngr.statusUpdateSucceeds()
        TpsPaymentsBackend.getTaxTypeOk(pngrPaymentItemId, pngr)
        verifySuccess(testConnector.sendStatusUpdateToPngr(pngrStatusUpdateRequest.copy(status = PngrStatusTypes.Failed)).futureValue)
      }
    }
  }

  def aSynchronousEndpointWhenThePngrStatusUpdateReturns4xx(): Unit = {
    "fail without persisting to the queue for a POST to the internal endpoint /pngr/send-update" when {
      "the Pngr call returns 400" in {
        Pngr.statusUpdateRespondsWith(400, "")

        val response = testConnector.sendStatusUpdateToPngr(pngrStatusUpdateRequest).failed.futureValue
        response.isInstanceOf[UpstreamErrorResponse] shouldBe true

        numberOfQueuedNotifications shouldBe 0
      }

      "the Pngr returns 404" in {
        Pngr.statusUpdateRespondsWith(404, "")

        val response = testConnector.sendStatusUpdateToPngr(pngrStatusUpdateRequest).failed.futureValue
        response.asInstanceOf[UpstreamErrorResponse].reportAs shouldBe 502

        numberOfQueuedNotifications shouldBe 0
      }
    }
  }


}
