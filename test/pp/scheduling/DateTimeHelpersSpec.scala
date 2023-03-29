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

import java.time.ZoneId.systemDefault
import java.time.{Clock, Instant, ZoneId}
import java.util.TimeZone

import org.joda.time.DateTime
import org.joda.time.DateTimeZone.forTimeZone
import pp.scheduling.DateTimeHelpers._
import support.UnitSpec

class DateTimeHelpersSpec extends UnitSpec {
  private val now: Instant = Instant.now
  private val zoneId = systemDefault()
  private val zone = TimeZone.getTimeZone(zoneId)
  private val stoppedClock = new StoppedClock(now, zoneId)

  "nowAsJoda should return a joda time representation of the clock time" in {
    stoppedClock.nowAsJoda shouldBe new DateTime(now.toEpochMilli, forTimeZone(zone))
  }
}

private class StoppedClock(time: Instant, zoneId: ZoneId) extends Clock {
  override def getZone: ZoneId = zoneId
  override def withZone(zone: ZoneId): Clock = this
  override def instant(): Instant = time
}
