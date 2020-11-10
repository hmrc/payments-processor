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

package pp.model.pcipal

import play.api.libs.json.{Json, OFormat}
import pp.model.Origins.PCI_PAL
import pp.model.{chargeref, _}
import pp.model.chargeref.ChargeRefNotificationRequest

final case class ChargeRefNotificationPcipalRequest(
    HoD:                  HeadOfDutyIndicator,
    TaxReference:         String,
    Amount:               BigDecimal,
    Commission:           BigDecimal,
    CardType:             String,
    Status:               StatusType,
    PCIPalSessionId:      PcipalSessionId,
    TransactionReference: String,
    paymentItemId:        PaymentItemId,
    ChargeReference:      String              = ""
)

object ChargeRefNotificationPcipalRequest {
  implicit val format: OFormat[ChargeRefNotificationPcipalRequest] = Json.format[ChargeRefNotificationPcipalRequest]

  def toChargeRefNotificationRequest(chargeRefNotificationPciPalRequest: ChargeRefNotificationPcipalRequest, taxType: TaxType): ChargeRefNotificationRequest = {
    chargeref.ChargeRefNotificationRequest(
      taxType, chargeRefNotificationPciPalRequest.ChargeReference, chargeRefNotificationPciPalRequest.Amount, PCI_PAL)
  }
}
