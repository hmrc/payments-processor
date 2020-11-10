package pp.controllers

import play.api.http.Status
import play.api.libs.json.Json
import support.{ItSpec, PaymentsProcessData}
import uk.gov.hmrc.http.HttpResponse

class ApiDocumentationControllerNotEnabledSpec extends ItSpec {

  override def configMap: Map[String, Any] =
    super
      .configMap
      .updated("api.enabled", "false")
      .updated("api.status", "STABLE")


  "DefinitionController.definition return OK status" in {
    val response: HttpResponse = testConnector.getDef.futureValue
    response.status shouldBe Status.OK
    val json = Json.parse(response.body)
    json shouldBe PaymentsProcessData.definition(endpointsEnabled = false, "STABLE")
  }


}

