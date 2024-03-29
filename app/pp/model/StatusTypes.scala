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

import cats.Eq
import enumeratum._
import play.api.libs.json.Format
import pp.controllers.ValueClassBinder._
import pp.jsonext.EnumFormat
import play.api.mvc.{PathBindable, QueryStringBindable}

import scala.collection.immutable

object StatusType {
  implicit val format: Format[StatusType] = EnumFormat(StatusTypes)
  implicit val pathBinder: QueryStringBindable[StatusType] = bindableA(_.toString)
  implicit val statusBinder: PathBindable[StatusType] = valueClassBinder(_.toString)
  implicit val eq: Eq[StatusType] = Eq.fromUniversalEquals
}

sealed trait StatusType extends EnumEntry

object StatusTypes extends Enum[StatusType] {

  case object validated extends StatusType

  case object failed extends StatusType

  def values: immutable.IndexedSeq[StatusType] = findValues
}
