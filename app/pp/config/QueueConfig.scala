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

package pp.config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

@Singleton
class QueueConfig @Inject() (configuration: Configuration) {

  val queueEnabled: Boolean = configuration.underlying.getBoolean("queue.enabled")

  val pollerInitialDelay: FiniteDuration = FiniteDuration(configuration.underlying.getDuration("poller.initialDelay").toNanos, TimeUnit.NANOSECONDS)

  val pollerInterval: FiniteDuration = FiniteDuration(configuration.underlying.getDuration("poller.interval").toNanos, TimeUnit.NANOSECONDS)

  val polleraEnabled: Boolean = configuration.underlying.getBoolean("poller.enabled")
}

