package com.carmen.mijuego;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.screens.IntroScreen;

public class Main extends Game {

    public SpriteBatch batch;
    public Assets assets;

    @Override
    public void create() {
        batch = new SpriteBatch();

        assets = new Assets();
        assets.queueLoadAll();
        assets.finishLoading(); // rápido: sin loading screen

        setScreen(new IntroScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        assets.dispose();
    }
}
