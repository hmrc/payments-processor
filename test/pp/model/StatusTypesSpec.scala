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

package pp.model

import play.api.libs.json.{JsString, Json}
import support.UnitSpec

class StatusTypesSpec extends UnitSpec {

  "de/serialize TaxTypes" in {

    val statusTypes = List(
      "validated" -> StatusTypes.validated,
      "failed" -> StatusTypes.failed
    )

    statusTypes.foreach { tt =>
      val jsValue = Json.toJson(tt._2: StatusType)
      jsValue shouldBe JsString(tt._1) withClue s"serialize $tt"
      jsValue.as[StatusType] shouldBe tt._2 withClue s"deserialize $tt"
    }
  }
}

