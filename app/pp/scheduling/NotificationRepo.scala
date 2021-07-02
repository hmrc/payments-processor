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

package pp.scheduling

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

import java.time.Clock

import org.joda.time.{DateTime, Duration}
import play.api.Configuration
import play.api.libs.json.{JsObject, Json, OFormat}
import play.modules.reactivemongo.ReactiveMongoComponent
import pp.config.QueueConfig
import pp.scheduling.DateTimeHelpers._
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.play.json.ImplicitBSONHandlers._
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.workitem._

import scala.concurrent.{ExecutionContext, Future}

abstract class NotificationRepo[A](
    reactiveMongoComponent: ReactiveMongoComponent,
    configuration:          Configuration,
    clock:                  Clock,
    queueConfig:            QueueConfig)
  (implicit ec: ExecutionContext, format: OFormat[A], mfItem: Manifest[A])
  extends WorkItemRepository[A, BSONObjectID](
    collectionName = queueConfig.collectionName,
    mongo          = reactiveMongoComponent.mongoConnector.db,
    itemFormat     = WorkItem.workItemMongoFormat[A],
    configuration.underlying) {

  lazy val retryIntervalMillis: Long = configuration.getMillis(inProgressRetryAfterProperty)
  override lazy val inProgressRetryAfter: Duration = Duration.millis(retryIntervalMillis)
  private lazy val ttlInSeconds = {
    queueConfig.ttl.getSeconds
  }
  override val inProgressRetryAfterProperty: String = queueConfig.retryAfterProperty

  override def indexes: Seq[Index] = super.indexes ++ Seq(
    Index(key     = Seq("receivedAt" -> IndexType.Ascending), name = Some("receivedAtTime"), options = BSONDocument("expireAfterSeconds" -> ttlInSeconds)))

  def workItemFields: WorkItemFieldNames = new WorkItemFieldNames {
    val receivedAt = "receivedAt"
    val updatedAt = "updatedAt"
    val availableAt = "availableAt"
    val status = "status"
    val id = "_id"
    val failureCount = "failureCount"
  }

  def pullOutstanding(implicit ec: ExecutionContext): Future[Option[WorkItem[A]]] =
    super.pullOutstanding(now.minusMillis(retryIntervalMillis.toInt), now)

  def complete(id: BSONObjectID)(implicit ec: ExecutionContext): Future[Boolean] = {
    val selector = JsObject(
      Seq("_id" -> Json.toJson(id)(ReactiveMongoFormats.objectIdFormats), "status" -> Json.toJson(InProgress: ProcessingStatus)))
    collection.delete().one(selector).map(_.n > 0)
  }

  def failed(id: BSONObjectID)(implicit ec: ExecutionContext): Future[Boolean] = {
    markAs(id, Failed, Some(now.plusMillis(retryIntervalMillis.toInt)))
  }

  override def now: DateTime = clock.nowAsJoda

}

