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

package pp.model

import play.api.libs.json.JsString
import play.api.libs.json.Json.toJson
import pp.model.Origins.{OPS, PCI_PAL}
import support.{RichMatchers, UnitSpec}

class OriginSpec extends UnitSpec with RichMatchers {
  "Origins should de/serialize" in {
    val origins = List(
      "OPS" -> OPS,
      "PCI_PAL" -> PCI_PAL
    )

    Origins.values.toSet shouldBe origins.map(o => o._2).toSet

    origins.foreach { origin =>
      val jsValue = toJson(origin._2: Origin)
      jsValue shouldBe JsString(origin._1) withClue s"serialize $origin"
      jsValue.as[Origin] shouldBe origin._2 withClue s"deserialize $origin"
    }
  }
}

