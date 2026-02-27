package com.carmen.mijuego.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class GameSettings {

    // Nombre del archivo donde se guardan las preferencias
    private static final String PREF_NAME = "sands_of_ruin_settings";

    // Claves internas para guardar cada ajuste
    private static final String KEY_LANG_ES = "lang_es";
    private static final String KEY_ACCEL_JUMP = "accel_jump";
    private static final String KEY_VIBRATION = "vibration";
    private static final String KEY_MUSIC = "music";
    private static final String KEY_SFX = "sfx";

    // Objeto que permite guardar datos persistentes en el dispositivo
    private Preferences prefs;

    // Variables internas que representan el estado actual de cada opción
    private boolean langSpanish;
    private boolean accelJumpEnabled;
    private boolean vibrationEnabled;
    private boolean musicEnabled;
    private boolean sfxEnabled;

    // Constructor
    // Cuando se crea el objeto, se conecta al archivo de preferencias
    // y carga los valores guardados anteriormente
    public GameSettings() {
        prefs = Gdx.app.getPreferences(PREF_NAME);
        load();
    }

    // Carga los valores guardados
    // Si no existen todavía, usa valores por defecto
    public void load() {

        // Idioma por defecto inglés
        langSpanish = prefs.getBoolean(KEY_LANG_ES, false);

        // Salto por acelerómetro desactivado por defecto
        accelJumpEnabled = prefs.getBoolean(KEY_ACCEL_JUMP, false);

        // Vibración activada por defecto
        vibrationEnabled = prefs.getBoolean(KEY_VIBRATION, true);

        // Música activada por defecto
        musicEnabled = prefs.getBoolean(KEY_MUSIC, true);

        // Sonidos activados por defecto
        sfxEnabled = prefs.getBoolean(KEY_SFX, true);
    }

    // Guarda en disco los valores actuales
    public void save() {

        prefs.putBoolean(KEY_LANG_ES, langSpanish);
        prefs.putBoolean(KEY_ACCEL_JUMP, accelJumpEnabled);
        prefs.putBoolean(KEY_VIBRATION, vibrationEnabled);
        prefs.putBoolean(KEY_MUSIC, musicEnabled);
        prefs.putBoolean(KEY_SFX, sfxEnabled);

        // Fuerza a que se escriban realmente en el dispositivo
        prefs.flush();
    }

    // Métodos para consultar el estado de cada opción

    public boolean isLangSpanish() {
        return langSpanish;
    }

    public boolean isAccelJumpEnabled() {
        return accelJumpEnabled;
    }

    public boolean isVibrationEnabled() {
        return vibrationEnabled;
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public boolean isSfxEnabled() {
        return sfxEnabled;
    }

    // Métodos para cambiar el estado de cada opción

    public void setLangSpanish(boolean value) {
        langSpanish = value;
    }

    public void setAccelJumpEnabled(boolean value) {
        accelJumpEnabled = value;
    }

    public void setVibrationEnabled(boolean value) {
        vibrationEnabled = value;
    }

    public void setMusicEnabled(boolean value) {
        musicEnabled = value;
    }

    public void setSfxEnabled(boolean value) {
        sfxEnabled = value;
    }
}
