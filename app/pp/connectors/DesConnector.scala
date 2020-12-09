/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}
import pp.model.chargeref.ChargeRefNotificationDesRequest
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.ExecutionContext

@Singleton
class DesConnector @Inject() (
    servicesConfig: ServicesConfig,
    httpClient:     HttpClient,
    configuration:  Configuration)
  (implicit ec: ExecutionContext) {

  private val logger: Logger = Logger(this.getClass.getSimpleName)

  private val serviceURL: String = servicesConfig.baseUrl("des")
  private val authorizationToken: String = configuration.underlying.getString("microservice.services.des.authorizationToken")
  private val serviceEnvironment: String = configuration.underlying.getString("microservice.services.des.environment")
  private val chargeref: String = configuration.underlying.getString("microservice.services.des.chargeref-url")

  implicit val readUnit: HttpReads[Unit] = UnitReadsThrowingException.readUnit

  private val desHeaderCarrier: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(s"Bearer $authorizationToken")))
    .withExtraHeaders("Environment" -> serviceEnvironment, "OriginatorID" -> "MDTP")

  def sendCardPaymentsNotification(chargeRefNotificationDesRequest: ChargeRefNotificationDesRequest) = {
    logger.debug(s"Calling des api 1541 for chargeRefNotificationDesRequest ${chargeRefNotificationDesRequest.toString}")
    implicit val hc: HeaderCarrier = desHeaderCarrier
    val sendChargeRefUrl: String = s"$serviceURL$chargeref"
    logger.debug(s"""Calling des api 1541 with url $sendChargeRefUrl""")

    httpClient.POST[ChargeRefNotificationDesRequest, Unit](sendChargeRefUrl, chargeRefNotificationDesRequest)

  }

}
