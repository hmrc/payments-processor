package pp.services

import java.time.{Clock, LocalDateTime, ZoneId}

import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.Logger
import play.api.mvc.Results
import pp.config.CdsOpsQueueConfig
import pp.connectors.CdsConnector
import pp.model.cds.NotifyImmediatePaymentRequest
import pp.model.wokitems.CdsOpsWorkItem
import pp.model.{Origins, TaxTypes}
import pp.scheduling.cds.CdsOpsMongoRepo
import uk.gov.hmrc.workitem.WorkItem

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CdsOpsService @Inject()(
                               val repo: CdsOpsMongoRepo,
                               val queueConfig: CdsOpsQueueConfig,
                               cdsConnector: CdsConnector,
                               val clock: Clock,
                             )(implicit val executionContext: ExecutionContext) extends WorkItemService[CdsOpsWorkItem] with Results {

  val logger: Logger = Logger(this.getClass.getSimpleName)

  //These are all specific to cds processing
  def sendWorkItem(workItem: WorkItem[CdsOpsWorkItem]): Future[Unit] = {

    logger.debug("inside sendWorkItemToCdsOps")
    for {
      _ <- cdsConnector.paymentCallback(workItem.item.notifyImmediatePaymentRequest)
    } yield ()

  }


  def sendCdsOpsToWorkItemRepo(notifyImmediatePaymentRequest: NotifyImmediatePaymentRequest): Future[WorkItem[CdsOpsWorkItem]] = {
    logger.debug("inside sendCardPaymentsNotificationAsync")
    val time = LocalDateTime.now(clock)
    val jodaLocalDateTime = new DateTime(time.atZone(ZoneId.systemDefault).toInstant.toEpochMilli)
    val workItem = CdsOpsWorkItem(time, availableUntil(time), TaxTypes.cds, Origins.OPS, notifyImmediatePaymentRequest)
    repo.pushNew(workItem, jodaLocalDateTime)

  }


}