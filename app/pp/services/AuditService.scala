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

package pp.services

import com.google.inject.{Inject, Singleton}
import play.api.libs.json.{Json, Writes}
import pp.model.audit.{AuditDetail, PcipalNotificationAuditEvent}
import pp.model.pcipal.ChargeRefNotificationPcipalRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions.auditHeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import java.util.UUID
import scala.concurrent.ExecutionContext

@Singleton
class AuditService @Inject() (auditConnector: AuditConnector)(implicit executionContext: ExecutionContext) {

  private val auditSource: String = "payments-processor"

  private def audit[A <: AuditDetail: Writes](a: A)(implicit hc: HeaderCarrier): Unit = {
    val _ = auditConnector.sendExtendedEvent(
      ExtendedDataEvent(
        auditSource = auditSource,
        auditType   = a.auditType,
        eventId     = UUID.randomUUID().toString,
        tags        = hc.toAuditTags(),
        detail      = Json.toJson(a)
      )
    )
  }

  def auditPcipalNotificationEvent(notificationPcipalRequest: ChargeRefNotificationPcipalRequest)(implicit headerCarrier: HeaderCarrier): Unit =
    audit(PcipalNotificationAuditEvent(notificationPcipalRequest))
}
