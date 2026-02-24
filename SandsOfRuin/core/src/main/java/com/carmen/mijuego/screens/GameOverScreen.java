package com.carmen.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.carmen.mijuego.Main;
import com.carmen.mijuego.assets.Assets;

public class GameOverScreen implements Screen {

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    private final Main game;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture background;

    private BitmapFont font;
    private GlyphLayout layout;

    private boolean clickedOnce = false;

    public GameOverScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply();

        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0f);
        camera.update();

        background = game.assets.get(Assets.SCREEN_GAMEOVER_BG);

        font = new BitmapFont();
        font.getData().setScale(2.5f);

        layout = new GlyphLayout();
    }

    @Override
    public void show() {
        // Música de game over
        game.audio.playMusic(Assets.MUS_GAME_OVER_THEME, true);
    }

    @Override
    public void render(float delta) {

        // Input (móvil + PC)
        if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (!clickedOnce) {
                clickedOnce = true;
                game.audio.playSfx(Assets.SFX_BUTTON_CLICKED);
            }
            game.setScreen(new MenuScreen(game));
            return;
        }

        ScreenUtils.clear(0, 0, 0, 1);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

        game.batch.draw(background, 0, 0, WORLD_W, WORLD_H);

        String text = "Toca para volver al menu\nPulsa ESC para volver al menu";
        layout.setText(font, text);

        float x = (WORLD_W - layout.width) / 2f;
        float y = 140f;

        font.draw(game.batch, layout, x, y);

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
        font.dispose();
    }
}
