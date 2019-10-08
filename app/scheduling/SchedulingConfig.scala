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

import java.util.concurrent.TimeUnit

import javax.inject.Inject
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.duration._

/**
 *
 * # run the retry poller
 * retry.poller.enabled = true
 * # poller delay
 * retry.poller.interval.milliseconds = 1000
 * # time to wait before a failed notification is processed again
 * retry.poller.retryAfterFailureInterval.seconds = 30
 * # time to wait before an notification stuck at in-progress status is processed again
 * retry.poller.inProgressRetryAfter.seconds = 30
 * # number of poller instances per application
 * retry.poller.instances = 1
 * # two weeks
 * ttlInSeconds = 1209600
 *
 */

case class AppConfig(
    appName: String
) {

  @Inject
  def this(config: Configuration, servicesConfig: ServicesConfig) = this(
    appName = servicesConfig.getString("appName")
  )

}

case class SchedulingConfig(
    ttlInSeconds: Int,
    retryAfter:   FiniteDuration
) {

  @Inject
  def this(config: Configuration, servicesConfig: ServicesConfig) = this(
    ttlInSeconds = servicesConfig.getInt("queue.ttlInSeconds"),
    retryAfter   = Duration(servicesConfig.getInt("queue.retryAfter"), TimeUnit.SECONDS)
  )

}
