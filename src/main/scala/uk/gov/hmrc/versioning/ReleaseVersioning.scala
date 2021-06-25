/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.versioning

object ReleaseVersioning {

  def calculateNextVersion(
    release: Boolean,
    hotfix: Boolean,
    releaseCandidate: Boolean,
    maybeGitDescribe: Option[String],
    majorVersion: Int): String =
    calculateVersion(release, hotfix, releaseCandidate, maybeGitDescribe, majorVersion) + suffix(release)

  private def calculateVersion(
    release: Boolean,
    hotfix: Boolean,
    releaseCandidate: Boolean,
    maybeGitDescribe: Option[String],
    requestedMajorVersion: Int
  ): String = maybeGitDescribe.flatMap(Version.parse) match {

    case None if requestedMajorVersion > 0 =>
      throw new IllegalArgumentException(
        s"Invalid majorVersion: $requestedMajorVersion. You cannot request a major version of $requestedMajorVersion if there are no tags in the repository."
      )

    case None =>
      "0.1.0"

    case Some(Version(major, _, _, _)) if major != requestedMajorVersion && hotfix =>
      throw new IllegalArgumentException(
        s"Invalid majorVersion: $requestedMajorVersion. It is not possible to change the major version as part of a hotfix."
      )

    case Some(Version(major, _, _, _)) if !validMajorVersion(major, requestedMajorVersion) =>
      throw new IllegalArgumentException(
        s"Invalid majorVersion: $requestedMajorVersion. " +
          s"The accepted values are $major or ${major + 1} based on current git tags."
      )

    case Some(Version(major, _, _, _)) if requestedMajorVersion != major =>
      s"$requestedMajorVersion.0.0"

    case Some(Version(major, minor, patch, _)) if hotfix =>
      s"$major.$minor.${patch + 1}"

    case Some(Version(major, minor, _, _)) =>
      s"$major.${minor + 1}.0"

    case Some(unrecognizedGitDescribe) =>
      throw new IllegalArgumentException(s"invalid version format for '$unrecognizedGitDescribe'")
  }

  case class Version(
    major: Int,
    minor: Int,
    patch: Int,
    rc: Option[Int]
  )
  object Version {
    private val tag = """^(?:release\/|v)?(\d+)\.(\d+)\.(\d+)(-RC(\d+))?(?:-\d+-g[a-z0-9]{4,40}$)?""".r
    def parse(s: String): Option[Version] =
      s match {
        case tag(major, minor, patch, _, rc) =>
          Some(Version(major.toInt, minor.toInt, patch.toInt, Option(rc).filterNot(_ == "null").map(_.toInt)))
        case _ => None
      }
  }

  private def validMajorVersion(current: Int, requested: Int): Boolean =
    requested == current || requested == current + 1

  private def suffix(release: Boolean) =
    if (release) ""
    else "-SNAPSHOT"
}
