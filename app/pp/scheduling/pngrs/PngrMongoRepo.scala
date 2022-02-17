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

package pp.scheduling.pngrs

import java.time.Clock

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.modules.reactivemongo.ReactiveMongoComponent
import pp.config.PngrsQueueConfig
import pp.model.wokitems.PngrWorkItem
import pp.scheduling.NotificationRepo

import scala.concurrent.ExecutionContext

@Singleton
class PngrMongoRepo @Inject() (
    reactiveMongoComponent: ReactiveMongoComponent,
    configuration:          Configuration,
    clock:                  Clock,
    queueConfig:            PngrsQueueConfig)(implicit ec: ExecutionContext)
  extends NotificationRepo[PngrWorkItem](reactiveMongoComponent, configuration, clock, queueConfig)

