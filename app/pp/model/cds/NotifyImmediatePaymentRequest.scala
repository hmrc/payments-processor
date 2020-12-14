package pp.model.cds

import play.api.libs.json.{Json, OFormat}

final case class NotifyImmediatePaymentRequest(
                                                requestCommon: RequestCommon,
                                                requestDetail: RequestDetail
                                              )
final case class RequestCommon(receiptDate: String, acknowledgementReference: String, regime: String = "CDS", originatingSystem: String = "OPS")

final case class RequestDetail(paymentReference: String, amountPaid: String, unitType: String = "GBP", declarationID: String)

object NotifyImmediatePaymentRequest {
  implicit val format: OFormat[NotifyImmediatePaymentRequest] = Json.format[NotifyImmediatePaymentRequest]
}
