package pp.model.wokitems

import java.time.LocalDateTime

import play.api.libs.json.{Json, OFormat}
import pp.model.cds.NotifyImmediatePaymentRequest
import pp.model.{Origin, TaxType, WorkItemFields}

case class CdsOpsWorkItem(
  createdOn:      LocalDateTime,
  availableUntil: LocalDateTime,
  taxType:        TaxType,
  origin:         Origin,
  reference:      String,
  notifyImmediatePaymentRequest: NotifyImmediatePaymentRequest) extends WorkItemFields {

  override def toString: String =
    s"CdsWorkItem for chargeReference $reference ... {createdOn: $createdOn, availableUntil: $availableUntil, taxType: $taxType, origin: $origin, reference: $reference}"
}

object CdsOpsWorkItem {
  implicit val format: OFormat[CdsOpsWorkItem] = Json.format[CdsOpsWorkItem]
}

