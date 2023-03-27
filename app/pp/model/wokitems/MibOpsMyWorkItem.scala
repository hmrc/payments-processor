/*
 * Copyright 2023 HM Revenue & Customs
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

package pp.model.wokitems

import java.time.LocalDateTime
import play.api.libs.json._
import pp.model.mods.ModsPaymentCallBackRequest
import pp.model.{Origin, TaxType, MyWorkItemFields}

case class MibOpsMyWorkItem(
    createdOn:                  LocalDateTime,
    availableUntil:             LocalDateTime,
    taxType:                    TaxType,
    origin:                     Origin,
    reference:                  String,
    modsPaymentCallBackRequest: ModsPaymentCallBackRequest) extends MyWorkItemFields {

  override def toString: String =
    s"MibWorkItem for chargeReference ${modsPaymentCallBackRequest.chargeReference} ... " +
      s"{createdOn: $createdOn, availableUntil: $availableUntil, taxType: $taxType, origin: $origin, " +
      s"reference: $reference, amendmentReference: ${modsPaymentCallBackRequest.amendmentReference}}"
}

object MibOpsMyWorkItem {
  implicit val format: OFormat[MibOpsMyWorkItem] = Json.format[MibOpsMyWorkItem]
}
