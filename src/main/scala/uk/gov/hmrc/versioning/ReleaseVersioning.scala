/*
 * Copyright 2018 HM Revenue & Customs
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
  def version(release: Boolean, hotfix: Boolean, latestTag: Option[String], majorVersion: Int): String =
    nextVersion(release, hotfix, latestTag, majorVersion) + (if (release) "" else "-SNAPSHOT")

  private def nextVersion(
    release: Boolean,
    hotfix: Boolean,
    latestTag: Option[String],
    requestedMajorVersion: Int): String =
    latestTag match {
      case None if requestedMajorVersion > 0 =>
        throw new IllegalArgumentException(
          s"Invalid majorVersion: $requestedMajorVersion. You cannot request a major version of $requestedMajorVersion if there are no tags in the repository."
        )

      case None =>
        "0.1.0"

      case Some(latestTagFormat(AsInt(major), _, _)) if major != requestedMajorVersion && hotfix =>
        throw new IllegalArgumentException(
          s"Invalid majorVersion: $requestedMajorVersion. It is not possible to change the major version as part of a hotfix."
        )

      case Some(latestTagFormat(AsInt(major), _, _)) if !validMajorVersion(major, requestedMajorVersion) =>
        throw new IllegalArgumentException(
          s"Invalid majorVersion: $requestedMajorVersion. " +
            s"The accepted values are $major or ${major + 1} based on current git tags."
        )

      case Some(latestTagFormat(AsInt(major), _, _)) if requestedMajorVersion != major =>
        s"$requestedMajorVersion.0.0"

      case Some(latestTagFormat(major, minor, AsInt(patch))) if hotfix =>
        s"$major.$minor.${patch + 1}"

      case Some(latestTagFormat(major, AsInt(minor), _)) =>
        s"$major.${minor + 1}.0"

      case Some(unrecognizedGitDescribe) =>
        throw new IllegalArgumentException(s"invalid version format for '$unrecognizedGitDescribe'")
    }

  private val latestTagFormat = """^(?:release\/|v)?(\d+)\.(\d+)\.(\d+)$""".r

  private object AsInt {
    def unapply(arg: String): Option[Int] = Some(arg.toInt)
  }

  private def validMajorVersion(current: Int, requested: Int): Boolean =
    requested == current || requested == current + 1
}
