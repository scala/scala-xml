#!/bin/bash

set -e

# Builds of tagged revisions are published to sonatype staging.

# Travis runs a build on new revisions and on new tags, so a tagged revision is built twice.
# Builds for a tag have TRAVIS_TAG defined, which we use for identifying tagged builds.
# Checking the local git clone would not work because git on travis does not fetch tags.

# The version number to be published is extracted from the tag, e.g., v1.2.3 publishes
# version 1.2.3 on all combinations of the travis matrix where `[ "$RELEASE_COMBO" = "true" ]`.

# In order to build a previously released version against a new (binary incompatible) Scala release,
# a new commit that modifies (and prunes) the Scala versions in .travis.yml needs to be added on top
# of the existing tag. Then a new tag can be created for that commit, e.g., `v1.2.3#2.13.0-M5`.
# Everything after the `#` in the tag name is ignored.

if [[ "$TRAVIS_JDK_VERSION" == "openjdk6" && "$TRAVIS_SCALA_VERSION" =~ 2\.11\..* \
      || "$TRAVIS_JDK_VERSION" == "oraclejdk8" && "$TRAVIS_SCALA_VERSION" =~ 2\.1[23]\..* ]]; then
  RELEASE_COMBO=true;
fi

if [ "$SCALAJS_VERSION" = "" ]; then
  projectPrefix="xml"
else
  projectPrefix="xmlJS"
fi

verPat="[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9-]+)?"
tagPat="^v$verPat(#.*)?$"

if [[ "$TRAVIS_TAG" =~ $tagPat ]]; then
  tagVer=$(echo $TRAVIS_TAG | sed s/#.*// | sed s/^v//)
  publishVersion='set every version := "'$tagVer'"'

  if [ "$RELEASE_COMBO" = "true" ]; then
    currentJvmVer=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | sed 's/^1\.//' | sed 's/[^0-9].*//')
    echo "Releasing $tagVer with Scala $TRAVIS_SCALA_VERSION on Java version $currentJvmVer."

    publishTask="$projectPrefix/publish-signed"

    cat admin/gpg.sbt >> project/plugins.sbt
    cp admin/publish-settings.sbt .

    # Copied from the output of genKeyPair.sh
    K=$encrypted_6b8d67feaab7_key
    IV=$encrypted_6b8d67feaab7_iv
    openssl aes-256-cbc -K $K -iv $IV -in admin/secring.asc.enc -out admin/secring.asc -d
  fi
fi

sbt "++$TRAVIS_SCALA_VERSION" "$publishVersion" "$projectPrefix/clean" "$projectPrefix/test" "$projectPrefix/publishLocal" "$publishTask"
