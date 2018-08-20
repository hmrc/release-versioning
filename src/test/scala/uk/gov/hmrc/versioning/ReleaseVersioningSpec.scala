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

class ReleaseVersioningSpec extends WordSpec with Matchers with TableDrivenPropertyChecks {

  "version for a single tag" should {
    val scenarios = Table(
      "Make Release" -> "Expected Version Suffix",
      true           -> "",
      false          -> "-SNAPSHOT"
    )

    forAll(scenarios) { (makeRelease, expectedVersionSuffix) =>
      s"return 0.2.0$expectedVersionSuffix when git describe is v0.1.1-1-g1234567 and makeRelease is $makeRelease" in {
        new ReleaseVersioning(makeRelease, makeHotfix = false)
          .version(tag = Some("v0.1.1"), gitDescribe = "v0.1.1-1-g1234567", majorVersion = 0) shouldBe s"0.2.0$expectedVersionSuffix"
      }

      s"return 0.2.0$expectedVersionSuffix when git describe is v0.1.0 (a tag on HEAD) and makeRelease is $makeRelease" in {
        new ReleaseVersioning(makeRelease, makeHotfix = false)
          .version(tag = Some("v0.1.1"), gitDescribe = "v0.1.0", majorVersion = 0) shouldBe s"0.2.0$expectedVersionSuffix"
      }

      s"return 0.1.0$expectedVersionSuffix when git describe returns the id on HEAD (no tags) and makeRelease is $makeRelease" in {
        new ReleaseVersioning(makeRelease, makeHotfix = false)
          .version(tag = None, gitDescribe = "3c9e281", majorVersion = 0) shouldBe s"0.1.0$expectedVersionSuffix"
      }

      s"throw exception when given v0.1.0.1 (a tag with an incorrect format) and makeRelease is $makeRelease" in {
        intercept[IllegalArgumentException] {
          new ReleaseVersioning(makeRelease, makeHotfix = false)
            .version(tag = Some("v0.1.0.1"), gitDescribe = "v0.1.0.1", majorVersion = 0)
        }.getMessage shouldBe "invalid version format for 'v0.1.0.1'"
      }

      s"use the new major version when makeRelease is $makeRelease" in {
        new ReleaseVersioning(makeRelease, makeHotfix = false)
          .version(tag = Some("v0.1.0"), gitDescribe = "v0.1.0-1-g1234567", majorVersion = 1) shouldBe s"1.0.0$expectedVersionSuffix"
      }

      s"use the new major version and return 1.0.0$expectedVersionSuffix when given v0.1.0 (a tag on HEAD) and makeRelease is $makeRelease" in {
        new ReleaseVersioning(makeRelease, makeHotfix = false)
          .version(tag = Some("v0.1.0"), gitDescribe = "v0.1.0", majorVersion = 1) shouldBe s"1.0.0$expectedVersionSuffix"
      }

      s"use the new major version and return 1.0.0$expectedVersionSuffix when given release/0.1.0 (a tag on HEAD) and makeRelease is $makeRelease" in {
        new ReleaseVersioning(makeRelease, makeHotfix = false)
          .version(tag = Some("release/0.1.0"), gitDescribe = "release/0.1.0", majorVersion = 1) shouldBe s"1.0.0$expectedVersionSuffix"
      }

      s"create a new patch if makeHotfix is true and makeRelease is $makeRelease" in {
        new ReleaseVersioning(makeRelease, makeHotfix = true)
          .version(tag = Some("v0.1.0"), gitDescribe = "v0.1.0-1-g1234567", majorVersion = 0) shouldBe s"0.1.1$expectedVersionSuffix"
      }

      s"throw an exception if a new major is requested at the same time as a hotfix and makeRelease is $makeRelease" in {
        intercept[IllegalArgumentException] {
          new ReleaseVersioning(makeRelease, makeHotfix = true)
            .version(tag = Some("v0.1.0"), gitDescribe = "v0.1.0-1-g1234567", majorVersion = 1)
        }.getMessage shouldBe "Invalid majorVersion: 1. It is not possible to change the major version as part of a hotfix."
      }

      s"throw exception if new major version is > current version + 1 and makeRelease is $makeRelease" in {
        intercept[IllegalArgumentException] {
          new ReleaseVersioning(makeRelease, makeHotfix = false)
            .version(tag = Some("v0.1.0"), gitDescribe = "v0.1.0-1-g1234567", majorVersion = 2)
        }.getMessage shouldBe "Invalid majorVersion: 2. The accepted values are 0 or 1 based on current git tags."
      }

      s"throw exception if new major version is < current version and makeRelease is $makeRelease" in {
        intercept[IllegalArgumentException] {
          new ReleaseVersioning(makeRelease, makeHotfix = false)
            .version(tag = Some("v1.1.0"), gitDescribe = "v1.1.0-1-g1234567", majorVersion = 0)
        }.getMessage shouldBe "Invalid majorVersion: 0. The accepted values are 1 or 2 based on current git tags."
      }
    }
  }

  "version for a list of tags" should {
    val scenarios = Table(
      "Make Release" -> "Expected Version Suffix",
      true           -> "",
      false          -> "-SNAPSHOT"
    )

    forAll(scenarios) { (makeRelease, expectedVersionSuffix) =>
      s"return 0.3.0$expectedVersionSuffix when the latest tag from 'git tag --list' is 0.2.0 and makeRelease is $makeRelease" in {
        new ReleaseVersioning(makeRelease, makeHotfix = false)
          .version(
            tags         = Seq("v0.1.1", "v0.1.0", "v0.2.0"),
            gitDescribe  = "v0.2.0-1-g1234567",
            majorVersion = 0
          ) shouldBe s"0.3.0$expectedVersionSuffix"
      }

      s"return 0.3.0$expectedVersionSuffix when the latest tag is in 'release/' format and makeRelease is $makeRelease" in {
        new ReleaseVersioning(makeRelease, makeHotfix = false)
          .version(
            tags         = Seq("release/0.2.0"),
            gitDescribe  = "release/0.2.0-1-g1234567",
            majorVersion = 0
          ) shouldBe s"0.3.0$expectedVersionSuffix"
      }

      s"return 0.1.0$expectedVersionSuffix when there is no tags and git describe is the head commit and makeRelease is $makeRelease" in {
        new ReleaseVersioning(makeRelease, makeHotfix = false)
          .version(
            tags         = Nil,
            gitDescribe  = "c13c993",
            majorVersion = 0
          ) shouldBe s"0.1.0$expectedVersionSuffix"
      }

      s"throw exception when given v0.1.0.1 (a tag with an incorrect format) and makeRelease is $makeRelease" in {
        intercept[IllegalArgumentException] {
          new ReleaseVersioning(makeRelease, makeHotfix = false)
            .version(
              tags         = Seq("v0.1.0.1"),
              gitDescribe  = "v0.1.0.1-1-g1234567",
              majorVersion = 0
            )
        }.getMessage shouldBe "invalid version format for 'v0.1.0.1-1-g1234567'"
      }

      s"use the new major version when makeRelease is $makeRelease" in {
        new ReleaseVersioning(makeRelease, makeHotfix = false)
          .version(
            tags         = Seq("v0.1.0"),
            gitDescribe  = "v0.1.0-1-g1234561",
            majorVersion = 1
          ) shouldBe s"1.0.0$expectedVersionSuffix"
      }

      s"use the new major version and return 1.0.0$expectedVersionSuffix when given v0.1.0 (a tag on HEAD) and makeRelease is $makeRelease" in {
        new ReleaseVersioning(makeRelease, makeHotfix = false)
          .version(
            tags         = Seq("v0.1.0"),
            gitDescribe  = "v0.1.0",
            majorVersion = 1
          ) shouldBe s"1.0.0$expectedVersionSuffix"
      }

      s"use the new major version and return 1.0.0$expectedVersionSuffix when given release/0.1.0 (a tag on HEAD) and makeRelease is $makeRelease" in {
        new ReleaseVersioning(makeRelease, makeHotfix = false)
          .version(
            tags         = Seq("release/0.1.0"),
            gitDescribe  = "release/0.1.0",
            majorVersion = 1
          ) shouldBe s"1.0.0$expectedVersionSuffix"
      }

      s"create a new patch if makeHotfix is true and makeRelease is $makeRelease" in {
        new ReleaseVersioning(makeRelease, makeHotfix = true)
          .version(
            tags         = Seq("v0.1.0"),
            gitDescribe  = "v0.1.0-1-g1234561",
            majorVersion = 0
          ) shouldBe s"0.1.1$expectedVersionSuffix"
      }

      s"throw an exception if a new major is requested at the same time as a hotfix and makeRelease is $makeRelease" in {
        intercept[IllegalArgumentException] {
          new ReleaseVersioning(makeRelease, makeHotfix = true)
            .version(
              tags         = Seq("v0.1.0"),
              gitDescribe  = "v0.1.0-1-g1234561",
              majorVersion = 1
            )
        }.getMessage shouldBe "Invalid majorVersion: 1. It is not possible to change the major version as part of a hotfix."
      }

      s"throw exception if new major version is > current version + 1 and makeRelease is $makeRelease" in {
        intercept[IllegalArgumentException] {
          new ReleaseVersioning(makeRelease, makeHotfix = false)
            .version(
              tags         = Seq("v0.1.0"),
              gitDescribe  = "v0.1.0-1-g1234561",
              majorVersion = 2
            )
        }.getMessage shouldBe "Invalid majorVersion: 2. The accepted values are 0 or 1 based on current git tags."
      }

      s"throw exception if new major version is < current version and makeRelease is $makeRelease" in {
        intercept[IllegalArgumentException] {
          new ReleaseVersioning(makeRelease, makeHotfix = false)
            .version(
              tags         = Seq("v1.1.0"),
              gitDescribe  = "v1.1.0-1-g1234561",
              majorVersion = 0
            )
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
      unsorted.sortWith(ReleaseVersioning.versionComparator) shouldBe expectedOrder
    }

}
