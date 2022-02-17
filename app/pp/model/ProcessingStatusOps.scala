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

import enumeratum._
import play.api.libs.json.Format
import play.api.mvc.{PathBindable, QueryStringBindable}
import pp.controllers.ValueClassBinder._
import pp.jsonext.EnumFormat

import scala.collection.immutable

object ProcessingStatusOps {
  implicit val format: Format[ProcessingStatusOps] = EnumFormat(ProcessingStatusOpsValues)
  implicit val pathBinder: QueryStringBindable[ProcessingStatusOps] = bindableA(_.toString)
  implicit val statusBinder: PathBindable[ProcessingStatusOps] = valueClassBinder(_.toString)

}

sealed abstract class ProcessingStatusOps extends EnumEntry {
  val processingStatus: uk.gov.hmrc.workitem.ProcessingStatus
}

object ProcessingStatusOpsValues extends Enum[ProcessingStatusOps] {

  def forCode(code: String): Option[ProcessingStatusOps] = values.find(_.toString == code)

  def values: immutable.IndexedSeq[ProcessingStatusOps] = findValues

  case object PermanentlyFailed extends ProcessingStatusOps {
    val processingStatus = uk.gov.hmrc.workitem.PermanentlyFailed
  }

  case object ToDo extends ProcessingStatusOps {
    val processingStatus = uk.gov.hmrc.workitem.ToDo
  }

  case object Failed extends ProcessingStatusOps {
    val processingStatus = uk.gov.hmrc.workitem.Failed
  }
}
