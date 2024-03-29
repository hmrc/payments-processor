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

package pp.model.pcipal

import play.api.libs.json.{Json, OFormat}
import pp.model.Origins.PCI_PAL
import pp.model.StatusTypes.validated
import pp.model.{HeadOfDutyIndicator, PaymentItemId, StatusType, TaxType, chargeref}
import pp.model.chargeref.ChargeRefNotificationRequest
import pp.model.pngrs.{PngrStatusTypes, PngrStatusUpdateRequest}

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
    ChargeReference:      String              = "",
    ReferenceNumber:      String,
    CardLast4:            String
)

object ChargeRefNotificationPcipalRequest {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[ChargeRefNotificationPcipalRequest] = Json.format[ChargeRefNotificationPcipalRequest]

  def toChargeRefNotificationRequest(chargeRefNotificationPciPalRequest: ChargeRefNotificationPcipalRequest, taxType: TaxType): ChargeRefNotificationRequest = {
    chargeref.ChargeRefNotificationRequest(
      taxType         = taxType,
      chargeRefNumber = chargeRefNotificationPciPalRequest.ChargeReference,
      amountPaid      = chargeRefNotificationPciPalRequest.Amount,
      origin          = PCI_PAL
    )
  }

  def toPngrStatusUpdateRequest(chargeRefNotificationPciPalRequest: ChargeRefNotificationPcipalRequest): PngrStatusUpdateRequest = {
    PngrStatusUpdateRequest(chargeRefNotificationPciPalRequest.ChargeReference,
      if (chargeRefNotificationPciPalRequest.Status == validated) PngrStatusTypes.Successful else PngrStatusTypes.Failed)
  }

}
