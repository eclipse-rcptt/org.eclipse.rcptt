#!/usr/bin/env bash

set -ex

SOURCE=$1
shift
TARGET=$1
shift

MERGE="merge/$TARGET/$SOURCE"

git config --local push.autoSetupRemote true
git fetch --shallow-since=2025-01-01 origin "$SOURCE" "$TARGET" "$MERGE" || true
git checkout --track "$MERGE" || git checkout -b "$MERGE" "origin/$TARGET"
git merge "origin/$TARGET"
git diff --name-only HEAD...origin/$SOURCE | grep pom.xml$ && exit 2 # Do not merge version bumps and release management
git merge --no-edit "origin/$SOURCE"
git push origin "$MERGE"
gh workflow run verify.yml --ref "$MERGE"
