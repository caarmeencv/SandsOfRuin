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

    @Override
    public void create() {
        batch = new SpriteBatch();

        assets = new Assets();
        assets.queueLoadAll();

        // ✅ si aquí peta, es que falta algún archivo o ruta mal
        assets.finishLoading();

        audio = new AudioManager(assets);

        setScreen(new IntroScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (audio != null) audio.stopMusic();
        if (batch != null) batch.dispose();
        if (assets != null) assets.dispose();
    }
}
