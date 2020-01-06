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

import pp.model.{ChargeRefNotificationDesRequest, ChargeRefNotificationPciPalRequest, ChargeRefNotificationWorkItem, TaxTypes}
import play.api.libs.json.{JsValue, Json}

object PaymentsProcessData {

  private val clock: Clock = Clock.systemUTC()

  val chargeReferenceNumber = "XQ002610015768"

  val chargeRefNotificationWorkItem = ChargeRefNotificationWorkItem(LocalDateTime.now(clock), TaxTypes.CDS, chargeReferenceNumber, 100.12)

  val chargeRefNotificationDesRequest = ChargeRefNotificationDesRequest(TaxTypes.CDS, chargeReferenceNumber, 100.11)

  val chargeRefNotificationPciPalRequest = ChargeRefNotificationPciPalRequest(TaxTypes.CDS, chargeReferenceNumber, 100.11)

  val chargeRefNotificationDesRequestJson: JsValue = Json.parse(
    s"""{
       "taxType" : "CDS",
       "chargeRefNumber" : "XQ002610015768",
       "amountPaid" : 100.11
       }
     """.stripMargin

  )

  val chargeRefNotificationPciPalRequestJson: JsValue = Json.parse(
    s"""{
       "taxType" : "CDS",
       "chargeRefNumber" : "XQ002610015768",
       "amountPaid" : 100.11
       }
     """.stripMargin

  )
  //language=JSON
  val definition = Json.parse(s"""{
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
                                        "endpointsEnabled": true,
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
