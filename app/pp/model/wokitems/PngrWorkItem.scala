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

package pp.model.wokitems

import java.time.LocalDateTime

import play.api.libs.json._
import pp.model.pngrs.PngrStatusType
import pp.model.{Origin, TaxType, WorkItemFields}

case class PngrWorkItem(
    createdOn:      LocalDateTime,
    availableUntil: LocalDateTime,
    taxType:        TaxType,
    origin:         Origin,
    reference:      String,
    status:         PngrStatusType) extends WorkItemFields {

  override def toString: String =
    s"PngrWorkItem for chargeReference $reference ... {createdOn: $createdOn, availableUntil: $availableUntil, taxType: $taxType, origin: $origin, reference: $reference, status: $status}"
}

object PngrWorkItem {
  implicit val format: OFormat[PngrWorkItem] = Json.format[PngrWorkItem]
}
