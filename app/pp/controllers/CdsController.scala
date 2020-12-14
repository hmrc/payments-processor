package pp.controllers

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.api.{Configuration, Logger}
import pp.config.CdsOpsQueueConfig
import pp.connectors.CdsConnector
import pp.controllers.retries.CdsRetries
import pp.model.cds.NotifyImmediatePaymentRequest
import pp.services.CdsOpsService
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.ExecutionContext

class CdsController @Inject() (
                                cc:                    ControllerComponents,
                                val cdsOpsQueueConfig: CdsOpsQueueConfig,
                                val configuration:     Configuration,
                                val cdsOpsService:     CdsOpsService,
                                val cdsConnector:      CdsConnector

                              )
                              (implicit val executionContext: ExecutionContext) extends BackendController(cc) with CdsRetries {

  val logger: Logger = Logger(this.getClass.getSimpleName)

  def ipsCallback(notifyImmediatePaymentRequest: NotifyImmediatePaymentRequest): Action[AnyContent] = Action.async { implicit request =>
    logger.debug("sendStatusUpdateToCds")
    sendPaymentUpdateToCds(notifyImmediatePaymentRequest)
  }
}

