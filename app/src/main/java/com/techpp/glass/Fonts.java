package com.techpp.glass;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.SparseArray;

/**
 * Figtree at a weight, made once. setFontVariationSettings makes a new typeface on each call, which adds up when every
 * card and tab asks for one; setting a typeface already made costs next to nothing. (An idea from codermert's build.)
 */
final class Fonts {
    private static final SparseArray<Typeface> MADE = new SparseArray<>();

    private Fonts() { }

    static synchronized Typeface figtree(Context c, int weight) {
        Typeface t = MADE.get(weight);
        if (t == null) {
            Paint p = new Paint();
            p.setTypeface(c.getResources().getFont(R.font.figtree));
            p.setFontVariationSettings("'wght' " + weight);
            t = p.getTypeface();
            MADE.put(weight, t);
        }
        return t;
    }
}
