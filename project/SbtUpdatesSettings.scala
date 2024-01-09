import com.timushev.sbt.updates.UpdatesKeys.dependencyUpdates
import com.timushev.sbt.updates.UpdatesPlugin.autoImport.{dependencyUpdatesFailBuild, dependencyUpdatesFilter, moduleFilterRemoveValue}
import sbt.Keys._
import sbt._

object SbtUpdatesSettings {

  lazy val sbtUpdatesSettings = Seq(
    (Compile / compile) := ((Compile / compile) dependsOn dependencyUpdates).value,
    dependencyUpdatesFilter -= moduleFilter("org.scala-lang"),
    dependencyUpdatesFilter -= moduleFilter("org.playframework"),
    // later versions result in this error:
    // ---
    // java.lang.UnsupportedClassVersionError: com/vladsch/flexmark/util/ast/Node has been
    // compiled by a more recent version of the Java Runtime (class file version 55.0), this
    // version of the Java Runtime only recognizes class file versions up to 52.0
    // ---
    dependencyUpdatesFilter -= moduleFilter("com.vladsch.flexmark", "flexmark-all"),
    // I have had to add enumeratum to the ignore list, due to:
    // java.lang.NoSuchMethodError: 'scala.Option play.api.libs.json.JsBoolean$.unapply(play.api.libs.json.JsBoolean)'
    // error on 1.7.2
    dependencyUpdatesFilter -= moduleFilter("com.beachape", "enumeratum"),
    dependencyUpdatesFilter -= moduleFilter("com.beachape", "enumeratum-play-json"),
    // Not having this means we have to add a load of funky other dependency overrides due to jackson xml version conflicts
    // if you want that, implement what they've done here:
    // https://github.com/hmrc/benefits/blob/075c2ef6b81a43568ded3cff452c381985fdbb9d/project/AppDependencies.scala#L29
    dependencyUpdatesFilter -= moduleFilter("com.github.tomakehurst", "wiremock-jre8"),
    dependencyUpdatesFilter -= moduleFilter("org.scalatestplus.play", "scalatestplus-play")
  )

}
