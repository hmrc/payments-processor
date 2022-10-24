/*
 * Copyright 2022 HM Revenue & Customs
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

package pp.scheduling.cds

import play.api.Configuration
import pp.config.CdsOpsQueueConfig
import pp.model.wokitems.CdsOpsMyWorkItem
import pp.scheduling.NotificationRepo
import uk.gov.hmrc.mongo.MongoComponent

import java.time.Clock
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CdsOpsMongoRepo @Inject() (
    mongoComponent: MongoComponent,
    configuration:  Configuration,
    clock:          Clock,
    queueConfig:    CdsOpsQueueConfig)
  (implicit ec: ExecutionContext)
  extends NotificationRepo[CdsOpsMyWorkItem](
    mongoComponent = mongoComponent,
    configuration  = configuration,
    clock          = clock,
    queueConfig    = queueConfig)
