package pp.controllers

import com.github.tomakehurst.wiremock.client.WireMock
import play.api.http.Status
import pp.model.Item
import pp.model.ProcessingStatusOpsValues.Failed
import pp.scheduling.chargeref.ChargeRefNotificationMongoRepo
import pp.scheduling.mib.MibOpsMongoRepo
import pp.scheduling.pngrs.PngrMongoRepo
import support.PaymentsProcessData.{modsPaymentCallBackRequestWithAmendmentRef, p800ChargeRefNotificationRequest, pngrStatusUpdateRequest}
import support.{Des, ItSpec, Mib, Pngr}
import tps.model.TaxTypes.{MIB, P800, PNGR}

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

  protected def numberOfQueuedNotificationsPngr: Long = repoPngr.countAll().futureValue
  protected def numberOfQueuedNotificationsChargeRef: Long = repoChargeRef.countAll().futureValue
  protected def numberOfQueuedNotificationsMibOps: Long = mibOpsRepo.countAll().futureValue

  "test pngr reporting" in {

    Pngr.statusUpdateFailsWithAnInternalServerError()

    numberOfQueuedNotificationsPngr shouldBe 0

    val response = testConnector.sendStatusUpdateToPngr(pngrStatusUpdateRequest).futureValue
    response.status shouldBe Status.OK
    numberOfQueuedNotificationsPngr shouldBe 1
    eventually {
      testConnector.count(PNGR, Failed).futureValue.json.as[Int] shouldBe 1
      testConnector.count(P800, Failed).futureValue.json.as[Int] shouldBe 0
      testConnector.count(MIB, Failed).futureValue.json.as[Int] shouldBe 0
      testConnector.getAll(PNGR).futureValue.json.as[List[Item]].size shouldBe 1
      testConnector.getAll(P800).futureValue.json.as[List[Item]].size shouldBe 0
      testConnector.getAll(MIB).futureValue.json.as[List[Item]].size shouldBe 0
    }

  }

  "test chargeref reporting" in {

    Des.cardPaymentsNotificationFailsWithAnInternalServerError()

    numberOfQueuedNotificationsPngr shouldBe 0

    val response = testConnector.sendCardPaymentsNotification(p800ChargeRefNotificationRequest).futureValue
    response.status shouldBe Status.OK
    numberOfQueuedNotificationsChargeRef shouldBe 1
    eventually {
      testConnector.count(P800, Failed).futureValue.json.as[Int] shouldBe 1
      testConnector.count(PNGR, Failed).futureValue.json.as[Int] shouldBe 0
      testConnector.count(MIB, Failed).futureValue.json.as[Int] shouldBe 0
      testConnector.getAll(P800).futureValue.json.as[List[Item]].size shouldBe 1
      testConnector.getAll(PNGR).futureValue.json.as[List[Item]].size shouldBe 0
      testConnector.getAll(MIB).futureValue.json.as[List[Item]].size shouldBe 0
    }

  }

  "test mib reporting" in {

    Mib.statusUpdateFailsWithAnInternalServerError()
    numberOfQueuedNotificationsMibOps shouldBe 0

    val response = testConnector.mibPaymentCallBack(modsPaymentCallBackRequestWithAmendmentRef).futureValue
    response.status shouldBe Status.OK
    numberOfQueuedNotificationsMibOps shouldBe 1
    eventually {
      testConnector.count(P800, Failed).futureValue.json.as[Int] shouldBe 0
      testConnector.count(PNGR, Failed).futureValue.json.as[Int] shouldBe 0
      testConnector.count(MIB, Failed).futureValue.json.as[Int] shouldBe 1
      testConnector.getAll(P800).futureValue.json.as[List[Item]].size shouldBe 0
      testConnector.getAll(PNGR).futureValue.json.as[List[Item]].size shouldBe 0
      testConnector.getAll(MIB).futureValue.json.as[List[Item]].size shouldBe 1
    }

  }

}
