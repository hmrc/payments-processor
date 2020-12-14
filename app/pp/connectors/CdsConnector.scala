package pp.connectors

import javax.inject.{Inject, Singleton}
import play.api.Logger
import pp.connectors.ResponseReadsThrowingException.readResponse
import pp.model.cds.NotifyImmediatePaymentRequest
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CdsConnector @Inject()(httpClient: HttpClient, servicesConfig: ServicesConfig)(implicit ec: ExecutionContext) {

  private val serviceURL: String = s"${servicesConfig.baseUrl("cds")}/get-me-from-pay-frontend"
  private val authorizationToken: String = servicesConfig.getString("get-me-from-pay-frontend")
  private val serviceEnvironment: String = servicesConfig.getString("get-me-from-pay-frontend")

  //get thisfrom pay-frontend... should be ok
  private val desHeaderCarrier: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(s"Bearer $authorizationToken")))
    .withExtraHeaders("Environment" -> serviceEnvironment)

  private val logger: Logger = Logger(this.getClass.getSimpleName)

  def paymentCallback(notifyImmediatePaymentRequest: NotifyImmediatePaymentRequest): Future[HttpResponse] = {
    val url: String = s"$serviceURL/addme"
    logger.debug(s"""c $url""")
    implicit val hc: HeaderCarrier = HeaderCarrier()
    httpClient.POST[NotifyImmediatePaymentRequest, HttpResponse](url, notifyImmediatePaymentRequest)
  }
}
