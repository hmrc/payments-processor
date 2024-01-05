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

package pp.scheduling.pngrs

import org.apache.pekko.actor.ActorSystem
import com.google.inject.{Inject, Singleton}
import pp.config.PngrsQueueConfig
import pp.model.wokitems.PngrMyWorkItem
import pp.scheduling.PollingService
import pp.services.PngrService

import scala.concurrent.ExecutionContext

@Singleton
class PngrPollingService @Inject() (actorSystem: ActorSystem,
                                    queueConfig: PngrsQueueConfig, workItemService: PngrService)(
    implicit
    ec: ExecutionContext) extends PollingService[PngrMyWorkItem](actorSystem, queueConfig, workItemService) {

  override def name: String = "PngrPollingService"

}
