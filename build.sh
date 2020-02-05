#!/bin/bash

set -e

# Builds of tagged revisions are published to sonatype staging.

# Travis runs a build on new revisions and on new tags, so a tagged revision is built twice.
# Builds for a tag have TRAVIS_TAG defined, which we use for identifying tagged builds.

# sbt-dynver sets the version number from the tag
# sbt-travisci sets the Scala version from the travis job matrix

# When a new binary incompatible Scala version becomes available, a previously released version
# can be released using that new Scala version by creating a new tag containing the Scala version
# after a hash, e.g., v1.2.3#2.13.0-M3.

# For normal tags that are cross-built, we release on JDK 8 for Scala 2.x and Dotty 0.x
isReleaseJob() {
  if [[ "$ADOPTOPENJDK" == "8" && "$TRAVIS_SCALA_VERSION" =~ ^2\.1[234]\..*$ ]]; then
    true
  elif [[ "$ADOPTOPENJDK" == "8" && "$TRAVIS_SCALA_VERSION" =~ ^0\.[0-9]+\..*$ ]]; then
    true
  else
    false
  fi
}

# For tags that define a Scala version, we pick the jobs of a Scala version (2.13.x) or Dotty (0.x) to do the releases
isTagScalaReleaseJob() {
  if [[ "$ADOPTOPENJDK" == "8" && "$TRAVIS_SCALA_VERSION" =~ ^2\.13\.[0-9]+$ ]]; then
    true
  elif [[ "$ADOPTOPENJDK" == "8" && "$TRAVIS_SCALA_VERSION" =~ ^0\.[0-9]+\..*$ ]]; then
    true
  else
    false
  fi
}

# For tags that define a Scala.js version, we pick the jobs of one Scala.js version (1.0.0) to do the releases
isTagScalaJsReleaseJob() {
  if [[ "$ADOPTOPENJDK" == "8" && "$SCALAJS_VERSION" =~ ^1\.0\.0(-[A-Za-z0-9-]+)?$ ]]; then
    true
  else
    false
  fi
}

if [[ "$SCALAJS_VERSION" == "" ]]; then
  projectPrefix="xml"
else
  projectPrefix="xmlJS"
fi

verPat="[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9-]+)?"
tagPat="^v$verPat(#(sjs_)?$verPat)?$"

if [[ "$TRAVIS_TAG" =~ $tagPat ]]; then
  releaseTask="ci-release"
  tagScalaVer=$(echo $TRAVIS_TAG | sed s/[^#]*// | sed s/^#//)
  if [[ "$tagScalaVer" == "" ]]; then
    if ! isReleaseJob; then
      echo "Not releasing on Java $ADOPTOPENJDK with Scala $TRAVIS_SCALA_VERSION"
      exit 0
    fi
  elif [[ "$tagScalaVer" == "sjs_$SCALAJS_VERSION" ]]; then
    if ! isTagScalaJsReleaseJob; then
      echo "The releases for Scala.js $tagScalaVer are built by other jobs in the travis job matrix"
      exit 0
    fi
  else
    if ! isTagScalaReleaseJob; then
      echo "The releases for Scala $tagScalaVer are built by other jobs in the travis job matrix"
      exit 0
    fi
  fi
fi

# default is +publishSigned; we cross-build with travis jobs, not sbt's crossScalaVersions
export CI_RELEASE="$projectPrefix/publishSigned"
export CI_SNAPSHOT_RELEASE="$projectPrefix/publish"

# default is sonatypeBundleRelease, which closes and releases the staging repo
# see https://github.com/xerial/sbt-sonatype#commands
# for now, until we're confident in the new release scripts, just close the staging repo.
export CI_SONATYPE_RELEASE="; sonatypePrepare; sonatypeBundleUpload; sonatypeClose"

sbt clean $projectPrefix/test $projectPrefix/publishLocal $releaseTask
