import uk.gov.hmrc.DefaultBuildSettings._

lazy val releaseVersioning = Project("release-versioning", file("."))
  .settings(
    scalaVersion        := "2.12.18",
    majorVersion        := 0,
    isPublicArtefact    := true,
    libraryDependencies ++= compileDependencies ++ testDependencies,
    assemblySettings
  )

val compileDependencies = Seq(
  "com.github.scopt" %% "scopt"     % "3.7.1"
)

val testDependencies = Seq(
  "org.scalatest"         %% "scalatest"    % "3.2.17"  % Test,
  "com.vladsch.flexmark"  %  "flexmark-all" % "0.64.8"  % Test
)

val assemblySettings = Seq(
  assembly / test                  := {},
  assembly / assemblyMergeStrategy := {
                                        case PathList("META-INF", xs @ _*) => MergeStrategy.discard
                                        case x                             => (assembly / assemblyMergeStrategy).value(x)
                                      },
  Compile / assembly / artifact    := (Compile / assembly / artifact).value.withClassifier(Some("assembly"))
)

addArtifact(Compile / assembly / artifact, assembly)
