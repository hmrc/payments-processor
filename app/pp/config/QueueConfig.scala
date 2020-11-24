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

package pp.config
import java.time.Duration
import java.util.concurrent.TimeUnit

import play.api.Configuration

import scala.concurrent.duration.FiniteDuration

trait QueueConfig {

  val prefix: String

  val configuration: Configuration

  val collectionName: String

  lazy val queueEnabled: Boolean = configuration.underlying.getBoolean(s"$prefix.queue.enabled")

  lazy val pollLimit: Int = configuration.underlying.getInt(s"$prefix.poller.pollLimit")

  lazy val pollerInitialDelay: FiniteDuration = FiniteDuration(configuration.underlying.getDuration(s"$prefix.poller.initialDelay").toNanos, TimeUnit.NANOSECONDS)

  lazy val pollerInterval: FiniteDuration = FiniteDuration(configuration.underlying.getDuration(s"$prefix.poller.interval").toNanos, TimeUnit.NANOSECONDS)

  lazy val pollerEnabled: Boolean = configuration.underlying.getBoolean(s"$prefix.poller.enabled")

  lazy val retryAfter: String = configuration.underlying.getString(s"$prefix.queue.retryAfter")

  lazy val retryAfterProperty: String = s"$prefix.queue.retryAfter"

  lazy val ttl: Duration = configuration.underlying.getDuration(s"$prefix.queue.ttl")

  lazy val queueBufferError: Int = configuration.underlying.getInt(s"$prefix.queue.buffer.error")
  lazy val queueBufferWarning: Int = configuration.underlying.getInt(s"$prefix.queue.buffer.warning")

  lazy val ttlMinusBufferError: Duration = ttl.minusSeconds(queueBufferError)

  lazy val ttlMinusBufferWarning: Duration = ttl.minusSeconds(queueBufferWarning)
}
