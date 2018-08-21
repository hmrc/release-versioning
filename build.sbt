import sbt.Keys._
import sbt._
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.versioning.SbtGitVersioning
import uk.gov.hmrc.SbtArtifactory

val appName: String = "release-versioning"

lazy val releaseVersioning = Project(appName, file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(scalaSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    scalaVersion := "2.10.7",
    majorVersion := 0,
    makePublicallyAvailableOnBintray := true,
    libraryDependencies ++= compileDependencies ++ testDependencies,
    retrieveManaged := true,
    assemblySettings,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )

val compileDependencies = Seq(
  "com.github.scopt" %% "scopt"     % "3.7.0",
  "org.typelevel"    %% "cats-core" % "1.2.0"
)

val testDependencies = Seq(
  "org.pegdown"   % "pegdown"    % "1.6.0" % Test,
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

val assemblySettings = Seq(
  test in assembly := {},
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x                             => (assemblyMergeStrategy in assembly).value(x)
  },
  artifact in (Compile, assembly) := {
    val art = (artifact in (Compile, assembly)).value
    art.copy(`classifier` = Some("assembly"))
  }
)

addArtifact(artifact in (Compile, assembly), assembly)
