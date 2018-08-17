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

import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{Matchers, WordSpec}
import scala.util.Random

class ReleaseVersionFinderSpec extends WordSpec with Matchers with TableDrivenPropertyChecks {
  "version" should {
    val scenarios = Table(
      "Make Release" -> "Expected Version Suffix",
      true           -> "",
      false          -> "-SNAPSHOT"
    )

    forAll(scenarios) { (makeRelease, expectedVersionSuffix) =>
      s"return 0.2.0$expectedVersionSuffix when git describe is v0.1.1-1-g1234567 and makeRelease is $makeRelease" in {
        new ReleaseVersionFinder(makeRelease, makeHotfix = false)
          .version(tagOrGitDescribe = "v0.1.1-1-g1234567", gitHeadCommit = None, majorVersion = 0) shouldBe s"0.2.0$expectedVersionSuffix"
      }

      s"return 0.2.0$expectedVersionSuffix when git describe is v0.1.0 (a tag on HEAD) and makeRelease is $makeRelease" in {
        new ReleaseVersionFinder(makeRelease, makeHotfix = false)
          .version(tagOrGitDescribe = "v0.1.0", gitHeadCommit = None, majorVersion = 0) shouldBe s"0.2.0$expectedVersionSuffix"
      }

      s"return 0.1.0$expectedVersionSuffix when git describe returns the id on HEAD (no tags) and makeRelease is $makeRelease" in {
        new ReleaseVersionFinder(makeRelease, makeHotfix = false)
          .version(tagOrGitDescribe = "12345", gitHeadCommit = Some("123456789"), majorVersion = 0) shouldBe s"0.1.0$expectedVersionSuffix"
      }

      s"throw exception when given v0.1.0.1 (a tag with an incorrect format) and makeRelease is $makeRelease" in {
        intercept[IllegalArgumentException] {
          new ReleaseVersionFinder(makeRelease, makeHotfix = false)
            .version(tagOrGitDescribe = "v0.1.0.1", gitHeadCommit = None, majorVersion = 0)
        }.getMessage shouldBe "invalid version format for 'v0.1.0.1'"
      }

      s"use the new major version when makeRelease is $makeRelease" in {
        new ReleaseVersionFinder(makeRelease, makeHotfix = false)
          .version(tagOrGitDescribe = "v0.1.0-1-g1234567", gitHeadCommit = None, majorVersion = 1) shouldBe s"1.0.0$expectedVersionSuffix"
      }

      s"use the new major version and return 1.0.0$expectedVersionSuffix when given v0.1.0 (a tag on HEAD) and makeRelease is $makeRelease" in {
        new ReleaseVersionFinder(makeRelease, makeHotfix = false)
          .version(tagOrGitDescribe = "v0.1.0", gitHeadCommit = None, majorVersion = 1) shouldBe s"1.0.0$expectedVersionSuffix"
      }

      s"use the new major version and return 1.0.0$expectedVersionSuffix when given release/0.1.0 (a tag on HEAD) and makeRelease is $makeRelease" in {
        new ReleaseVersionFinder(makeRelease, makeHotfix = false)
          .version(tagOrGitDescribe = "release/0.1.0", gitHeadCommit = None, majorVersion = 1) shouldBe s"1.0.0$expectedVersionSuffix"
      }

      s"create a new patch if makeHotfix is true and makeRelease is $makeRelease" in {
        new ReleaseVersionFinder(makeRelease, makeHotfix = true)
          .version(tagOrGitDescribe = "v0.1.0", gitHeadCommit = None, majorVersion = 0) shouldBe s"0.1.1$expectedVersionSuffix"
      }

      s"throw an exception if a new major is requested at the same time as a hotfix and makeRelease is $makeRelease" in {
        intercept[IllegalArgumentException] {
          new ReleaseVersionFinder(makeRelease, makeHotfix = true)
            .version(tagOrGitDescribe = "v0.1.0", gitHeadCommit = None, majorVersion = 1)
        }.getMessage shouldBe "Invalid majorVersion: 1. It is not possible to change the major version as part of a hotfix."
      }

      s"throw exception if new major version is > current version + 1 and makeRelease is $makeRelease" in {
        intercept[IllegalArgumentException] {
          new ReleaseVersionFinder(makeRelease, makeHotfix = false)
            .version(tagOrGitDescribe = "v0.1.0", gitHeadCommit = None, majorVersion = 2)
        }.getMessage shouldBe "Invalid majorVersion: 2. The accepted values are 0 or 1 based on current git tags."
      }

      s"throw exception if new major version is < current version and makeRelease is $makeRelease" in {
        intercept[IllegalArgumentException] {
          new ReleaseVersionFinder(makeRelease, makeHotfix = false)
            .version(tagOrGitDescribe = "v1.1.0", gitHeadCommit = None, majorVersion = 0)
        }.getMessage shouldBe "Invalid majorVersion: 0. The accepted values are 1 or 2 based on current git tags."
      }
    }

  }

  "sorting tags" should {
    "consider major versions first" in {
      assertOrder(List("v8.0.0", "v9.0.0", "v10.10.0"))
    }
    "consider minor versions second" in {
      assertOrder(List("v0.8.0", "v0.9.0", "v0.10.0"))
    }
    "consider hotfix versions third" in {
      assertOrder(List("v0.1.8", "v0.1.9", "v0.1.10"))
    }
    "deprioritize tags in unknown formats" in {
      assertOrder(List("not-a-valid-tag", "not-a-valid-tag", "v0.1.0", "v1.0.0"))
    }
    "work for both v and release/ styles" in {
      assertOrder(List("release/1.0.0", "release/1.1.0", "v2.0.0"))
    }
  }

  def assertOrder(expectedOrder: List[String]): Unit =
    (1 to 25).foreach { _ =>
      val unsorted = Random.shuffle(expectedOrder)
      unsorted.sortWith(ReleaseVersionFinder.versionComparator) shouldBe expectedOrder
    }

}
