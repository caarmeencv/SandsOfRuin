package com.carmen.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.carmen.mijuego.Main;
import com.carmen.mijuego.assets.Assets;

public class IntroScreen implements Screen {

    private final Main game;

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture introImage;

    private BitmapFont font;
    private GlyphLayout layout;

    private float blinkTime = 0f;

    public IntroScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0f);
        camera.update();

        // ✅ AssetManager
        introImage = game.assets.get(Assets.SCREEN_INTRO);

        font = new BitmapFont();
        font.getData().setScale(1.5f);
        layout = new GlyphLayout();
    }

    @Override
    public void show() {
        // ✅ Empieza aquí y seguirá en el menú sin reiniciar
        game.audio.playMusic(Assets.MUS_INTRO_THEME, true);
    }

    @Override
    public void render(float delta) {
        blinkTime += delta;

        if (Gdx.input.justTouched()
            || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.setScreen(new MenuScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.draw(introImage, 0, 0, WORLD_W, WORLD_H);

        boolean visible = (blinkTime % 1f) < 0.5f;
        if (visible) {
            String text = "TOCA PARA CONTINUAR";
            layout.setText(font, text);

            float x = (WORLD_W - layout.width) / 2f;
            float y = 80f;

            font.draw(game.batch, layout, x, y);
        }

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        // ❌ NO disposes introImage (es del AssetManager)
        font.dispose();
    }
}
