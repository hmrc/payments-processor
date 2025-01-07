/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pp.controllers.chargeref

import com.github.tomakehurst.wiremock.client.WireMock.{patchRequestedFor, postRequestedFor, urlEqualTo, verify}
import play.api.Logger
import play.api.libs.json.{JsObject, Json}
import pp.model.{PaymentItemId, TaxType, TaxTypes}
import pp.model.TaxTypes.{mib, p800, pngr}
import pp.model.chargeref.ChargeRefNotificationRequest
import pp.model.pcipal.ChargeRefNotificationPcipalRequest
import support.PaymentsProcessData._
import support._
import uk.gov.hmrc.http.HttpResponse

class ChargeRefControllerTaxTypeCheckSpec extends ChargeRefControllerSpec {

  override def configMap: Map[String, Any] =
    super
      .configMap
      .updated("chargeref.queue.enabled", "true")
      .updated("chargeref.poller.enabled", "true")
      .updated("pngr.queue.enabled", "true")
      .updated("pngr.poller.enabled", "true")
      .updated("mibops.queue.enabled", "true")
      .updated("mibops.poller.enabled", "true")
      .updated("sendAllToDes", "false")

  private def verifySuccess(
      response:        HttpResponse,
      checkTpsBackend: Boolean      = false,
      backendCount:    Int          = 1,
      checkPngr:       Boolean      = false,
      checkMib:        Boolean      = false): Unit = {
    response.status shouldBe 200
    verify(0, postRequestedFor(urlEqualTo(Des.endpoint)))
    if (checkPngr) verify(1, postRequestedFor(urlEqualTo(Pngr.endpoint)))
    if (checkMib) verify(postRequestedFor(urlEqualTo(Mib.endpoint)))
    if (checkTpsBackend) verify(backendCount, patchRequestedFor(urlEqualTo(TpsPaymentsBackend.updateEndpoint)))
  }
  Seq[(PaymentItemId, TaxType, ChargeRefNotificationPcipalRequest, ChargeRefNotificationRequest)](
    (p800PaymentItemId, p800, p800PcipalNotification, p800ChargeRefNotificationRequest),
    (mibPaymentItemId, mib, mibPcipalNotification, mibChargeRefNotificationRequest),
    (pngrPaymentItemId, pngr, pngrPcipalNotification, pngrChargeRefNotificationRequest)).foreach { fixture =>
      val paymentItemId = fixture._1
      val taxType = fixture._2
      val pcipalNotification = fixture._3
      val chargeRefNotificationRequest = fixture._4

      "return Ok for a POST to the public api /send-card-payments with no call to des" when {
        s"status=complete, taxType = ${taxType.toString}" in {
          TpsPaymentsBackend.getTaxTypeOk(paymentItemId, taxType)
          TpsPaymentsBackend.tpsUpdateOk
          AuditConnectorStub.stubAudit
          if (taxType === TaxTypes.mib) TpsPaymentsBackend.getAmendmentRefOk(paymentItemId, modsPaymentCallBackRequestWithAmendmentRef)
          taxType match {
            case TaxTypes.pngr => Pngr.statusUpdateSucceeds()
            case TaxTypes.mib  => Mib.statusUpdateSucceeds()
            case _             => Logger(this.getClass).debug("Not needed")
          }
          taxType match {
            case TaxTypes.mib  => verifySuccess(testConnector.sendCardPaymentsPcipalNotification(pcipalNotification).futureValue, checkTpsBackend = true, checkMib = true)
            case TaxTypes.pngr => verifySuccess(testConnector.sendCardPaymentsPcipalNotification(pcipalNotification).futureValue, checkTpsBackend = true, checkPngr = true)
            case _             => verifySuccess(testConnector.sendCardPaymentsPcipalNotification(pcipalNotification).futureValue, checkTpsBackend = true)
          }
          AuditConnectorStub.verifyEventAudited(
            auditType  = "PciPalNotificationSuccess",
            auditEvent = Json.obj("chargeRefNotificationPcipalRequest" -> Json.toJson(pcipalNotification)).as[JsObject])
        }
      }

      "return Ok for a POST to the synchronous api with no call to des" when {
        s"status=complete, taxType = ${taxType.toString}" in {
          TpsPaymentsBackend.tpsUpdateOk
          TpsPaymentsBackend.getTaxTypeOk(paymentItemId, taxType)
          verifySuccess(
            response     = testConnector.sendCardPaymentsNotification(chargeRefNotificationRequest).futureValue,
            backendCount = 0)
        }
      }
    }

}
