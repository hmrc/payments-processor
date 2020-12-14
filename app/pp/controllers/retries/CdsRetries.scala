package pp.controllers.retries

import play.api.Logger
import play.api.mvc.Results
import pp.config.CdsOpsQueueConfig
import pp.connectors.CdsConnector
import pp.model.cds.NotifyImmediatePaymentRequest
import pp.services.CdsOpsService
import uk.gov.hmrc.http.{BadGatewayException, BadRequestException, UpstreamErrorResponse}
import uk.gov.hmrc.workitem.ToDo

import scala.concurrent.{ExecutionContext, Future}

trait CdsRetries extends Results {

  val logger: Logger
  val cdsOpsQueueConfig: CdsOpsQueueConfig
  val cdsConnector: CdsConnector
  val cdsOpsService: CdsOpsService

  implicit val executionContext: ExecutionContext

  def sendPaymentUpdateToCds(notifyImmediatePaymentRequest: NotifyImmediatePaymentRequest): Future[Status] = {
    logger.debug("sendPaymentUpdateToCds")
    cdsConnector
      .paymentCallback(notifyImmediatePaymentRequest)
      .map(_ => Ok)
      .recoverWith {
        case e: UpstreamErrorResponse if e.statusCode == 400 =>
          Future.failed(new BadRequestException(e.getMessage()))
        case e: UpstreamErrorResponse if e.statusCode == 404 =>
          Future.failed(new BadGatewayException(e.message))
        case e =>
          if (cdsOpsQueueConfig.queueEnabled) {
            logger.debug("Queue enabled")
            cdsOpsService.sendCdsOpsToWorkItemRepo(notifyImmediatePaymentRequest)
              .map(
                res => res.status match {
                  case ToDo => Ok
                  case _ =>
                    logger.error("Could not add message to work item repo")
                    InternalServerError
                }
              )
          } else {
            logger.warn("Queue disabled")
            Future.failed(e)
          }
      }
  }

}
