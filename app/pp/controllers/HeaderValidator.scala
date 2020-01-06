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

package pp.controllers

import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex
import scala.util.matching.Regex.Match

trait HeaderValidator extends Results with Status {

  def validateVersion(version: String): Boolean = version == "1.0"

  def validateContentType(contentType: String): Boolean = contentType == "json"

  def matchHeader(header: String): Option[Match] = new Regex("""^application/vnd[.]{1}hmrc[.]{1}(.*?)[+]{1}(.*)$""", "version", "contenttype") findFirstMatchIn header

  def acceptHeaderValidationRules(header: Option[String]): Boolean = header flatMap (a => matchHeader(a) map (res => validateContentType(res.group("contenttype")) && validateVersion(res.group("version")))) getOrElse (false)

  def validateAccept(rules: Option[String] => Boolean, parse: PlayBodyParsers)(implicit ec: ExecutionContext): ActionBuilder[Request, AnyContent] = new ActionBuilder[Request, AnyContent] {
    override def parser: BodyParser[AnyContent] = parse.defaultBodyParser
    override protected def executionContext: ExecutionContext = ec

    def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
      if (rules(request.headers.get("Accept")))
        block(request)
      else {
        val response = ErrorResponse(NOT_ACCEPTABLE, Constants.acceptHeaderMissing)
        Future.successful(NotAcceptable(Json.toJson(response)))
      }
    }
  }
}
