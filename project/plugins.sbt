resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
//resolvers += Resolver.url("HMRC Sbt Plugin Releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(
//  Resolver.ivyStylePatterns)
resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

resolvers += "HMRC Releases" at "https://dl.bintray.com/hmrc/releases"
resolvers += Resolver.typesafeRepo("releases")
resolvers += "hmrc-releases" at "https://artefacts.tax.services.gov.uk/artifactory/hmrc-releases/"
//resolvers += "HMRC-open-artefacts-maven2" at "https://open.artefacts.tax.service.gov.uk/maven2"

addSbtPlugin("uk.gov.hmrc"       %  "sbt-git-versioning"     % "2.2.0")
addSbtPlugin("uk.gov.hmrc"       %  "sbt-auto-build"         % "2.15.0")
addSbtPlugin("uk.gov.hmrc"       %  "sbt-distributables"     % "2.1.0")
addSbtPlugin("uk.gov.hmrc"       %  "sbt-bobby"              % "3.4.0")

addSbtPlugin("com.typesafe.play" %  "sbt-plugin"             % "2.8.8")
addSbtPlugin("org.wartremover"   %  "sbt-wartremover"        % "2.4.13")
addSbtPlugin("org.scoverage"     %  "sbt-scoverage"          % "1.6.1")
addSbtPlugin("org.scalariform"   %  "sbt-scalariform"        % "1.8.3")
addSbtPlugin("org.scalastyle"    %% "scalastyle-sbt-plugin"  % "1.0.0")
addSbtPlugin("net.virtual-void"  %  "sbt-dependency-graph"   % "0.10.0-RC1")
addSbtPlugin("com.timushev.sbt"  %  "sbt-updates"            % "0.5.1")

