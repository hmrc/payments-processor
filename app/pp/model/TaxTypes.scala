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

import enumeratum._
import play.api.libs.json.Format
import play.api.mvc.{PathBindable, QueryStringBindable}
import pp.controllers.ValueClassBinder._
import pp.jsonext.EnumFormat
import cats.Eq

import java.util.Locale
import scala.collection.immutable

sealed abstract class TaxType extends EnumEntry {
  val sendToDes: Boolean = true
  val tpsValue: String = entryName.toUpperCase(Locale.UK)
}

object TaxType {
  implicit val format: Format[TaxType] = EnumFormat(TaxTypes)
  implicit val pathBinder: QueryStringBindable[TaxType] = bindableA(_.toString)
  implicit val taxTypeBinder: PathBindable[TaxType] = valueClassBinder(_.toString)
  implicit val eq: Eq[TaxType] = Eq.fromUniversalEquals
}

object TaxTypes extends Enum[TaxType] {
  //As per DES API ....
  case object NLIJ extends TaxType

  case object CDSX extends TaxType

  case object PARC extends TaxType

  case object P302 extends TaxType

  //As per standard OPS TaxTypes ....

  case object `self-assessment` extends TaxType

  case object vat extends TaxType {
    override val sendToDes: Boolean = false
  }

  case object epaye extends TaxType

  /**
   * Machine Game Duty
   */
  case object `mg-duty` extends TaxType

  /**
   * Merchandise in Baggage
   */

  case object mib extends TaxType {
    override val sendToDes: Boolean = false
  }

  /**
   * Other tax types
   */

  case object other extends TaxType

  /**
   * Stamp Duty Land Tax (Sdlt)
   */
  case object `stamp-duty` extends TaxType

  case object class2NationalInsurance extends TaxType

  /**
   * Passengers
   */
  case object pngr extends TaxType {
    override val sendToDes: Boolean = false
  }

  case object `corporation-tax` extends TaxType

  final case object p800 extends TaxType {
    override val sendToDes: Boolean = false
  }

  /**
   * Northern Ireland
   */

  case object ni extends TaxType

  case object parcels extends TaxType

  /**
   * Insurance Premium Tax
   */

  case object insurancePremium extends TaxType

  /**
   * class3NationalInsurance
   */

  case object class3NationalInsurance extends TaxType

  case object `bioFuelsAndRoadGas` extends TaxType

  case object `airPassengerDuty` extends TaxType

  case object `beerDuty` extends TaxType

  case object `landfillTax` extends TaxType

  case object `aggregatesLevy` extends TaxType

  case object `climateChangeLevy` extends TaxType

  case object cds extends TaxType

  case object childbenefitsrepayments extends TaxType {
    override val sendToDes: Boolean = false
  }

  case object ppt extends TaxType

  case object sa extends TaxType {
    override val sendToDes: Boolean = false
  }

  case object sdlt extends TaxType

  case object safe extends TaxType {
    override val sendToDes: Boolean = false
  }

  case object cotax extends TaxType {
    override val sendToDes: Boolean = false
  }

  case object ntc extends TaxType {
    override val sendToDes: Boolean = false
  }

  case object paye extends TaxType {
    override val sendToDes: Boolean = false
  }

  case object nps extends TaxType {
    override val sendToDes: Boolean = false
  }

  override def values: immutable.IndexedSeq[TaxType] = findValues
}
