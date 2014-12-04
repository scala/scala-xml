#!/bin/bash

# prep environment for publish to sonatype staging if the HEAD commit is tagged

# git on travis does not fetch tags, but we have TRAVIS_TAG
# headTag=$(git describe --exact-match ||:)

if [[ "$TRAVIS_TAG" =~ ^v[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9-]+)? ]]; then
  echo "Going to release from tag $TRAVIS_TAG!"
  export publishVersion="set every version := \"$(echo $TRAVIS_TAG | sed -e s/^v//)\""
  export extraTarget="publish-signed"
  cat admin/gpg.sbt >> project/plugins.sbt
  admin/decrypt.sh sensitive.sbt
  (cd admin/ && ./decrypt.sh secring.asc)
fi
