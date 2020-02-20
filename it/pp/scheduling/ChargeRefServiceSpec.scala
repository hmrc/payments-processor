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

package pp.scheduling

import java.time.Clock

import com.github.tomakehurst.wiremock.client.WireMock
import play.api.libs.json.Json
import pp.config.QueueConfig
import pp.connectors.des.DesConnector
import pp.model.ChargeRefNotificationWorkItem
import pp.services.ChargeRefService
import support.PaymentsProcessData.chargeRefNotificationRequest
import support.{Des, ItSpec}
import uk.gov.hmrc.workitem.{InProgress, ToDo, WorkItem}

class ChargeRefServiceSpec extends ItSpec {
  private val pollLimit = 2

  override def configMap: Map[String, Any] = super.configMap.updated("poller.pollLimit", s"$pollLimit")

  private lazy val repo = injector.instanceOf[ChargeRefNotificationMongoRepo]
  private lazy val desConnector = injector.instanceOf[DesConnector]
  private lazy val queueConfig = injector.instanceOf[QueueConfig]

  private lazy val chargeRefService = new ChargeRefService(desConnector, repo, Clock.systemDefaultZone(), queueConfig)

  override def beforeEach(): Unit = {
    val _ = repo.removeAll().futureValue
    WireMock.reset()
  }

  protected def numberOfQueuedNotifications: Integer = repo.count(Json.obj()).futureValue

  "sendCardPaymentsNotificationToWorkItemRepo" should {
    "add a notification to the queue" in {
      numberOfQueuedNotifications shouldBe 0
      val workItem = chargeRefService.sendCardPaymentsNotificationToWorkItemRepo(chargeRefNotificationRequest).futureValue
      numberOfQueuedNotifications shouldBe 1

      workItem.item.taxType shouldBe chargeRefNotificationRequest.taxType
      workItem.item.chargeRefNumber shouldBe chargeRefNotificationRequest.chargeRefNumber
      workItem.item.amountPaid shouldBe chargeRefNotificationRequest.amountPaid
      workItem.item.origin shouldBe chargeRefNotificationRequest.origin
      workItem.status shouldBe ToDo
    }
  }

  "retrieveWorkItems" should {
    "send queued work items to des" when {
      "the des call succeeds" in {
        Des.cardPaymentsNotificationSucceeds()

        numberOfQueuedNotifications shouldBe 0
        chargeRefService.sendCardPaymentsNotificationToWorkItemRepo(chargeRefNotificationRequest).futureValue
        numberOfQueuedNotifications shouldBe 1

        val sentItems: Seq[WorkItem[ChargeRefNotificationWorkItem]] = chargeRefService.retrieveWorkItems.futureValue

        sentItems.size shouldBe 1
        sentItems.head.status shouldBe InProgress
        numberOfQueuedNotifications shouldBe 0
      }
    }

    "retain queued work items" when {
      "the retry des call fails" in {
        Des.cardPaymentsNotificationFailsWithAnInternalServerError()

        numberOfQueuedNotifications shouldBe 0
        chargeRefService.sendCardPaymentsNotificationToWorkItemRepo(chargeRefNotificationRequest).futureValue
        numberOfQueuedNotifications shouldBe 1

        chargeRefService.retrieveWorkItems.futureValue.isEmpty shouldBe true
        numberOfQueuedNotifications shouldBe 1
      }
    }

    "process a previously queued work item after the retry interval" when {
      "the first des retry call fails but the second succeeds" in {
        Des.cardPaymentsNotificationFailsWithAnInternalServerError(0, 0)
        Des.cardPaymentsNotificationSucceeds(0, 1)

        numberOfQueuedNotifications shouldBe 0
        chargeRefService.sendCardPaymentsNotificationToWorkItemRepo(chargeRefNotificationRequest).futureValue
        numberOfQueuedNotifications shouldBe 1

        chargeRefService.retrieveWorkItems.futureValue.isEmpty shouldBe true
        numberOfQueuedNotifications shouldBe 1

        eventually {
          val sentItems: Seq[WorkItem[ChargeRefNotificationWorkItem]] = chargeRefService.retrieveWorkItems.futureValue

          sentItems.size shouldBe 1
          sentItems.head.status shouldBe InProgress
          numberOfQueuedNotifications shouldBe 0
        }
      }
    }

    "process a number of queued work items up to the poll limit" in {
      Des.cardPaymentsNotificationSucceeds(0, 0)
      Des.cardPaymentsNotificationSucceeds(0, 1)
      Des.cardPaymentsNotificationSucceeds(0, 2)

      numberOfQueuedNotifications shouldBe 0

      chargeRefService.sendCardPaymentsNotificationToWorkItemRepo(chargeRefNotificationRequest).futureValue
      chargeRefService.sendCardPaymentsNotificationToWorkItemRepo(chargeRefNotificationRequest.copy(chargeRefNumber = "XQ002610015760")).futureValue
      chargeRefService.sendCardPaymentsNotificationToWorkItemRepo(chargeRefNotificationRequest.copy(chargeRefNumber = "XQ002610015761")).futureValue

      val expectedQueuedNotifications = 3

      numberOfQueuedNotifications shouldBe expectedQueuedNotifications
      numberOfQueuedNotifications > pollLimit shouldBe true

      eventually {
        val sentItems: Seq[WorkItem[ChargeRefNotificationWorkItem]] = chargeRefService.retrieveWorkItems.futureValue

        sentItems.size shouldBe pollLimit
        sentItems.map(_.status).toSet shouldBe Set(InProgress)
        numberOfQueuedNotifications shouldBe 1
      }
    }
  }
}
