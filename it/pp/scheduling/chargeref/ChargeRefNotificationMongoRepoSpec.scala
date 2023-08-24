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

package pp.scheduling.chargeref

import org.bson.types.ObjectId
import pp.model.TaxTypes
import pp.model.wokitems.ChargeRefNotificationMyWorkItem
import support.{ItSpec, PaymentsProcessData}
import uk.gov.hmrc.mongo.workitem._

class ChargeRefNotificationMongoRepoSpec extends ItSpec {

  private lazy val repo = injector.instanceOf[ChargeRefNotificationMongoRepo]
  private val dateTime = repo.now()

  override def beforeEach(): Unit = {
    val _ = repo.removeAll().futureValue
    super.beforeEach()
  }

  "Count should be 0 with empty repo" in {
    collectionSize shouldBe 0
  }

  "ensure indexes are created" in {
    repo.removeAll().futureValue
    repo.ensureIndexes().futureValue
    repo.collection.listIndexes().toFuture().futureValue.size shouldBe 5
  }

  "be able to push a new request and reload a request" in {
    val workItem = repo.pushNew(PaymentsProcessData.chargeRefNotificationWorkItem, dateTime).futureValue
    val found = repo.findById(workItem.id).futureValue

    found match {
      case Some(x) =>
        x.status shouldBe ProcessingStatus.ToDo
        x.item.chargeRefNumber shouldBe PaymentsProcessData.chargeReferenceNumber
        x.item.taxType shouldBe TaxTypes.p800.entryName
      case None => "failed" shouldBe "to find a value"
    }
  }

  "be able to pull a request" in {
    val _ = repo.pushNew(PaymentsProcessData.chargeRefNotificationWorkItem, dateTime).futureValue
    val outstanding: Option[WorkItem[ChargeRefNotificationMyWorkItem]] = repo.pullOutstanding.futureValue
    outstanding match {
      case Some(x) =>
        x.status shouldBe ProcessingStatus.InProgress
        x.item.chargeRefNumber shouldBe PaymentsProcessData.chargeReferenceNumber
        x.item.taxType shouldBe TaxTypes.p800.entryName
      case None => "failed" shouldBe "to find a value"
    }

    val outstanding2: Option[WorkItem[ChargeRefNotificationMyWorkItem]] = repo.pullOutstanding.futureValue
    outstanding2 match {
      case Some(_) =>
        "found" shouldBe "a value when we should not"
      case None =>
    }
  }

  Seq[ProcessingStatus](
    ProcessingStatus.Failed,
    ProcessingStatus.InProgress,
    ProcessingStatus.Duplicate,
    ProcessingStatus.Cancelled,
    ProcessingStatus.Ignored,
    ProcessingStatus.Deferred,
    ProcessingStatus.PermanentlyFailed,
    ProcessingStatus.Succeeded).foreach { status =>
      s"Pull a request with a status of ${status.toString} should not find anything if we have not waited" in {
        val workItem = repo.pushNew(PaymentsProcessData.chargeRefNotificationWorkItem, dateTime).futureValue
        repo.markAs(workItem.id, status).futureValue should be(true)
        val outstanding: Option[WorkItem[ChargeRefNotificationMyWorkItem]] = repo.pullOutstanding.futureValue
        outstanding match {
          case Some(_) =>
            "found" shouldBe "a value when we should not"
          case None =>
        }
      }
    }

  s"Pull a request with a status of Failed should find something as we have waited" in {
    val workItem = repo.pushNew(PaymentsProcessData.chargeRefNotificationWorkItem, dateTime).futureValue
    repo.markAs(workItem.id, ProcessingStatus.Failed).futureValue should be(true)

    eventually {
      val outstanding: Option[WorkItem[ChargeRefNotificationMyWorkItem]] = repo.pullOutstanding.futureValue
      outstanding match {
        case Some(x) =>
          x.item.chargeRefNumber shouldBe PaymentsProcessData.chargeReferenceNumber
          x.item.taxType shouldBe TaxTypes.p800.entryName
        case None => "failed" shouldBe "to find a value"
      }
    }
  }

  Seq[ProcessingStatus](ProcessingStatus.Duplicate,
                        ProcessingStatus.Cancelled,
                        ProcessingStatus.Ignored,
                        ProcessingStatus.Deferred,
                        ProcessingStatus.PermanentlyFailed,
                        ProcessingStatus.Succeeded).foreach { status =>
      s"Pull a request with a status of ${status.toString} should not find anything, we have waited" in {
        val workItem = repo.pushNew(PaymentsProcessData.chargeRefNotificationWorkItem, dateTime).futureValue
        repo.markAs(workItem.id, status).futureValue should be(true)

        eventually {
          val outstanding: Option[WorkItem[ChargeRefNotificationMyWorkItem]] = repo.pullOutstanding.futureValue
          outstanding match {
            case Some(_) => "found" shouldBe "a value when we should not"
            case None    =>
          }
        }
      }
    }

  "complete and delete an in progress request" in {
    val workItem = repo.pushNew(PaymentsProcessData.chargeRefNotificationWorkItem, dateTime).futureValue
    repo.markAs(workItem.id, ProcessingStatus.InProgress).futureValue should be(true)
    repo.completeAndDelete(workItem.id).futureValue should be(true)
    repo.findById(workItem.id).futureValue shouldBe None
  }

  "cannot complete a request if it is not in progress" in {
    val workItem = repo.pushNew(PaymentsProcessData.chargeRefNotificationWorkItem, dateTime).futureValue
    repo.completeAndDelete(workItem.id).futureValue should be(false)
    val workItemUpdated = repo.findById(workItem.id).futureValue
    workItemUpdated match {
      case Some(x) => x.status shouldBe ProcessingStatus.ToDo
      case None    => "failed" shouldBe "to find a workitem"
    }
  }

  "Cannot complete a request if it cannot be found" in {
    repo.completeAndDelete(ObjectId.get()).futureValue should be(false)
  }

  "fail a workitem and check count, should not be able to pull it until we have waited for the retryafter duration" in {
    val workItem = repo.pushNew(PaymentsProcessData.chargeRefNotificationWorkItem, dateTime).futureValue
    repo.failed(workItem.id).futureValue should be(true)
    val failed: Option[WorkItem[ChargeRefNotificationMyWorkItem]] = repo.findById(workItem.id).futureValue
    failed match {
      case Some(x) => x.failureCount shouldBe 1
      case None    => "failed" shouldBe "to find a workitem"
    }
    repo.failed(workItem.id).futureValue should be(true)
    val failed2: Option[WorkItem[ChargeRefNotificationMyWorkItem]] = repo.findById(workItem.id).futureValue
    failed2 match {
      case Some(x) => x.failureCount shouldBe 2
      case None    => "failed" shouldBe "to find a workitem"
    }

    val outstanding: Option[WorkItem[ChargeRefNotificationMyWorkItem]] = repo.pullOutstanding.futureValue
    outstanding match {
      case Some(_) => "found" shouldBe "a value when we should not"
      case None    =>
    }

    eventually {
      val outstanding2: Option[WorkItem[ChargeRefNotificationMyWorkItem]] = repo.pullOutstanding.futureValue
      outstanding2 match {
        case Some(x) =>
          x.item.chargeRefNumber shouldBe PaymentsProcessData.chargeReferenceNumber
          x.item.taxType shouldBe TaxTypes.p800.entryName
          x.status shouldBe ProcessingStatus.InProgress
        case None => "failed" shouldBe "to find a value"
      }
    }
  }

  private def collectionSize: Long = {
    repo.countAll().futureValue
  }

}
