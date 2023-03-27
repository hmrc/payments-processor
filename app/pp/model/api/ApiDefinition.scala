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

package pp.model.api

import play.api.libs.json.{Json, OFormat}

case class ApiDefinition(
    scopes: Seq[String] = Seq(),
    api:    Api
)

case class Api(
    name:        String = "Charge Ref Notification",
    description: String = "Provides a way to notify DES/ETMP of Charge Refs",
    //Field 'context' must start with one of 'agents', 'customs', 'individuals', 'mobile', 'organisations', 'test'
    context:    String,
    categories: Seq[String]  = Seq("OTHER"),
    versions:   Seq[Version]
)

case class Version(
    version:          String  = "1.0",
    status:           String,
    endpointsEnabled: Boolean,
    access:           Access
)

case class Access(
    `type`:                    String      = "PRIVATE",
    whitelistedApplicationIds: Seq[String],
    isTrial:                   Boolean     = false

)

object ApiDefinition {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val oFormats: OFormat[ApiDefinition] = Json.format[ApiDefinition]
}

object Api {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val oFormats: OFormat[Api] = Json.format[Api]
}

object Version {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val oFormats: OFormat[Version] = Json.format[Version]
}

object Access {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val oFormats: OFormat[Access] = Json.format[Access]
}
