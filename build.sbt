import uk.gov.hmrc.DefaultBuildSettings

val appName = "payments-processor"

ThisBuild / majorVersion := 1
ThisBuild / scalaVersion := "2.13.16"

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
  .settings(ScalariformSettings.scalariformSettings *)
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
  .settings(resolvers += Resolver.jcenterRepo)


lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.test)

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
