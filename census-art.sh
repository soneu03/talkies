#!/bin/bash
# census-art.sh: how Talkies got the pictures on its cards, as text you can paste into a conversation.
#
# It reads three things and never writes to the TV: Watch Next as Android sees it (which artwork address each entry
# carries), what Talkies fetched and what it could not (its own log), and what landed in the art cache on disk. That
# separates the two ways a row can show the same picture twice - the provider really publishes one image for a whole
# series, or two different addresses came out with the same file name - and the reason a card is empty (no address at
# all, a fetch that failed, or a picture too big to decode before this script's fix).
#
#   ./census-art.sh [tv-ip:port]        read the TV, save artwork-census.txt here, print the report
#   ./census-art.sh --from FILE         print a report from a bundle saved earlier (yours, or someone else's)
#   ./census-art.sh --log-only [tv]     just the fetched and failed lines, no queries
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
PKG=com.techpp.glass
DIR="//sdcard/Android/data/$PKG/files"
ART_DIRS="$DIR/recent/watchnext $DIR/channels $DIR/trending $DIR/recent $DIR/cricket"

MODE=live
OUT="$HERE/artwork-census.txt"
DEV=""
while [ $# -gt 0 ]; do
  case "$1" in
    --from) MODE=from; OUT="${2:?--from needs a bundle file}"; shift 2 ;;
    --log-only) MODE=live; LOGONLY=1; shift ;;
    *) DEV="$1"; shift ;;
  esac
done
[ "$MODE" = from ] || { . "$HERE/tv-address.sh"; DEV="${DEV:-$(tv_default)}"; need_tv; }
# Only --from reads a bundle someone else saved; live mode is about to write it.
[ "$MODE" != from ] || [ -f "$OUT" ] || { echo "!! no bundle at $OUT." >&2; exit 1; }

collect() {
  local A="adb -s $DEV shell -n"
  echo "##SCREEN"
  echo "model: $($A getprop ro.product.model | tr -d '\r')"
  echo "sdk: $($A getprop ro.build.version.sdk | tr -d '\r')"
  echo "$($A wm size | tr -d '\r')"
  echo "$($A wm density | tr -d '\r')"
  echo "##QUERY"
  # No projection: some builds name these columns differently, and the whole row is easier to read when a value is
  # missing than a projection that quietly returns nothing. The error stays in the bundle: adb shell isn't allowed to
  # read Watch Next on every TV, and a table that says "0 rows" then blames the provider for our own lack of rights.
  $A content query --uri content://android.media.tv/watch_next_programs 2>&1 | tr -d '\r' || true
  echo "##LOG"
  adb -s "$DEV" logcat -d -s GlassWatchNext:V GlassTrending:V GlassChannels:V 2>/dev/null | tr -d '\r' || true
  echo "##DISK"
  if [ "${LOGONLY:-}" = 1 ]; then echo "(skipped: --log-only)"; else $A "ls -l $ART_DIRS 2>/dev/null" | tr -d '\r' || true; fi
}

[ "$MODE" = live ] && collect > "$OUT"
echo "bundle: $OUT ($(wc -l < "$OUT" | tr -d ' ') lines)"

awk '
function value(line, key,   i, s, j, k, c) {
    i = index(line, key "=")
    if (i == 0) return ""
    s = substr(line, i + length(key) + 1)
    j = length(s)
    for (k = 1; k <= length(s); k++) {
        c = substr(s, k, 1)
        if (c == "," || c == " " || c == "\t") { j = k - 1; break }
    }
    s = substr(s, 1, j)
    if (s == "null" || s == "(null)") return ""
    return s
}
function host(u,   i, s) {
    if (u == "") return "(no address)"
    i = index(u, "://")
    if (i == 0) return u
    s = substr(u, i + 3)
    i = index(s, "/")
    return (i > 0 ? substr(s, 1, i - 1) : s)
}
function trim(s) { sub(/[ \t]+$/, "", s); return s }

# Alphabetical order, so two TVs and two runs give a report that can be compared line by line.
function sorted(cnt, keys,   i, j, k) {
    for (i = 2; i <= cnt; i++) {
        k = keys[i]; j = i - 1
        while (j >= 1 && keys[j] > k) { keys[j + 1] = keys[j]; j-- }
        keys[j + 1] = k
    }
}
function keys_of(src, into,   p, cnt) {
    delete into; cnt = 0
    for (p in src) into[++cnt] = p
    sorted(cnt, into)
    return cnt
}

/^##SCREEN/ { sec = "s"; next }
/^##QUERY/ { sec = "q"; next }
/^##LOG/   { sec = "l"; next }
/^##DISK/  { sec = "d"; next }

sec == "s" {
    if ($0 ~ /model:/) { sub(/^model:[ \t]*/, ""); model = $0 }
    else if ($0 ~ /sdk:/) { sub(/^sdk:[ \t]*/, ""); sdk = $0 }
    else if ($0 ~ /size:/) { if ($0 ~ /Override/) osize = $NF; else psize = $NF }
    else if ($0 ~ /density:/) { if ($0 ~ /Override/) odens = $NF + 0; else pdens = $NF + 0 }
    next
}

sec == "q" {
    if ($0 !~ /CursorRow/) { if (qerr == "" && $0 ~ /Error|Exception|denied|permission/) qerr = trim($0); next }
    pkg = value($0, "package_name")
    if (pkg == "") pkg = "(unknown package)"
    thumb = value($0, "thumbnail_uri")
    poster = value($0, "poster_art_uri")
    n[pkg]++
    if (thumb != "") { withThumb[pkg]++; pair = pkg SUBSEP thumb
        if (pair in uri) dup[pkg]++; else { uri[pair] = 1; uniqThumb[pkg]++ } }
    if (poster != "") { withPoster[pkg]++; ppair = pkg SUBSEP poster
        if (ppair in uriP) dupP[pkg]++; else { uriP[ppair] = 1; uniqPoster[pkg]++ } }
    next
}

sec == "l" {
    i = index($0, ": art ")
    if (i > 0) {
        split(trim(substr($0, i + 6)), a, " ")
        if (a[3] != "->") next
        got++
        h = host(a[1])
        fetched[h]++
        if (!(h in seenSize)) { seenSize[h] = 1; sizes[h] = a[2] }
        else if (index(sizes[h], a[2]) == 0 && sizeCount[h] < 4) { sizes[h] = sizes[h] " " a[2]; sizeCount[h]++ }
        if (destFiles[a[4]] == "") destFiles[a[4]] = a[1]
        else if (index(destFiles[a[4]], a[1]) == 0) destFiles[a[4]] = destFiles[a[4]] " | " a[1]
        next
    }
    if ($0 ~ /Artwork failed/) {
        f = trim(substr($0, index($0, "Artwork failed")))
        fails++
        hi = index(f, " from ")
        h = hi > 0 ? host(substr(f, hi + 6)) : "(unknown)"
        failAt[h]++
        why[f]++
        next
    }
    next
}

sec == "d" {
    if ($0 ~ /^[^ ]*:$/) { cwd = substr($0, 1, length($0) - 1); sub(/^\/?\/?sdcard\/Android\/data\/[^/]*\/files/, "...", cwd); next }
    if ($0 !~ /^[-dl]/) next
    if ($NF ~ /\.raw$/) stray++
    d[cwd]++
    dkb[cwd] += $5 / 1024
    if ($5 + 0 > biggest[cwd] + 0) { biggest[cwd] = $5; bigName[cwd] = $NF }
    # What shape each kept picture is, read out of its name: a card is now cut to the shape of its picture, so the
    # balance of posters to frames across a TV is what says whether that ever shows.
    p = index($NF, "x")
    if ($NF ~ /_blur/) p = 0   # a blur of a picture is that picture counted twice; the original is already here
    if (p > 1) {
        head = substr($NF, 1, p - 1); tail = substr($NF, p + 1)
        w = 0; mul = 1
        for (j = length(head); j >= 1; j--) { ch = substr(head, j, 1); if (ch < "0" || ch > "9") break; w = w + (ch - "0") * mul; mul *= 10 }
        h = 0
        for (j = 1; j <= length(tail); j++) { ch = substr(tail, j, 1); if (ch < "0" || ch > "9") break; h = h * 10 + (ch - "0") }
        if (w > 0 && h > 0) { r = w / h; tot[cwd]++
            if (r < 0.8) po[cwd]++; else if (r < 1.2) sq[cwd]++; else if (r < 1.55) fo[cwd]++; else fr[cwd]++ }
    }
    next
}

END {
    # The screen in dp, which is what every card constant is written in: this is the number the redesign needs, and
    # it comes from the TV rather than from a guess about it.
    if (psize != "" || osize != "") {
        wp = (osize != "" ? osize : psize); dp = (odens > 0 ? odens : pdens)
        split(wp, s, "x")
        if (dp > 0 && s[1] + 0 > 0) {
            dw = int(s[1] * 160 / dp + 0.5); dh = int(s[2] * 160 / dp + 0.5)
            printf "== The TV\n   %s, Android API %s\n   %s px at %d dpi = %dx%d dp\n",
                   model, sdk, wp, dp, dw, dh
            printf "   a card measured in dp is that many px here: large 331dp = %dpx (PicksRow LARGE_W),\n",
                   int(331 * dp / 160 + 0.5)
            printf "   small 134dp = %dpx, tall 186dp = %dpx, channel 200x112dp = %dx%dpx\n",
                   int(134 * dp / 160 + 0.5), int(186 * dp / 160 + 0.5), int(200 * dp / 160 + 0.5), int(112 * dp / 160 + 0.5)
        }
        print ""
    }

    print "== Watch Next, as the TV itself publishes it"
    printf "%-38s %5s %7s %7s %9s\n", "package", "rows", "thumb", "poster", "reused"
    cnt = keys_of(n, ks)
    for (i = 1; i <= cnt; i++) { p = ks[i]
        printf "%-38s %5d %7d %7d %9d\n", p, n[p], withThumb[p] + 0, withPoster[p] + 0, dup[p] + dupP[p] }
    if (cnt == 0) {
        if (qerr != "") print "   nothing read: " qerr
        print "   so this table is empty because adb shell has no right to ask the TV for Watch Next, not because the"
        print "   TV has nothing to give. Read the two sections below (what Talkies fetched, what is on disk) instead."
    } else {
        print "   rows = entries; thumb/poster = how many carry that address; reused = entries whose address another"
        print "   entry already carries. A big reused column means the provider gives one picture per series, which no"
        print "   code can invent; a zero thumb column means there is nothing to fetch at all."
    }

    print ""
    print "== What Talkies fetched (its log; a warm cache adds nothing to it)"
    if (got == 0) print "   no art lines: every address was already cached. Force a census by clearing the art dirs,"
    if (got == 0) print "   or use --log-only after a fresh install."
    else {
        printf "   %-40s %6s  %s\n", "where from", "ok", "sizes found at the source"
        cnt = keys_of(fetched, ks)
        for (i = 1; i <= cnt; i++) { h = ks[i]; printf "   %-40s %6d  %s\n", h, fetched[h], sizes[h] }
    }

    print ""
    print "== Failures"
    if (fails == 0) print "   none in the log."
    else {
        printf "   %-40s %6s\n", "where from", "times"
        cnt = keys_of(failAt, ks)
        for (i = 1; i <= cnt; i++) { h = ks[i]; printf "   %-40s %6d\n", h, failAt[h] }
        print "   reasons:"
        cnt = keys_of(why, ks)
        for (i = 1; i <= cnt; i++) printf "     %dx  %s\n", why[ks[i]], ks[i]
    }

    print ""
    print "== On disk"
    printf "   %-34s %7s %8s  %s\n", "dir", "files", "KB", "biggest"
    cnt = keys_of(d, ks)
    for (i = 1; i <= cnt; i++) { k = ks[i]
        printf "   %-34s %7d %8d  %s\n", k, d[k], dkb[k], bigName[k]
        if (tot[k] > 0) printf "       of those %d sized: frames %d, four-three %d, square %d, posters %d\n",
                              tot[k], fr[k] + 0, fo[k] + 0, sq[k] + 0, po[k] + 0
    }
    if (stray > 0) printf "   %d leftover .raw files (a download died halfway; the next load clears them)\n", stray
    print "   shapes come from the size kept in each file name, so a card and its picture are decided by the same"
    print "   number. Poster-only addresses show up there as posters, not as frames cut down to fit one."

    print ""
    print "== One file name from two different addresses (the bug, not the provider)"
    hits = 0
    cnt = keys_of(destFiles, ks)
    for (i = 1; i <= cnt; i++) if (destFiles[ks[i]] ~ /\|/) { print "   " ks[i] " <- " destFiles[ks[i]]; hits++ }
    if (hits == 0) print "   none in this log."
    else {
        print "   " hits " file name(s) shared by different addresses: the name comes from a 32-bit hash of the"
        print "   address, so this needs the content-based key, not a retry."
    }
}
' "$OUT"
