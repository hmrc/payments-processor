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

package pp.scheduling

import java.time.Clock

import javax.inject.{Inject, Singleton}
import pp.model.ChargeRefNotificationWorkItem
import org.joda.time.{DateTime, Duration}
import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.play.json.ImplicitBSONHandlers._
import pp.scheduling.DateTimeHelpers._
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.workitem._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChargeRefNotificationMongoRepo @Inject() (
    reactiveMongoComponent: ReactiveMongoComponent,
    configuration:          Configuration,
    clock:                  Clock,
    servicesConfig:         ServicesConfig)
  (implicit ec: ExecutionContext)
  extends WorkItemRepository[ChargeRefNotificationWorkItem, BSONObjectID](
    collectionName = "notifications-work-item",
    mongo          = reactiveMongoComponent.mongoConnector.db,
    itemFormat     = ChargeRefNotificationWorkItem.workItemFormats,
    configuration.underlying) {

  lazy val retryIntervalMillis: Long = configuration
    .getMillis(inProgressRetryAfterProperty)
  override lazy val inProgressRetryAfter: Duration = Duration.millis(retryIntervalMillis)
  override val inProgressRetryAfterProperty: String = "queue.retryAfter"
  private val ttlInSeconds = servicesConfig.getInt("queue.ttlInSeconds")

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

  def pullOutstanding(implicit ec: ExecutionContext): Future[Option[WorkItem[ChargeRefNotificationWorkItem]]] =
    super.pullOutstanding(now.minusMillis(retryIntervalMillis.toInt), now)

  def complete(id: BSONObjectID)(implicit ec: ExecutionContext): Future[Boolean] = {
    val selector = JsObject(
      Seq("_id" -> Json.toJson(id)(ReactiveMongoFormats.objectIdFormats), "status" -> Json.toJson(InProgress)))
    collection.delete().one(selector).map(_.n > 0)
  }

  def failed(id: BSONObjectID)(implicit ec: ExecutionContext): Future[Boolean] = {
    markAs(id, uk.gov.hmrc.workitem.Failed, Some(now.plusMillis(retryIntervalMillis.toInt)))
  }

  override def now: DateTime = clock.nowAsJoda

}

