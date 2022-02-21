/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.libs.json.Json.parse
import pp.model.Origins.OPS
import pp.model.StatusTypes.validated
import pp.model.TaxTypes.{mib, p800, pngr}
import pp.model.cds.{NotificationCds, NotifyImmediatePaymentRequest, RequestCommon, RequestDetail}
import pp.model.chargeref.{ChargeRefNotificationDesRequest, ChargeRefNotificationRequest}
import pp.model.mods.ModsPaymentCallBackRequest
import pp.model.pcipal.{ChargeRefNotificationPcipalRequest, PcipalSessionId}
import pp.model.pngrs.{PngrStatusTypes, PngrStatusUpdateRequest}
import pp.model.wokitems.ChargeRefNotificationWorkItem
import pp.model.{chargeref, _}

import java.time.Clock
import java.time.Clock.systemUTC
import java.time.LocalDateTime.now

object PaymentsProcessData {

  private val clock: Clock = systemUTC()

  val reference = "JE231111B"
  val chargeReferenceNumber = "XQ002610015768"
  val mibReference = "reference"
  val cdsReference = "CDSI191234567890"

  val p800PaymentItemId: PaymentItemId = PaymentItemId("p800-48c978bb-64b6-4a00-a1f1-51e267d84f91")
  val mibPaymentItemId: PaymentItemId = PaymentItemId("mib-48c978bb-64b6-4a00-a1f1-51e267d84f91")
  val pngrPaymentItemId: PaymentItemId = PaymentItemId("pngr-48c978bb-64b6-4a00-a1f1-51e267d84f91")

  val pngrStatusUpdateRequest: PngrStatusUpdateRequest = PngrStatusUpdateRequest("chargeref", PngrStatusTypes.Successful)

  val cdsStatusUpdateRequest: NotificationCds = NotificationCds(
    NotifyImmediatePaymentRequest(
      requestCommon = RequestCommon(
        receiptDate              = "somedate", acknowledgementReference = "1234-5678-9012", regime = "CDS", originatingSystem = "OPS"
      ),
      requestDetail = RequestDetail(
        paymentReference = "CDSI191234567890", amountPaid = "1000", declarationID = "1234567890", unitType = "GBP"
      )
    )
  )

  val modsPaymentCallBackRequestWithAmendmentRef: ModsPaymentCallBackRequest = ModsPaymentCallBackRequest(
    chargeReference    = mibReference,
    amendmentReference = Some(1)
  )

  val modsPaymentCallBackRequestWithoutAmendmentRef: ModsPaymentCallBackRequest = ModsPaymentCallBackRequest(
    chargeReference    = mibReference,
    amendmentReference = None
  )

  private val pciPalSessionId = PcipalSessionId("48c978bb")

  val chargeRefNotificationWorkItem: ChargeRefNotificationWorkItem = ChargeRefNotificationWorkItem(now(clock), now(clock).minusSeconds(100), p800, chargeReferenceNumber, 100.12, OPS)

  val chargeRefNotificationDesRequest: ChargeRefNotificationDesRequest = chargeref.ChargeRefNotificationDesRequest(p800, chargeReferenceNumber, 100.11)

  val p800ChargeRefNotificationRequest: ChargeRefNotificationRequest = chargeref.ChargeRefNotificationRequest(p800, chargeReferenceNumber, 100.11, OPS)

  val mibChargeRefNotificationRequest: ChargeRefNotificationRequest = chargeref.ChargeRefNotificationRequest(mib, chargeReferenceNumber, 100.11, OPS)

  val pngrChargeRefNotificationRequest: ChargeRefNotificationRequest = chargeref.ChargeRefNotificationRequest(pngr, chargeReferenceNumber, 100.11, OPS)

  //language=JSON
  val chargeRefNotificationDesRequestJson: JsValue = parse(
    s"""{
       "taxType" : "p800",
       "chargeRefNumber" : "XQ002610015768",
       "amountPaid" : 100.11
       }
     """.stripMargin

  )

  //language=JSON
  val chargeRefNotificationRequestJson: JsValue = parse(
    s"""{
       "taxType" : "p800",
       "chargeRefNumber" : "XQ002610015768",
       "amountPaid" : 100.11,
       "origin" : "OPS"
       }
     """.stripMargin

  )

  //language=JSON
  val wrongFormatChargeRefNotificationRequestJson: JsValue = parse(
    s"""{
       "taxType" : "p800",
       "chargeRefNumber" : "XQ002610015768",
       "amountPaid" : 100.11,
       "origin" : "OPS",
       "extraField" : "Extar Value"
       }
     """.stripMargin

  )

  //language=JSON
  val pngrStatusUpdateRequestJson: JsValue = parse(
    s"""{
       "reference" : "chargeref",
       "status" : "Successful"
       }
     """.stripMargin
  )

  //language=JSON
  val cdsStatusUpdateRequestJson: JsValue = parse(
    s"""
       {
    "notifyImmediatePaymentRequest": {
        "requestCommon": {
            "receiptDate": "somedate",
            "acknowledgementReference": "1234-5678-9012",
            "regime": "CDS",
            "originatingSystem": "OPS"
        },
        "requestDetail": {
            "paymentReference": "CDSI191234567890",
            "amountPaid": "1000",
            "declarationID": "1234567890",
            "unitType": "GBP"
        }
    }
}
       """.stripMargin
  )

  //language=JSON
  val modsPaymentCallbackRequestWithAmendmentRefJson: JsValue = parse(
    s"""
        {
          "chargeReference": "reference",
          "amendmentReference": 1
        }""".stripMargin)

  //language=JSON
  val modsPaymentCallbackRequestWithoutAmendmentRefJson: JsValue = parse(
    s"""
        {
          "chargeReference": "reference"
        }""".stripMargin)

  //language=JSON
  val chargeRefNotificationPciPalRequestJson: JsValue = parse(
    s"""{
            "HoD": "B",
            "TaxReference": "${reference}",
            "Amount": 100.11,
            "Commission": 1.23,
            "CardType": "VISA",
            "Status": "${validated.toString}",
            "PCIPalSessionId": "${pciPalSessionId.value}",
            "TransactionReference": "${chargeReferenceNumber}",
            "PaymentItemId": "${p800PaymentItemId.value}",
            "ChargeReference": "chargeRef",
            "ReferenceNumber": "3000000000",
            "CardLast4": "1234"
      }""".stripMargin)

  val p800PcipalNotification: ChargeRefNotificationPcipalRequest = chargeRefNotificationPciPalRequest(p800PaymentItemId)

  val mibPcipalNotification: ChargeRefNotificationPcipalRequest = chargeRefNotificationPciPalRequest(mibPaymentItemId)

  val pngrPcipalNotification: ChargeRefNotificationPcipalRequest = chargeRefNotificationPciPalRequest(pngrPaymentItemId)

  private def chargeRefNotificationPciPalRequest(paymentItemId: PaymentItemId) = ChargeRefNotificationPcipalRequest(
    HeadOfDutyIndicators.B,
    reference,
    100.11,
    1.23,
    "VISA",
    validated,
    pciPalSessionId,
    chargeReferenceNumber,
    paymentItemId,
    "chargeRef",
    "3000000000',
    "1234"
  )

  //language=JSON
  def definition(endpointsEnabled: Boolean, status: String): JsValue = parse(
    s"""{
                                  "scopes":[],
                                  "api": {
                                    "name": "Charge Ref Notification",
                                    "description": "Provides a way to notify DES/ETMP of Charge Refs",
                                    "context": "payments/notifications",
                                    "categories": ["OTHER"],
                                    "versions": [
                                      {
                                        "version": "1.0",
                                        "status": "${status}",
                                        "endpointsEnabled": ${endpointsEnabled},
                                        "access": {
                                          "type": "PRIVATE",
                                          "whitelistedApplicationIds": ["testId"],
                                          "isTrial":false
                                        }
                                      }
                                    ]
                                  }
                                }""".stripMargin)

}
