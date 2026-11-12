#!/bin/bash
# Checks the words before Android does. Three things take Talkies' screen down or leave it half translated:
#   - a percent with a number and no dollar after it: Android throws UnknownFormatConversionException the moment the
#     string is reached, which is how Home ended up crash-looping, black and flickering once a second, on a TV with
#     developer options on;
#   - a name that exists in one language and not the other, or one the Java asks for that no strings.xml has;
#   - a getString call with fewer arguments than its placeholders need (MissingFormatArgumentException, same crash).
# It reads text files only, so it costs a second and needs nothing beyond awk and comm. build-and-install.sh runs it
# before it builds, so a bad string never reaches a TV.
#   ./check-l10n.sh [repo-root]
set -euo pipefail
HERE="${1:-$(cd "$(dirname "$0")" && pwd)}"
RES="$HERE/app/src/main/res"
EN="$RES/values/strings.xml"
ES="$RES/values-es/strings.xml"
JAVA="$HERE/app/src/main/java/com/techpp/glass"
[ -f "$EN" ] && [ -f "$ES" ] || { echo "!! no $EN or $ES: pass the repo root." >&2; exit 1; }
ls "$JAVA"/*.java >/dev/null 2>&1 || { echo "!! no Java under $JAVA." >&2; exit 1; }

# 1. The same names in both languages. plurals count too: an Android name is one or the other.
names() { sed -n 's/.*<\(string\|plurals\) name="\([^"]*\)".*/\2/p' "$1" | LC_ALL=C sort; }
ONLY="$(LC_ALL=C comm -3 <(names "$EN") <(names "$ES"))"

# 2. The placeholders themselves, how many arguments each language needs, and what each call site passes.
SAYS="$(awk -v en="$EN" -v es="$ES" '
BEGIN { SQ = "\047" }        # an apostrophe, which this program cannot hold literally
BEGIN { CAP = 4000 }         # a call spanning more than this is judged no further; see the skipped count

function name_of(line,   i, s, j) {
    i = index(line, "name=\042")
    s = substr(line, i + 6)
    j = index(s, "\042")
    return substr(s, 1, j - 1)
}
function body_of(line, closer,   i, s, j) {
    i = index(line, ">")
    s = substr(line, i + 1)
    j = index(s, closer)
    return j == 0 ? "" : substr(s, 1, j - 1)
}

# How many arguments a format needs, and what is wrong with it.
function analyze(t, n, file,   L, i, j, c, digits, idx, plain, pos, maxp, flags) {
    L = length(t); i = 1; plain = 0; pos = 0; maxp = 0
    split("", seen)
    flags = "-+ 0#,(<.0123456789"
    while (i <= L) {
        if (substr(t, i, 1) != "%") { i++; continue }
        if (substr(t, i + 1, 1) == "%") { i += 2; continue }      # a doubled percent is one literal percent
        j = i + 1; digits = ""
        while (substr(t, j, 1) >= "0" && substr(t, j, 1) <= "9") { digits = digits substr(t, j, 1); j++ }
        if (digits != "") {
            if (substr(t, j, 1) != "$") {
                printf "   %s %s: percent-number %s wants a dollar after it\n", file, n, digits
                errs++
                i = j
                continue
            }
            idx = digits + 0
            seen[idx] = 1
            if (idx > maxp) maxp = idx
            pos++
            j++
        } else plain++
        while (j <= L && index(flags, substr(t, j, 1)) > 0) j++    # flags, width, precision
        c = substr(t, j, 1)
        if (c == "" || index("sdifeFgGacbnth", c) == 0) {
            printf "   %s %s: a percent with nothing to format after it\n", file, n
            errs++
        }
        i = j + 1
    }
    if (pos > 0 && plain > 0) {
        printf "   %s %s: mixes plain and numbered arguments; Android wants one style\n", file, n
        errs++
    }
    if (pos > 0) {
        for (idx = 1; idx <= maxp; idx++)
            if (!(idx in seen)) { printf "   %s %s: argument number %d is never used\n", file, n, idx; errs++ }
        return maxp
    }
    return plain
}

# Comments off one Java line, strings and character literals left standing. An apostrophe inside a // comment - a
# possessive in a credit line - otherwise looks like the start of a character literal and hides the real code after it.
function strip(l,   i, L, c, n, out, q) {
    L = length(l); out = ""; q = 0; i = 1
    while (i <= L) {
        c = substr(l, i, 1); n = substr(l, i + 1, 1)
        if (inblk) {                                    # inside /* … */, possibly over several lines
            if (c == "*" && n == "/") { inblk = 0; i += 2 } else i++
            continue
        }
        if (q) {                                        # inside a quoted run: copy it out as it stands
            out = out c
            if (c == "\\") { out = out n; i += 2; continue }
            if (c == "\042" && q == 1) q = 0
            if (c == SQ && q == 2) q = 0
            i++; continue
        }
        if (c == "/") {
            if (n == "/") break
            if (n == "*") { inblk = 1; i += 2; continue }
        }
        out = out c
        if (c == "\042") q = 1
        else if (c == SQ) q = 2
        i++
    }
    return out
}

# Parentheses outside strings and character literals: what says a Java line is not finished yet.
function balance(s,   i, L, c, inq, inc, esc, d) {
    L = length(s); inq = 0; inc = 0; esc = 0; d = 0
    for (i = 1; i <= L; i++) {
        c = substr(s, i, 1)
        if (esc) { esc = 0; continue }
        if (c == "\\") { if (inq || inc) esc = 1; continue }
        if (inq) { if (c == "\042") inq = 0; continue }
        if (inc) { if (c == SQ) inc = 0; continue }
        if (c == "\042") { inq = 1; continue }
        if (c == SQ) { inc = 1; continue }
        if (c == "(") d++
        else if (c == ")") d--
    }
    return d
}

# The line a call sits on, once the logical line has been joined back together.
function at_line(s, at, from,   k, nl) {
    nl = 0
    for (k = 1; k < at; k++) if (substr(s, k, 1) == "\n") nl++
    return from + nl
}

# Every call of one shape in a logical line: getString(R.string.name, ...) and its plural sibling.
function walk(s, pre, want, isq, file, from,   p, q, at, tail, ci, cj, name, i, c, depth, commas, inq, inc, esc, given) {
    p = 1
    while ((q = index(substr(s, p), pre)) > 0) {
        at = p + q - 1
        if (substr(s, at + length(pre), length(want)) != want) { p = at + 1; continue }
        tail = substr(s, at + length(pre) + length(want))
        ci = index(tail, ",")
        cj = index(tail, ")")
        if (ci == 0 && cj == 0) { p = at + 1; continue }
        if (ci == 0 || (cj > 0 && cj < ci)) ci = cj
        name = substr(tail, 1, ci - 1)
        i = at + length(pre) + length(want)
        depth = 1; commas = 0; inq = 0; inc = 0; esc = 0
        while (i <= length(s)) {
            c = substr(s, i, 1)
            if (esc) { esc = 0; i++; continue }
            if (c == "\\") { if (inq || inc) esc = 1; i++; continue }
            if (inq) { if (c == "\042") inq = 0; i++; continue }
            if (inc) { if (c == SQ) inc = 0; i++; continue }
            if (c == "\042") { inq = 1; i++; continue }
            if (c == SQ) { inc = 1; i++; continue }
            if (c == "(") depth++
            else if (c == ")") { depth--; if (depth == 0) break }
            else if (c == "," && depth == 1) commas++
            i++
        }
        if (depth > 0) { skipped++; p = at + 1; continue }        # longer than the joined line: not judged
        checked++
        given = commas - (isq ? 1 : 0)                             # plurals pass the quantity first
        sub(/.*\//, "", file)
        if (!(name in en_name)) {
            printf "   %s:%d: R.%s.%s is in the Java but not in values/strings.xml\n", file, at_line(s, at, from),
                   (isq ? "plurals" : "string"), name
            errs++
        } else if ((name in need) && need[name] != given) {
            printf "   %s:%d: R.%s.%s needs %d argument%s, this call passes %d\n", file, at_line(s, at, from),
                   (isq ? "plurals" : "string"), name, need[name], (need[name] == 1 ? "" : "s"), given
            errs++
        }
        p = at + length(pre)
    }
}

FILENAME == en && /<string name=/ {
    n = name_of($0); en_name[n] = 1; total++
    r = analyze(body_of($0, "</string>"), n, "values")
    if (r > need[n]) need[n] = r
    next
}
FILENAME == en && /<plurals name=/ { cur = name_of($0); en_name[cur] = 1; total++; inpl = 1; next }
FILENAME == en && inpl && /<item quantity=/ {
    r = analyze(body_of($0, "</item>"), cur, "values")
    if (r > need[cur]) need[cur] = r
    next
}
FILENAME == en && /<\/plurals>/ { inpl = 0; next }
FILENAME == en { next }

# The Spanish file answers for its own placeholders, and for the same number of them as English.
FILENAME == es && /<string name=/ {
    n = name_of($0); es_name[n] = 1
    r = analyze(body_of($0, "</string>"), n, "values-es")
    if (r > es_need[n]) es_need[n] = r
    next
}
FILENAME == es && /<plurals name=/ { cur = name_of($0); inpl = 1; next }
FILENAME == es && inpl && /<item quantity=/ {
    r = analyze(body_of($0, "</item>"), cur, "values-es")
    if (r > es_need[cur]) es_need[cur] = r
    next
}
FILENAME == es && /<\/plurals>/ { inpl = 0; next }
FILENAME == es { next }

# Java: join a line to the next while its parentheses are still open, then judge each whole call.
function flush(f,   s) {
    if (buf == "") return
    s = buf; buf = ""
    if (balance(s) > 0) skipped++                       # ran past CAP, or the file ended mid-call
    walk(s, "getString(", "R.string.", 0, f, from)
    walk(s, "getQuantityString(", "R.plurals.", 1, f, from)
}
FNR == 1 { flush(pf); pf = FILENAME; inblk = 0 }
{
    line = strip($0)
    if (buf == "") { if (line == "") next; buf = line; from = FNR }
    else buf = buf "\n" line
    if (balance(buf) > 0 && length(buf) < CAP) next
    flush(pf)
}
END {
    flush(pf)
    for (n in en_name) if ((n in es_name) && es_need[n] != need[n]) {
        printf "   %s: %d argument%s in English, %d in Spanish\n", n, need[n], (need[n] == 1 ? "" : "s"), es_need[n]
        errs++
    }
    printf "%d %d %d %d\n", errs + 0, checked + 0, skipped + 0, total + 0
}
' "$EN" "$ES" "$JAVA"/*.java)"

COUNTS="$(printf '%s\n' "$SAYS" | tail -1)"
MESSAGES="$(printf '%s\n' "$SAYS" | sed '$d')"
ERRS="${COUNTS%% *}"
CHECKED="$(printf '%s' "$COUNTS" | awk '{print $2+0}')"
SKIPPED="$(printf '%s' "$COUNTS" | awk '{print $3+0}')"
TOTAL="$(printf '%s' "$COUNTS" | awk '{print $4+0}')"

FAIL=0
if [ -n "$ONLY" ]; then
    echo "!! a name in one language and not the other:"
    printf '%s\n' "$ONLY" | while IFS= read -r l; do
        case $l in
            $'\t'*) printf '   only in values-es: %s\n' "${l#?}" ;;
            *) printf '   only in values:    %s\n' "$l" ;;
        esac
    done
    FAIL=1
fi
if [ -n "$MESSAGES" ]; then
    echo "!! the strings do not add up:"
    printf '%s\n' "$MESSAGES"
    FAIL=1
fi
[ "${ERRS:-0}" -gt 0 ] && FAIL=1

if [ "$FAIL" = 1 ]; then
    echo "!! Fix the above: Android formats these when the screen opens, and throws instead of drawing." >&2
    exit 1
fi
if [ "$SKIPPED" -gt 0 ]; then
    echo "   $TOTAL names in both languages, $CHECKED call sites checked, $SKIPPED too long to judge"
else
    echo "   $TOTAL names in both languages, $CHECKED call sites checked"
fi
