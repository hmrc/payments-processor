import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import sbt.Tests.{Group, SubProcess}

val appName = "payments-processor"

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] =
  tests map { test =>
    Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }

scalaVersion := "2.12.12"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    majorVersion := 0,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test ++ AppDependencies.itTest
  )
  .settings(ScalariformSettings())
  .settings(
    unmanagedResourceDirectories in Compile += baseDirectory.value / "resources"
  )
  .settings(WartRemoverSettings.wartRemoverError)
  .settings(WartRemoverSettings.wartRemoverWarning)
  .settings(wartremoverErrors in(Test, compile) --= Seq(Wart.Any, Wart.Equals, Wart.Null, Wart.NonUnitStatements, Wart.PublicInference))
  .settings(wartremoverExcluded ++=
    routes.in(Compile).value ++
      (baseDirectory.value / "it").get ++
      (baseDirectory.value / "test").get ++
      Seq(sourceManaged.value / "main" / "sbt-buildinfo" / "BuildInfo.scala"))
  .settings(ScalariformSettings())
  .settings(
    unmanagedSourceDirectories in Test := Seq(baseDirectory.value / "test", baseDirectory.value / "test-common")
  )
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
  .settings(
    Keys.fork in IntegrationTest := true,
    Defaults.itSettings,
    resolvers += Resolver.jcenterRepo,
    unmanagedSourceDirectories in IntegrationTest += baseDirectory(_ / "it").value,
    unmanagedSourceDirectories in IntegrationTest += baseDirectory(_ / "test-common").value,
    parallelExecution in IntegrationTest := false,
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value)
  )
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(PlayKeys.playDefaultPort := 9211)
  .settings(
    routesImport ++= Seq(
       "pp.model._"
    ))
  .settings(
    scalacOptions ++= Seq(
      "-Ywarn-unused:-imports,-patvars,-privates,-locals,-explicits,-implicits,_",
      "-Xfatal-warnings",
      "-Xlint:-missing-interpolator,_",
      "-Yno-adapted-args",
      "-Ywarn-value-discard",
      "-Ywarn-dead-code",
      "-deprecation",
      "-feature",
      "-unchecked",
      "-language:implicitConversions",
      "-language:reflectiveCalls",
      "-Ypartial-unification" //required by cats
    )
  )
  .settings(scalacOptions in Compile -= "utf8")
