import sbt.Tests.{Group, SubProcess}

val appName = "payments-processor"

scalaVersion := "2.13.12"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(scalaVersion := "2.13.10")
  .settings(
    majorVersion := 0,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test ++ AppDependencies.itTest,
    libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
    Compile / scalacOptions -= "utf8"
  )
  // compiler flags, linting/code quality tools
  .settings(scalacOptions ++= scalaCompilerOptions)
  .settings(ScalariformSettings.scalariformSettings: _*)
  .settings(WartRemoverSettings.wartRemoverSettings: _*)
  .settings(SbtUpdatesSettings.sbtUpdatesSettings: _*)
  .settings(ScoverageSettings.scoverageSettings: _*)
  //
  // play related settings
  .settings(
    PlayKeys.playDefaultPort := 9211,
    routesImport ++= Seq("pp.model._")
  )
  //
  // test related settings
  .settings(Test / unmanagedSourceDirectories := Seq(baseDirectory.value / "test", baseDirectory.value / "test-common"))
  .settings(
    IntegrationTest / Keys.fork := true,
    Defaults.itSettings,
    IntegrationTest / unmanagedSourceDirectories += baseDirectory(_ / "it").value,
    IntegrationTest / unmanagedSourceDirectories += baseDirectory(_ / "test-common").value,
    IntegrationTest / parallelExecution := false,
    IntegrationTest / testGrouping := oneForkedJvmPerTest((IntegrationTest / definedTests).value)
  )
  .configs(IntegrationTest)
  .settings(resolvers += Resolver.jcenterRepo)

lazy val scalaCompilerOptions = Seq(
  "-Xfatal-warnings",
  "-Xlint:-missing-interpolator,_",
  "-Xlint:adapted-args",
  "-Ywarn-unused:implicits",
  "-Ywarn-unused:imports",
  "-Ywarn-unused:locals",
  "-Ywarn-unused:params",
  "-Ywarn-unused:patvars",
  "-Ywarn-unused:privates",
  "-Ywarn-value-discard",
  "-Ywarn-dead-code",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions",
  "-language:reflectiveCalls",
  // required in place of silencer plugin
  "-Wconf:cat=unused-imports&src=routes/.*:s",
  "-Wconf:src=routes/.*:s"
)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] = tests.map { test =>
  Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
}
