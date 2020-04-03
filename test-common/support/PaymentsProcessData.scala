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

import java.time.{Clock, LocalDateTime}

import play.api.libs.json.{JsValue, Json}
import pp.model.Origins.OPS
import pp.model.pcipal.{ChargeRefNotificationPcipalRequest, PcipalSessionId}
import pp.model._

object PaymentsProcessData {

  private val clock: Clock = Clock.systemUTC()

  val chargeReferenceNumber = "XQ002610015768"

  val paymentId = PaymentItemId("session-48c978bb-64b6-4a00-a1f1-51e267d84f91")
  val pciPalSessionId = PcipalSessionId("48c978bb")
  val reference = "JE231111B"
  val pid = "123"
  val transReference = "51e267d84f91"

  val chargeRefNotificationWorkItem = ChargeRefNotificationWorkItem(LocalDateTime.now(clock), TaxTypes.p800, chargeReferenceNumber, 100.12, OPS)

  val chargeRefNotificationDesRequest = ChargeRefNotificationDesRequest(TaxTypes.p800, chargeReferenceNumber, 100.11)

  val chargeRefNotificationRequest = ChargeRefNotificationRequest(TaxTypes.p800, chargeReferenceNumber, 100.11, OPS)

  val chargeRefNotificationDesRequestJson: JsValue = Json.parse(
    s"""{
       "taxType" : "p800",
       "chargeRefNumber" : "XQ002610015768",
       "amountPaid" : 100.11
       }
     """.stripMargin

  )

  val chargeRefNotificationRequestJson: JsValue = Json.parse(
    s"""{
       "taxType" : "p800",
       "chargeRefNumber" : "XQ002610015768",
       "amountPaid" : 100.11,
       "origin" : "OPS"
       }
     """.stripMargin

  )
  //language=JSON
  def definition(endpointsEnabled: Boolean) = Json.parse(s"""{
                                  "scopes":[],
                                  "api": {
                                    "name": "Charge Ref Notification",
                                    "description": "Provides a way to notify DES/ETMP of Charge Refs",
                                    "context": "payments/notifications",
                                    "categories": ["OTHER"],
                                    "versions": [
                                      {
                                        "version": "1.0",
                                        "status": "STABLE",
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

  val chargeRefNotificationPciPalRequest: ChargeRefNotificationPcipalRequest = ChargeRefNotificationPcipalRequest(
    HeadOfDutyIndicators.B,
    reference,
    100.11,
    1.23,
    "VISA",
    StatusTypes.complete,
    pciPalSessionId,
    chargeReferenceNumber,
    paymentId,
    Some("chargeRef")
  )

  //language=JSON
  val chargeRefNotificationPciPalRequestJson = Json.parse(
    s"""{
            "HoD": "B",
            "TaxReference": "${reference}",
            "Amount": 100.11,
            "Commission": 1.23,
            "CardType": "VISA",
            "Status": "${StatusTypes.complete.toString}",
            "PCIPalSessionId": "${pciPalSessionId.value}",
            "TransactionReference": "${chargeReferenceNumber}",
            "paymentItemId": "${paymentId.value}",
            "ChargeReference": "chargeRef"
      }""".stripMargin)

}
