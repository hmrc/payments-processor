/*
 * Copyright 2023 HM Revenue & Customs
 *
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
          equalToJson(s"""{"auditType": "${auditType}"}""", true, true)
        )
        .withRequestBody(
          equalToJson(s"""{ "auditSource" : "payments-processor"  }""", true, true)
        )
        .withRequestBody(
          equalToJson(s"""{ "detail" : ${auditEvent.toString} }""", true, true)
        )
    )
  }

  def verifyNoAuditEvent(auditType: Option[String] = None): Unit = {
    auditType.fold {
      verify(exactly(0), postRequestedFor(urlPathEqualTo(auditUrl)))
    } { at =>
      verify(
        exactly(0),
        postRequestedFor(urlPathEqualTo(auditUrl))
          .withRequestBody(equalToJson(s"""{"auditType": "${at}"}""", true, true))
      )
    }
  }

}
