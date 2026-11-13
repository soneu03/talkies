package com.techpp.glass;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.tv.TvContract;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Android TV's "Watch Next" list: what apps like Netflix and Prime Video say you're in the middle of, with a link
 * that opens the right title or episode. Readable with the TV listings permissions on this TV. Only "continue" and
 * "next episode" entries are used; "new season" and watchlist entries are promotions, not things you watched.
 * Blocking (database + artwork downloads): call it off the main thread.
 * Episodes you haven't started (the "next episode" entries) don't show the app's still from them, which can give the
 * story away (Talkies settings → Hide spoilers): the app's poster when it's landscape, else the still blurred, with
 * a portrait poster on the small cards. An idea from harrydbarnes (discussion #14).
 */
final class WatchNext {
    private static final String TAG = "GlassWatchNext";
    private static final int MAX = 16;
    /** Past this far in, a title is done: apps don't always take it off their list. */
    private static final float FINISHED = 0.97f;

    private WatchNext() { }

    static List<Trending.Pick> load(Context c) {
        List<Trending.Pick> out = new ArrayList<>();
        File dir = new File(c.getExternalFilesDir(null), "recent/watchnext");
        boolean hideSpoilers = hideSpoilers(c);
        Set<String> keep = new HashSet<>();
        String[] cols = {
                TvContract.WatchNextPrograms.COLUMN_PACKAGE_NAME, TvContract.WatchNextPrograms.COLUMN_TITLE,
                TvContract.WatchNextPrograms.COLUMN_EPISODE_TITLE, TvContract.WatchNextPrograms.COLUMN_SEASON_DISPLAY_NUMBER,
                TvContract.WatchNextPrograms.COLUMN_EPISODE_DISPLAY_NUMBER, TvContract.WatchNextPrograms.COLUMN_WATCH_NEXT_TYPE,
                TvContract.WatchNextPrograms.COLUMN_LAST_ENGAGEMENT_TIME_UTC_MILLIS,
                TvContract.WatchNextPrograms.COLUMN_LAST_PLAYBACK_POSITION_MILLIS,
                TvContract.WatchNextPrograms.COLUMN_THUMBNAIL_URI, TvContract.WatchNextPrograms.COLUMN_POSTER_ART_URI,
                TvContract.WatchNextPrograms.COLUMN_INTENT_URI, TvContract.WatchNextPrograms.COLUMN_BROWSABLE,
                TvContract.WatchNextPrograms.COLUMN_DURATION_MILLIS,
        };
        try (Cursor cur = c.getContentResolver().query(TvContract.WatchNextPrograms.CONTENT_URI, cols, null, null,
                TvContract.WatchNextPrograms.COLUMN_LAST_ENGAGEMENT_TIME_UTC_MILLIS + " DESC")) {
            if (cur == null) return out;
            Log.i(TAG, cur.getCount() + " rows");
            while (cur.moveToNext() && out.size() < MAX) {
                String pkg = cur.getString(0);
                int type = cur.isNull(5) ? -1 : cur.getInt(5);
                boolean browsable = cur.isNull(11) || cur.getInt(11) != 0;
                Log.i(TAG, "row " + pkg + " type=" + type + " browsable=" + browsable + " intent=" + !cur.isNull(10)
                        + " engaged=" + (cur.isNull(6) ? "-" : new java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.US).format(new java.util.Date(cur.getLong(6))))
                        + " " + str(cur, 1));
                if (type != TvContract.WatchNextPrograms.WATCH_NEXT_TYPE_CONTINUE
                        && type != TvContract.WatchNextPrograms.WATCH_NEXT_TYPE_NEXT) continue;
                if (!browsable || pkg == null || c.getPackageManager().getLeanbackLaunchIntentForPackage(pkg) == null) continue;

                String title = str(cur, 1), episode = str(cur, 2), season = str(cur, 3), number = str(cur, 4);
                // The line under the card: "Season 2 · Episode 1 · Prime Video" (or the episode's name).
                StringBuilder m = new StringBuilder();
                if (!season.isEmpty()) m.append("Season ").append(season);
                if (!number.isEmpty()) m.append(m.length() > 0 ? " · " : "").append("Episode ").append(number);
                if (m.length() == 0 && !episode.isEmpty() && !episode.equals(title)) m.append(episode);
                String t = title;

                Trending.Pick p = new Trending.Pick();
                p.id = (pkg + "\n" + t + "\n" + m).hashCode();
                p.kind = "watchnext";
                p.tab = RecentStore.TAB;
                p.pkg = pkg;
                p.title = t;
                p.service = label(c, pkg);
                p.meta = (m.length() > 0 ? m + " · " : "") + p.service;
                long position = cur.isNull(7) ? 0 : cur.getLong(7), duration = cur.isNull(12) ? 0 : cur.getLong(12);
                if (duration > 0 && position >= duration * FINISHED) continue;
                if (position > 0 && duration > 0) p.progress = Math.min(1f, position / (float) duration);
                p.at = cur.isNull(6) ? 0 : cur.getLong(6);
                p.intentUri = str(cur, 10).isEmpty() ? null : str(cur, 10);
                String art = !str(cur, 8).isEmpty() ? str(cur, 8) : str(cur, 9);
                if (hideSpoilers && type == TvContract.WatchNextPrograms.WATCH_NEXT_TYPE_NEXT) {
                    String posterArt = str(cur, 9);
                    File poster = posterArt.isEmpty() || posterArt.equals(str(cur, 8)) ? null : cached(c, dir, posterArt);
                    if (poster != null && landscape(poster)) {
                        p.image = poster;
                    } else {
                        File still = art.isEmpty() ? null : cached(c, dir, art);
                        p.image = still == null ? null : blurred(still);
                        if (still != null) keep.add(still.getName());  // kept, so the blur isn't redone each time
                        if (poster != null) { p.poster = poster; keep.add(poster.getName()); }
                    }
                } else {
                    p.image = art.isEmpty() ? null : cached(c, dir, art);
                }
                if (p.image != null) keep.add(p.image.getName());
                p.artMissing = p.image == null && !art.isEmpty();
                out.add(p);
            }
        } catch (SecurityException e) {
            Log.w(TAG, "No permission to read Watch Next: " + e.getMessage());
        } catch (Exception e) {
            Log.w(TAG, "Could not read Watch Next: " + e);
        }
        addContinueRows(c, out, dir, keep);
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) if (!keep.contains(f.getName())) f.delete();
        forget(dir);   // the listing is a page behind now, and a page behind is a card showing nothing
        Log.i(TAG, out.size() + " to show");
        return out;
    }

    /**
     * Adds the cards apps mark "continue watching" on their rows that aren't on Watch Next, which Netflix can leave
     * unchanged. These rarely say when you watched, so the time is when the first one last changed (title or episode);
     * until it first does, new cards sit at the end of Continue, since a first look says nothing about when.
     */
    private static void addContinueRows(Context c, List<Trending.Pick> out, File dir, Set<String> keep) {
        android.content.SharedPreferences seen = c.getSharedPreferences("continue_rows", Context.MODE_PRIVATE);
        java.util.Map<String, Trending.Pick> known = new java.util.HashMap<>();
        for (Trending.Pick p : out) known.put(p.pkg + "\n" + p.title, p);
        for (Channels.Channel ch : Channels.continueRows(c, dir, keep, 1280)) {
            if (ch.items.isEmpty()) continue;
            // What you chose for this row in the menu: eight unless you say otherwise, none if you hid it.
            int cap = Channels.cap(c, ch.key);
            if (cap == Channels.CAP_OFF) continue;
            String top = ch.items.get(0).title + "\n" + ch.items.get(0).meta, before = seen.getString(ch.key, null);
            long changed = seen.getLong(ch.key + "@", 0);
            if (!top.equals(before)) {
                changed = before == null ? 0 : System.currentTimeMillis();
                seen.edit().putString(ch.key, top).putLong(ch.key + "@", changed).apply();
            }
            Log.i(TAG, "row " + ch.key + ": " + ch.items.size() + " items, changed " + changed + ", cap " + cap);
            String service = label(c, ch.pkg);
            int added = 0;
            for (int i = 0; i < ch.items.size() && (cap < 0 || added < cap); i++) {
                Trending.Pick p = ch.items.get(i);
                if (p.progress >= FINISHED) continue;
                long at = p.at > 0 ? p.at : changed > 0 ? changed - i * 60_000L : 0;
                Trending.Pick same = known.get(ch.pkg + "\n" + p.title);
                if (same != null) {   // Watch Next's card opens the right episode: keep it, with the row's newer time
                    if (i == 0 && at > same.at) same.at = at;
                    continue;
                }
                p.kind = "watchnext";
                p.row = ch.key;
                p.tab = RecentStore.TAB;
                p.service = service;
                p.at = at;
                p.id = (ch.pkg + "\n" + p.title + "\n" + p.meta).hashCode();
                String about = p.meta.replaceFirst("(?i)^continue watching\\s*", "");
                p.meta = (about.isEmpty() ? "" : about + " · ") + service;
                out.add(p);
                added++;
            }
        }
    }

    /** Talkies settings → Hide spoilers (off unless you turn it on: the app's own stills are what people expect). */
    static boolean hideSpoilers(Context c) {
        return c.getSharedPreferences("home", Context.MODE_PRIVATE).getBoolean("hide_spoilers", false);
    }

    private static boolean landscape(File f) {
        float s = shape(f);
        if (s > 0) return s >= 1f;   // the name already says it, and says it without a decode
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getPath(), o);
        return o.outWidth > 0 && o.outWidth > o.outHeight;
    }

    /** A soft blur of the picture (shrunk to 32 px wide and grown back in steps), saved next to it once. */
    private static File blurred(File src) {
        File out = new File(src.getParentFile(), src.getName().replaceAll("\\.(jpg|png)$", "") + "_blur.jpg");
        if (out.isFile()) return out;
        try {
            Bitmap b = BitmapFactory.decodeFile(src.getPath());
            if (b == null) return null;
            int w = 32, h = Math.max(1, b.getHeight() * w / b.getWidth());
            Bitmap big = Bitmap.createScaledBitmap(b, w, h, true);
            for (int k = 2; w * k <= 640; k *= 2) big = Bitmap.createScaledBitmap(big, w * k, h * k, true);  // doubling: no blocks
            File tmp = new File(out.getPath() + ".part");
            try (OutputStream o = new FileOutputStream(tmp)) { big.compress(Bitmap.CompressFormat.JPEG, 85, o); }
            return tmp.renameTo(out) ? out : null;
        } catch (Exception | OutOfMemoryError e) {
            Log.w(TAG, "Blur failed: " + e.getClass().getSimpleName());
            return null;
        }
    }

    private static String str(Cursor c, int i) {
        String s = c.isNull(i) ? null : c.getString(i);
        return s == null ? "" : s.trim();
    }

    private static String label(Context c, String pkg) {
        try {
            PackageManager pm = c.getPackageManager();
            return pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString().trim();
        } catch (Exception e) {
            return pkg;
        }
    }

    /** Up to 780 px: the large card is 331 dp wide, 662 px on a TV whose interface is 1920 px, and 780 is the step
     * just above that. It used to be 1280, which asked for two and a half times the memory for pixels no card shows. */
    private static File cached(Context c, File dir, String uri) { return cached(c, dir, uri, 780); }

    /**
     * The name an artwork is kept under: a digest of the whole address, not a 32-bit hash of it. Providers put a
     * changing token in the address, and two of those differing only there landed on one file and shared one picture
     * - which is what "TVE repeats images" looked like from here.
     */
    private static String key(String uri) {
        byte[] d;
        try {
            d = java.security.MessageDigest.getInstance("MD5").digest(uri.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (java.security.NoSuchAlgorithmException e) {
            return Integer.toHexString(uri.hashCode());
        }
        StringBuilder s = new StringBuilder(24);
        for (int i = 0; i < 12; i++) s.append(Character.forDigit((d[i] >> 4) & 0xF, 16)).append(Character.forDigit(d[i] & 0xF, 16));
        return s.toString();
    }

    /**
     * The file this address already landed as, at the width asked for - or null, which means fetch it. The width stays
     * in the name because the same address asked for at 440 and at 1280 is two different pictures, and answering one
     * with the other is how the large card would quietly go soft on every refresh.
     */
    private static File existing(File dir, String hash, int width) {
        String want = hash + "_" + width + "_";
        for (java.util.Map.Entry<String, File> e : index(dir).entrySet()) {
            String name = e.getKey();
            if (!name.startsWith(want)) continue;
            // A blur of a picture keeps this prefix too, and is not the picture. A .part is a download that died
            // halfway through: the sweep that follows a read deletes both, but a half-written file is not artwork.
            if (name.contains("_blur")) continue;
            if (!(name.endsWith(".jpg") || name.endsWith(".png"))) continue;
            if (sizeOf(name) == null) continue;   // a name that says nothing about its size is no use to a card
            return e.getValue();
        }
        return null;
    }

    /**
     * An artwork kept under the name used before the size went into it (`hash_440.jpg`): measured once, renamed to
     * carry its size, and used. Fetching it again is not an option - a `content://` address is only good while the app
     * that wrote it still means it, and Plex answers a second look with FileNotFoundException, so re-downloading the
     * cache would have emptied the cards that were already filled.
     */
    private static File migrated(File dir, String hash, int width) {
        for (String ext : new String[]{".jpg", ".png"}) {
            File old = new File(dir, hash + "_" + width + ext);
            if (!old.isFile()) continue;
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(old.getPath(), o);
            if (o.outWidth <= 0 || o.outHeight <= 0) return old;
            File renamed = new File(dir, hash + "_" + width + "_" + o.outWidth + "x" + o.outHeight + ext);
            if (!old.renameTo(renamed)) return old;   // a name it won't take is still the same picture
            java.util.Map<String, File> ls = index(dir);
            ls.remove(old.getName());
            ls.put(renamed.getName(), renamed);
            return renamed;
        }
        return null;
    }

    /** Folder listings, kept because a name's size is only known by reading the names: one readdir per folder per
     *  read rather than one per card. Anything deleting artwork calls forget on the folder it swept. */
    private static final java.util.Map<String, java.util.Map<String, File>> listing = new java.util.concurrent.ConcurrentHashMap<>();

    private static java.util.Map<String, File> index(File dir) {
        return listing.computeIfAbsent(dir.getPath(), k -> {
            java.util.Map<String, File> m = new java.util.HashMap<>();
            File[] fs = dir.listFiles();
            if (fs != null) for (File f : fs) m.put(f.getName(), f);
            return m;
        });
    }

    /** A folder was swept: drop its listing so the next lookup reads the folder again. */
    static void forget(File dir) { listing.remove(dir.getPath()); }

    /** The pixels a name says it holds, or null when it says nothing. The hash is hex, so the only "x" in a name is
     *  ours; a blur of a picture keeps its name and so keeps its size in it too. */
    static int[] sizeOf(String name) {
        int x = name.indexOf('x');
        if (x <= 0) return null;
        int ws = x;
        while (ws > 0 && name.charAt(ws - 1) >= '0' && name.charAt(ws - 1) <= '9') ws--;
        int hs = x + 1;
        while (hs < name.length() && name.charAt(hs) >= '0' && name.charAt(hs) <= '9') hs++;
        if (hs - ws < 3 || ws == x || hs == x + 1) return null;
        try {
            int w = Integer.parseInt(name.substring(ws, x)), h = Integer.parseInt(name.substring(x + 1, hs));
            return w > 0 && h > 0 ? new int[]{w, h} : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** How wide one picture is for every dp of its height: 1.78 for a frame, 0.67 for a poster. Zero when the name
     *  doesn't say, which is the same as saying "keep the card you would have used anyway". */
    static float shape(File f) {
        int[] wh = f == null ? null : sizeOf(f.getName());
        return wh == null ? 0f : (float) wh[0] / wh[1];
    }

    /**
     * The artwork as a small JPEG on disk (at most `width` px wide), downloaded once per address; a PNG instead when it
     * has transparency (team logos), which JPEG would fill with black. Also used for channels and match cards.
     * Its name keeps both the width asked for and the size the file really holds: a card wants to know the shape of
     * its picture before decoding it, and the two widths only agree when the source is at least that big.
     */
    static File cached(Context c, File dir, String uri, int width) {
        String hash = key(uri);
        File have = existing(dir, hash, width);
        if (have != null) return have;
        File kept = migrated(dir, hash, width);   // one already on disk, under the name it had then
        if (kept != null) return kept;
        if (!dir.isDirectory() && !dir.mkdirs()) return null;
        File raw = new File(dir, hash + "_" + width + ".raw");
        try (InputStream in = open(c.getContentResolver(), uri)) {
            if (in == null) return null;
            // The bytes land on disk before anything is decoded, so the picture can be measured and then read at a
            // sample size. Decoding whole is how a 4000 px fanart asked for more memory than a TV has, the decoder
            // answered null, and that card stayed empty for good: nothing tried the address again.
            try (OutputStream out = new FileOutputStream(raw)) {
                byte[] b = new byte[16384];
                for (int n; (n = in.read(b)) > 0; ) out.write(b, 0, n);
            }
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(raw.getPath(), o);
            if (o.outWidth <= 0 || o.outHeight <= 0) {
                Log.w(TAG, "Artwork failed: not a picture we can read from " + shortOf(uri));
                return null;
            }
            int sample = 1;
            while (o.outWidth / (sample * 2) >= width) sample *= 2;
            o.inJustDecodeBounds = false;
            o.inSampleSize = sample;
            Bitmap b = BitmapFactory.decodeFile(raw.getPath(), o);
            if (b == null) {
                Log.w(TAG, "Artwork failed: nothing to decode from " + shortOf(uri));
                return null;
            }
            if (b.getWidth() > width) b = Bitmap.createScaledBitmap(b, width, Math.max(1, b.getHeight() * width / b.getWidth()), true);
            boolean alpha = b.hasAlpha();
            File done = new File(dir, hash + "_" + width + "_" + b.getWidth() + "x" + b.getHeight() + (alpha ? ".png" : ".jpg"));
            File tmp = new File(dir, done.getName() + ".part");
            try (OutputStream out = new FileOutputStream(tmp)) {
                b.compress(alpha ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 85, out);
            }
            // One line per artwork fetched (a warm cache logs nothing): where it came from, how big it really was,
            // and the file it became. Enough to tell a provider that reuses one picture from a name that collided.
            if (!tmp.renameTo(done)) {
                Log.w(TAG, "Artwork failed: could not keep " + done.getName());
                return null;
            }
            index(dir).put(done.getName(), done);   // the listing of this folder learns the file it just grew
            Log.i(TAG, "art " + shortOf(uri) + " " + o.outWidth + "x" + o.outHeight + " -> " + done.getName());
            return done;
        } catch (Exception e) {
            Uri u = Uri.parse(uri);
            Log.w(TAG, "Artwork failed: " + e.getClass().getSimpleName() + " from " + u.getScheme() + "://" + u.getHost());
            return null;
        } finally {
            raw.delete();
        }
    }

    /**
     * Where an artwork address came from, without its secrets: providers put tokens in the query, and logcat is
     * readable over the network on some TVs.
     */
    private static String shortOf(String uri) {
        try {
            Uri u = Uri.parse(uri);
            String path = u.getPath() == null ? "" : u.getPath();
            return u.getScheme() + "://" + u.getAuthority() + (path.length() > 90 ? path.substring(path.length() - 90) : path);
        } catch (Exception e) {
            return "an address that will not parse";
        }
    }

    private static InputStream open(ContentResolver cr, String uri) throws Exception {
        if (uri.startsWith("http://") || uri.startsWith("https://")) {
            HttpURLConnection h = (HttpURLConnection) new URL(uri).openConnection();
            h.setConnectTimeout(15_000);
            h.setReadTimeout(20_000);
            return h.getInputStream();
        }
        return cr.openInputStream(Uri.parse(uri));
    }
}
