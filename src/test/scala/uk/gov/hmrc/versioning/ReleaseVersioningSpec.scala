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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.prop.TableDrivenPropertyChecks

class ReleaseVersioningSpec extends AnyWordSpec with Matchers with TableDrivenPropertyChecks {

  import ReleaseVersioning.calculateNextVersion

  "calculateNextVersion" should {

    val releaseScenarios = Table(
      "release arg value" -> "Expected version suffix",
      true                -> "",
      false               -> "-SNAPSHOT"
    )

    val releaseCandidateScenarios = Table(
      "release-candidate arg value" -> "Expected RC suffix",
      true                -> "-RC1",
      false               -> ""
    )

    forAll(releaseScenarios) { (release, expectedVersionSuffix) =>
      forAll(releaseCandidateScenarios) { (releaseCandidate, expectedRCSuffix) =>
        s"return 0.2.0$expectedVersionSuffix when git describe is v0.1.1-1-g1234567 and release: $release, releaseCandidate: $releaseCandidate" in {
          calculateNextVersion(
            release,
            hotfix           = false,
            releaseCandidate,
            maybeGitDescribe = Some("v0.1.1-1-g1234567"),
            majorVersion     = 0
          ) shouldBe s"0.2.0$expectedRCSuffix$expectedVersionSuffix"
        }

        s"return 0.2.0$expectedVersionSuffix when git describe is release/0.53.0-1-gd8f1a21c and release: $release, releaseCandidate: $releaseCandidate" in {
          calculateNextVersion(
            release,
            hotfix           = false,
            releaseCandidate,
            maybeGitDescribe = Some("release/0.53.0-1-gd8f1a21c"),
            majorVersion     = 0
          ) shouldBe s"0.54.0$expectedRCSuffix$expectedVersionSuffix"
        }

        s"return 0.2.0$expectedVersionSuffix when git describe is v0.1.0 (a tag on HEAD) and release: $release, releaseCandidate: $releaseCandidate" in {
          calculateNextVersion(
            release,
            hotfix           = false,
            releaseCandidate,
            maybeGitDescribe = Some("v0.1.0"),
            majorVersion     = 0
          ) shouldBe s"0.2.0$expectedRCSuffix$expectedVersionSuffix"
        }

        s"return 0.1.0$expectedVersionSuffix when git describe returns the id on HEAD (no tags) and release: $release, releaseCandidate: $releaseCandidate" in {
          calculateNextVersion(
            release,
            hotfix           = false,
            releaseCandidate,
            maybeGitDescribe = None,
            majorVersion     = 0
          ) shouldBe s"0.1.0$expectedRCSuffix$expectedVersionSuffix"
        }

        s"throw exception when given v0.1.0.1 (a tag with an incorrect format) and release: $release, releaseCandidate: $releaseCandidate" in {
          intercept[IllegalArgumentException] {
            calculateNextVersion(
              release,
              hotfix           = false,
              releaseCandidate,
              maybeGitDescribe = Some("v0.1.0.1"),
              majorVersion     = 0
            )
          }.getMessage shouldBe "invalid version format for 'v0.1.0.1'"
        }

        s"use the new major version when there are no tags on HEAD but there is one on previous commits and release: $release, releaseCandidate: $releaseCandidate" in {
          calculateNextVersion(
            release,
            hotfix           = false,
            releaseCandidate,
            maybeGitDescribe = Some("v0.1.0-1-g1234567"),
            majorVersion     = 1
          ) shouldBe s"1.0.0$expectedRCSuffix$expectedVersionSuffix"
        }

        s"use the new major version and return 1.0.0$expectedVersionSuffix when given v0.1.0 (a tag on HEAD) and release: $release, releaseCandidate: $releaseCandidate" +
          s" is $release" in {
          calculateNextVersion(
            release,
            hotfix           = false,
            releaseCandidate,
            maybeGitDescribe = Some("v0.1.0"),
            majorVersion     = 1
          ) shouldBe s"1.0.0$expectedRCSuffix$expectedVersionSuffix"
        }

        s"use the new major version and return 1.0.0$expectedVersionSuffix when given release/0.1.0 (a tag on HEAD) and release: $release, releaseCandidate: $releaseCandidate" in {
          calculateNextVersion(
            release,
            hotfix           = false,
            releaseCandidate,
            maybeGitDescribe = Some("release/0.1.0"),
            majorVersion     = 1
          ) shouldBe s"1.0.0$expectedRCSuffix$expectedVersionSuffix"
        }

        s"create a new patch if hotfix is true and release: $release, releaseCandidate: $releaseCandidate" in {
          calculateNextVersion(
            release,
            hotfix           = true,
            releaseCandidate,
            maybeGitDescribe = Some("v0.1.0-1-g1234567"),
            majorVersion     = 0
          ) shouldBe s"0.1.1$expectedRCSuffix$expectedVersionSuffix"
        }
      }
    }

    s"if was a release candidate" should {
      forAll(releaseScenarios) { (release, expectedVersionSuffix) =>
        s"increment the release candidate if a releaseCandidate, not a hotfix and release: $release" in {
          calculateNextVersion(
            release,
            hotfix           = false,
            releaseCandidate = true,
            maybeGitDescribe = Some("v0.1.0-RC1-1-g1234567"),
            majorVersion     = 0
          ) shouldBe s"0.2.0-RC2$expectedVersionSuffix"
        }

        s"increment the release candidate if a releaseCandidate, a hotfix and release: $release" in {
          calculateNextVersion(
            release,
            hotfix           = true,
            releaseCandidate = true,
            maybeGitDescribe = Some("v0.1.0-RC1-1-g1234567"),
            majorVersion     = 0
          ) shouldBe s"0.1.1-RC2$expectedVersionSuffix"
        }

        s"remove the release candidate if not a releaseCandidate, not a hotfix and release: $release" in {
          calculateNextVersion(
            release,
            hotfix           = true,
            releaseCandidate = false,
            maybeGitDescribe = Some("v0.1.0-RC1-1-g1234567"),
            majorVersion     = 0
          ) shouldBe s"0.1.0$expectedVersionSuffix"
        }
      }
    }
  }
}
