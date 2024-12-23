/*
 * Copyright 2020 HM Revenue & Customs
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

package support

import play.api.libs.json.{Json, JsValue}
import pp.connectors.ResponseReadsThrowingException
import pp.model.chargeref.ChargeRefNotificationRequest
import pp.model.mods.ModsPaymentCallBackRequest
import pp.model.pcipal.ChargeRefNotificationPcipalRequest
import pp.model.pngrs.PngrStatusUpdateRequest
import pp.model.{ProcessingStatusOps, TaxType}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps, HttpReads, HttpResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestConnector @Inject() (httpClient: HttpClientV2)(implicit executionContext: ExecutionContext) {

  val port: String = "19001"
  val headers: Seq[(String, String)] = Seq(("Content-Type", "application/json"))

  implicit val readRaw: HttpReads[HttpResponse] = ResponseReadsThrowingException.readResponse

  def sendCardPaymentsNotification(cardPaymentsNotificationRequest: ChargeRefNotificationRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient
      .post(url"http://localhost:$port/payments-processor/send-card-payments-notification")
      .withBody(Json.toJson(cardPaymentsNotificationRequest))
      .setHeader(headers: _*)
      .execute[HttpResponse]

  def sendCardPaymentsPcipalNotification(chargeRefNotificationPciPalRequest: ChargeRefNotificationPcipalRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient
      .post(url"http://localhost:$port/send-card-payments")
      .withBody(Json.toJson(chargeRefNotificationPciPalRequest))
      .setHeader(headers: _*)
      .execute[HttpResponse]

  def sendCardPaymentsWrongFormatRequest(wrongFormatRequest: JsValue)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient
      .post(url"http://localhost:$port/send-card-payments")
      .withBody(wrongFormatRequest)
      .setHeader(headers: _*)
      .execute[HttpResponse]

  def getApiDoc(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient
      .get(url"http://localhost:$port/api/conf/1.0/application.yaml")
      .execute[HttpResponse]

  def getDef(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient
      .get(url"http://localhost:$port/api/definition")
      .execute[HttpResponse]

  def count(taxType: TaxType, processingStatusOps: ProcessingStatusOps)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient
      .get(url"http://localhost:$port/payments-processor/reporting/count/${taxType.toString}/${processingStatusOps.toString}")
      .execute[HttpResponse]

  def getAll(taxType: TaxType)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient
      .get(url"http://localhost:$port/payments-processor/reporting/${taxType.toString}")
      .execute[HttpResponse]

  def sendStatusUpdateToPngr(pngrStatusUpdateRequest: PngrStatusUpdateRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient
      .post(url"http://localhost:$port/payments-processor/pngr/send-update")
      .withBody(Json.toJson(pngrStatusUpdateRequest))
      .execute[HttpResponse]

  def mibPaymentCallBack(modsPaymentCallBackRequest: ModsPaymentCallBackRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient
      .post(url"http://localhost:$port/payments-processor/mib/payment-callback")
      .withBody(Json.toJson(modsPaymentCallBackRequest))
      .execute[HttpResponse]
}
