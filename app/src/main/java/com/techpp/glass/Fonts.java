package com.techpp.glass;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.SparseArray;

/**
 * The two faces at a weight, each made once. setFontVariationSettings makes a new typeface on every call, which adds
 * up when every card and tab asks for one; setting a typeface already made costs next to nothing. (An idea from
 * codermert's build.) Both are variable, so a weight is a number rather than another file; 400 is what each font's
 * own default instance draws, which is why the layouts set no weight at all.
 */
final class Fonts {
    private static final SparseArray<Typeface> TITLES = new SparseArray<>();
    private static final SparseArray<Typeface> BODIES = new SparseArray<>();

    private Fonts() { }

    /** Plus Jakarta Sans: names, titles and the clock - the words that stand for a thing. */
    static Typeface title(Context c, int weight) { return made(c, TITLES, R.font.plus_jakarta_sans, weight); }

    /** Inter Tight: menus, lists and every line read while moving through them. */
    static Typeface body(Context c, int weight) { return made(c, BODIES, R.font.inter_tight, weight); }

    private static synchronized Typeface made(Context c, SparseArray<Typeface> cache, int face, int weight) {
        Typeface t = cache.get(weight);
        if (t == null) {
            Paint p = new Paint();
            p.setTypeface(c.getResources().getFont(face));
            p.setFontVariationSettings("'wght' " + weight);
            t = p.getTypeface();
            cache.put(weight, t);
        }
        return t;
    }
}
