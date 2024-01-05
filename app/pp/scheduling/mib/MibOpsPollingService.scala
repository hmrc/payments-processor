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

package pp.scheduling.mib

import org.apache.pekko.actor.ActorSystem
import com.google.inject.{Inject, Singleton}
import pp.config.MibOpsQueueConfig
import pp.model.wokitems.MibOpsMyWorkItem
import pp.scheduling.PollingService
import pp.services.MibOpsService

import scala.concurrent.ExecutionContext

@Singleton
class MibOpsPollingService @Inject() (actorSystem: ActorSystem,
                                      queueConfig: MibOpsQueueConfig, workItemService: MibOpsService)(
    implicit
    ec: ExecutionContext) extends PollingService[MibOpsMyWorkItem](actorSystem, queueConfig, workItemService) {

  override def name: String = "MibOpsPollingService"

}
