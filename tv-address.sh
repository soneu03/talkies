# Sourced by the scripts here: which TV to talk to. Pass its address as the last argument, set TALKIES_TV, or save it
# once in a .tv file next to the scripts (git ignores it):  echo 192.168.1.20:5555 > .tv

# Device paths in these scripts are written with two leading slashes (//sdcard/…). Git Bash rewrites an argument that
# begins with a single / into a Windows path before adb sees it, so /sdcard/… arrives as C:\Program Files\Git\sdcard\…
# and the check fails without saying anything; a doubled // is left alone, and Android reads //sdcard as /sdcard.
# Local paths keep the single form, because those are the ones the rewrite exists to translate.

tv_default() {
  if [ -n "${TALKIES_TV:-}" ]; then echo "$TALKIES_TV"
  elif [ -n "${GLASS_TV:-}" ]; then echo "$GLASS_TV"   # the old name still works
  elif [ -f "$(dirname "$0")/.tv" ]; then tr -d ' \r\n' < "$(dirname "$0")/.tv"
  fi
}
need_tv() {
  [ -n "${DEV:-}" ] && return 0
  echo "Which TV? Pass its address (e.g. ./$(basename "$0") 192.168.1.20:5555), set TALKIES_TV, or save it once:"
  echo "  echo 192.168.1.20:5555 > .tv"
  exit 1
}
