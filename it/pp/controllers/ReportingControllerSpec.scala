package pp.controllers

import com.github.tomakehurst.wiremock.client.WireMock
import play.api.http.Status
import play.api.libs.json.Json
import pp.model.ProcessingStatusOpsValues.Failed
import pp.model.Item
import pp.model.TaxTypes.{mib, p800, pngr}
import pp.scheduling.chargeref.ChargeRefNotificationMongoRepo
import pp.scheduling.mib.MibOpsMongoRepo
import pp.scheduling.pngrs.PngrMongoRepo
import support.{Des, ItSpec, Mib, Pngr}
import support.PaymentsProcessData.{p800ChargeRefNotificationRequest, pngrStatusUpdateRequest}
import support.PaymentsProcessData.mibReference

class ReportingControllerSpec extends ItSpec {
  private lazy val repoPngr = injector.instanceOf[PngrMongoRepo]
  private lazy val repoChargeRef = injector.instanceOf[ChargeRefNotificationMongoRepo]
  private lazy val mibOpsRepo = injector.instanceOf[MibOpsMongoRepo]

  override def beforeEach(): Unit = {
    repoPngr.removeAll().futureValue
    repoChargeRef.removeAll().futureValue
    mibOpsRepo.removeAll().futureValue
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
      .updated("mibops.queue.enabled", "true")
      .updated("mibops.poller.enabled", "true")



  protected def numberOfQueuedNotificationsPngr: Integer = repoPngr.count(Json.obj()).futureValue
  protected def numberOfQueuedNotificationsChargeRef: Integer = repoChargeRef.count(Json.obj()).futureValue
  protected def numberOfQueuedNotificationsMibOps: Integer = mibOpsRepo.count(Json.obj()).futureValue

  "test pngr reporting" in {

    Pngr.statusUpdateFailsWithAnInternalServerError()

    numberOfQueuedNotificationsPngr shouldBe 0

    val response = testConnector.sendStatusUpdateToPngr(pngrStatusUpdateRequest).futureValue
    response.status shouldBe Status.OK
    numberOfQueuedNotificationsPngr shouldBe 1
    eventually {
      testConnector.count(pngr, Failed).futureValue.json.as[Int] shouldBe 1
      testConnector.count(p800, Failed).futureValue.json.as[Int] shouldBe 0
      testConnector.count(mib, Failed).futureValue.json.as[Int] shouldBe 0
      testConnector.getAll(pngr).futureValue.json.as[List[Item]].size shouldBe 1
      testConnector.getAll(p800).futureValue.json.as[List[Item]].size shouldBe 0
      testConnector.getAll(mib).futureValue.json.as[List[Item]].size shouldBe 0
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
      testConnector.count(mib, Failed).futureValue.json.as[Int] shouldBe 0
      testConnector.getAll(p800).futureValue.json.as[List[Item]].size shouldBe 1
      testConnector.getAll(pngr).futureValue.json.as[List[Item]].size shouldBe 0
      testConnector.getAll(mib).futureValue.json.as[List[Item]].size shouldBe 0
    }

  }

  "test mib reporting" in {

    Mib.statusUpdateFailsWithAnInternalServerError(reference = mibReference)
    numberOfQueuedNotificationsMibOps shouldBe 0

    val response = testConnector.mibPaymentCallBack(mibReference).futureValue
    response.status shouldBe Status.OK
    numberOfQueuedNotificationsMibOps shouldBe 1
    eventually {
      testConnector.count(p800, Failed).futureValue.json.as[Int] shouldBe 0
      testConnector.count(pngr, Failed).futureValue.json.as[Int] shouldBe 0
      testConnector.count(mib, Failed).futureValue.json.as[Int] shouldBe 1
      testConnector.getAll(p800).futureValue.json.as[List[Item]].size shouldBe 0
      testConnector.getAll(pngr).futureValue.json.as[List[Item]].size shouldBe 0
      testConnector.getAll(mib).futureValue.json.as[List[Item]].size shouldBe 1
    }

  }

}
