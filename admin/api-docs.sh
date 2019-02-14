#!/bin/bash

###
 # Copyright (C) 2019 LAMP/EPFL and Lightbend, Inc.
 #
 # Build API docs for scala-xml.
 #
 # Runs Scaladoc, then commits to the gh-pages branch, so they are
 # published to https://docs.scala-lang.org/scala-xml/api/1.0.0/
 #
 # Usage:
 #   ./admin/api-docs.sh SCALA-XML-VERSION SCALA-VERSION
 #
 # SCALA-XML-VERSION is the scala-xml version, e.g. v1.0.5
 # SCALA-VERSION is the Scala version, e.g. 2.11.12
 #
 # Example:
 #   ./admin/api-docs.sh v1.0.6 2.12.0
 #
 # Required dependencies:
 #  - sbt 1.x
 #  - git
 #  - rsync
 ##

set -e

if [ -z "$1" ]; then
    echo "Error: Missing scala-xml version" >&2
    exit 1
elif [ -z "$2" ]; then
    echo "Error: Missing Scala version" >&2
    exit 1
fi

SCALA_XML_VERSION=${1%%#*} # Cleanup tags, e.g. v1.1.1#2.13.0-M5#8
SCALA_VERSION=$2

TARGET_DIR=${TARGET_DIR-./jvm/target}
API_DIR=$TARGET_DIR/scala-${SCALA_VERSION%.*}/api
GIT_DIR=${GIT_DIR-./jvm/target}
DOC_DIR=$GIT_DIR/api/${SCALA_XML_VERSION#v}

git checkout $SCALA_XML_VERSION
sbt "++$SCALA_VERSION" doc
mkdir $GIT_DIR/api
rsync -a $API_DIR/ $DOC_DIR/
echo "Initializing git directory in $GIT_DIR"
cd $GIT_DIR
git init
git remote add upstream git@github.com:scala/scala-xml.git
git fetch upstream gh-pages
git checkout gh-pages
git add -A ./api
git commit -m"Scaladoc for $SCALA_XML_VERSION with Scala $SCALA_VERSION"
echo "Please review the commit in $GIT_DIR and push upstream"

exit 0
## End of script

## Rebuild the universe with:

env JAVA_HOME=$(/usr/libexec/java_home -v 1.6) TARGET_DIR=./target ./admin/api-docs.sh v1.0.1 2.11.12
env JAVA_HOME=$(/usr/libexec/java_home -v 1.6) TARGET_DIR=./target ./admin/api-docs.sh v1.0.2 2.11.12
env JAVA_HOME=$(/usr/libexec/java_home -v 1.6) TARGET_DIR=./target ./admin/api-docs.sh v1.0.3 2.11.12
env JAVA_HOME=$(/usr/libexec/java_home -v 1.6) TARGET_DIR=./target ./admin/api-docs.sh v1.0.4 2.11.12
env JAVA_HOME=$(/usr/libexec/java_home -v 1.6) TARGET_DIR=./target ./admin/api-docs.sh v1.0.5 2.11.12
env JAVA_HOME=$(/usr/libexec/java_home -v 1.8) TARGET_DIR=./target ./admin/api-docs.sh v1.0.6 2.12.0
env JAVA_HOME=$(/usr/libexec/java_home -v 1.8) ./admin/api-docs.sh v1.1.0 2.12.4
env JAVA_HOME=$(/usr/libexec/java_home -v 1.8) ./admin/api-docs.sh v1.1.1 2.12.6
