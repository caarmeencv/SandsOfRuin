package com.carmen.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.carmen.mijuego.Main;
import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.ui.Fonts;

public class HowToPlayScreen implements Screen {

    private final Main game;

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture bg;

    // ✅ Una sola fuente global
    private BitmapFont font;

    private GlyphLayout layoutTitle;
    private GlyphLayout layoutLeft;
    private GlyphLayout layoutRight;
    private GlyphLayout layoutTapBack;
    private GlyphLayout layoutKeyBack;

    private final Vector2 pointerWorld = new Vector2();
    private final Rectangle rTapBack = new Rectangle();
    private final Rectangle rKeyBack = new Rectangle();

    private boolean hoverTapBack = false;
    private boolean hoverKeyBack = false;

    public HowToPlayScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0f);
        camera.update();

        bg = game.assets.get(Assets.SCREEN_HOWTOPLAY_BG);

        font = Fonts.main(game);
        font.setColor(Color.WHITE);

        layoutTitle = new GlyphLayout();
        layoutLeft = new GlyphLayout();
        layoutRight = new GlyphLayout();
        layoutTapBack = new GlyphLayout();
        layoutKeyBack = new GlyphLayout();
    }

    @Override
    public void show() {
        game.audio.playMusic(Assets.MUS_INTRO_THEME, true);
    }

    private void goBack() {
        game.setScreen(new MenuScreen(game));
    }

    private void updatePointer() {
        pointerWorld.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(pointerWorld);

        if (rTapBack.contains(pointerWorld)) {
            hoverTapBack = true;
        } else {
            hoverTapBack = false;
        }

        if (rKeyBack.contains(pointerWorld)) {
            hoverKeyBack = true;
        } else {
            hoverKeyBack = false;
        }
    }

    private void updateFooterRects() {

        float tapX = (WORLD_W - layoutTapBack.width) / 2f;
        float tapY = 95f;
        rTapBack.set(tapX, tapY - layoutTapBack.height,
            layoutTapBack.width, layoutTapBack.height);

        float keyX = (WORLD_W - layoutKeyBack.width) / 2f;
        float keyY = 65f;
        rKeyBack.set(keyX, keyY - layoutKeyBack.height,
            layoutKeyBack.width, layoutKeyBack.height);
    }

    @Override
    public void render(float delta) {

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            goBack();
            return;
        }

        updatePointer();

        if (Gdx.input.justTouched()) {
            if (hoverTapBack) {
                goBack();
                return;
            }
            if (hoverKeyBack) {
                goBack();
                return;
            }
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);

        String leftText = game.i18n.t("howto.left");
        String rightText = game.i18n.t("howto.right");

        float margin = 100f;
        float columnGap = 60f;
        float columnW = (WORLD_W - margin * 2f - columnGap) / 2f;

        float leftX = margin;
        float rightX = margin + columnW + columnGap;

        float topY = 590f;
        float bottomLimit = 130f;
        float availableH = topY - bottomLimit;

        // ======================
        // TITLE
        // ======================
        font.getData().setScale(2.6f);
        layoutTitle.setText(font, game.i18n.t("howto.title"));

        // ======================
        // BODY AUTO-SCALE
        // ======================
        float scale = 1.9f;

        for (int i = 0; i < 8; i++) {

            font.getData().setScale(scale);

            layoutLeft.setText(font, leftText, Color.WHITE,
                columnW, Align.left, true);

            layoutRight.setText(font, rightText, Color.WHITE,
                columnW, Align.left, true);

            float neededH = Math.max(layoutLeft.height, layoutRight.height);

            if (neededH <= availableH) {
                break;
            }

            scale = scale - 0.10f;
        }

        // ======================
        // FOOTER TEXTS
        // ======================

        String tapBack;
        if (hoverTapBack) {
            tapBack = game.i18n.t("howto.tap.hover");
        } else {
            tapBack = game.i18n.t("howto.tap.normal");
        }

        font.getData().setScale(1.45f);
        layoutTapBack.setText(font, tapBack);

        String keyBack;
        if (hoverKeyBack) {
            keyBack = game.i18n.t("howto.key.hover");
        } else {
            keyBack = game.i18n.t("howto.key.normal");
        }

        font.getData().setScale(1.20f);
        layoutKeyBack.setText(font, keyBack);

        updateFooterRects();

        // ======================
        // DRAW
        // ======================
        game.batch.begin();

        game.batch.draw(bg, 0, 0, WORLD_W, WORLD_H);

        // Title
        font.setColor(Color.WHITE);
        font.getData().setScale(2.6f);
        font.draw(game.batch, layoutTitle,
            (WORLD_W - layoutTitle.width) / 2f, 665f);

        // Body
        font.getData().setScale(scale);
        font.draw(game.batch, layoutLeft, leftX, topY);
        font.draw(game.batch, layoutRight, rightX, topY);

        // Tap back
        font.getData().setScale(1.45f);
        if (hoverTapBack) {
            font.setColor(Color.WHITE);
        } else {
            font.setColor(Color.LIGHT_GRAY);
        }
        font.draw(game.batch, layoutTapBack,
            (WORLD_W - layoutTapBack.width) / 2f, 95f);

        // Key back
        font.getData().setScale(1.20f);
        if (hoverKeyBack) {
            font.setColor(Color.WHITE);
        } else {
            font.setColor(Color.LIGHT_GRAY);
        }
        font.draw(game.batch, layoutKeyBack,
            (WORLD_W - layoutKeyBack.width) / 2f, 65f);

        // Reset
        Fonts.resetColor(font);
        font.getData().setScale(1f);

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
        // NO dispose (AssetManager)
    }
}
