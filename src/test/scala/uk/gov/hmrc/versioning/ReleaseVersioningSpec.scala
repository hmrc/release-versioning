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

import org.scalatest.{Matchers, WordSpec}
import org.scalatest.prop.TableDrivenPropertyChecks

class ReleaseVersioningSpec extends WordSpec with Matchers with TableDrivenPropertyChecks {

  import ReleaseVersioning.calculateNextVersion

  "calculateNextVersion" should {

    val scenarios = Table(
      "release arg value" -> "Expected version suffix",
      true                -> "",
      false               -> "-SNAPSHOT"
    )

    forAll(scenarios) { (release, expectedVersionSuffix) =>
      s"return 0.2.0$expectedVersionSuffix when git describe is v0.1.1-1-g1234567 and release is $release" in {
        calculateNextVersion(
          release,
          hotfix           = false,
          releaseCandidate = false,
          maybeGitDescribe = Some("v0.1.1-1-g1234567"),
          majorVersion     = 0
        ) shouldBe s"0.2.0$expectedVersionSuffix"
      }

      s"return 0.2.0$expectedVersionSuffix when git describe is release/0.53.0-1-gd8f1a21c and release is $release" in {
        calculateNextVersion(
          release,
          hotfix           = false,
          releaseCandidate = false,
          maybeGitDescribe = Some("release/0.53.0-1-gd8f1a21c"),
          majorVersion     = 0
        ) shouldBe s"0.54.0$expectedVersionSuffix"
      }

      s"return 0.2.0$expectedVersionSuffix when git describe is v0.1.0 (a tag on HEAD) and release" +
        s" is $release" in {
        calculateNextVersion(
          release,
          hotfix           = false,
          releaseCandidate = false,
          maybeGitDescribe = Some("v0.1.0"),
          majorVersion     = 0
        ) shouldBe s"0.2.0$expectedVersionSuffix"
      }

      s"return 0.1.0$expectedVersionSuffix when git describe returns the id on HEAD (no tags) and release" +
        s" is $release" in {
        calculateNextVersion(
          release,
          hotfix           = false,
          releaseCandidate = false,
          maybeGitDescribe = None,
          majorVersion     = 0
        ) shouldBe s"0.1.0$expectedVersionSuffix"
      }

      s"throw exception when given v0.1.0.1 (a tag with an incorrect format) and release" +
        s" is $release" in {
        intercept[IllegalArgumentException] {
          calculateNextVersion(
            release,
            releaseCandidate = false,
            hotfix           = false,
            maybeGitDescribe = Some("v0.1.0.1"),
            majorVersion     = 0
          )
        }.getMessage shouldBe "invalid version format for 'v0.1.0.1'"
      }

      "use the new major version when there are no tags on HEAD but there is one on previous commits" +
        s" and release is $release" in {
        calculateNextVersion(
          release,
          hotfix           = false,
          releaseCandidate = false,
          maybeGitDescribe = Some("v0.1.0-1-g1234567"),
          majorVersion     = 1
        ) shouldBe s"1.0.0$expectedVersionSuffix"
      }

      s"use the new major version and return 1.0.0$expectedVersionSuffix when given v0.1.0 (a tag on HEAD) and release" +
        s" is $release" in {
        calculateNextVersion(
          release,
          hotfix           = false,
          releaseCandidate = false,
          maybeGitDescribe = Some("v0.1.0"),
          majorVersion     = 1
        ) shouldBe s"1.0.0$expectedVersionSuffix"
      }

      s"use the new major version and return 1.0.0$expectedVersionSuffix when given release/0.1.0 (a tag on HEAD) and release" +
        s" is $release" in {
        calculateNextVersion(
          release,
          hotfix           = false,
          releaseCandidate = false,
          maybeGitDescribe = Some("release/0.1.0"),
          majorVersion     = 1
        ) shouldBe s"1.0.0$expectedVersionSuffix"
      }

      s"create a new patch if hotfix is true and release is $release" in {
        calculateNextVersion(
          release,
          hotfix           = true,
          releaseCandidate = false,
          maybeGitDescribe = Some("v0.1.0-1-g1234567"),
          majorVersion     = 0
        ) shouldBe s"0.1.1$expectedVersionSuffix"
      }

      s"throw an exception if a new major is requested at the same time as a hotfix and release is $release" in {
        intercept[IllegalArgumentException] {
          calculateNextVersion(
            release,
            hotfix           = true,
            releaseCandidate = false,
            maybeGitDescribe = Some("v0.1.0-1-g1234567"),
            majorVersion     = 1
          )
        }.getMessage shouldBe "Invalid majorVersion: 1. It is not possible to change the major version as part of a hotfix."
      }

      s"throw exception if new major version is > current version + 1 and release is $release" in {
        intercept[IllegalArgumentException] {
          calculateNextVersion(
            release,
            hotfix           = false,
            releaseCandidate = false,
            maybeGitDescribe = Some("v0.1.0-1-g1234567"),
            majorVersion     = 2
          )
        }.getMessage shouldBe "Invalid majorVersion: 2. The accepted values are 0 or 1 based on current git tags."
      }

      s"throw exception if new major version is < current version and release is $release" in {
        intercept[IllegalArgumentException] {
          calculateNextVersion(
            release,
            hotfix           = false,
            releaseCandidate = false,
            maybeGitDescribe = Some("v1.1.0-1-g1234567"),
            majorVersion     = 0
          )
        }.getMessage shouldBe "Invalid majorVersion: 0. The accepted values are 1 or 2 based on current git tags."
      }

      s"throw exception if major version > 0 is specified but no tags exist and release is $release" in {
        intercept[IllegalArgumentException] {
          calculateNextVersion(
            release,
            hotfix           = false,
            releaseCandidate = false,
            maybeGitDescribe = None,
            majorVersion     = 1
          )
        }.getMessage shouldBe "Invalid majorVersion: 1. You cannot request a major version of 1 if there are no tags in the repository."
      }
    }
  }
}
