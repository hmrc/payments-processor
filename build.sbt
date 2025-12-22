import uk.gov.hmrc.DefaultBuildSettings

val appName = "payments-processor"

ThisBuild / majorVersion := 1
ThisBuild / scalaVersion := "3.3.7"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    majorVersion := 0,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
    Compile / scalacOptions -= "utf8"
  )
  // compiler flags, linting/code quality tools
  .settings(scalacOptions ++= scalaCompilerOptions)
  .settings(scalafmtOnCompile := true)
  .settings(WartRemoverSettings.wartRemoverSettings *)
  .settings(SbtUpdatesSettings.sbtUpdatesSettings *)
  .settings(ScoverageSettings.scoverageSettings *)
  //
  // play related settings
  .settings(
    PlayKeys.playDefaultPort := 9211,
    routesImport ++= Seq("pp.model._")
  )
  //
  // test related settings
  .settings(Test / unmanagedSourceDirectories := Seq(baseDirectory.value / "test", baseDirectory.value / "test-common"))

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.test)

lazy val scalaCompilerOptions = Seq(
  "-language:implicitConversions",
  "-language:reflectiveCalls",
  "-language:strictEquality",
  "-Xfatal-warnings",
  "-Wunused:implicits",
  "-Wunused:imports",
  "-Wunused:locals",
  "-Wunused:params",
  "-Wunused:patvars",
  "-Wunused:privates",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Wconf:src=routes/.*:s"
)
