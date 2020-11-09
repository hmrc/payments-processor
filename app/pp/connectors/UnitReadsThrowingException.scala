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

package pp.connectors

import uk.gov.hmrc.http.HttpReads.Implicits.{readRaw, readEitherOf, throwOnFailure, _}
import uk.gov.hmrc.http.{HttpReads, HttpResponse, UpstreamErrorResponse}

object UnitReadsThrowingException {

  /**
   * It's a backward compatible implementation of readUnit which throws exception
   * if the http responds status is 5xx or 4xx.
   *
   * Use it by shadowing `readUnit` from import uk.gov.hmrc.http.HttpReads.Implicits.readUnit
   *
   * Example:
   * {{{
   *   import uk.gov.hmrc.http.HttpReads.Implicits._
   *   //TIP: don't rename it. It shadows an implicit reads from uk.gov.hmrc.http.HttpReads.Implicits.readUnit
   *   implicit val readUnit: HttpReads[Unit] = UnitReadsThrowingException.readUnit
   * }}}
   *
   */
  implicit val readUnit: HttpReads[Unit] = {
    val eitherHttpResponseReads: HttpReads[Either[UpstreamErrorResponse, HttpResponse]] = readEitherOf[HttpResponse]
    val eitherUnitReads: HttpReads[Either[UpstreamErrorResponse, Unit]] = eitherHttpResponseReads.map(x => x.right.map(_ => ()))
    throwOnFailure(eitherUnitReads)
  }

}
