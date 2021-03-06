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

//Note if you run a clean it may removed the following import which is needed !
// import uk.gov.hmrc.mongo.json.ReactiveMongoFormats.objectIdFormats

package pp.model.wokitems

import java.time.LocalDateTime

import play.api.libs.json._
import pp.model.{Origin, TaxType, WorkItemFields}

case class ChargeRefNotificationWorkItem(
    createdOn:       LocalDateTime,
    availableUntil:  LocalDateTime,
    taxType:         TaxType,
    chargeRefNumber: String,
    amountPaid:      BigDecimal,
    origin:          Origin) extends WorkItemFields {

  override def toString: String =
    s"ChargeRefNotificationWorkItem for chargeReference $chargeRefNumber ... {createdOn: $createdOn, availableUntil: $availableUntil, taxType: $taxType, origin: $origin, chargeRefNumber: $chargeRefNumber}"

}

object ChargeRefNotificationWorkItem {
  implicit val format: OFormat[ChargeRefNotificationWorkItem] = Json.format[ChargeRefNotificationWorkItem]
}

