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

import support.UnitSpec
import tps.model.TaxTypes

/**
 * we have a requirement from DES to send a tax type as a code of length 1 - 4.
 * This spec is here so we don't accidentally introduce a TaxType to the list of TaxType's that would break this
 * (since we're reusing the entryName from TPS backend)
 */
class TaxTypeSpec extends UnitSpec {
  "TaxType enum values shouldn't be longer than 4 characters" in {
    TaxTypes.values.toList.foreach { tt =>
      tt.entryName.nonEmpty shouldBe true withClue s"tax type string value shouldn't be less than 1 characters: [ ${tt.entryName} ]"
      tt.entryName.length <= 4 shouldBe true withClue s"tax type string value shouldn't be more than 4 characters [ ${tt.entryName} ]"
    }
  }
}
