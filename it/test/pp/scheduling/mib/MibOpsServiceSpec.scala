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

package pp.scheduling.mib

import com.github.tomakehurst.wiremock.client.WireMock
import pp.config.MibOpsQueueConfig
import pp.connectors.MibConnector
import pp.model.wokitems.MibOpsMyWorkItem
import pp.model.{Origins, TaxTypes}
import pp.services.MibOpsService
import support.PaymentsProcessData.mibReference
import support.{ItSpec, Mib, PaymentsProcessData}
import uk.gov.hmrc.mongo.workitem.{ProcessingStatus, WorkItem}

import java.time.{Clock, LocalDateTime}
import scala.concurrent.ExecutionContext.Implicits.global

class MibOpsServiceSpec extends ItSpec {
  private lazy val repo = injector.instanceOf[MibOpsMongoRepo]
  private lazy val mibConnector = injector.instanceOf[MibConnector]
  private lazy val queueConfig = injector.instanceOf[MibOpsQueueConfig]
  private lazy val mibOpsService = new MibOpsService(repo, queueConfig, mibConnector, Clock.systemDefaultZone())
  val time: LocalDateTime = LocalDateTime.now
  val created: LocalDateTime = time
  val availableUntilInPast: LocalDateTime = time.minusSeconds(60)
  val availUntilInFuture: LocalDateTime = time.plusSeconds(60)

  val workItem: MibOpsMyWorkItem = MibOpsMyWorkItem(created, availableUntilInPast, TaxTypes.mib.entryName, Origins.OPS, "reference", PaymentsProcessData.modsPaymentCallBackRequestWithAmendmentRef)

  override def configMap: Map[String, Any] =
    super.configMap
      .updated("mibops.queue.available.for", "1 seconds")

  override def beforeEach(): Unit = {
    val _ = repo.removeAll().futureValue
    WireMock.reset()
    super.beforeEach()
  }

  protected def numberOfQueuedNotifications: Long = repo.countAll().futureValue

  "check error mechanism, not available" in {
    mibOpsService.isAvailable(workItem) shouldBe false
  }

  "check error mechanism, available" in {
    mibOpsService.isAvailable(workItem.copy(createdOn      = time, availableUntil = availUntilInFuture)) shouldBe true
  }

  "sendMibOpsToWorkItemRepo" should {
    "add a notification to the queue" in {
      numberOfQueuedNotifications shouldBe 0
      val workItem = mibOpsService.sendMibOpsToWorkItemRepo(PaymentsProcessData.modsPaymentCallBackRequestWithAmendmentRef).futureValue
      numberOfQueuedNotifications shouldBe 1

      workItem.item.taxType shouldBe TaxTypes.mib.entryName
      workItem.item.reference shouldBe mibReference
      workItem.item.origin shouldBe Origins.OPS
      workItem.status shouldBe ProcessingStatus.ToDo
    }
  }

  "retrieveWorkItems" should {

    "mark workitem as permanently failed" when {
      "the first mib fails and the second one fails also, when available until is before created On" in {
        Mib.statusUpdateFailsWithAnInternalServerError()
        Mib.statusUpdateFailsWithAnInternalServerError(2000, 1)

        numberOfQueuedNotifications shouldBe 0
        val workItem = mibOpsService.sendMibOpsToWorkItemRepo(PaymentsProcessData.modsPaymentCallBackRequestWithAmendmentRef).futureValue
        workItem.item.availableUntil.isAfter(workItem.item.createdOn) shouldBe true
        numberOfQueuedNotifications shouldBe 1
        eventually {
          mibOpsService.retrieveWorkItems.futureValue.isEmpty shouldBe false
          numberOfQueuedNotifications shouldBe 1
          val sentItems: Seq[WorkItem[MibOpsMyWorkItem]] = mibOpsService.retrieveWorkItems.futureValue
          sentItems.size shouldBe 0
        }
      }
    }
  }
}

