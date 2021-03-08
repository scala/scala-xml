#!/bin/bash

set -e

# Builds of tagged revisions are published to sonatype staging.

# Travis runs a build on new revisions and on new tags, so a tagged revision is built twice.
# Builds for a tag have TRAVIS_TAG defined, which we use for identifying tagged builds.

# sbt-dynver sets the version number from the tag
# sbt-travisci sets the Scala version from the travis job matrix

# To back-publish an existing release for a new Scala / Scala.js / Scala Native version:
# - check out the tag for the version that needs to be published
# - change `.travis.yml` to adjust the version numbers and trim down the build matrix as necessary
# - commit the changes and tag this new revision with an arbitrary suffix after a hash, e.g.,
#   `v1.2.3#dotty-0.27` (the suffix is ignored, the version will be `1.2.3`)

# We release on JDK 8 (for Scala 2.x and Dotty 0.x)
isReleaseJob() {
  if [[ "$ADOPTOPENJDK" == "8" ]]; then
    true
  else
    false
  fi
}

if [[ "$SCALAJS_VERSION" == "" ]] && [[ "$SCALANATIVE_VERSION" == "" ]]; then
  projectPrefix="xml/"
elif [[ "$SCALAJS_VERSION" == "" ]]; then
  projectPrefix="xmlNative/"
else
  projectPrefix="xmlJS/"
fi

verPat="[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9-]+)?"
tagPat="^v$verPat(#.*)?$"

if [[ "$TRAVIS_TAG" =~ $tagPat ]]; then
  releaseTask="ci-release"
  if ! isReleaseJob; then
    echo "Not releasing on Java $ADOPTOPENJDK with Scala $TRAVIS_SCALA_VERSION"
    exit 0
  fi
fi

# default is +publishSigned; we cross-build with travis jobs, not sbt's crossScalaVersions
export CI_RELEASE="${projectPrefix}publishSigned"
export CI_SNAPSHOT_RELEASE="${projectPrefix}publish"

# default is sonatypeBundleRelease, which closes and releases the staging repo
# see https://github.com/xerial/sbt-sonatype#commands
# for now, until we're confident in the new release scripts, just close the staging repo.
export CI_SONATYPE_RELEASE="; sonatypePrepare; sonatypeBundleUpload; sonatypeClose"

# change this only when we need to update the sbt-the-bash-script
SBT_LAUNCH_VER=1.4.8
curl -L --silent "https://github.com/sbt/sbt/releases/download/v$SBT_LAUNCH_VER/sbt-$SBT_LAUNCH_VER.tgz" > $HOME/sbt.tgz
tar zxf $HOME/sbt.tgz
export PATH="$HOME/sbt/bin:$PATH"
which sbt
sbt clean ${projectPrefix}test ${projectPrefix}publishLocal $releaseTask
