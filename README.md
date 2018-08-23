
# release-versioning

 [ ![Download](https://api.bintray.com/packages/hmrc/releases/release-versioning/images/download.svg) ](https://bintray.com/hmrc/releases/release-versioning/_latestVersion)
 
 This library determines a next release version for a git repository.
 
 It can be used as a library or as a standalone tool.
 
 The rules for determining the next version are following:
 
 1. The library recognises tags in the format: `release/x.y.z` or `vx.y.z` possibly with a suffix `-X-g<short SHA-1 hash commit id>`
 2. It is not possible to change the major version as part of a hotfix
 3. The requested major version must be increased by at most 1
 4. If the repository has not been tagged yet the initial version will be `0.1.0`
 5. A major version bump from `x.y.z` will result in a next version of `x+1.0.0`
 6. If it is a hotfix for version `x.y.z` the new version will become `x.y.z+1`
 7. Otherwise the minor version will be increased by 1 
 8. An unrecognised tag format will cause throwing an exception
 
 ## Usage as a Library
 
 ```scala
 ReleaseVersioning.version(
   release      = <a Boolean value>,
   hotfix       = <a Boolean value>,
   gitDescribe  = <an Option[String] with `git describe` value>,
   majorVersion = <an Int with the desired major version>
 )
 ```
 
 ## Usage as a standalone tool
 
 You can see the available options by running the assembly jar with: 
 
```bash
java -jar $WORKSPACE/release-versioning/target/scala-2.10/release-versioning-assembly-x.y.z.jar --help
```

```bash
Usage: release-versioning [options]

  --help               prints this usage text
  --release            release is an optional flag indicating whether it should be a release or a snapshot
  --hotfix             hotfix is an optional flag indicating whether it should be a hotfix or a major/minor release
  --git-describe <value>
                       git-describe is an optional argument expecting an outcome of `git describe` command
  --major-version <value>
                       major-version is required when git-describe option is defined
```
 
 Examples: 
 
 * Some latest tag defined
 ```bash
 java -jar release-versioning-assembly-x.y.z.jar --release --git-describe v0.1.0 --major-version 0
 ```

 * No latest tag defined
 ```bash
 java -jar release-versioning-assembly-x.y.z.jar --release
 ```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
