#!/usr/bin/env bash

set -ex

SOURCE="$1"
shift
COMMIT="$1"
shift
TARGET="$1"
shift
MESSAGE="$1"

MERGE="merge/$TARGET/$SOURCE"

git config --local push.autoSetupRemote true
git fetch --shallow-since=2025-01-01 origin "$SOURCE" "$TARGET"
if git fetch --shallow-since=2025-01-01 origin "$MERGE" ; then
	git merge-base --is-ancestor "$COMMIT" "$MERGE" && exit 0 # Already merged
	git diff --name-only "$MERGE...$COMMIT" | grep pom.xml$ && exit 2 # Do not merge version bumps and release management
	git checkout --track "$MERGE"
else
	git merge-base --is-ancestor "$COMMIT" "origin/$TARGET" && exit 0 # Already merged
	git diff --name-only "origin/$TARGET...$COMMIT" | grep pom.xml$ && exit 2 # Do not merge version bumps and release management
	git checkout -b "$MERGE" "$COMMIT"
fi
git merge --no-edit "$COMMIT"
git merge "origin/$TARGET" || true # If merge fails due to a conflict, create PR anyway for user to resolve
git push origin "$MERGE"
gh workflow run verify.yml --ref "$MERGE"
gh pr create -B "$TARGET" -H "$MERGE" --title "Merge $SOURCE into $TARGET" --body "$MESSAGE" 