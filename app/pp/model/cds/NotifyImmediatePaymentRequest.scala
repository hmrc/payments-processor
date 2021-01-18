/*
 * Copyright 2021 HM Revenue & Customs
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

package pp.model.cds

import play.api.libs.json._
final case class NotificationCds(notifyImmediatePaymentRequest: NotifyImmediatePaymentRequest)

object NotificationCds {
  implicit val format: OFormat[NotificationCds] = Json.format[NotificationCds]
}

final case class NotifyImmediatePaymentRequest(
    requestCommon: RequestCommon,
    requestDetail: RequestDetail
)

final case class RequestCommon(receiptDate: String, acknowledgementReference: String, regime: String = "CDS", originatingSystem: String = "OPS")

object RequestCommon {
  implicit val format: OFormat[RequestCommon] = Json.format[RequestCommon]
}

final case class RequestDetail(paymentReference: String, amountPaid: String, unitType: String = "GBP", declarationID: String)

object RequestDetail {
  implicit val format: OFormat[RequestDetail] = Json.format[RequestDetail]
}

object NotifyImmediatePaymentRequest {
  implicit val format: OFormat[NotifyImmediatePaymentRequest] = Json.format[NotifyImmediatePaymentRequest]
}
