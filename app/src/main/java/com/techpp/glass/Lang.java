package com.techpp.glass;

import android.content.Context;
import android.content.res.Configuration;
import android.os.LocaleList;

import java.util.Locale;

/**
 * The language Talkies speaks, which is not the TV's often enough to be worth a setting: plenty of TVs stay on
 * "English (United States)" wherever they are sold, and a Spanish screen then has no way to ask for Spanish.
 * Auto follows the TV; English and Español say otherwise.
 * Only Talkies' own words (docs/l10n.en.md). What country the picks, the scores and °C/°F come from is Region's
 * business, and that reads the TV's time zone and the system locale, which this leaves alone: choosing Spanish
 * here changes no data, only the labels.
 */
final class Lang {
    /** The codes the Language row cycles through, in that order; lang_labels holds what they're called. */
    static final String[] CODES = {"auto", "en", "es"};

    private Lang() { }

    static String code(Context c) {
        String v = c.getSharedPreferences("home", Context.MODE_PRIVATE).getString("lang", "es");
        return v == null || v.isEmpty() ? "es" : v;
    }

    static int index(Context c) {
        String code = code(c);
        for (int i = 0; i < CODES.length; i++) if (CODES[i].equals(code)) return i;
        return 0;
    }

    /** The row's next choice, saved; the caller restarts Home so every label is read again. */
    static void cycle(Context c) {
        c.getSharedPreferences("home", Context.MODE_PRIVATE).edit()
                .putString("lang", CODES[(index(c) + 1) % CODES.length]).apply();
    }

    static String label(Context c) {
        return c.getResources().getStringArray(R.array.lang_labels)[index(c)];
    }

    /** The context, reading Talkies' strings in the chosen language. Auto hands back what it was given. */
    static Context wrap(Context base) {
        String code = code(base);
        if (code.equals("auto")) return base;
        Configuration c = new Configuration(base.getResources().getConfiguration());
        Locale here = new Locale(code);
        c.setLocales(new LocaleList(here));
        // Dates and numbers come from the default locale, not from a context, so a wrapped Home would still say
        // "Monday": these two lines are what make "lunes" and "1,2 GB" happen.
        Locale.setDefault(Locale.Category.DISPLAY, here);
        Locale.setDefault(Locale.Category.FORMAT, here);
        return base.createConfigurationContext(c);
    }
}
