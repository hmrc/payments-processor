resolvers += Resolver.url("HMRC Sbt Plugin Releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(
  Resolver.ivyStylePatterns)

resolvers += "HMRC Releases" at "https://dl.bintray.com/hmrc/releases"
resolvers += Resolver.typesafeRepo("releases")
resolvers += "hmrc-releases" at "https://artefacts.tax.services.gov.uk/artifactory/hmrc-releases/"

addSbtPlugin("uk.gov.hmrc"       %  "sbt-artifactory"        % "1.13.0")
addSbtPlugin("uk.gov.hmrc"       %  "sbt-git-versioning"     % "2.2.0")
addSbtPlugin("uk.gov.hmrc"       %  "sbt-auto-build"         % "2.13.0")
addSbtPlugin("uk.gov.hmrc"       %  "sbt-distributables"     % "2.1.0")
addSbtPlugin("uk.gov.hmrc"       %  "sbt-bobby"              % "2.4.0")

addSbtPlugin("com.typesafe.play" %  "sbt-plugin"             % "2.7.9")
addSbtPlugin("org.wartremover"   %  "sbt-wartremover"        % "2.4.13")
addSbtPlugin("org.scoverage"     %  "sbt-scoverage"          % "1.6.1")
addSbtPlugin("org.scalariform"   %  "sbt-scalariform"        % "1.8.3")
addSbtPlugin("org.scalastyle"    %% "scalastyle-sbt-plugin"  % "1.0.0")
addSbtPlugin("net.virtual-void"  %  "sbt-dependency-graph"   % "0.10.0-RC1")
addSbtPlugin("com.timushev.sbt"  %  "sbt-updates"            % "0.5.1")
