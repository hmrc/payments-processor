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

import java.time.{Clock, ZoneOffset}

import com.google.inject.{AbstractModule, Provides, Singleton}
import pp.scheduling.ChargeRefNotificationPoler

import play.api.inject.SimpleModule
import play.api.inject._

class Module() extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ChargeRefNotificationPoler]).asEagerSingleton
  }

  @Provides
  @Singleton
  def clock(): Clock = Clock.systemDefaultZone.withZone(ZoneOffset.UTC)

}

//class TasksModule extends SimpleModule(bind[ChargeRefNotificationPoler].toSelf.eagerly())
