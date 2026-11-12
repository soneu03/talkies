# Sourced by build-and-install.sh and release-apk.sh: where JDK 17 and the Android SDK are on this machine, and which
# apksigner to use. macOS looks in Homebrew, Linux in the paths the package managers use, Windows (Git Bash) in the
# usual SDK folders. Anything you already set in JAVA_HOME or ANDROID_HOME wins over all of it.

# The JDK's home, guessed from the platform; on Windows from the java on PATH.
java_home_guess() {
  local d j
  case "$(uname -s)" in
    Darwin*)
      for d in /opt/homebrew/opt/openjdk@17 /usr/local/opt/openjdk@17; do
        [ -d "$d/libexec/openjdk.jdk/Contents/Home" ] && { echo "$d/libexec/openjdk.jdk/Contents/Home"; return; }
      done ;;
    MINGW*|MSYS*|CYGWIN*)
      j="$(command -v java.exe || command -v java || true)"
      [ -n "$j" ] && { cd "$(dirname "$j")/.." && { pwd -W 2>/dev/null || pwd; }; } && return ;;
    Linux*)
      for d in /usr/lib/jvm/java-17-openjdk-amd64 /usr/lib/jvm/java-17-openjdk /usr/lib/jvm/temurin-17-jdk-amd64 \
               /usr/lib/jvm/java-17 /usr/java/latest; do
        [ -d "$d" ] && { echo "$d"; return; }
      done ;;
  esac
}

# The SDK root: Android Studio's default for this platform, then the command-line tools' own folders.
android_home_guess() {
  local d
  case "$(uname -s)" in
    Darwin*)  for d in "$HOME/Library/Android/sdk" /opt/homebrew/share/android-commandlinetools \
                       /usr/local/share/android-commandlinetools; do
                [ -d "$d" ] && { echo "$d"; return; } done ;;
    MINGW*|MSYS*|CYGWIN*)
              for d in "C:/Android" "C:/Users/$USERNAME/AppData/Local/Android/Sdk" "C:/android-sdk" \
                       "/c/Android" "/c/Users/$USERNAME/AppData/Local/Android/Sdk"; do
                [ -d "$d" ] && { echo "$d"; return; } done ;;
    Linux*)   for d in "$HOME/Android/Sdk" /opt/android-sdk /usr/lib/android-sdk; do
                [ -d "$d" ] && { echo "$d"; return; } done ;;
  esac
}

# Stops with what to install, rather than letting Gradle fail three lines later with a message about SDK locations.
env_check() {
  local here="${1:-$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)}"
  [ -n "${JAVA_HOME:-}" ] || export JAVA_HOME="$(java_home_guess)"
  [ -n "${ANDROID_HOME:-}" ] || export ANDROID_HOME="$(android_home_guess)"
  [ -n "${ANDROID_SDK_ROOT:-}" ] || export ANDROID_SDK_ROOT="$ANDROID_HOME"
  if [ -z "${JAVA_HOME:-}" ] || [ ! -x "$JAVA_HOME/bin/java" ]; then
    echo "!! No JDK 17. Set JAVA_HOME (macOS: brew install openjdk@17; Windows: install one and set the variable)." >&2
    return 1
  fi
  if [ -z "${ANDROID_HOME:-}" ] || [ ! -d "$ANDROID_HOME/platforms" ]; then
    echo "!! No Android SDK. Set ANDROID_HOME (docs/building.md) or install Android Studio." >&2
    return 1
  fi
  # Gradle reads local.properties first; it is git-ignored, so writing it only ever helps this machine.
  if [ ! -f "$here/local.properties" ]; then
    printf 'sdk.dir=%s\n' "${ANDROID_HOME//\\//}" > "$here/local.properties"
    echo "== Wrote local.properties (sdk.dir=$ANDROID_HOME); git ignores it."
  fi
  return 0
}

# The newest build-tools, by version rather than by name: 9.x would sort below 10 with a plain ls.
build_tools_dir() {
  ls -d "$ANDROID_HOME"/build-tools/*/ 2>/dev/null | tr -d '\r' | sed 's:/*$::' | sort -V | tail -1
}

# apksigner, by the right name for this OS: Windows ships a .bat where macOS and Linux ship a shell script.
apksigner_bin() {
  local d; d="$(build_tools_dir)"
  [ -n "$d" ] || { echo "!! No build-tools in $ANDROID_HOME: sdkmanager \"build-tools;35.0.0\"" >&2; return 1; }
  if [ -f "$d/apksigner" ]; then echo "$d/apksigner"; else echo "$d/apksigner.bat"; fi
}
