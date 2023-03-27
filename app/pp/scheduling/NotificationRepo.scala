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

package pp.scheduling

import org.bson.types.ObjectId
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import play.api.Configuration
import play.api.libs.json.OFormat
import pp.config.QueueConfig
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.workitem.{ProcessingStatus, WorkItem, WorkItemFields, WorkItemRepository}

import java.time.{Clock, Duration, Instant}
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
abstract class NotificationRepo[A](
    mongoComponent: MongoComponent,
    configuration:  Configuration,
    clock:          Clock,
    queueConfig:    QueueConfig
)(implicit ec: ExecutionContext, format: OFormat[A], mfItem: Manifest[A]) extends WorkItemRepository[A](
    collectionName = queueConfig.collectionName,
    mongoComponent = mongoComponent,
    itemFormat     = format,
    extraIndexes   = NotificationRepo.indexes(queueConfig.ttl),
    workItemFields = WorkItemFields.default.copy(availableAt = "availableAt")
  ) {

  lazy val retryIntervalMillis: Long = configuration.getMillis(queueConfig.retryAfterProperty)
  override lazy val inProgressRetryAfter: Duration = Duration.ofMillis(retryIntervalMillis)

  def pullOutstanding(implicit ec: ExecutionContext): Future[Option[WorkItem[A]]] =
    super.pullOutstanding(now().minusMillis(retryIntervalMillis.toInt), now())

  def failed(id: ObjectId)(implicit ec: ExecutionContext): Future[Boolean] = {
    markAs(id, ProcessingStatus.Failed)
  }

  override def now(): Instant =
    Instant.now()

  def findAll(): Future[List[WorkItem[A]]] =
    collection.find()
      .toFuture()
      .map(_.toList)

  def removeAll(): Future[Unit] = collection
    .drop()
    .toFuture()
    .map(_ => ())

  def countAll(): Future[Long] = collection
    .countDocuments()
    .toFuture()

}

object NotificationRepo {
  def indexes(ttlInSeconds: FiniteDuration): Seq[IndexModel] = Seq(
    IndexModel(
      keys         = Indexes.ascending("receivedAtTime"),
      indexOptions = IndexOptions().name("receivedAtTime").expireAfter(ttlInSeconds.toSeconds, TimeUnit.SECONDS)
    )
  )
}
