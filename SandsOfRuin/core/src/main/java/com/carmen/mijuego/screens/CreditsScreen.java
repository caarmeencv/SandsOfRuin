package com.carmen.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.carmen.mijuego.Main;
import com.carmen.mijuego.assets.Assets;

public class CreditsScreen implements Screen {

    private final Main game;

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture bg;

    private BitmapFont fontTitle;
    private BitmapFont fontMain;
    private BitmapFont fontHint;

    private GlyphLayout layout = new GlyphLayout();

    private float blinkTime = 0f;

    public CreditsScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0f);
        camera.update();

        bg = game.assets.get(Assets.SCREEN_CREDITS_BG);

        fontTitle = new BitmapFont();
        fontTitle.getData().setScale(4.0f);

        fontMain = new BitmapFont();
        fontMain.getData().setScale(2.0f);

        fontHint = new BitmapFont();
        fontHint.getData().setScale(1.6f);
    }

    @Override
    public void show() {
        game.audio.playMusic(Assets.MUS_CREDITS_THEME, true);
    }

    @Override
    public void render(float delta) {

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.BACK) ||
            Gdx.input.justTouched()) {

            game.setScreen(new MenuScreen(game));
            return;
        }

        blinkTime += delta;

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);

        String title = game.i18n.t("credits.title");
        String creditsText = game.i18n.t("credits.text");
        String hintText = game.i18n.t("credits.hint");

        game.batch.begin();

        game.batch.draw(bg, 0, 0, WORLD_W, WORLD_H);

        // Título
        float titleY = WORLD_H - 120f;
        fontTitle.draw(game.batch, title, 0, titleY, WORLD_W, Align.center, false);

        // Texto principal
        layout.setText(fontMain, creditsText, com.badlogic.gdx.graphics.Color.WHITE, WORLD_W * 0.8f, Align.center, true);
        float mainY = WORLD_H / 2f + layout.height / 2f - 40f;
        fontMain.draw(game.batch, creditsText, 0, mainY, WORLD_W, Align.center, true);

        // Hint parpadeo
        float alpha = 0.4f + 0.6f * (0.5f + 0.5f * (float)Math.sin(blinkTime * 4f));
        fontHint.setColor(1f, 1f, 1f, alpha);
        fontHint.draw(game.batch, hintText, 0, 60f, WORLD_W, Align.center, false);
        fontHint.setColor(1f, 1f, 1f, 1f);

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
        fontTitle.dispose();
        fontMain.dispose();
        fontHint.dispose();
    }
}
