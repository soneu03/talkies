#!/bin/bash
# Puts the TV back the way it was.
#   ./restore.sh                 the TV's own home screen back, Talkies removed
#   ./restore.sh --keep-talkies  the TV's own home screen back, Talkies left installed
#   ./restore.sh --yes           the same, without asking (for scripts and AI assistants)
# Optional last argument: the TV's address (default: TALKIES_TV or the .tv file, see tv-address.sh).
set -euo pipefail
. "$(dirname "$0")/tv-address.sh"
KEEP=0; DEV="$(tv_default)"; ASSUME_YES=0
for a in "$@"; do
  case "$a" in
    --keep-talkies|--keep-glass) KEEP=1 ;;
    --yes|-y) ASSUME_YES=1 ;;
    *) DEV="$a" ;;
  esac
done
need_tv
adb connect "$DEV" >/dev/null 2>&1 || true
# Called tv_, not sh: a function named sh hides the shell itself, and adb's answer then runs through it.
tv() { adb -s "$DEV" shell -n "$@" | tr -d '\r'; }

echo "== Restoring the TV's own home screen"
for p in com.google.android.apps.tv.launcherx com.google.android.tungsten.setupwraith; do
  tv pm path "$p" >/dev/null 2>&1 || continue   # not a Google TV: nothing of Google's to put back
  tv pm enable "$p" >/dev/null 2>&1 || tv cmd package install-existing "$p"
done
if tv pm path com.google.android.apps.tv.launcherx >/dev/null 2>&1; then
  STOCK=com.google.android.apps.tv.launcherx/.home.HomeActivity
else
  # Other TVs: the first home screen that came with the TV (a system app), not Talkies or a fallback page.
  SYSTEM="$(tv pm list packages -s)"
  STOCK=""
  while IFS= read -r a; do
    case "$a" in com.techpp.glass/*|*FallbackHome*|*setupwraith*|*Settings*) continue ;; esac
    if printf '%s\n' "$SYSTEM" | grep -qx "package:${a%%/*}"; then STOCK="$a"; break; fi
  done < <(tv cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME | grep /)
fi
# A component name goes back to the TV through a shell, so it is checked before it is sent: nothing that came off
# dumpsys gets to decide what the device runs.
if [ -n "$STOCK" ]; then
  case "$STOCK" in
    *[!A-Za-z0-9._/-]*) echo "!! Ignoring a home screen whose name isn't a plain component ($STOCK)"; STOCK="" ;;
  esac
fi
[ -z "$STOCK" ] || tv cmd package set-home-activity --user 0 "$STOCK" \
  || echo "!! The TV wouldn't take ${STOCK:-a built-in home screen} back; set it in its own settings."

if [ "$KEEP" = 0 ]; then
  echo "Next: uninstall Talkies, which takes its settings and your TMDB key with it."
  echo "     (./backup-settings.sh saves them first, and ./restore.sh --keep-talkies leaves the app alone.)"
  [ "$ASSUME_YES" = 1 ] || { printf 'Uninstall Talkies? [y/N] '; read -r REPLY; [ "$REPLY" = "y" ] || [ "$REPLY" = "Y" ] || {
    echo "Left installed. Home is the TV's own again; drop Talkies yourself when you like."; exit 0; }; }
  adb -s "$DEV" uninstall com.techpp.glass >/dev/null 2>&1 || echo "!! Talkies is still installed."
fi

tv input keyevent KEYCODE_HOME
echo "Default home now: $(tv cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME | tail -1)"
