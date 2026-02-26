package com.carmen.mijuego.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MummyTimeRecords {

    private static final String PREF_NAME = "sands_of_ruin_records";
    private static final String KEY_TIMES = "mummy_times_seconds";
    private static final int MAX_RECORDS = 10;

    private MummyTimeRecords() {}

    private static Preferences prefs() {
        return Gdx.app.getPreferences(PREF_NAME);
    }

    public static void addTimeSeconds(int seconds) {
        if (seconds < 1) seconds = 1;

        List<Integer> times = getTimesSecondsInternal();
        times.add(seconds);

        Collections.sort(times);

        while (times.size() > MAX_RECORDS) {
            times.remove(times.size() - 1);
        }

        saveList(times);

        Gdx.app.log("MummyTimeRecords", "SAVED " + seconds + "s. LIST=" + times);
    }

    public static List<Integer> getTimesSeconds() {
        List<Integer> out = getTimesSecondsInternal();
        Gdx.app.log("MummyTimeRecords", "READ LIST=" + out);
        return out;
    }

    public static String debugRaw() {
        String raw = prefs().getString(KEY_TIMES, "");
        Gdx.app.log("MummyTimeRecords", "READ RAW='" + raw + "' pref=" + PREF_NAME + " key=" + KEY_TIMES);
        return raw;
    }

    private static List<Integer> getTimesSecondsInternal() {
        String raw = prefs().getString(KEY_TIMES, "");
        ArrayList<Integer> out = new ArrayList<>();

        if (raw == null || raw.trim().isEmpty()) return out;

        String[] parts = raw.split(",");
        for (int i = 0; i < parts.length; i++) {
            try {
                int v = Integer.parseInt(parts[i].trim());
                if (v >= 0) out.add(v);
            } catch (Exception ignored) {}
        }

        Collections.sort(out);
        return out;
    }

    private static void saveList(List<Integer> times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(times.get(i));
        }

        prefs().putString(KEY_TIMES, sb.toString());
        prefs().flush();

        Gdx.app.log("MummyTimeRecords", "FLUSH OK raw=" + sb);
    }

    public static String formatMMSS(int seconds) {
        if (seconds < 0) seconds = 0;
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format("%02d:%02d", min, sec);
    }
}
