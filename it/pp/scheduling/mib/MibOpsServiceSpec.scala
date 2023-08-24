package pp.scheduling.mib

import com.github.tomakehurst.wiremock.client.WireMock
import pp.config.MibOpsQueueConfig
import pp.connectors.MibConnector
import pp.model.Origins
import pp.model.wokitems.MibOpsMyWorkItem
import pp.services.MibOpsService
import support.PaymentsProcessData.mibReference
import support.{ItSpec, Mib, PaymentsProcessData}
import tps.model.TaxTypes
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

  val workItem: MibOpsMyWorkItem = MibOpsMyWorkItem(created, availableUntilInPast, TaxTypes.MIB, Origins.OPS, "reference", PaymentsProcessData.modsPaymentCallBackRequestWithAmendmentRef)

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

      workItem.item.taxType shouldBe TaxTypes.MIB
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

