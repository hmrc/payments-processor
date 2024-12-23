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

package support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.JsObject
import play.mvc.Http.Status

object AuditConnectorStub {

  val auditUrl: String = "/write/audit"

  def stubAudit: StubMapping = stubFor(post(urlEqualTo(auditUrl)).willReturn(aResponse().withStatus(Status.OK)))

  // helper method to stop wiremock 'stub not found' being spammed into the logs when testing. Just creates a basic stub for implicit requests.
  def stubImplicitAuditEvents: StubMapping = {
    stubFor(post(urlEqualTo(s"$auditUrl")).withRequestBody(equalToJson(s"""{"auditType": "RequestReceived"}""", true, true)).willReturn(aResponse().withStatus(Status.OK)))
    stubFor(post(urlEqualTo(s"$auditUrl")).withRequestBody(equalToJson(s"""{"auditType": "OutboundCall"}""", true, true)).willReturn(aResponse().withStatus(Status.OK)))
    stubFor(post(urlEqualTo(s"$auditUrl/merged")).willReturn(aResponse().withStatus(Status.OK)))
  }

  def verifyEventAudited(auditType: String, auditEvent: JsObject): Unit = {
    verify(
      postRequestedFor(urlPathEqualTo(auditUrl))
        .withRequestBody(
          equalToJson(s"""{"auditType": "${auditType}"}""", true, true))
        .withRequestBody(
          equalToJson(s"""{ "auditSource" : "payments-processor"  }""", true, true))
        .withRequestBody(
          equalToJson(s"""{ "detail" : ${auditEvent.toString} }""", true, true)))
  }

  def verifyNoAuditEvent(auditType: Option[String] = None): Unit = {
    auditType.fold {
      verify(exactly(0), postRequestedFor(urlPathEqualTo(auditUrl)))
    } { at =>
      verify(
        exactly(0),
        postRequestedFor(urlPathEqualTo(auditUrl))
          .withRequestBody(equalToJson(s"""{"auditType": "${at}"}""", true, true)))
    }
  }

}
