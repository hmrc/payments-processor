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

import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(

    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"                 % "0.73.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-work-item-repo-play-28"  % "0.73.0",
    "uk.gov.hmrc"       %% "play-hmrc-api"                      % "6.4.0-play-28",
    "uk.gov.hmrc"       %% "play-scheduling-play-27"            % "8.0.0",
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28"          % "5.4.0",
    "com.beachape"      %% "enumeratum"                         % "1.7.0",
    "com.beachape"      %% "enumeratum-play-json"               % "1.7.0"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-backend-play-28" % "5.4.0"  % Test,
    "org.scalatest"          %% "scalatest"                 % "3.2.9"  % Test,
    "com.typesafe.play"      %% "play-test"                 % current  % Test,
    "org.pegdown"            %  "pegdown"                   % "1.6.0"  % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"        % "5.1.0"  % Test,
    "com.github.tomakehurst" %  "wiremock-jre8"             % "2.27.2" % Test,
    "com.vladsch.flexmark"   %  "flexmark-all"              % "0.36.8" % Test //required by scalatest, see: https://github.com/scalatest/scalatest/issues/1736
  )

  val itTest = Seq(
    "uk.gov.hmrc"            %% "bootstrap-backend-play-28" % "5.4.0"  % "it",
    "org.scalatest"          %% "scalatest"                 % "3.2.9"  % "it",
    "com.typesafe.play"      %% "play-test"                 % current  % "it",
    "org.pegdown"            %  "pegdown"                   % "1.6.0"  % "it",
    "org.scalatestplus.play" %% "scalatestplus-play"        % "3.1.2"  % "it",
    "com.github.tomakehurst" %  "wiremock-jre8"             % "2.27.2" % "it",
    "com.vladsch.flexmark"   %  "flexmark-all"              % "0.36.8" % "it" //required by scalatest, see: https://github.com/scalatest/scalatest/issues/1736
  )

}

