/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.mvc.QueryStringBindable
import pp.controllers.ValueClassBinder._
import pp.jsonext.EnumFormat

import scala.collection.immutable

sealed abstract class TaxType extends EnumEntry {
  val sendToDes: Boolean
}

object TaxType {
  implicit val format: Format[TaxType] = EnumFormat(TaxTypes)
  implicit val pathBinder: QueryStringBindable[TaxType] = bindableA(_.toString)
}

object TaxTypes extends Enum[TaxType] {

  def forCode(code: String): Option[TaxType] = values.find(_.toString == code)

  //As per DES API ....

  case object NLIJ extends TaxType {
    val sendToDes = false
  }

  case object CDSX extends TaxType {
    val sendToDes = false
  }

  case object PARC extends TaxType {
    val sendToDes = false
  }

  case object P302 extends TaxType {
    val sendToDes = false
  }

  //As per standard OPS TaxTypes ....

  case object `self-assessment` extends TaxType {
    val sendToDes = false
  }

  case object vat extends TaxType {
    val sendToDes = false
  }

  case object epaye extends TaxType {
    val sendToDes = false
  }

  /**
   * Machine Game Duty
   */
  case object `mg-duty` extends TaxType {
    val sendToDes = false
  }

  /**
   * Merchandise in Baggage
   */

  case object mib extends TaxType {
    val sendToDes = false
  }

  /**
   * Other tax types
   */

  case object other extends TaxType {
    val sendToDes = false
  }

  /**
   * Stamp Duty Land Tax (Sdlt)
   */
  case object `stamp-duty` extends TaxType {
    val sendToDes = false
  }

  case object class2NationalInsurance extends TaxType {
    val sendToDes = false
  }

  //  /**
  //   * Customs or Custom Declarations Service
  //   */
  //  case object cds extends TaxType {
  //  }

  /**
   * Passengers
   */
  case object pngr extends TaxType {
    val sendToDes = false
  }

  case object `corporation-tax` extends TaxType {
    val sendToDes = false
  }

  final case object p800 extends TaxType {
    val sendToDes = false
  }

  /**
   * Northern Ireland
   */

  case object ni extends TaxType {
    val sendToDes = false
  }

  case object parcels extends TaxType {
    val sendToDes = false
  }

  /**
   * Insurance Premium Tax
   */

  case object insurancePremium extends TaxType {
    val sendToDes = false
  }

  /**
   * class3NationalInsurance
   */

  case object class3NationalInsurance extends TaxType {
    val sendToDes = false
  }

  case object `bioFuelsAndRoadGas` extends TaxType {
    val sendToDes = false
  }

  case object `airPassengerDuty` extends TaxType {
    val sendToDes = false
  }

  case object `beerDuty` extends TaxType {
    val sendToDes = false
  }

  case object `landfillTax` extends TaxType {
    val sendToDes = false
  }

  case object `aggregatesLevy` extends TaxType {
    val sendToDes = false
  }

  case object `climateChangeLevy` extends TaxType {
    val sendToDes = false
  }

  override def values: immutable.IndexedSeq[TaxType] = findValues
}
