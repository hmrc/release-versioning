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

case class Args(
  release: Boolean             = false,
  hotfix: Boolean              = false,
  tags: Seq[String]            = Nil,
  optLatestTag: Option[String] = None,
  majorVersion: Int            = 0
)

object Main {
  def main(args: Array[String]): Unit =
    parseArgs(args).map { programArgs =>
      Either.catchNonFatal {
        ReleaseVersioning
          .version(programArgs.release, programArgs.hotfix, programArgs.optLatestTag, programArgs.majorVersion)
      }
    } match {
      case Some(Right(nextReleaseVersion)) =>
        Console.out.println(nextReleaseVersion)
        System.exit(0)
      case Some(Left(exception)) =>
        Console.err.println(exception.getMessage)
        System.exit(1)
      case None =>
        System.exit(1)
    }

  private def parseArgs(args: Array[String]): Option[Args] =
    new OptionParser[Args]("release-versioning") {
      opt[Unit]('r', "release")
        .action((_, args) => args.copy(release = true))
        .text("release is a required Boolean argument indicating whether it should be a release or a snapshot")
      opt[Unit]('f', "hotfix")
        .action((_, args) => args.copy(hotfix = true))
        .text("hotfix is a required Boolean argument indicating whether it should be a hotfix or a major/minor release")
      opt[String]('t', "latest-tag")
        .action((gd, args) => args.copy(optLatestTag = Some(gd)))
        .text("latest-tag is an optional argument expecting the latest tag name")
      opt[Int]('m', "major-version")
        .required()
        .action((m, args) => args.copy(majorVersion = m))
        .text("major-version is a required argument with the major version number")
      help("help").text("prints this usage text")
    }.parse(args, Args())
}
