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

package pp.services.pngr

import java.time.{Clock, LocalDateTime, ZoneId}

import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.Logger
import play.api.mvc.Results
import pp.config.PngrQueueConfig
import pp.connectors.pngr.PngrConnector
import pp.model.pngr.{PngrStatusUpdateRequest, PngrWorkItem}
import pp.model.{Origins, TaxTypes}
import pp.scheduling.pngr.PngrMongoRepo
import pp.services.WorkItemService
import uk.gov.hmrc.http.{BadGatewayException, BadRequestException, UpstreamErrorResponse}
import uk.gov.hmrc.workitem.{Failed, ToDo, WorkItem}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PngrService @Inject()(
                             pngrMongoRepo: PngrMongoRepo,
                             queueConfig: PngrQueueConfig,
                             pngrConnector: PngrConnector,
                             clock: Clock,
                           )(implicit executionContext: ExecutionContext) extends WorkItemService[PngrWorkItem] with Results {

  private val logger: Logger = Logger(this.getClass.getSimpleName)

  //Need to have an implementation of these twp.  Nuts and Bolts of getting items from the queue ... needs to satisfy interface WorkItemService

  def retrieveWorkItems: Future[Seq[WorkItem[PngrWorkItem]]] = {

    @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
    def sendNotificationIfFound(count: Int, sentWorkItems: Seq[WorkItem[PngrWorkItem]]): Future[Seq[WorkItem[PngrWorkItem]]] = {

      def retrieveWorkItem(count: Int): Future[Option[WorkItem[PngrWorkItem]]] = {
        if (count == queueConfig.pollLimit) Future successful None
        else pngrMongoRepo.pullOutstanding
      }

      retrieveWorkItem(count).flatMap {
        case None => Future successful sentWorkItems
        case Some(workItem) =>
          processThenMarkAsComplete(sentWorkItems, workItem).flatMap { workItems =>
            sendNotificationIfFound(count + 1, workItems)
          }
      }
    }

    sendNotificationIfFound(0, Seq.empty)
  }

  def processThenMarkAsComplete(acc: Seq[WorkItem[PngrWorkItem]], workItem: WorkItem[PngrWorkItem]): Future[Seq[WorkItem[PngrWorkItem]]] = {
    logger.debug("inside processThenMarkAsComplete")
    sendWorkItemToPngr(workItem)
      .map(_ => pngrMongoRepo.complete(workItem.id))
      .map(_ => acc :+ workItem)
      .recoverWith {
        case _ =>
          pngrMongoRepo.markAs(workItem.id, Failed).map(_ => acc)
      }
  }

  //These are all specific to pngr processing

  private def sendWorkItemToPngr(pngrWorkItem: WorkItem[PngrWorkItem]) = {

    logger.debug("inside sendWorkItemToPngr")
    val statusUpdate = PngrStatusUpdateRequest(pngrWorkItem.item.reference, pngrWorkItem.item.status)
    for {
      _ <- pngrConnector.updateWithStatus(statusUpdate)
    } yield ()

  }



   def sendPngrToWorkItemRepo(pngrStatusUpdate: PngrStatusUpdateRequest): Future[WorkItem[PngrWorkItem]] = {
    logger.debug("inside sendCardPaymentsNotificationAsync")
    val time = LocalDateTime.now(clock)

    val jodaLocalDateTime = new DateTime(time.atZone(ZoneId.systemDefault).toInstant.toEpochMilli)
    val workItem = PngrWorkItem(time, TaxTypes.pngr, Origins.PCI_PAL,
      pngrStatusUpdate.reference, pngrStatusUpdate.status)

    pngrMongoRepo.pushNew(workItem, jodaLocalDateTime)

  }


}
