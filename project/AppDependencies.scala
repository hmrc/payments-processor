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

  val bootstrapVersion = "7.19.0"
  val hmrcMongoVersion = "1.3.0"
  val enumeratumVersion = "1.7.0"

  val scalaTestVersion = "3.2.16"
  val pegdownVersion = "1.6.0"
  val scalaTestPlusPlayVersion = "5.1.0"
  val wiremockJre8Version = "2.27.2"
  val flexmarkAllVersion = "0.62.2"

  val compile: Seq[ModuleID] = Seq(
    // format: OFF
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-work-item-repo-play-28"  % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "play-hmrc-api"                      % "7.2.0-play-28",
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28"          % bootstrapVersion,
    "com.beachape"      %% "enumeratum"                         % enumeratumVersion,
    "com.beachape"      %% "enumeratum-play-json"               % enumeratumVersion,
    "org.typelevel"     %% "cats-core"                          % "2.9.0"
  // format: ON
  )

  val test: Seq[ModuleID] = Seq(
    // format: OFF
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"    % bootstrapVersion,
    "org.scalatest"          %% "scalatest"                 % scalaTestVersion,
    "com.typesafe.play"      %% "play-test"                 % PlayVersion.current,
    "org.pegdown"            %  "pegdown"                   % pegdownVersion,
    "org.scalatestplus.play" %% "scalatestplus-play"        % scalaTestPlusPlayVersion,
    "com.github.tomakehurst" %  "wiremock-jre8"             % wiremockJre8Version,
    "com.vladsch.flexmark"   %  "flexmark-all"              % flexmarkAllVersion //required by scalatest, see: https://github.com/scalatest/scalatest/issues/1736
  // format: ON
  ).map(_ % Test)

  val itTest: Seq[ModuleID] = Seq(
    // format: OFF
    "uk.gov.hmrc"            %% "bootstrap-backend-play-28" % bootstrapVersion,//todo jake why is this like it is, update it
    "org.scalatest"          %% "scalatest"                 % scalaTestVersion,
    "com.typesafe.play"      %% "play-test"                 % PlayVersion.current,
    "org.pegdown"            %  "pegdown"                   % pegdownVersion,
    "org.scalatestplus.play" %% "scalatestplus-play"        % scalaTestPlusPlayVersion,
    "com.github.tomakehurst" %  "wiremock-jre8"             % wiremockJre8Version,
    "com.vladsch.flexmark"   %  "flexmark-all"              % flexmarkAllVersion //required by scalatest, see: https://github.com/scalatest/scalatest/issues/1736
  // format: ON
  ).map(_ % "it")
}
