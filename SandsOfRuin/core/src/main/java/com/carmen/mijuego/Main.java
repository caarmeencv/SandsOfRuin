package com.carmen.mijuego;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.audio.AudioManager;
import com.carmen.mijuego.screens.IntroScreen;

import com.carmen.mijuego.settings.GameSettings;
import com.carmen.mijuego.settings.I18N;

public class Main extends Game {

    public SpriteBatch batch;
    public Assets assets;
    public AudioManager audio;
    public GameSettings settings;
    public I18N i18n;

    public int vidas = 5;
    public float runTimeSeconds = 0f;

    @Override
    public void create() {

        batch = new SpriteBatch();

        assets = new Assets();
        assets.queueLoadAll();
        assets.finishLoading();

        // ✅ 1) settings primero
        settings = new GameSettings();

        // ✅ 2) i18n después
        i18n = new I18N(settings);

        // ✅ 3) audio al final (ya tiene settings)
        audio = new AudioManager(assets, settings);

        resetRun();

        setScreen(new IntroScreen(this));
    }

    public void resetRun() {
        vidas = 5;
        runTimeSeconds = 0f;
    }

    // ✅ NUEVO: vibra SOLO si está activado en opciones
    public void vibrateHit(int ms) {
        if (settings != null) {
            if (settings.isVibrationEnabled()) {
                Gdx.input.vibrate(ms);
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        if (audio != null) audio.stopMusic();
        if (batch != null) batch.dispose();
        if (assets != null) assets.dispose();
    }
}
