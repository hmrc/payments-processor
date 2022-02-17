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

import play.api.libs.json.JsValue

import javax.inject.{Inject, Singleton}
import pp.connectors.ResponseReadsThrowingException
import pp.model.cds.{NotificationCds, NotifyImmediatePaymentRequest}
import pp.model.{ProcessingStatusOps, TaxType}
import pp.model.chargeref.ChargeRefNotificationRequest
import pp.model.mods.ModsPaymentCallBackRequest
import pp.model.pcipal.ChargeRefNotificationPcipalRequest
import pp.model.pngrs.PngrStatusUpdateRequest
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestConnector @Inject() (httpClient: HttpClient)(implicit executionContext: ExecutionContext) {

  val port = 19001
  val headers: Seq[(String, String)] = Seq(("Content-Type", "application/json"))


  implicit val readRaw: HttpReads[HttpResponse] = ResponseReadsThrowingException.readResponse

  def sendCardPaymentsNotification(cardPaymentsNotificationRequest: ChargeRefNotificationRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.POST[ChargeRefNotificationRequest, HttpResponse](s"http://localhost:$port/payments-processor/send-card-payments-notification", cardPaymentsNotificationRequest, headers)

  def sendCardPayments(chargeRefNotificationPciPalRequest: ChargeRefNotificationPcipalRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.POST[ChargeRefNotificationPcipalRequest,HttpResponse](s"http://localhost:$port/send-card-payments", chargeRefNotificationPciPalRequest, headers)

  def sendCardPaymentsWrongFormatRequest(wrongFormatRequest: JsValue)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.POST[JsValue,HttpResponse](s"http://localhost:$port/send-card-payments", wrongFormatRequest, headers)

  def getApiDoc(implicit hc: HeaderCarrier): Future[HttpResponse] = httpClient.GET[HttpResponse](s"http://localhost:$port/api/conf/1.0/application.raml")

  def getDef(implicit hc: HeaderCarrier): Future[HttpResponse] = httpClient.GET[HttpResponse](s"http://localhost:$port/api/definition")

  def count(taxType: TaxType, processingStatusOps: ProcessingStatusOps)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.GET[HttpResponse](s"http://localhost:$port/payments-processor/reporting/count/$taxType/$processingStatusOps")

  def getAll(taxType: TaxType)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.GET[HttpResponse](s"http://localhost:$port/payments-processor/reporting/$taxType")

  def sendStatusUpdateToPngr(pngrStatusUpdateRequest: PngrStatusUpdateRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.POST[PngrStatusUpdateRequest, HttpResponse](s"http://localhost:$port/payments-processor/pngr/send-update", pngrStatusUpdateRequest)

  def sendStatusUpdateToCds(notificationCds: NotificationCds)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.POST[NotificationCds, HttpResponse](s"http://localhost:$port/payments-processor/cds/send-notification", notificationCds)

  def mibPaymentCallBack(modsPaymentCallBackRequest: ModsPaymentCallBackRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.POST[ModsPaymentCallBackRequest, HttpResponse](s"http://localhost:$port/payments-processor/mib/payment-callback", modsPaymentCallBackRequest)
}
