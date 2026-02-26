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
import com.carmen.mijuego.ui.Fonts;

public class CreditsScreen implements Screen {

    private final Main game;

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture bg;

    private BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    private float blinkTime = 0f;

    public CreditsScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0f);
        camera.update();

        bg = game.assets.get(Assets.SCREEN_CREDITS_BG);

        // ✅ Fuente del juego (1 sola)
        font = Fonts.main(game);
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
        font.getData().setScale(4.0f);
        font.draw(game.batch, title, 0, WORLD_H - 120f, WORLD_W, Align.center, false);

        // Texto principal
        font.getData().setScale(2.0f);
        layout.setText(font, creditsText, com.badlogic.gdx.graphics.Color.WHITE, WORLD_W * 0.8f, Align.center, true);
        float mainY = WORLD_H / 2f + layout.height / 2f - 40f;
        font.draw(game.batch, creditsText, 0, mainY, WORLD_W, Align.center, true);

        // Hint parpadeo
        font.getData().setScale(1.6f);
        float alpha = 0.4f + 0.6f * (0.5f + 0.5f * (float)Math.sin(blinkTime * 4f));
        font.setColor(1f, 1f, 1f, alpha);
        font.draw(game.batch, hintText, 0, 60f, WORLD_W, Align.center, false);
        Fonts.resetColor(font);

        // Reset scale para no arrastrar a otras pantallas
        font.getData().setScale(2.0f);

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
        // ✅ NO dispose()
    }
}
