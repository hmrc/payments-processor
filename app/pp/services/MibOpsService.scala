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

package pp.services

import java.time.{Clock, LocalDateTime, ZoneId}

import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.Logger
import play.api.mvc.Results
import pp.config.MibOpsQueueConfig
import pp.connectors.MibConnector
import pp.model.wokitems.MibOpsWorkItem
import pp.model.{Origins, TaxTypes}
import pp.scheduling.mib.MibOpsMongoRepo
import uk.gov.hmrc.workitem.WorkItem

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MibOpsService @Inject() (
    val repo:        MibOpsMongoRepo,
    val queueConfig: MibOpsQueueConfig,
    mibConnector:    MibConnector,
    val clock:       Clock
)(implicit val executionContext: ExecutionContext) extends WorkItemService[MibOpsWorkItem] with Results {

  val logger: Logger = Logger(this.getClass.getSimpleName)

  //These are all specific to pngr processing
  def sendWorkItem(workItem: WorkItem[MibOpsWorkItem]): Future[Unit] = {

    logger.debug("inside sendWorkItemToMibOps")
    for {
      _ <- mibConnector.paymentCallback(workItem.item.reference)
    } yield ()

  }

  def sendMibOpsToWorkItemRepo(reference: String): Future[WorkItem[MibOpsWorkItem]] = {
    logger.debug("inside sendCardPaymentsNotificationAsync")
    val time = LocalDateTime.now(clock)
    val jodaLocalDateTime = new DateTime(time.atZone(ZoneId.systemDefault).toInstant.toEpochMilli)
    val workItem = MibOpsWorkItem(time, availableUntil(time), TaxTypes.mib, Origins.OPS,
                                  reference)
    repo.pushNew(workItem, jodaLocalDateTime)

  }

}
