package pp.controllers.chargeref

import com.github.tomakehurst.wiremock.client.WireMock.{patchRequestedFor, postRequestedFor, urlEqualTo, verify}
import pp.model.TaxTypes.{mib, p800, pngr}
import support.PaymentsProcessData._
import support.{Des, Pngr, TpsPaymentsBackend}
import uk.gov.hmrc.http.HttpResponse

class ChargeRefControllerTaxTypeCheckSpec extends ChargeRefControllerSpec {


  override def configMap: Map[String, Any] =
    super
      .configMap
      .updated("chargeref.queue.enabled", "true")
      .updated("chargeref.poller.enabled", "true")
      .updated("pngr.queue.enabled", "true")
      .updated("pngr.poller.enabled", "true")
      .updated("sendAllToDes", "false")


  private def verifySuccess(response: HttpResponse,
                            checkTpsBackend: Boolean = false,
                            backendCount: Int = 1,
                            checkPngr: Boolean = false
                           ): Unit = {
    response.status shouldBe 200
    verify(0, postRequestedFor(urlEqualTo(Des.endpoint)))
    if (checkPngr)verify(1, postRequestedFor(urlEqualTo(Pngr.endpoint)))
    if (checkTpsBackend) verify(backendCount, patchRequestedFor(urlEqualTo(TpsPaymentsBackend.updateEndpoint)))
  }

  Seq(
    (p800PaymentItemId, p800, p800PcipalNotification, p800ChargeRefNotificationRequest),
    (mibPaymentItemId, mib, mibPcipalNotification, mibChargeRefNotificationRequest),
     ).foreach { fixture =>
    val paymentItemId = fixture._1
    val taxType = fixture._2
    val pcipalNotification = fixture._3
    val chargeRefNotificationRequest = fixture._4


    "return Ok for a POST to the public api /send-card-payments with no call to des" when {
      s"status=complete, taxType = $taxType" in {
        TpsPaymentsBackend.getTaxTypeOk(paymentItemId, taxType)
        TpsPaymentsBackend.tpsUpdateOk
        verifySuccess(testConnector.sendCardPayments(pcipalNotification).futureValue, checkTpsBackend = true)
      }
    }


    "return Ok for a POST to the synchronous api with no call to des" when {
      s"status=complete, taxType = $taxType" in {
        TpsPaymentsBackend.tpsUpdateOk
        verifySuccess(
          response = testConnector.sendCardPaymentsNotification(chargeRefNotificationRequest).futureValue,
          backendCount = 0)
      }
    }
  }
  "return Ok for a POST to the public api /send-card-payments with no call to des" when {
    s"status=complete, taxType = pngr" in {
      TpsPaymentsBackend.getTaxTypeOk(pngrPaymentItemId, pngr)
      TpsPaymentsBackend.tpsUpdateOk
      Pngr.statusUpdateSucceeds()
      verifySuccess(testConnector.sendCardPayments(pngrPcipalNotification).futureValue, checkTpsBackend = true, checkPngr= true)
    }
  }

}
