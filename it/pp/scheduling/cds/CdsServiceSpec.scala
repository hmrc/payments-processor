package pp.scheduling.cds

import java.time.{Clock, LocalDateTime}

import com.github.tomakehurst.wiremock.client.WireMock
import play.api.libs.json.Json
import pp.config.CdsOpsQueueConfig
import pp.connectors.CdsConnector
import pp.model.wokitems.{CdsOpsWorkItem, MibOpsWorkItem}
import pp.model.{Origins, TaxTypes}
import pp.services.CdsOpsService
import support.PaymentsProcessData.{cdsReference, cdsStatusUpdateRequest, mibReference}
import support.{Cds, ItSpec, Mib}
import uk.gov.hmrc.workitem.{ToDo, WorkItem}

class CdsServiceSpec extends ItSpec {
  private lazy val repo = injector.instanceOf[CdsOpsMongoRepo]
  private lazy val cdsConnector = injector.instanceOf[CdsConnector]
  private lazy val queueConfig = injector.instanceOf[CdsOpsQueueConfig]
  private lazy val cdsOpsService = new CdsOpsService(repo, queueConfig, cdsConnector, Clock.systemDefaultZone())
  val time: LocalDateTime = LocalDateTime.now
  val created: LocalDateTime = time
  val availableUntilInPast: LocalDateTime = time.minusSeconds(60)
  val availUntilInFuture: LocalDateTime = time.plusSeconds(60)

  val workItem: CdsOpsWorkItem = CdsOpsWorkItem(created, availableUntilInPast, TaxTypes.cds, Origins.OPS, "reference", cdsStatusUpdateRequest)

  override def configMap: Map[String, Any] =
    super.configMap
      .updated("cds.queue.available.for", "1 seconds")

  override def beforeEach(): Unit = {
    val _ = repo.removeAll().futureValue
    WireMock.reset()
    super.beforeEach()
  }

  protected def numberOfQueuedNotifications: Integer = repo.count(Json.obj()).futureValue

  "check error mechanism, not available" in {
    cdsOpsService.isAvailable(workItem) shouldBe false
  }

  "check error mechanism, available" in {
    cdsOpsService.isAvailable(workItem.copy(createdOn = time, availableUntil = availUntilInFuture)) shouldBe true
  }


  "sendCdsOpsToWorkItemRepo" should {
    "add a notification to the queue" in {
      numberOfQueuedNotifications shouldBe 0
      val workItem = cdsOpsService.sendCdsOpsToWorkItemRepo(cdsStatusUpdateRequest).futureValue
      numberOfQueuedNotifications shouldBe 1

      workItem.item.taxType shouldBe TaxTypes.cds
      workItem.item.reference shouldBe cdsReference
      workItem.item.origin shouldBe Origins.OPS
      workItem.status shouldBe ToDo
    }
  }

  "retrieveWorkItems" should {

    "mark workitem as permanently failed" when {
      "the first cds fails and the second one fails also, when available until is before created On" in {
        Cds.statusUpdateFailsWithAnInternalServerError()
        Cds.statusUpdateFailsWithAnInternalServerError(2000, 1)

        numberOfQueuedNotifications shouldBe 0
        val workItem = cdsOpsService.sendCdsOpsToWorkItemRepo(cdsStatusUpdateRequest).futureValue
        workItem.item.availableUntil.isAfter(workItem.item.createdOn) shouldBe true
        numberOfQueuedNotifications shouldBe 1
        eventually {
          cdsOpsService.retrieveWorkItems.futureValue.isEmpty shouldBe false
          numberOfQueuedNotifications shouldBe 1
          val sentItems: Seq[WorkItem[CdsOpsWorkItem]] = cdsOpsService.retrieveWorkItems.futureValue
          sentItems.size shouldBe 0
        }
      }
    }
  }
}
