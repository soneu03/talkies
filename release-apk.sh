#!/bin/bash
# Builds the release APK, signed with the project's key, and attaches it to the GitHub release of the version in
# app/build.gradle as talkies.apk: what people install with adb, and what Talkies settings → Updates installs.
# Only the maintainer can run it: it needs release.properties (never in git) and the key it points to.
#   ./release-apk.sh            the release must already exist (gh release create vX.Y.Z …)
#   ./release-apk.sh --force    publish it anyway, with the checks below only reported
# Everything it refuses exists because the uploaded asset is what Talkies installs on people's TVs: an APK that
# isn't the code of that version would update every TV in the project's name.
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
cd "$HERE"
. "$HERE/android-env.sh"
FORCE=0
[ "${1:-}" = "--force" ] && FORCE=1 && shift
env_check "$HERE" || exit 1
[ -f release.properties ] || { echo "No release.properties: only the project's key can sign releases."; exit 1; }
command -v gh >/dev/null || { echo "!! Needs the gh command line (brew install gh, then gh auth login)."; exit 1; }

VERSION="$(sed -n "s/.*versionName '\(.*\)'.*/\1/p" app/build.gradle | head -1)"
CODE="$(sed -n 's/^[[:space:]]*versionCode \([0-9]*\).*/\1/p' app/build.gradle | head -1)"
# The key Talkies checks updates against (SelfUpdate.RELEASE_CERT).
CERT="$(sed -n 's/.*RELEASE_CERT = "\([0-9a-f]*\)".*/\1/p' app/src/main/java/com/techpp/glass/SelfUpdate.java)"
HEAD="$(git rev-parse HEAD)"

refuse() {
  if [ "$FORCE" = 1 ]; then echo "!! $1  (--force: continuing)"; else echo "!! $1"; exit 1; fi
}

# 1. What gets published has to be exactly a commit: no half-finished edit inside a release asset. Compared by
#    content, not by git status: a Windows checkout leaves files flagged as modified on nothing but their timestamps.
DIRTY="$( { git diff --name-only HEAD; git diff --cached --name-only; } 2>/dev/null | grep -v '^$' | sort -u )"
[ -z "$DIRTY" ] || refuse "these files differ from the commit: $(echo "$DIRTY" | tr '\n' ' ')  - commit them first"
# 2. The version has to be released in CHANGELOG.md, not still sitting under "## Unreleased". The heading is
#    "## 1.2.11 — 8 November 2026", so it is a prefix, not a whole line; and the \r goes first, because a Windows
#    checkout puts one at the end of every line.
SECTION="## $VERSION"
tr -d '\r' < CHANGELOG.md | awk -v s="$SECTION" 'index($0, s " ") == 1 || $0 == s { found = 1; exit }
                                            END { exit found ? 0 : 1 }' \
  || refuse "CHANGELOG.md has no '## $VERSION' section: say what this version is"
# 3. Android only installs a *higher* versionCode, so a repeat means an update nobody can take. Ask for the last
#    release before this one and compare.
PREV_TAG="$(git tag --sort=-v:refname --contains HEAD 2>/dev/null | grep '^v' | sed -n 2p || true)"
if [ -n "$PREV_TAG" ]; then
  PREV_CODE="$(git show "$PREV_TAG:app/build.gradle" | sed -n 's/^[[:space:]]*versionCode \([0-9]*\).*/\1/p' | head -1)"
  [ "$CODE" -gt "$PREV_CODE" ] || refuse "versionCode is still $CODE, same as in $PREV_TAG"
fi
# 4. The release on GitHub must be the commit we are building, or its talkies.apk would stop matching its tag.
if RELEASE_COMMIT="$(gh release view "v$VERSION" --json targetCommitish --jq .targetCommitish 2>/dev/null)"; then
  case "$RELEASE_COMMIT" in
    "") echo "   Release v$VERSION doesn't name a commit; the tag should point here."; refuse "v$VERSION points nowhere: gh release create v$VERSION $HEAD" ;;
    "$HEAD"|"$HEAD"*|*"$HEAD"*) ;;   # the commit we are on
    *) refuse "release v$VERSION points at $RELEASE_COMMIT, not at $HEAD: make the tag, or --force" ;;
  esac
else
  refuse "no release v$VERSION on GitHub yet: gh release create v$VERSION $(git rev-parse --short HEAD)"
fi

echo "== Building $VERSION ($CODE) from $(git rev-parse --short HEAD)"
./gradlew -q clean assembleRelease
APK=app/build/outputs/apk/release/app-release.apk
SIGNER="$(apksigner_bin)"   # build-tools by version, and the .bat Windows names it with
GOT="$("$SIGNER" verify --print-certs "$APK" | sed -n 's/.*certificate SHA-256 digest: //p' | head -1)"
[ "$GOT" = "$CERT" ] || { echo "!! The APK isn't signed with the release key (got $GOT)."; exit 1; }
cp "$APK" "$HERE/build/talkies.apk" 2>/dev/null || { mkdir -p build; cp "$APK" build/talkies.apk; }
echo "== Attaching talkies.apk ($(wc -c < build/talkies.apk | tr -d ' ') bytes) to v$VERSION"
gh release upload "v$VERSION" build/talkies.apk --clobber
echo "Done: $(gh release view "v$VERSION" --json url --jq .url)"
