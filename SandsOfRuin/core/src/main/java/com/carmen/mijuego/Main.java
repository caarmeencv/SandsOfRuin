package com.carmen.mijuego;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.audio.AudioManager;
import com.carmen.mijuego.screens.IntroScreen;

public class Main extends Game {

    public SpriteBatch batch;
    public Assets assets;
    public AudioManager audio;

    public int vidas = 5;
    public float runTimeSeconds = 0f;

    @Override
    public void create() {
        batch = new SpriteBatch();

        assets = new Assets();
        assets.queueLoadAll();
        assets.finishLoading();

        audio = new AudioManager(assets);

        resetRun(); // ✅ inicializa run nueva

        setScreen(new IntroScreen(this));
    }

    // ✅ NUEVO
    public void resetRun() {
        vidas = 5;
        runTimeSeconds = 0f;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (audio != null) audio.stopMusic();
        if (batch != null) batch.dispose();
        if (assets != null) assets.dispose();
    }
}
