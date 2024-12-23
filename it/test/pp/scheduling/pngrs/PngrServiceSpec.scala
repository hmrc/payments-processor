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

package pp.scheduling.pngrs

import com.github.tomakehurst.wiremock.client.WireMock
import pp.config.PngrsQueueConfig
import pp.connectors.PngrConnector
import pp.model.pngrs.PngrStatusTypes
import pp.model.wokitems.PngrMyWorkItem
import pp.model.{Origins, TaxTypes, wokitems}
import pp.services.PngrService
import support.PaymentsProcessData.pngrStatusUpdateRequest
import support.{ItSpec, Pngr}
import uk.gov.hmrc.mongo.workitem.{ProcessingStatus, WorkItem}

import java.time.{Clock, LocalDateTime}
import scala.concurrent.ExecutionContext.Implicits.global

class PngrServiceSpec extends ItSpec {
  private lazy val repo = injector.instanceOf[PngrMongoRepo]
  private lazy val pngrConnector = injector.instanceOf[PngrConnector]
  private lazy val queueConfig = injector.instanceOf[PngrsQueueConfig]
  private lazy val pngrService = new PngrService(repo, queueConfig, pngrConnector, Clock.systemDefaultZone())
  val time: LocalDateTime = LocalDateTime.now
  val created: LocalDateTime = time
  val availableUntilInPast: LocalDateTime = time.minusSeconds(60)
  val availUntilInFuture: LocalDateTime = time.plusSeconds(60)

  val workItem: PngrMyWorkItem = wokitems.PngrMyWorkItem(created, availableUntilInPast, TaxTypes.pngr.entryName, Origins.OPS, "reference", PngrStatusTypes.Successful)

  override def configMap: Map[String, Any] =
    super.configMap
      .updated("pngr.queue.available.for", "1 seconds")

  override def beforeEach(): Unit = {
    val _ = repo.removeAll().futureValue
    WireMock.reset()
    super.beforeEach()
  }

  protected def numberOfQueuedNotifications: Long = repo.countAll().futureValue

  "check error mechanism, not available" in {
    pngrService.isAvailable(workItem) shouldBe false
  }

  "check error mechanism, available" in {
    pngrService.isAvailable(workItem.copy(createdOn      = time, availableUntil = availUntilInFuture)) shouldBe true
  }

  "sendPngrToWorkItemRepo" should {
    "add a notification to the queue" in {
      numberOfQueuedNotifications shouldBe 0
      val workItem = pngrService.sendPngrToWorkItemRepo(pngrStatusUpdateRequest).futureValue
      numberOfQueuedNotifications shouldBe 1

      workItem.item.taxType shouldBe TaxTypes.pngr.entryName
      workItem.item.reference shouldBe pngrStatusUpdateRequest.reference
      workItem.item.status shouldBe PngrStatusTypes.Successful
      workItem.item.origin shouldBe Origins.PCI_PAL
      workItem.status shouldBe ProcessingStatus.ToDo
    }
  }

  "retrieveWorkItems" should {

    "mark workitem as permanently failed" when {
      "the first pngr fails and the second one fails also, when available until is before created On" in {
        Pngr.statusUpdateFailsWithAnInternalServerError()
        Pngr.statusUpdateFailsWithAnInternalServerError(2000, 1)

        numberOfQueuedNotifications shouldBe 0
        val workItem = pngrService.sendPngrToWorkItemRepo(pngrStatusUpdateRequest).futureValue
        workItem.item.availableUntil.isAfter(workItem.item.createdOn) shouldBe true
        numberOfQueuedNotifications shouldBe 1
        eventually {
          pngrService.retrieveWorkItems.futureValue.isEmpty shouldBe false
          numberOfQueuedNotifications shouldBe 1
          val sentItems: Seq[WorkItem[PngrMyWorkItem]] = pngrService.retrieveWorkItems.futureValue
          sentItems.size shouldBe 0
        }
      }
    }
  }
}

