/*
 * Copyright 2021 HM Revenue & Customs
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

package pp.controllers

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import pp.model.{Item, ProcessingStatusOps, TaxType, TaxTypes}
import pp.scheduling.cds.CdsOpsMongoRepo
import pp.scheduling.chargeref.ChargeRefNotificationMongoRepo
import pp.scheduling.mib.MibOpsMongoRepo
import pp.scheduling.pngrs.PngrMongoRepo
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.ExecutionContext
@Singleton
class ReportingController @Inject() (
    cc:                             ControllerComponents,
    pngrMongoRepo:                  PngrMongoRepo,
    chargeRefNotificationMongoRepo: ChargeRefNotificationMongoRepo,
    mibOpsMongoRepo:                MibOpsMongoRepo,
    cdsOpsMongoRepo:                CdsOpsMongoRepo
)
  (implicit val executionContext: ExecutionContext) extends BackendController(cc) {

  val logger: Logger = Logger(this.getClass.getSimpleName)

  def count(taxType: TaxType, processingState: ProcessingStatusOps): Action[AnyContent] = Action.async { implicit request =>
    logger.debug("count")
    taxType match {

      case TaxTypes.pngr =>
        pngrMongoRepo.count(processingState.processingStatus).map(m => Ok(m.toString))
      case TaxTypes.p800 =>
        chargeRefNotificationMongoRepo.count(processingState.processingStatus).map(m => Ok(m.toString))
      case TaxTypes.mib =>
        mibOpsMongoRepo.count(processingState.processingStatus).map(m => Ok(m.toString))
      case TaxTypes.cds =>
        cdsOpsMongoRepo.count(processingState.processingStatus).map(m => Ok(m.toString))
      case _ => throw new RuntimeException(s"taxType $taxType not supported, processingState $processingState not supported")
    }
  }

  def getAll(taxType: TaxType): Action[AnyContent] = Action.async { implicit request =>
    logger.debug("count")
    taxType match {
      case TaxTypes.pngr =>
        for {
          m <- pngrMongoRepo.findAll()
          i = m.map(m2 => Item(m2.item.createdOn, m2.item.reference, m2.failureCount, m2.status.toString))
        } yield Ok(Json.toJson(i))
      case TaxTypes.p800 =>
        for {
          m <- chargeRefNotificationMongoRepo.findAll()
          i = m.map(m2 => Item(m2.item.createdOn, m2.item.chargeRefNumber, m2.failureCount, m2.status.toString))
        } yield Ok(Json.toJson(i))
      case TaxTypes.mib =>
        for {
          m <- mibOpsMongoRepo.findAll()
          i = m.map(m2 => Item(m2.item.createdOn, m2.item.reference, m2.failureCount, m2.status.toString))
        } yield Ok(Json.toJson(i))
      case TaxTypes.cds =>
        for {
          allRecords <- cdsOpsMongoRepo.findAll()
          allRecordsAsItems = allRecords.map(m2 => Item(m2.item.createdOn, m2.item.reference, m2.failureCount, m2.status.toString))
        } yield Ok(Json.toJson(allRecordsAsItems))
      case _ => throw new RuntimeException(s"taxType $taxType not supported")
    }
  }

}

