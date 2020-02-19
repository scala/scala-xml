#!/bin/bash
#
# Encrypt sonatype credentials so that they can be
# decrypted in trusted builds on Travis CI.
#
set -e

read -s -p 'SONATYPE_USERNAME: ' SONATYPE_USERNAME
travis encrypt SONATYPE_USERNAME="$SONATYPE_USERNAME"
read -s -p 'SONATYPE_PASSWORD: ' SONATYPE_PASSWORD
travis encrypt SONATYPE_PASSWORD="$SONATYPE_PASSWORD"
