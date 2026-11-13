package com.techpp.glass;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * App channels: the rows apps publish to Android TV ("Top Picks For You" from JioHotstar, "Netflix", "Recommended
 * for you" from YouTube…), plus Watch Next, shown as Projectivy does. You choose which show and their order
 * (Talkies settings → Channels); that is kept in the "channels" preferences. Until you do, Watch Next and the rows
 * each app asked Android TV to show on the home screen are shown. Artwork is cached in files/channels/.
 * Reading programs and downloading artwork is blocking: call off the main thread.
 */
final class Channels {
    private static final String TAG = "GlassChannels";
    static final String WATCH_NEXT = "watchnext";
    private static final int MAX_ITEMS = 15, ART_W = 440;  // cards are 400 px on screen (424 focused)
    /**
     * The first rows Talkies ever had (its author's, from Projectivy): kept for TVs that installed Talkies before the
     * general default, so their rows don't change under them. New installs start from the browsable rows instead.
     */
    private static final String[] FIRST_SHOWN = {WATCH_NEXT, "in.startv.hotstar|Top Picks For You",
            "com.netflix.ninja|Netflix", "com.google.android.youtube.tv|Recommended for you",
            "com.amazon.amazonvideo.livingroom|Prime Video"};

    static final class Channel {
        String key, pkg, name, title;
        long id = -1;
        int count;
        /** What this read took from the row, or - in a census, where nothing is taken - what the row has to offer. */
        int kept;
        boolean shown;
        final List<Trending.Pick> items = new ArrayList<>();
    }

    /**
     * How many cards one row may put in Continue, chosen per row in Talkies settings → Continue apps. Anything else
     * is that number. Eight is what every row got before the choice existed, so a TV that never opened the menu keeps
     * the row it has always had.
     */
    static final int CAP_OFF = 0, CAP_FREE = -1, CAP_DEFAULT = 8;
    /** What OK walks through, and what the arrows step along: hidden · 2 · 4 · 6 · 8 · as many as it has. */
    static final int[] CAPS = {CAP_OFF, 2, 4, 6, CAP_DEFAULT, CAP_FREE};

    static int cap(Context c, String row) { return caps(c).getInt(row, CAP_DEFAULT); }

    static void setCap(Context c, String row, int cap) { caps(c).edit().putInt(row, cap).apply(); }

    /** How many rows you have hidden from Continue, whatever their apps' own switches say. */
    static int rowsOff(Context c) {
        int n = 0;
        for (Object v : caps(c).getAll().values()) if (v instanceof Integer && (Integer) v == CAP_OFF) n++;
        return n;
    }

    /** The next choice, going back to hidden after the last one. */
    static int capNext(int from) { return CAPS[(rung(from) + 1) % CAPS.length]; }

    /** One step up or down the choices, where the ends stay put: the arrows, which shouldn't hide a row by accident. */
    static int capStep(int from, int dir) {
        return CAPS[Math.max(0, Math.min(rung(from) + dir, CAPS.length - 1))];
    }

    /** Where a number sits among the choices; a hand-edited one is read as the default it came from. */
    private static int rung(int cap) {
        for (int i = 0; i < CAPS.length; i++) if (CAPS[i] == cap) return i;
        for (int i = 0; i < CAPS.length; i++) if (CAPS[i] == CAP_DEFAULT) return i;
        return 0;
    }

    private static SharedPreferences caps(Context c) { return c.getSharedPreferences("continue_caps", Context.MODE_PRIVATE); }

    private Channels() { }

    private static SharedPreferences prefs(Context c) { return c.getSharedPreferences("channels", Context.MODE_PRIVATE); }

    /** Every channel that has something in it, in your order, each marked shown or not. Without items. */
    static List<Channel> list(Context c, int watchNextCount) {
        PackageManager pm = c.getPackageManager();
        Map<String, Channel> found = new LinkedHashMap<>();
        if (watchNextCount > 0) {
            Channel w = new Channel();
            w.key = WATCH_NEXT;
            w.pkg = null;
            w.name = "Watch Next";
            w.title = "Watch Next";
            w.count = watchNextCount;
            found.put(w.key, w);
        }
        String[] cols = {TvContract.Channels._ID, TvContract.Channels.COLUMN_PACKAGE_NAME,
                TvContract.Channels.COLUMN_DISPLAY_NAME, TvContract.Channels.COLUMN_TYPE, TvContract.Channels.COLUMN_BROWSABLE};
        Set<String> browsable = new HashSet<>();  // rows their apps asked Android TV to show on the home screen
        try (Cursor cur = c.getContentResolver().query(TvContract.Channels.CONTENT_URI, cols, null, null, null)) {
            while (cur != null && cur.moveToNext()) {
                if (!TvContract.Channels.TYPE_PREVIEW.equals(cur.getString(3))) continue;
                String pkg = cur.getString(1), name = cur.getString(2);
                if (pkg == null || name == null || pm.getLeanbackLaunchIntentForPackage(pkg) == null) continue;
                long id = cur.getLong(0);
                int count = count(c, id);
                if (count == 0) continue;
                String key = pkg + "|" + name;
                Channel ch = found.get(key);
                if (ch != null) {
                    // Some apps publish the same row twice (Apple TV): keep the fuller one.
                    if (count > ch.count) { ch.id = id; ch.count = count; }
                    continue;
                }
                ch = new Channel();
                ch.key = key;
                ch.pkg = pkg;
                ch.name = name;
                ch.id = id;
                ch.count = count;
                ch.title = titleOf(pm, pkg, name);
                found.put(key, ch);
                if (cur.getInt(4) == 1) browsable.add(key);
            }
        } catch (Exception e) {
            Log.w(TAG, "Couldn't read channels: " + e);
        }

        SharedPreferences p = prefs(c);
        // Decided once: a TV updating from a Talkies older than the general default keeps the first rows.
        String defaults = p.getString("defaults", null);
        if (defaults == null) {
            defaults = updated(c) ? "first" : "browsable";
            p.edit().putString("defaults", defaults).apply();
        }
        boolean first = defaults.equals("first");
        if (!p.contains("shown")) Log.i(TAG, found.size() + " channels, " + browsable.size() + " browsable; starting rows: " + defaults);
        Set<String> start = new HashSet<>();
        if (first) {
            start.addAll(Arrays.asList(FIRST_SHOWN));
        } else {
            start.add(WATCH_NEXT);
            start.addAll(browsable);
            if (browsable.isEmpty()) {
                // No app marked a row: each app's first.
                Set<String> apps = new HashSet<>();
                for (Channel ch : found.values()) if (ch.pkg != null && apps.add(ch.pkg)) start.add(ch.key);
            }
        }
        List<String> order = new ArrayList<>(Arrays.asList(p.getString("order", "").split("\n")));
        Set<String> shown = p.contains("shown") ? new HashSet<>(Arrays.asList(p.getString("shown", "").split("\n"))) : start;
        if (!p.contains("order") && first) order = new ArrayList<>(Arrays.asList(FIRST_SHOWN));
        List<Channel> out = new ArrayList<>();
        for (String k : order) if (found.containsKey(k)) out.add(found.remove(k));
        // Channels you haven't placed yet go after, by name.
        List<Channel> rest = new ArrayList<>(found.values());
        Collections.sort(rest, (a, b) -> a.title.compareToIgnoreCase(b.title));
        out.addAll(rest);
        for (Channel ch : out) ch.shown = shown.contains(ch.key);
        return out;
    }

    /** Whether this install was an update (first installed before the current version), not a fresh install. */
    private static boolean updated(Context c) {
        try {
            android.content.pm.PackageInfo i = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            return i.firstInstallTime != i.lastUpdateTime;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /** Saves the order and which are shown. */
    static void save(Context c, List<Channel> list) {
        StringBuilder order = new StringBuilder(), shown = new StringBuilder();
        for (Channel ch : list) {
            order.append(ch.key).append('\n');
            if (ch.shown) shown.append(ch.key).append('\n');
        }
        prefs(c).edit().putString("order", order.toString()).putString("shown", shown.toString()).apply();
    }

    /** The shown channels with their items and artwork (Watch Next items are passed in). Blocking. */
    static List<Channel> loadShown(Context c, List<Trending.Pick> watchNext) {
        File dir = new File(c.getExternalFilesDir(null), "channels");
        Set<String> keep = new HashSet<>();
        List<Channel> out = new ArrayList<>();
        for (Channel ch : list(c, watchNext.size())) {
            if (!ch.shown) continue;
            if (WATCH_NEXT.equals(ch.key)) {
                ch.items.addAll(watchNext);
            } else {
                readItems(c, ch, dir, keep, ART_W, false, false);
            }
            if (!ch.items.isEmpty()) out.add(ch);
        }
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) if (!keep.contains(f.getName())) f.delete();
        Log.i(TAG, out.size() + " rows shown");
        return out;
    }

    /**
     * What apps' rows mark as partway through, shown or not: every card on a row named for it, and on other rows the
     * cards that say so one by one, either with where they left you (Stremio and Plex fill that in, and every row that
     * only promotes leaves it empty) or with their link and description, as Netflix's do. With artwork. Blocking. An
     * idea from tvOS Launcher (Apache-2.0).
     */
    static List<Channel> continueRows(Context c, File dir, Set<String> keep, int artWidth) {
        return continueRows(c, dir, keep, artWidth, false);
    }

    /** The same rows with their numbers and no artwork, for the menu that lists them. Blocking, but not online. */
    static List<Channel> continueCensus(Context c) {
        return continueRows(c, null, null, 0, true);
    }

    private static List<Channel> continueRows(Context c, File dir, Set<String> keep, int artWidth, boolean census) {
        PackageManager pm = c.getPackageManager();
        Map<String, Channel> found = new LinkedHashMap<>();
        String[] cols = {TvContract.Channels._ID, TvContract.Channels.COLUMN_PACKAGE_NAME,
                TvContract.Channels.COLUMN_DISPLAY_NAME, TvContract.Channels.COLUMN_TYPE};
        try (Cursor cur = c.getContentResolver().query(TvContract.Channels.CONTENT_URI, cols, null, null, null)) {
            while (cur != null && cur.moveToNext()) {
                String pkg = cur.getString(1), name = cur.getString(2);
                if (!TvContract.Channels.TYPE_PREVIEW.equals(cur.getString(3)) || pkg == null || name == null
                        || pm.getLeanbackLaunchIntentForPackage(pkg) == null) continue;
                int count = count(c, cur.getLong(0));
                String key = pkg + "|" + name;
                if (count == 0 || found.containsKey(key) && found.get(key).count >= count) continue;
                Channel ch = new Channel();
                ch.key = key;
                ch.pkg = pkg;
                ch.name = name;
                ch.title = titleOf(pm, pkg, name);
                ch.id = cur.getLong(0);
                ch.count = count;
                found.put(key, ch);
            }
        } catch (Exception e) {
            Log.w(TAG, "Couldn't read continue rows: " + e);
        }
        for (Channel ch : found.values()) {
            boolean named = isContinueRow(ch.name);
            // A row hidden from the menu is never read, so its pictures are never even asked for.
            if (!census && cap(c, ch.key) == CAP_OFF) continue;
            readItems(c, ch, dir, keep, artWidth, !named, census);
            if (named) Log.i(TAG, "continue row " + ch.key + ": named for it, kept " + ch.items.size() + " of " + ch.count);
        }
        return new ArrayList<>(found.values());
    }

    /** "Netflix · Netflix" says the app twice: a row called for the app reads as the app. */
    private static String titleOf(PackageManager pm, String pkg, String name) {
        String app = label(pm, pkg);
        return name.equalsIgnoreCase(app) || app.toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT))
                ? app : app + " · " + name;
    }

    private static boolean isContinueRow(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        return n.contains("continue") || n.contains("keep watching") || n.contains("resume");
    }

    /** Whether a card on a mixed row says it's partway through (as Netflix's do). */
    private static boolean marksContinue(String link, String description) {
        String l = link.toLowerCase(Locale.ROOT);
        return l.contains("listcontext%3dcontinuewatching") || l.contains("listcontext=continuewatching")
                || description.toLowerCase(Locale.ROOT).startsWith("continue watching");
    }

    private static int count(Context c, long id) {
        try (Cursor cur = c.getContentResolver().query(TvContract.buildPreviewProgramsUriForChannel(id),
                new String[]{TvContract.PreviewPrograms._ID}, null, null, null)) {
            return cur == null ? 0 : cur.getCount();
        } catch (Exception e) {
            return 0;
        }
    }

    private static void readItems(Context c, Channel ch, File dir, Set<String> keep, int artWidth, boolean markedOnly, boolean census) {
        String[] cols = {TvContract.PreviewPrograms.COLUMN_TITLE, TvContract.PreviewPrograms.COLUMN_SHORT_DESCRIPTION,
                TvContract.PreviewPrograms.COLUMN_POSTER_ART_URI, TvContract.PreviewPrograms.COLUMN_THUMBNAIL_URI,
                TvContract.PreviewPrograms.COLUMN_INTENT_URI, TvContract.PreviewPrograms.COLUMN_BROWSABLE,
                TvContract.PreviewPrograms.COLUMN_SEASON_DISPLAY_NUMBER, TvContract.PreviewPrograms.COLUMN_EPISODE_DISPLAY_NUMBER,
                TvContract.PreviewPrograms.COLUMN_EPISODE_TITLE, TvContract.PreviewPrograms.COLUMN_WEIGHT,
                ENGAGED, TvContract.PreviewPrograms.COLUMN_LAST_PLAYBACK_POSITION_MILLIS,
                TvContract.PreviewPrograms.COLUMN_DURATION_MILLIS, WATCH_NEXT_TYPE};
        // Android declares the last-watched column for Watch Next only, but its table for channel cards has it too;
        // read it by name in case a TV's doesn't.
        try (Cursor cur = c.getContentResolver().query(TvContract.buildPreviewProgramsUriForChannel(ch.id), null, null, null, null)) {
            if (cur == null) return;
            int[] col = new int[cols.length];
            for (int i = 0; i < cols.length; i++) col[i] = cur.getColumnIndex(cols[i]);
            if (markedOnly && !toldColumns) {   // once per run: which signals this TV's card table actually carries
                toldColumns = true;
                Log.i(TAG, "cards carry these columns: " + java.util.Arrays.toString(cur.getColumnNames()));
            }
            int marked = 0, played = 0, timed = 0, both = 0, typed = 0, passed = 0;
            // What you watched last first, where the app says when (Nuvio does); then the app's own weight;
            // then the order the app added them (a stable sort keeps it). An idea from Plume (MIT).
            List<Integer> rows = new ArrayList<>();
            List<long[]> keys = new ArrayList<>();
            while (cur.moveToNext()) {
                if (col[5] >= 0 && !cur.isNull(col[5]) && cur.getInt(col[5]) == 0) continue;
                if (markedOnly) {   // counting each rule's hits before choosing one, rather than guessing from here
                    if (marksContinue(str(cur, col[4]), str(cur, col[1]))) marked++;
                    long position = num(cur, col[11]), duration = num(cur, col[12]);
                    if (position > 0) played++;
                    if (duration > 0) timed++;
                    if (position > 0 && duration > 0) both++;   // where a "already finished" filter could work
                    if (col[13] >= 0 && str(cur, col[13]).equals("0")) typed++;
                }
                rows.add(cur.getPosition());
                keys.add(new long[]{num(cur, col[10]), num(cur, col[9])});
            }
            List<Integer> order = new ArrayList<>();
            for (int i = 0; i < rows.size(); i++) order.add(i);
            order.sort((a, b) -> {
                long[] x = keys.get(a), y = keys.get(b);
                return x[0] != y[0] ? Long.compare(y[0], x[0]) : Long.compare(y[1], x[1]);
            });
            for (int i : order) {
                if (!census && ch.items.size() >= MAX_ITEMS) break;
                cur.moveToPosition(rows.get(i));
                String link = str(cur, col[4]);
                long position = num(cur, col[11]);
                // Where the app left you is the sign you started it; some rows say it only in the link or the text.
                if (markedOnly && position <= 0 && !marksContinue(link, str(cur, col[1]))) continue;
                passed++;
                if (census) continue;   // the menu counts the row: no cards, and so no pictures asked for
                Trending.Pick p = new Trending.Pick();
                p.kind = "channel";
                p.tab = ch.key;
                p.pkg = ch.pkg;
                p.title = str(cur, col[0]);
                String season = str(cur, col[6]), episode = str(cur, col[7]), epTitle = str(cur, col[8]), desc = str(cur, col[1]);
                StringBuilder m = new StringBuilder();
                if (!season.isEmpty()) m.append("Season ").append(season);
                if (!episode.isEmpty()) m.append(m.length() > 0 ? " · " : "").append("Episode ").append(episode);
                if (!epTitle.isEmpty() && !epTitle.equals(p.title)) m.append(m.length() > 0 ? " · " : "").append(epTitle);
                p.meta = m.length() > 0 ? m.toString() : desc;
                p.intentUri = link.isEmpty() ? null : link;
                String art = !str(cur, col[3]).isEmpty() ? str(cur, col[3]) : str(cur, col[2]);
                p.id = (ch.key + "\n" + p.title + "\n" + art).hashCode();
                p.image = art.isEmpty() ? null : WatchNext.cached(c, dir, art, artWidth);
                p.at = num(cur, col[10]);
                long duration = num(cur, col[12]);
                if (position > 0 && duration > 0) p.progress = Math.min(1f, position / (float) duration);
                if (p.image != null) keep.add(p.image.getName());
                p.artMissing = p.image == null && !art.isEmpty();
                ch.items.add(p);
            }
            ch.kept = passed;
            if (markedOnly)
                Log.i(TAG, "continue row " + ch.key + ": " + rows.size() + " cards, kept " + ch.items.size()
                        + " | " + marked + " say so in their link or text, " + played + " have a position, "
                        + timed + " have a length (" + both + " both), "
                        + (col[13] < 0 ? "no type column here" : typed + " of type continue"));
        } catch (Exception e) {
            Log.w(TAG, "Couldn't read " + ch.key + ": " + e);
        }
    }

    private static final String ENGAGED = "last_engagement_time_utc_millis";

    /** Channel cards have no declared watch-next type, so ask for it by name and treat a missing column as none. */
    private static final String WATCH_NEXT_TYPE = "watch_next_program_type";

    private static boolean toldColumns;

    private static long num(Cursor c, int i) { return i < 0 || c.isNull(i) ? 0 : c.getLong(i); }

    private static String str(Cursor c, int i) {
        String s = i < 0 || c.isNull(i) ? null : c.getString(i);
        return s == null || s.equals("null") ? "" : s.trim();
    }

    static String label(PackageManager pm, String pkg) {
        try {
            return pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString().trim();
        } catch (Exception e) {
            return pkg;
        }
    }
}
