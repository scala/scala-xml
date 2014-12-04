#!/bin/bash

# prep environment for publish to sonatype staging if the HEAD commit is tagged

headTag=$(git describe --exact-match ||:)

if [[ "$headTag" =~ ^v[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9-]+)? ]]; then
  echo "HEAD is tagged as $headTag."
  export publishVersion="set every version := \"$(echo $headTag | sed -e s/^v//)\""
  export extraTarget="publish-signed"
  cat admin/gpg.sbt >> project/plugins.sbt
  admin/decrypt.sh sensitive.sbt
  (cd admin/ && ./decrypt.sh secring.asc)
fi
