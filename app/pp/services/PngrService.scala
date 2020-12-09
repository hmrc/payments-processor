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

package pp.services

import java.time.{Clock, LocalDateTime, ZoneId}

import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.Logger
import play.api.mvc.Results
import pp.config.PngrsQueueConfig
import pp.connectors.PngrConnector
import pp.model.pngrs.PngrStatusUpdateRequest
import pp.model.wokitems.PngrWorkItem
import pp.model.{Origins, TaxTypes, wokitems}
import pp.scheduling.pngrs.PngrMongoRepo
import uk.gov.hmrc.workitem.WorkItem

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PngrService @Inject()(
                             val repo: PngrMongoRepo,
                             val queueConfig: PngrsQueueConfig,
                             pngrConnector: PngrConnector,
                             val clock: Clock,
                           )(implicit val executionContext: ExecutionContext) extends WorkItemService[PngrWorkItem] with Results {

  val logger: Logger = Logger(this.getClass.getSimpleName)

  //These are all specific to pngr processing
  def sendWorkItem(workItem: WorkItem[PngrWorkItem]) : Future[Unit] = {

    logger.debug("inside sendWorkItemToPngr")
    val statusUpdate = PngrStatusUpdateRequest(workItem.item.reference, workItem.item.status)
    for {
      _ <- pngrConnector.updateWithStatus(statusUpdate)
    } yield ()

  }


   def sendPngrToWorkItemRepo(pngrStatusUpdate: PngrStatusUpdateRequest): Future[WorkItem[PngrWorkItem]] = {
    logger.debug("inside sendCardPaymentsNotificationAsync")
     val time = LocalDateTime.now(clock)
    val jodaLocalDateTime = new DateTime(time.atZone(ZoneId.systemDefault).toInstant.toEpochMilli)
    val workItem = wokitems.PngrWorkItem(time, availableUntil(time), TaxTypes.pngr, Origins.PCI_PAL,
      pngrStatusUpdate.reference, pngrStatusUpdate.status)
    repo.pushNew(workItem, jodaLocalDateTime)

  }



}
