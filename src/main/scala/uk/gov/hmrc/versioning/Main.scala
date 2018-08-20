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
  makeRelease: Boolean             = false,
  makeHotfix: Boolean              = false,
  tags: Seq[String]                = Nil,
  maybeGitDescribe: Option[String] = None,
  majorVersion: Int                = 0
)

object Main {

  def main(args: Array[String]): Unit =
    (for {
      programArgs <- parseArgs(args)
      gitDescribe <- programArgs.maybeGitDescribe
    } yield
      Either.catchNonFatal {
        new ReleaseVersioning(programArgs.makeRelease, programArgs.makeHotfix)
          .version(programArgs.tags, gitDescribe, programArgs.majorVersion)
      }) match {
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
      opt[Boolean]('r', "make-release")
        .required()
        .action((r, args) => args.copy(makeRelease = r))
        .text("make-release is a required argument indicating if it should create a release or a snapshot")
      opt[Boolean]('f', "make-hotfix")
        .required()
        .action((hf, args) => args.copy(makeHotfix = hf))
        .text("make-hotfix is a required argument indicating if it should create a hotfix or major/minor release")
      opt[Seq[String]]('t', "tags")
        .required()
        .action((ts, args) => args.copy(tags = ts))
        .text("tags is a required argument containing the output from 'git tag --list'")
      opt[String]('d', "git-describe")
        .required()
        .action((gd, args) => args.copy(maybeGitDescribe = Some(gd)))
        .text("git-describe is a required argument with the output from 'git describe --always'")
      opt[Int]('m', "major-version")
        .required()
        .action((m, args) => args.copy(majorVersion = m))
        .text("major-version is a required argument with the major version number")
    }.parse(args, Args())
}
