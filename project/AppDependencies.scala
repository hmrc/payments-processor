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

import sbt.*

object AppDependencies {

  val bootstrapVersion = "9.13.0"
  val hmrcMongoVersion = "2.6.0"
  val enumeratumVersion = "1.9.0"

  val compile: Seq[ModuleID] = Seq(
    // format: OFF
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-work-item-repo-play-30"  % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"          % bootstrapVersion,
    "com.beachape"      %% "enumeratum-play"                    % enumeratumVersion,
    "com.beachape"      %% "enumeratum-play-json"               % enumeratumVersion,
    "org.typelevel"     %% "cats-core"                          % "2.13.0"
  // format: ON
  )

  val test: Seq[ModuleID] = Seq(
    // format: OFF
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"    % bootstrapVersion
  // format: ON
  ).map(_ % Test)
}
