/*
 * Copyright 2023 HM Revenue & Customs
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

package pp.connectors

import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import pp.model.chargeref.ChargeRefNotificationDesRequest
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, StringContextOps, HttpReads}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DesConnector @Inject() (
    servicesConfig: ServicesConfig,
    httpClient:     HttpClientV2,
    configuration:  Configuration
)(implicit ec: ExecutionContext) {

  private val logger: Logger = Logger(this.getClass.getSimpleName)

  private val serviceURL: String = servicesConfig.baseUrl("des")
  private val authorizationToken: String = configuration.underlying.getString("microservice.services.des.authorizationToken")
  private val serviceEnvironment: String = configuration.underlying.getString("microservice.services.des.environment")
  private val chargerefUrl: String = configuration.underlying.getString("microservice.services.des.chargeref-url")

  implicit val readUnit: HttpReads[Unit] = UnitReadsThrowingException.readUnit

  private val desHeaderCarrier: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(s"Bearer $authorizationToken")))
    .withExtraHeaders("Environment" -> serviceEnvironment, "OriginatorID" -> "MDTP")

  val desHeaders: Seq[(String, String)] = Seq(
    "Authorization" -> s"Bearer $authorizationToken",
    "Environment" -> serviceEnvironment,
    "OriginatorID" -> "MDTP",
    "Content-Type" -> "application/json"
  )

  //todo remove this once we know what's going on
  private val regex = """[0-9a-zA-Z{À-˿'}\\ &`'^]{1,16}"""

  def sendCardPaymentsNotification(chargeRefNotificationDesRequest: ChargeRefNotificationDesRequest): Future[Unit] = {
    implicit val hc: HeaderCarrier = desHeaderCarrier

    val sendChargeRefUrl: String = s"$serviceURL$chargerefUrl"
    logger.debug(s"Calling des api 1541 for chargeRefNotificationDesRequest ${chargeRefNotificationDesRequest.toString}")
    logger.debug(s"""Calling des api 1541 with url $sendChargeRefUrl""")

    //todo remove this once we know what's going on
    logger.warn(
      s"""ChargeRefNotificationDesRequest: [
         |TaxType: ${chargeRefNotificationDesRequest.taxType},
         |Amount: ${chargeRefNotificationDesRequest.amountPaid.toString()},
         |ChargeRefLength: ${chargeRefNotificationDesRequest.chargeRefNumber.length.toString},
         |ref matches regex: ${chargeRefNotificationDesRequest.chargeRefNumber.matches(regex).toString}
         |]""".stripMargin
    )

    httpClient
      .post(url"$sendChargeRefUrl")
      .setHeader(desHeaders: _*)
      .withBody(Json.toJson(chargeRefNotificationDesRequest))
      .execute[Unit]
  }

}
