package pp.controllers.mibops

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{getRequestedFor, urlEqualTo, verify}
import org.scalatest.Assertion
import play.api.libs.json.Json
import pp.scheduling.mib.MibOpsMongoRepo
import support.PaymentsProcessData.mibReference
import support.{ItSpec, Mib}
import uk.gov.hmrc.http.{HttpResponse, UpstreamErrorResponse}

trait MibControllerSpec extends ItSpec {

  private lazy val repo = injector.instanceOf[MibOpsMongoRepo]

  override def beforeEach(): Unit = {
    repo.removeAll().futureValue
    WireMock.reset()
    super.beforeEach()
  }

  protected def numberOfQueuedNotifications: Integer = repo.count(Json.obj()).futureValue


  def aSynchronousEndpointWhenTheMibPaymentUpdateSucceeds(): Unit = {
    def verifySuccess(response: HttpResponse
                     ): Assertion = {
      response.status shouldBe 200
      verify(1, getRequestedFor(urlEqualTo(Mib.endpoint(mibReference))))
      numberOfQueuedNotifications shouldBe 0
    }



    s"return Ok for a POST to the internal endpoint /mib/payment-callback/:reference" when {
      "the Mib call succeeds with OK, status=Successful" in {
        Mib.statusUpdateSucceeds(reference = mibReference)
        verifySuccess(testConnector.mibPaymentCallBack(mibReference).futureValue)
      }
    }
  }

  def aSynchronousEndpointWhenTheMibPaymentUpdateReturns4xx(): Unit = {
    "fail without persisting to the queue for a POST to the internal endpoint /mib/payment-callback/:reference" when {
      "the Mib call returns 400" in {
        Mib.statusUpdateRespondsWith(400, "", reference = mibReference)

        val response = testConnector.mibPaymentCallBack(mibReference).failed.futureValue
        response.isInstanceOf[UpstreamErrorResponse] shouldBe true

        numberOfQueuedNotifications shouldBe 0
      }

      "the Mib returns 404" in {
        Mib.statusUpdateRespondsWith(404, "",reference = mibReference)

        val response = testConnector.mibPaymentCallBack(mibReference).failed.futureValue

        response.asInstanceOf[UpstreamErrorResponse].reportAs shouldBe 502

        numberOfQueuedNotifications shouldBe 0
      }
    }
  }


}
