resolvers += Resolver.url("HMRC Sbt Plugin Releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(
  Resolver.ivyStylePatterns)

resolvers += "HMRC Releases" at "https://dl.bintray.com/hmrc/releases"
resolvers += Resolver.typesafeRepo("releases")


addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "1.16.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "1.19.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-artifactory" % "0.19.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-distributables" % "1.6.0")
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.20")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.5")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.3.1")
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")