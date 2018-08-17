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

class ReleaseVersionFinder(makeRelease: Boolean, makeHotfix: Boolean) {
  import ReleaseVersionFinder._

  def version(tag: Option[String], gitDescribe: String, majorVersion: Int): String =
    nextVersion(tag, gitDescribe, majorVersion) + (if (makeRelease) "" else "-SNAPSHOT")

  def version(tags: Seq[String], gitDescribe: String, majorVersion: Int): String =
    version(
      tag          = tags.sortWith(versionComparator).reverse.headOption,
      gitDescribe  = gitDescribe,
      majorVersion = majorVersion
    )

  private def nextVersion(tag: Option[String], gitDescribe: String, requestedMajorVersion: Int): String = {
    val gitDescribeFormat = s"""^$versionRegex(?:-.*-g.*$$){0,1}""".r

    def validMajorVersion(current: Int, requested: Int): Boolean =
      requested == current || requested == current + 1

    gitDescribe match {
      case gitDescribeFormat(AsInt(major), _, _) if major != requestedMajorVersion && makeHotfix =>
        throw new IllegalArgumentException(
          s"Invalid majorVersion: $requestedMajorVersion. It is not possible to change the major version as part of a hotfix."
        )
      case gitDescribeFormat(AsInt(major), _, _) if !validMajorVersion(major, requestedMajorVersion) =>
        throw new IllegalArgumentException(
          s"Invalid majorVersion: $requestedMajorVersion. " +
            s"The accepted values are $major or ${major + 1} based on current git tags."
        )

      case gitDescribeFormat(AsInt(major), _, _) if requestedMajorVersion != major =>
        s"$requestedMajorVersion.0.0"

      case gitDescribeFormat(major, minor, AsInt(patch)) if makeHotfix =>
        s"$major.$minor.${patch + 1}"

      case gitDescribeFormat(major, AsInt(minor), _) =>
        s"$major.${minor + 1}.0"

      case _ if tag.isEmpty => "0.1.0"

      case unrecognizedGitDescribe =>
        throw new IllegalArgumentException(s"invalid version format for '$unrecognizedGitDescribe'")
    }
  }
}

object ReleaseVersionFinder {
  private val versionRegex = """(?:release\/|v)?(\d+)\.(\d+)\.(\d+)"""

  def versionComparator(tag1: String, tag2: String): Boolean = {
    val Version = versionRegex.r
    (tag1, tag2) match {
      case (Version(AsInt(major1), _, _), Version(AsInt(major2), _, _)) if major1 != major2 => major1 < major2
      case (Version(_, AsInt(minor1), _), Version(_, AsInt(minor2), _)) if minor1 != minor2 => minor1 < minor2
      case (Version(_, _, AsInt(patch1)), Version(_, _, AsInt(patch2))) if patch1 != patch2 => patch1 < patch2
      case (Version(_, _, _), _)                                                            => false
      case (_, Version(_, _, _))                                                            => true
      case (_, _)                                                                           => true
    }
  }

  private object AsInt {
    def unapply(arg: String): Option[Int] = Some(arg.toInt)
  }

}
