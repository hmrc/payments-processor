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
import pp.model.HeadOfDutyIndicators._
import support.{RichMatchers, UnitSpec}

class HeadOfDutyIndicatorsSpec extends UnitSpec with RichMatchers {
  "HeadOfDutyIndicators should de/serialize" in {
    val headOfDutyIndicators = List(
      "B" -> B,
      "A" -> A,
      "N" -> N,
      "M" -> M,
      "J" -> J,
      "P" -> P,
      "V" -> V,
      "X" -> X,
      "K" -> K
    )

    HeadOfDutyIndicators.values.toSet shouldBe headOfDutyIndicators.map(o => o._2).toSet

    headOfDutyIndicators.foreach { headOfDutyIndicator =>
      val jsValue = toJson(headOfDutyIndicator._2: HeadOfDutyIndicator)
      jsValue shouldBe JsString(headOfDutyIndicator._1) withClue s"serialize $headOfDutyIndicator"
      jsValue.as[HeadOfDutyIndicator] shouldBe headOfDutyIndicator._2 withClue s"deserialize $headOfDutyIndicator"
    }
  }
}

