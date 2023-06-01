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

package pp.model

import play.api.libs.json.{JsString, Json}
import support.{RichMatchers, UnitSpec}

class ProcessingStatusOpsSpec extends UnitSpec with RichMatchers {

  "de/serialize ProcessingStatusOps" in {

    val processingStatusOps: List[(String, ProcessingStatusOps)] = List[(String, ProcessingStatusOps)](
      "ToDo" -> ProcessingStatusOpsValues.ToDo,
      "PermanentlyFailed" -> ProcessingStatusOpsValues.PermanentlyFailed,
      "Failed" -> ProcessingStatusOpsValues.Failed
    )

    ProcessingStatusOpsValues.values.toSet shouldBe processingStatusOps.map(tt => tt._2).toSet

    processingStatusOps.foreach { tt =>
      val jsValue = Json.toJson(tt._2: ProcessingStatusOps)
      jsValue shouldBe JsString(tt._1) withClue s"serialize ${tt.toString}"
      jsValue.as[ProcessingStatusOps] shouldBe tt._2 withClue s"deserialize ${tt.toString}"
    }
  }

}

