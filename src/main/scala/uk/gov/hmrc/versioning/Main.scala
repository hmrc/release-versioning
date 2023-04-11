/*
 * Copyright 2023 HM Revenue & Customs
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

import scopt.OptionParser

import scala.util.Try

object Main {

  import ReleaseVersioning.calculateNextVersion

  def main(args: Array[String]): Unit =
    parseArgs(args).map(toVersion) match {
      case Some(Right(nextReleaseVersion)) =>
        Console.out.println(nextReleaseVersion)
        System.exit(0)
      case Some(Left(exception)) =>
        Console.err.println(exception.getMessage)
        System.exit(1)
      case None =>
        System.exit(1)
    }

  private def toVersion(args: Args) =
    Try {
      calculateNextVersion(
        args.release,
        args.hotfix,
        args.releaseCandidate,
        args.maybeGitDescribe,
        majorVersion = args.maybeMajorVersion.getOrElse(0)
      )
    }.toEither

  private case class Args(
    release: Boolean                 = false,
    hotfix: Boolean                  = false,
    releaseCandidate: Boolean        = false,
    maybeGitDescribe: Option[String] = None,
    maybeMajorVersion: Option[Int]   = None
  )

  private def parseArgs(args: Array[String]): Option[Args] =
    new OptionParser[Args]("release-versioning") {

      private val gitDescribeOptionName = "git-describe"

      help("help").text("prints this usage text")

      opt[Unit]("release")
        .action((_, args) => args.copy(release = true))
        .text("release is an optional flag indicating whether it should be a release or a snapshot")
      opt[Unit]("hotfix")
        .action((_, args) => args.copy(hotfix = true))
        .text("hotfix is an optional flag indicating whether it should be a hotfix or a major/minor release")
      opt[Unit]("release-candidate")
        .action((_, args) => args.copy(releaseCandidate = true))
        .text("release-candidate is an optional flag indicating whether it should be a release-candidate or not")
      opt[String](gitDescribeOptionName)
        .action((gd, args) => args.copy(maybeGitDescribe = Some(gd)))
        .text(s"$gitDescribeOptionName is an optional argument expecting an outcome of `git describe` command")
      opt[Int]("major-version")
        .action((m, args) => args.copy(maybeMajorVersion = Some(m)))
        .text(s"major-version is required when $gitDescribeOptionName option is defined")

      checkConfig {
        case Args(_, _, _, None, Some(majorVersion)) if majorVersion > 0 =>
          Left(s"You cannot request a major version of $majorVersion if there is no $gitDescribeOptionName given.")
        case Args(_, _, _, Some(_), None) =>
          Left(s"major-version is required when $gitDescribeOptionName option is defined")
        case _ =>
          Right[String, Unit](())
      }
    }.parse(args, Args())
}
