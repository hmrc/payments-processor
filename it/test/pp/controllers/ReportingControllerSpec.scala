/*
 * Copyright 2024 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.client.WireMock
import play.api.http.Status
import pp.model.Item
import pp.model.ProcessingStatusOpsValues.Failed
import pp.model.TaxTypes.{mib, p800, pngr}
import pp.scheduling.chargeref.ChargeRefNotificationMongoRepo
import pp.scheduling.mib.MibOpsMongoRepo
import pp.scheduling.pngrs.PngrMongoRepo
import support.PaymentsProcessData.{modsPaymentCallBackRequestWithAmendmentRef, p800ChargeRefNotificationRequest, pngrStatusUpdateRequest}
import support.{Des, ItSpec, Mib, Pngr}

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

    Mib.statusUpdateFailsWithAnInternalServerError()
    numberOfQueuedNotificationsMibOps shouldBe 0

    val response = testConnector.mibPaymentCallBack(modsPaymentCallBackRequestWithAmendmentRef).futureValue
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
