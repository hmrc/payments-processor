/*
 * Copyright 2019 HM Revenue & Customs
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

import org.scalatestplus.play.WsScalaTestClient
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import support.{ItSpec, TestData}
import uk.gov.hmrc.http.HttpResponse

class ApiDocumentationControllerSpec extends ItSpec with WsScalaTestClient with FutureAwaits with DefaultAwaitTimeout {

  private lazy val controller = injector.instanceOf[ApiDocumentationController]

  "DefinitionController.definition return OK status" in {
    val response: HttpResponse = connector.getDef.futureValue
    response.status shouldBe Status.OK
    val json = Json.parse(response.body)
    json shouldBe TestData.definition
  }

  //This test works from SBT but not from intellij ... be warned!!
  "DocumentationController return OK status" in {
    val response: HttpResponse = connector.getApiDoc.futureValue
    response.status shouldBe Status.OK

  }

}
