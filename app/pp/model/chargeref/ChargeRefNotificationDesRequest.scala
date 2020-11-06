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

//Note if you run a clean it may removed the following import which is needed !
// import uk.gov.hmrc.mongo.json.ReactiveMongoFormats.objectIdFormats

package pp.model.chargeref

import play.api.libs.json._
import pp.model.TaxType

case class ChargeRefNotificationDesRequest(
    taxType:         TaxType,
    chargeRefNumber: String,
    amountPaid:      BigDecimal)

object ChargeRefNotificationDesRequest {
  implicit val format: OFormat[ChargeRefNotificationDesRequest] = Json.format[ChargeRefNotificationDesRequest]
}

