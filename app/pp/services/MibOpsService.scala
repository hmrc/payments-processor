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

import play.api.Logger
import play.api.mvc.Results
import pp.config.MibOpsQueueConfig
import pp.connectors.MibConnector
import pp.model.mods.ModsPaymentCallBackRequest
import pp.model.wokitems.MibOpsMyWorkItem
import pp.model.{Origins, TaxTypes}
import pp.scheduling.mib.MibOpsMongoRepo
import uk.gov.hmrc.mongo.workitem.WorkItem

import java.time.{Clock, LocalDateTime}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MibOpsService @Inject() (
    val repo:        MibOpsMongoRepo,
    val queueConfig: MibOpsQueueConfig,
    mibConnector:    MibConnector,
    val clock:       Clock
)(implicit val executionContext: ExecutionContext) extends WorkItemService[MibOpsMyWorkItem] with Results {

  val logger: Logger = Logger(this.getClass.getSimpleName)

  //These are all specific to mods processing
  def sendWorkItem(workItem: WorkItem[MibOpsMyWorkItem]): Future[Unit] = {

    logger.debug("inside sendWorkItemToMibOps")
    for {
      _ <- mibConnector.paymentCallback(workItem.item.modsPaymentCallBackRequest)
    } yield ()

  }

  def sendMibOpsToWorkItemRepo(modsPaymentCallBackRequest: ModsPaymentCallBackRequest): Future[WorkItem[MibOpsMyWorkItem]] = {
    logger.debug("inside sendCardPaymentsNotificationAsync")
    val time = LocalDateTime.now(clock)
    val localDateTime = repo.now()
    val workItem: MibOpsMyWorkItem = MibOpsMyWorkItem(
      createdOn                  = time,
      availableUntil             = availableUntil(time),
      taxType                    = TaxTypes.mib,
      origin                     = Origins.OPS,
      reference                  = modsPaymentCallBackRequest.chargeReference,
      modsPaymentCallBackRequest = modsPaymentCallBackRequest
    )
    repo.pushNew(workItem, localDateTime)

  }

}
