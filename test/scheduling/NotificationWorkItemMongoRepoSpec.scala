/*
 * Copyright 2019 HM Revenue & Customs
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

package scheduling

import java.time.{Clock, LocalDateTime}

import org.joda.time.DateTime
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import support.ItSpec
import uk.gov.hmrc.workitem.WorkItem

class NotificationWorkItemMongoRepoSpec extends ItSpec {

  //https://github.com/hmrc/customs-notification
  //https://github.com/hmrc/bank-account-reputation-dispatcher
  //https://github.com/hmrc/leak-detection

  private val clock: Clock = Clock.systemUTC()
  val jodaDateTime: DateTime = DateTime.now()
  val repo = injector.instanceOf[NotificationWorkItemMongoRepo]
  val item = NotificationWorkItem(BSONObjectID.generate, LocalDateTime.now(clock), "a value")

  override def beforeEach(): Unit = {
    val remove = repo.removeAll().futureValue
  }

  //val processingStatuses: Set[ProcessingStatus] = Set(ToDo, InProgress, Succeeded, Failed, PermanentlyFailed, Ignored, Duplicate, Deferred, Cancelled)

  "Count should be 0 with empty repo" in {
    collectionSize shouldBe 0
  }

  "ensure indexes are created" in {

    val remove = repo.drop.futureValue
    val ensure = repo.ensureIndexes.futureValue
    repo.collection.indexesManager.list().futureValue.size shouldBe 5
  }

  "a "in {
    val workItem = repo.pushNew(item, jodaDateTime).futureValue
    repo.markAs(workItem.id, uk.gov.hmrc.workitem.Failed).futureValue should be(true)
  }

  "be able to push a new request and reload a request" in {
    val workItem = repo.pushNew(item, jodaDateTime).futureValue
    val found = repo.findById(workItem.id).futureValue

    found match {
      case Some(x) => {
        x.status shouldBe uk.gov.hmrc.workitem.ToDo
        x.item.value shouldBe "a value"
      }
      case None => "failed" shouldBe "to find a value"
    }
  }

  "be able to pull a request" in {
    val workItem = repo.pushNew(item, jodaDateTime).futureValue
    val outstanding: Option[WorkItem[NotificationWorkItem]] = repo.pullOutstanding.futureValue
    outstanding match {
      case Some(x) => {
        x.status shouldBe uk.gov.hmrc.workitem.InProgress
        x.item.value shouldBe "a value"
      }
      case None => "failed" shouldBe "to find a value"
    }

    val outstanding2: Option[WorkItem[NotificationWorkItem]] = repo.pullOutstanding.futureValue
    outstanding2 match {
      case Some(x) => {
        "found" shouldBe "a value when we should not"
      }
      case None =>
    }
  }

  val statusList = Seq(uk.gov.hmrc.workitem.Failed, uk.gov.hmrc.workitem.InProgress,
                       uk.gov.hmrc.workitem.Duplicate, uk.gov.hmrc.workitem.Cancelled, uk.gov.hmrc.workitem.Ignored,
                       uk.gov.hmrc.workitem.Deferred, uk.gov.hmrc.workitem.PermanentlyFailed, uk.gov.hmrc.workitem.Succeeded)

  statusList.foreach(status =>
    s"Pull a request with a status of ${status.toString} should not find anything" in {
      val workItem = repo.pushNew(item, jodaDateTime).futureValue
      repo.markAs(workItem.id, status).futureValue should be(true)
      val outstanding: Option[WorkItem[NotificationWorkItem]] = repo.pullOutstanding.futureValue
      outstanding match {
        case Some(x) => {
          "found" shouldBe "a value when we should not"
        }
        case None =>
      }

    }
  )

  "complete and delete an in progress request" in {
    val workItem = repo.pushNew(item, jodaDateTime).futureValue
    repo.markAs(workItem.id, uk.gov.hmrc.workitem.InProgress).futureValue should be(true)
    repo.complete(workItem.id).futureValue should be(true)
    repo.findById(workItem.id).futureValue shouldBe None
  }

  "cannot complete a request if it is not in progress" in {
    val workItem = repo.pushNew(item, jodaDateTime).futureValue
    repo.complete(workItem.id).futureValue should be(false)
    val workItemUpdated = repo.findById(workItem.id).futureValue
    workItemUpdated match {
      case Some(x) => x.status shouldBe uk.gov.hmrc.workitem.ToDo
      case None    => "failed" shouldBe "to find a workitem"
    }
  }

  "Cannot complete a request if it cannot be found" in {
    repo.complete(BSONObjectID.generate).futureValue should be(false)
  }

  "fail a workitem and check count, should not be able to pull it until we have waited for the retryafter duration" in {
    val workItem = repo.pushNew(item, jodaDateTime).futureValue
    repo.failed(workItem.id).futureValue should be(true)
    val failed: Option[WorkItem[NotificationWorkItem]] = repo.findById(workItem.id).futureValue
    failed match {
      case Some(x) => {
        x.failureCount shouldBe 1
      }
      case None => "failed" shouldBe "to find a workitem"
    }
    repo.failed(workItem.id).futureValue should be(true)
    val failed2: Option[WorkItem[NotificationWorkItem]] = repo.findById(workItem.id).futureValue
    failed2 match {
      case Some(x) => {
        x.failureCount shouldBe 2
      }
      case None => "failed" shouldBe "to find a workitem"
    }

    val outstanding: Option[WorkItem[NotificationWorkItem]] = repo.pullOutstanding.futureValue
    outstanding match {
      case Some(x) => {
        "found" shouldBe "a value when we should not"
      }
      case None =>
    }
    //Sleep for 10 seconds which should be longer than the retryafter of 3 seconds
    Thread.sleep(10000)
    val outstanding2: Option[WorkItem[NotificationWorkItem]] = repo.pullOutstanding.futureValue
    outstanding2 match {
      case Some(x) => {
        x.item.value shouldBe "a value"
      }
      case None => "failed" shouldBe "to find a value"
    }

  }



  private def collectionSize: Int = {
    repo.count(Json.obj()).futureValue
  }

}
