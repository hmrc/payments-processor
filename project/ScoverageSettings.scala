import sbt.*
import scoverage.ScoverageKeys

object ScoverageSettings {
  lazy val scoverageSettings: Seq[Def.SettingsDefinition] = {

    val excludedFiles = Seq(
      """<empty>""",
      """Reverse.*""",
      """.*models.*""",
      """.*.template.*""",
      """.*components.*""",
      """.*Routes.*""",
      """.*Link.*""",
      """.*TestOnly.scala""",
      """.*JourneyLogger.scala"""
    ).mkString("", ";", ";")

    val excludedPackages = Seq(
      """.*.Reverse.*""",
      """app.Routes.*""",
      """prod.*""",
      """.*.javascript.*""",
      """testOnly.*""",
      """websocket.*""",
      """.*viewmodels.govuk""",
      """.*Reverse.*"""
    ).mkString("", ";", ";")

    Seq(
      ScoverageKeys.coverageExcludedPackages := excludedPackages,
      ScoverageKeys.coverageExcludedFiles := excludedFiles,
      ScoverageKeys.coverageMinimumStmtTotal := 80,
      ScoverageKeys.coverageFailOnMinimum := true,
      ScoverageKeys.coverageHighlighting := true
    )
  }
}
