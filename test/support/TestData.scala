/*
 * Copyright 2019 HM Revenue & Customs
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

import play.api.libs.json.Json

object TestData {

  //language=JSON
  val definition = Json.parse(s"""{
                                  "scopes":[],
                                  "api": {
                                    "name": "Charge Ref Notification",
                                    "description": "Provides a way to notify DES/ETMP of Charge Refs",
                                    "context": "payments-processor",
                                    "categories": ["VAT"],
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

