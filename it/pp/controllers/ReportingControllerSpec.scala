package pp.controllers

import com.github.tomakehurst.wiremock.client.WireMock
import play.api.http.Status
import play.api.libs.json.Json
import pp.model.ProcessingStatusOpsValues.Failed
import pp.model.Item
import pp.model.TaxTypes.{p800, pngr}
import pp.scheduling.chargeref.ChargeRefNotificationMongoRepo
import pp.scheduling.pngr.PngrMongoRepo
import support.{Des, ItSpec, Pngr}
import support.PaymentsProcessData.{p800ChargeRefNotificationRequest, pngrStatusUpdateRequest}

class ReportingControllerSpec extends ItSpec{

  private lazy val repoPngr = injector.instanceOf[PngrMongoRepo]
  private lazy val repoChargeRef = injector.instanceOf[ChargeRefNotificationMongoRepo]
  override def beforeEach(): Unit = {
    repoPngr.removeAll().futureValue
    repoChargeRef.removeAll().futureValue
    WireMock.reset()
    super.beforeEach()
  }

  override def configMap: Map[String, Any] =
    super
      .configMap
      .updated("pngr.queue.enabled", "true")
      .updated("pngr.poller.enabled", "true")
      .updated("chargeref.queue.enabled", "true")
      .updated("chargeref.poller.enabled", "true")


  protected def numberOfQueuedNotificationsPngr: Integer = repoPngr.count(Json.obj()).futureValue
  protected def numberOfQueuedNotificationsChargeRef: Integer = repoChargeRef.count(Json.obj()).futureValue

  "test pngr reporting" in {

    Pngr.statusUpdateFailsWithAnInternalServerError()

    numberOfQueuedNotificationsPngr shouldBe 0

    val response = testConnector.sendStatusUpdateToPngr(pngrStatusUpdateRequest).futureValue
    response.status shouldBe Status.OK
    numberOfQueuedNotificationsPngr shouldBe 1
    eventually {
      testConnector.count(pngr, Failed).futureValue.json.as[Int] shouldBe 1
      testConnector.count(p800, Failed).futureValue.json.as[Int] shouldBe 0
      testConnector.getAll(pngr).futureValue.json.as[List[Item]].size shouldBe 1
      testConnector.getAll(p800).futureValue.json.as[List[Item]].size shouldBe 0
    }



  }

  "test chargeref reporting" in {

    Des.cardPaymentsNotificationFailsWithAnInternalServerError()

    numberOfQueuedNotificationsPngr shouldBe 0

    val response = testConnector.sendCardPaymentsNotification(p800ChargeRefNotificationRequest).futureValue
    response.status shouldBe Status.OK
    numberOfQueuedNotificationsChargeRef shouldBe 1
    eventually {
      testConnector.count(p800, Failed).futureValue.json.as[Int] shouldBe 1
      testConnector.count(pngr, Failed).futureValue.json.as[Int] shouldBe 0
      testConnector.getAll(p800).futureValue.json.as[List[Item]].size shouldBe 1
      testConnector.getAll(pngr).futureValue.json.as[List[Item]].size shouldBe 0
    }



  }

}
