import uk.gov.hmrc.DefaultBuildSettings._

lazy val releaseVersioning = Project("release-versioning", file("."))
  .settings(scalaSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    scalaVersion := "2.12.14",
    crossScalaVersions := Vector("2.10.7", "2.11.12", "2.12.8"),
    majorVersion := 0,
    isPublicArtefact := true,
    libraryDependencies ++= compileDependencies ++ testDependencies,
    assemblySettings
  )

val compileDependencies = Seq(
  "com.github.scopt" %% "scopt"     % "3.7.1",
  "org.typelevel"    %% "cats-core" % "1.2.0"
)

val testDependencies = Seq(
  "org.scalatest"         %% "scalatest"    % "3.1.0-M2"  % Test,
  "com.vladsch.flexmark"  % "flexmark-all"  % "0.35.10"   % Test
)

val assemblySettings = Seq(
  assembly / test := {},
  assembly / assemblyMergeStrategy := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x                             => (assembly / assemblyMergeStrategy).value(x)
  },
  Compile / assembly / artifact := (Compile / assembly / artifact).value.withClassifier(Some("assembly"))
)

addArtifact(Compile / assembly / artifact, assembly)
