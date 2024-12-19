resolvers += Resolver.url("HMRC Sbt Plugin Releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
resolvers += "HMRC Releases" at "https://dl.bintray.com/hmrc/releases"
resolvers += "hmrc-releases" at "https://artefacts.tax.services.gov.uk/artifactory/hmrc-releases/"
resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
resolvers += Resolver.typesafeRepo("releases")

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)

addSbtPlugin("uk.gov.hmrc"       %  "sbt-auto-build"         % "3.24.0")
addSbtPlugin("uk.gov.hmrc"       %  "sbt-distributables"     % "2.5.0")
addSbtPlugin("org.playframework" %  "sbt-plugin"             % "3.0.5")
addSbtPlugin("org.wartremover"   %  "sbt-wartremover"        % "3.1.6")
addSbtPlugin("org.scoverage"     %  "sbt-scoverage"          % "2.0.9")
addSbtPlugin("org.scalariform"   %  "sbt-scalariform"        % "1.8.3")
addSbtPlugin("org.scalastyle"    %% "scalastyle-sbt-plugin"  % "1.0.0")
addSbtPlugin("com.timushev.sbt"  %  "sbt-updates"            % "0.6.3")

addDependencyTreePlugin
