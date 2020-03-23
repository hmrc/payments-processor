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

import javax.inject.{Inject, Singleton}
import pp.model.ChargeRefNotificationRequest
import pp.model.pcipal.ChargeRefNotificationPcipalRequest
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestConnector @Inject() (httpClient: HttpClient)(implicit executionContext: ExecutionContext) {

  val port = 19001
  val headers: Seq[(String, String)] = Seq(("Content-Type", "application/json"))

  def sendCardPaymentsNotification(cardPaymentsNotificationRequest: ChargeRefNotificationRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.POST(s"http://localhost:$port/payments-processor/send-card-payments-notification", cardPaymentsNotificationRequest, headers)

  def sendCardPayments(chargeRefNotificationPciPalRequest: ChargeRefNotificationPcipalRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.POST(s"http://localhost:$port/send-card-payments", chargeRefNotificationPciPalRequest, headers)

  def getApiDoc(implicit hc: HeaderCarrier): Future[HttpResponse] = httpClient.GET(s"http://localhost:$port/api/conf/1.0/application.raml")

  def getDef(implicit hc: HeaderCarrier): Future[HttpResponse] = httpClient.GET(s"http://localhost:$port/api/definition")
}
