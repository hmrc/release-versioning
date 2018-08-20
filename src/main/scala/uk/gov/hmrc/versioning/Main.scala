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

import cats.implicits._
import scopt.OptionParser

object Main {

  def main(args: Array[String]): Unit =
    parseArgs(args) map toVersion match {
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
    Either.catchNonFatal {
      ReleaseVersioning.version(args.release, args.hotfix, args.optLatestTag, args.majorVersion.getOrElse(0))
    }

  private case class Args(
    release: Boolean             = false,
    hotfix: Boolean              = false,
    optLatestTag: Option[String] = None,
    majorVersion: Option[Int]    = None
  )

  private def parseArgs(args: Array[String]): Option[Args] =
    new OptionParser[Args]("release-versioning") {

      private val latestTagOptionName = "latest-tag"

      help("help").text("prints this usage text")

      opt[Unit]('r', "release")
        .action((_, args) => args.copy(release = true))
        .text("release is a required Boolean argument indicating whether it should be a release or a snapshot")
      opt[Unit]('f', "hotfix")
        .action((_, args) => args.copy(hotfix = true))
        .text("hotfix is a required Boolean argument indicating whether it should be a hotfix or a major/minor release")
      opt[String]('t', latestTagOptionName)
        .action((gd, args) => args.copy(optLatestTag = Some(gd)))
        .text(s"$latestTagOptionName is an optional argument expecting the latest tag name")
      opt[Int]('m', "major-version")
        .action((m, args) => args.copy(majorVersion = Some(m)))
        .text(s"major-version is required when $latestTagOptionName option is defined")

      checkConfig {
        case Args(_, _, None, Some(majorVersion)) if majorVersion > 0 =>
          Left(s"You cannot request a major version of $majorVersion if there is no $latestTagOptionName given.")
        case Args(_, _, Some(_), None) =>
          Left(s"major-version is required when $latestTagOptionName option is defined")
        case _ =>
          Right[String, Unit](())
      }
    }.parse(args, Args())
}
