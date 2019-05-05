#!/bin/bash

###
 # Copyright (C) 2019 LAMP/EPFL and Lightbend, Inc.
 #
 # Compare JAR from Sonatype with version tag in Git.
 #
 # Builds a JAR from a tag, and then compares the JAR downloaded from
 # Sonatype.
 #
 # Usage:
 #   ./admin/audit-jar.sh SCALA-XML-VERSION SCALA-VERSION
 #
 # SCALA-XML-VERSION is the scala-xml version, e.g. v1.0.5
 # SCALA-VERSION is the Scala version, e.g. 2.11.12
 #
 # Examples:
 #   ./admin/audit-jar.sh v1.1.1 2.12.6
 #   env PROJECT=xmlJS TARGET_DIR=js/target JAR_DIR=jars \
 #       SCALAJS_SUFFIX=_sjs0.6 SCALAJS_VERSION=0.6.25 \
 #       ./admin/audit-jar.sh v1.1.1 2.11.12
 #
 # Required dependencies:
 #  - sbt 1.x
 #  - git
 #  - jardiff
 ##

set -e

if [ -z "$1" ]; then
    echo "Error: Missing scala-xml version" >&2
    exit 1
elif [ -z "$2" ]; then
    echo "Error: Missing Scala version" >&2
    exit 1
fi

SCALA_XML_TAG=${1%%#*} # Cleanup tags, e.g. v1.1.1#2.13.0-M5#8
SCALA_XML_VERSION=${SCALA_XML_TAG#v}
SCALA_VERSION=$2
SCALA_BINARY_VERSION=${SCALA_VERSION%.*}
SCALAJS_SUFFIX=${SCALAJS_SUFFIX-}

PROJECT=${PROJECT-xml}
TASK=${TASK-package}
TARGET_DIR=${TARGET_DIR-./jvm/target}
IVY_CACHE=${IVY_CACHE-~/.ivy2/cache}
PKG_ORG=${PKG_ORG-org.scala-lang.modules}
PKG_NAME=${PKG_NAME-scala-xml}
JARDIFF_JAR=${JARDIFF_JAR-jardiff.jar}
JAR_DIR=${JAR_DIR-bundles}
GIT_DIR=/tmp/$PKG_NAME${SCALAJS_SUFFIX}-$SCALA_XML_VERSION-$SCALA_VERSION

git checkout $SCALA_XML_TAG
sbt "++$SCALA_VERSION" $PROJECT/$TASK

PUBLISHED_ARTIFACT=$IVY_CACHE/$PKG_ORG/${PKG_NAME}${SCALAJS_SUFFIX}_${SCALA_BINARY_VERSION}/$JAR_DIR/${PKG_NAME}${SCALAJS_SUFFIX}_${SCALA_BINARY_VERSION}-${SCALA_XML_VERSION}.jar

LOCAL_ARTIFACT=$TARGET_DIR/scala-${SCALA_BINARY_VERSION}/${PKG_NAME}${SCALAJS_SUFFIX}_${SCALA_BINARY_VERSION}-${SCALA_XML_VERSION}-SNAPSHOT.jar

DIFF_FILE=$PKG_NAME${SCALAJS_SUFFIX}-$SCALA_XML_VERSION-$SCALA_VERSION.diff

java -jar $JARDIFF_JAR --git $GIT_DIR $PUBLISHED_ARTIFACT $LOCAL_ARTIFACT > $DIFF_FILE

echo "Please review $DIFF_FILE and the repo in $GIT_DIR"

## End of script
