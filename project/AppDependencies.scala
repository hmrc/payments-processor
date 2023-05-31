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

import play.core.PlayVersion
import sbt._

object AppDependencies {

  val bootstrapVersion = "7.15.0"
  val hmrcMongoVersion = "1.1.0"
  val enumeratumVersion = "1.7.0"

  val compile: Seq[ModuleID] = Seq(
    // format: OFF
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-work-item-repo-play-28"  % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "play-hmrc-api"                      % "7.1.0-play-28",
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28"          % bootstrapVersion,
    "com.beachape"      %% "enumeratum"                         % enumeratumVersion,
    "com.beachape"      %% "enumeratum-play-json"               % enumeratumVersion
    // format: ON
  )

  val test: Seq[ModuleID] = Seq(
    // format: OFF
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"    % bootstrapVersion,
    "org.scalatest"          %% "scalatest"                 % "3.2.9",
    "com.typesafe.play"      %% "play-test"                 % PlayVersion.current,
    "org.pegdown"            %  "pegdown"                   % "1.6.0",
    "org.scalatestplus.play" %% "scalatestplus-play"        % "5.1.0",
    "com.github.tomakehurst" %  "wiremock-jre8"             % "2.27.2",
    "com.vladsch.flexmark"   %  "flexmark-all"              % "0.36.8" //required by scalatest, see: https://github.com/scalatest/scalatest/issues/1736
    // format: ON
  ).map(_ % Test)

  val itTest: Seq[ModuleID] = Seq(
    // format: OFF
    "uk.gov.hmrc"            %% "bootstrap-backend-play-28" % "5.4.0",
    "org.scalatest"          %% "scalatest"                 % "3.2.9",
    "com.typesafe.play"      %% "play-test"                 % PlayVersion.current,
    "org.pegdown"            %  "pegdown"                   % "1.6.0",
    "org.scalatestplus.play" %% "scalatestplus-play"        % "5.1.0",
    "com.github.tomakehurst" %  "wiremock-jre8"             % "2.27.2",
    "com.vladsch.flexmark"   %  "flexmark-all"              % "0.36.8" //required by scalatest, see: https://github.com/scalatest/scalatest/issues/1736
    // format: ON
  ).map(_ % "it")

}

