package com.techpp.glass;

// Reads the TV's own debug switches, for Talkies settings → Network debugging.

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

/**
 * Whether the TV lets a computer look at it, in words a menu row can show. Reading is free and needs no permission.
 * Writing is another matter: Android only hands out WRITE_SECURE_SETTINGS over adb (the same permission ServiceRepair
 * uses), so without it the shortcut here is Android's own screen - which is where the switches live anyway.
 */
final class DebugState {
    /**
     * The key behind the "Wireless debugging" switch of Android 11 and later. It has no constant, and it is the only
     * way to see that switch: it listens on a port that changes every time, so nothing on 5555 tells you about it.
     * Android TV's older "Network debugging" instead listens on 5555 and leaves this key alone - a TV does one or
     * the other, so both are read.
     */
    private static final String WIFI_ENABLED = "adb_wifi_enabled";

    private DebugState() { }

    static boolean devOptions(Context c) {
        return Settings.Global.getInt(c.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0;
    }

    /** Any debugging at all: the "Android debugging" switch, or the wireless one beside it. */
    static boolean adbOn(Context c) {
        return wifiSwitch(c)
                || Settings.Global.getInt(c.getContentResolver(), Settings.Global.ADB_ENABLED, 0) != 0;
    }

    /** The Android 11+ switch, which can be on while the older one is off - that is how most TVs look now. */
    private static boolean wifiSwitch(Context c) {
        return Settings.Global.getInt(c.getContentResolver(), WIFI_ENABLED, 0) != 0;
    }

    /**
     * Whether a computer can get in right now, in code rather than in words: the text below is translated, and a
     * caller that reads it looks for the English one. Waits up to 400 ms for the port, so not on the UI thread.
     */
    static boolean reachable(Context c) {
        return wifiSwitch(c) || adbOn(c) && listening(c);
    }

    /** Off · On · Wi-Fi · On · port 5555 · USB only. Waits up to 400 ms for the port, so not on the UI thread. */
    static String wirelessText(Context c) {
        if (wifiSwitch(c)) return c.getString(R.string.dbg_wifi_on);
        if (!adbOn(c)) return devOptions(c) ? c.getString(R.string.state_off) : c.getString(R.string.dbg_off_dev);
        return listening(c) ? c.getString(R.string.dbg_port) : c.getString(R.string.dbg_usb_only);
    }

    private static boolean listening(Context c) {
        try (java.net.Socket sock = new java.net.Socket()) {
            sock.connect(new java.net.InetSocketAddress("127.0.0.1", 5555), 400);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * The one press that turns developer options on. Only the adb-granted permission can; false means nothing
     * changed, either because there was no permission or because Android refused.
     */
    static boolean switchOnDevOptions(Context c) {
        if (devOptions(c) || !ServiceRepair.canRepair(c)) return false;
        try {
            Settings.Global.putInt(c.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1);
            return devOptions(c);
        } catch (RuntimeException e) {
            Log.w("GlassDebug", "Couldn't switch developer options on: " + e);
            return false;
        }
    }

    /**
     * Both debug switches down, which is what the menu's Turn off row is for. The flags are read back rather than
     * trusted: Android keeps the wireless one for its own screen, so a write can land and still not close it.
     */
    static boolean switchOffAdb(Context c) {
        try {
            Settings.Global.putInt(c.getContentResolver(), Settings.Global.ADB_ENABLED, 0);
            Settings.Global.putInt(c.getContentResolver(), WIFI_ENABLED, 0);
        } catch (RuntimeException e) {
            Log.w("GlassDebug", "Couldn't switch debugging off: " + e);
        }
        return !adbOn(c);
    }
}
