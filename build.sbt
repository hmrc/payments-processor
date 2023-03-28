import sbt.Tests.{Group, SubProcess}

val appName = "payments-processor"

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] =
  tests map { test =>
    Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }

scalaVersion := "2.13.10"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    majorVersion := 0,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test ++ AppDependencies.itTest
  )
  .settings(ScalariformSettings())
  .settings(
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources"
  )
  .settings(WartRemoverSettings.wartRemoverError)
  .settings(WartRemoverSettings.wartRemoverWarning)
  .settings(Test / compile / wartremoverErrors --= Seq(Wart.Any, Wart.Equals, Wart.Null, Wart.NonUnitStatements, Wart.PublicInference))
  .settings(wartremoverExcluded ++=
    (Compile / routes) .value ++
      (baseDirectory.value / "it").get ++
      (baseDirectory.value / "test").get ++
      Seq(sourceManaged.value / "main" / "sbt-buildinfo" / "BuildInfo.scala"))
  .settings(ScalariformSettings())
  .settings(
    Test / unmanagedSourceDirectories := Seq(baseDirectory.value / "test", baseDirectory.value / "test-common")
  )
  .configs(IntegrationTest)
  .settings(
    IntegrationTest / Keys.fork := true,
    Defaults.itSettings,
    resolvers += Resolver.jcenterRepo,
    IntegrationTest / unmanagedSourceDirectories += baseDirectory(_ / "it").value,
    IntegrationTest / unmanagedSourceDirectories += baseDirectory(_ / "test-common").value,
    IntegrationTest / parallelExecution := false,
    IntegrationTest / testGrouping := oneForkedJvmPerTest((IntegrationTest / definedTests).value)
  )
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(PlayKeys.playDefaultPort := 9211)
  .settings(
    routesImport ++= Seq(
       "pp.model._"
    ))
  .settings(
    scalacOptions ++= scalaCompilerOptions
  )
  .settings(Compile / scalacOptions -= "utf8")


lazy val scalaCompilerOptions: Seq[String] = Seq(
  "-Ywarn-unused:-imports,-patvars,-privates,-locals,-explicits,-implicits,_",
  "-Xfatal-warnings",
  "-Xlint:-missing-interpolator,_",
  "-Xlint:adapted-args",
  "-Ywarn-value-discard",
  "-Ywarn-dead-code",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions",
  "-language:reflectiveCalls",
  "-Wconf:cat=unused-imports&src=html/.*:s",
  "-Wconf:src=routes/.*:s"
)