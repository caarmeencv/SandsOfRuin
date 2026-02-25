package com.carmen.mijuego.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class GameSettings {

    private static final String PREF_NAME = "sands_of_ruin_settings";

    private static final String KEY_LANG_ES = "lang_es";
    private static final String KEY_ACCEL_JUMP = "accel_jump";
    private static final String KEY_VIBRATION = "vibration";
    private static final String KEY_MUSIC = "music";
    private static final String KEY_SFX = "sfx";

    private final Preferences prefs;

    // valores en memoria
    private boolean langSpanish;
    private boolean accelJumpEnabled;
    private boolean vibrationEnabled;
    private boolean musicEnabled;
    private boolean sfxEnabled;

    public GameSettings() {
        prefs = Gdx.app.getPreferences(PREF_NAME);
        load();
    }

    public void load() {
        langSpanish = prefs.getBoolean(KEY_LANG_ES, false);

        // ✅ por defecto: OFF (juego normal al inicio)
        accelJumpEnabled = prefs.getBoolean(KEY_ACCEL_JUMP, false);

        vibrationEnabled = prefs.getBoolean(KEY_VIBRATION, true);
        musicEnabled = prefs.getBoolean(KEY_MUSIC, true);
        sfxEnabled = prefs.getBoolean(KEY_SFX, true);
    }

    public void save() {
        prefs.putBoolean(KEY_LANG_ES, langSpanish);
        prefs.putBoolean(KEY_ACCEL_JUMP, accelJumpEnabled);
        prefs.putBoolean(KEY_VIBRATION, vibrationEnabled);
        prefs.putBoolean(KEY_MUSIC, musicEnabled);
        prefs.putBoolean(KEY_SFX, sfxEnabled);
        prefs.flush();
    }

    // GETTERS
    public boolean isLangSpanish() { return langSpanish; }
    public boolean isAccelJumpEnabled() { return accelJumpEnabled; }
    public boolean isVibrationEnabled() { return vibrationEnabled; }
    public boolean isMusicEnabled() { return musicEnabled; }
    public boolean isSfxEnabled() { return sfxEnabled; }

    // SETTERS
    public void setLangSpanish(boolean value) { langSpanish = value; }
    public void setAccelJumpEnabled(boolean value) { accelJumpEnabled = value; }
    public void setVibrationEnabled(boolean value) { vibrationEnabled = value; }
    public void setMusicEnabled(boolean value) { musicEnabled = value; }
    public void setSfxEnabled(boolean value) { sfxEnabled = value; }
}
