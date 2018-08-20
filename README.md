
# release-versioning

 [ ![Download](https://api.bintray.com/packages/hmrc/releases/release-versioning/images/download.svg) ](https://bintray.com/hmrc/releases/release-versioning/_latestVersion)
 
 This library determines the next release version for a git repository.
 
 It can be used as a library or as a standalone tool.
 
 The rules for determining the next version are the following:
 
 1. The library recognises tags in the format: `release/x.y.z` or `vx.y.z` possibly with a suffix `-g<short SHA1 hash commit id`
 2. It is not possible to change the major version as part of a hotfix
 3. The requested major version must be increased by at most 1
 4. If the repository is untagged the initial version will be `0.1.0`
 5. A major version bump from `x.y.z` will result in a next version of `x+1.0.0`
 6. If it is a hotfix for version `x.y.z` the new version will become `x.y.z+1`
 7. Otherwise the minor version will be increased by 1 
 8. An unrecognised tag format will throw an exception
 
 ## Usage as a Library
 
 ```scala
 
 val releaseVersioning = new ReleaseVersionFinder(
   makeRelease = <a Boolean value>,
   makeHotfix  = <a Boolean value>
 )
  
 releaseVersioning.version(
   tags = <a Seq of tags>,
   gitDescribe = <output of `git describe --always`>,
   majorVersion = <desired major version>
 )
 ```
 
 ## Usage as a standalone tool
 
 You can see the available options by running the assembly jar with: 
 
```bash
java -jar $WORKSPACE/release-versioning/target/scala-2.10/release-versioning-assembly-x.y.z-SNAPSHOT.jar --help
```


```bash
  Usage: release-versioning [options]

  -r, --make-release <value>
                           make-release is a required argument indicating if it should create a release or a snapshot
  -f, --make-hotfix <value>
                           make-hotfix is a required argument indicating if it should create a hotfix or major/minor release
  -t, --tags <value>       tags is a required argument containing the output from 'git tag --list'
  -d, --git-describe <value>
                           git-describe is a required argument with the output from 'git describe --always'
  -m, --major-version <value>
                           major-version is a required argument with the major version number
  --help                   prints this usage text
```
 
 Example: 
 
 ```bash
 java -jar $WORKSPACE/release-versioning/target/scala-2.10/release-versioning-assembly-0.2.0-SNAPSHOT.jar -r true -f false -t $(git tag --list | tr '\n' ',') -d $(git describe --always) -m 0
 ```




### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
