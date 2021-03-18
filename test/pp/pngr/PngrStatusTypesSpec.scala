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

package pp.pngr

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.libs.json.{JsString, Json}
import pp.model.pngrs.{PngrStatusType, PngrStatusTypes}
import support.{RichMatchers, UnitSpec}

class PngrStatusTypesSpec extends UnitSpec with RichMatchers {

  "de/serialize PngrStatusTypes" in {

    val pngrStatusTypes = List(
      "Failed" -> PngrStatusTypes.Failed,
      "Successful" -> PngrStatusTypes.Successful
    )

    PngrStatusTypes.values.toSet shouldBe pngrStatusTypes.map(tt => tt._2).toSet

    pngrStatusTypes.foreach { tt =>
      val jsValue = Json.toJson(tt._2)
      jsValue shouldBe JsString(tt._1) withClue s"serialize $tt"
      jsValue.as[PngrStatusType] shouldBe tt._2 withClue s"deserialize $tt"
    }
  }
}
