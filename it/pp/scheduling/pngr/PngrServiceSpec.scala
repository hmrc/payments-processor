package pp.scheduling.pngr


import java.time.{Clock, LocalDateTime}

import com.github.tomakehurst.wiremock.client.WireMock
import play.api.libs.json.Json
import pp.config.PngrQueueConfig
import pp.connectors.pngr.PngrConnector
import pp.model.pngr.{PngrStatusTypes, PngrWorkItem}
import pp.model.{Origins, TaxTypes}
import pp.services.pngr.PngrService
import support.PaymentsProcessData.pngrStatusUpdateRequest
import support.{ItSpec, Pngr}
import uk.gov.hmrc.workitem.{ToDo, WorkItem}

class PngrServiceSpec extends ItSpec {
  private lazy val repo = injector.instanceOf[PngrMongoRepo]
  private lazy val pngrConnector = injector.instanceOf[PngrConnector]
  private lazy val queueConfig = injector.instanceOf[PngrQueueConfig]
  private lazy val pngrService = new PngrService(repo, queueConfig, pngrConnector, Clock.systemDefaultZone())
  val time: LocalDateTime = LocalDateTime.now
  val created: LocalDateTime = time
  val availableUntilInPast: LocalDateTime = time.minusSeconds(60)
  val availUntilInFuture: LocalDateTime = time.plusSeconds(60)

  val workItem: PngrWorkItem = PngrWorkItem(created, availableUntilInPast, TaxTypes.pngr, Origins.OPS, "reference", PngrStatusTypes.Successful)

  override def configMap: Map[String, Any] =
    super.configMap
            .updated("pngr.queue.available.for", "1 seconds")

  override def beforeEach(): Unit = {
    val _ = repo.removeAll().futureValue
    WireMock.reset()
    super.beforeEach()
  }

  protected def numberOfQueuedNotifications: Integer = repo.count(Json.obj()).futureValue

  "check error mechanism, not available" in {
    pngrService.isAvailable(workItem) shouldBe false
  }

  "check error mechanism, available" in {
    pngrService.isAvailable(workItem.copy(createdOn = time, availableUntil = availUntilInFuture)) shouldBe true
  }


  "sendPngrToWorkItemRepo" should {
    "add a notification to the queue" in {
      numberOfQueuedNotifications shouldBe 0
      val workItem = pngrService.sendPngrToWorkItemRepo(pngrStatusUpdateRequest).futureValue
      numberOfQueuedNotifications shouldBe 1

      workItem.item.taxType shouldBe TaxTypes.pngr
      workItem.item.reference shouldBe pngrStatusUpdateRequest.reference
      workItem.item.status shouldBe PngrStatusTypes.Successful
      workItem.item.origin shouldBe Origins.PCI_PAL
      workItem.status shouldBe ToDo
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
          val sentItems: Seq[WorkItem[PngrWorkItem]] = pngrService.retrieveWorkItems.futureValue
          sentItems.size shouldBe 0
        }
      }
    }
  }
}

