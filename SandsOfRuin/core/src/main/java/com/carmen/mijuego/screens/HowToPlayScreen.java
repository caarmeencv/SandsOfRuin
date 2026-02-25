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

public class HowToPlayScreen implements Screen {

    private final Main game;

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture bg;

    private BitmapFont fontTitle;
    private BitmapFont fontBody;
    private BitmapFont fontFooter;

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

        fontTitle = new BitmapFont();
        fontTitle.setColor(Color.WHITE);
        fontTitle.getData().setScale(2.6f);

        fontBody = new BitmapFont();
        fontBody.setColor(Color.WHITE);
        fontBody.getData().setScale(1.8f);

        fontFooter = new BitmapFont();
        fontFooter.setColor(Color.WHITE);
        fontFooter.getData().setScale(1.35f);

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

        hoverTapBack = rTapBack.contains(pointerWorld);
        hoverKeyBack = rKeyBack.contains(pointerWorld);
    }

    private void updateFooterRects() {
        float tapX = (WORLD_W - layoutTapBack.width) / 2f;
        float tapY = 95f;
        rTapBack.set(tapX, tapY - layoutTapBack.height, layoutTapBack.width, layoutTapBack.height);

        float keyX = (WORLD_W - layoutKeyBack.width) / 2f;
        float keyY = 65f;
        rKeyBack.set(keyX, keyY - layoutKeyBack.height, layoutKeyBack.width, layoutKeyBack.height);
    }

    @Override
    public void render(float delta) {

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            goBack();
            return;
        }

        updatePointer();

        if (Gdx.input.justTouched()) {
            if (hoverTapBack || hoverKeyBack) {
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

        layoutTitle.setText(fontTitle, game.i18n.t("howto.title"));

        float scale = 1.9f;
        for (int i = 0; i < 8; i++) {
            fontBody.getData().setScale(scale);

            layoutLeft.setText(fontBody, leftText, Color.WHITE, columnW, Align.left, true);
            layoutRight.setText(fontBody, rightText, Color.WHITE, columnW, Align.left, true);

            float neededH = Math.max(layoutLeft.height, layoutRight.height);
            if (neededH <= availableH) break;

            scale -= 0.10f;
        }

        fontFooter.getData().setScale(1.45f);
        String tapBack = hoverTapBack ? game.i18n.t("howto.tap.hover") : game.i18n.t("howto.tap.normal");
        layoutTapBack.setText(fontFooter, tapBack);

        fontFooter.getData().setScale(1.20f);
        String keyBack = hoverKeyBack ? game.i18n.t("howto.key.hover") : game.i18n.t("howto.key.normal");
        layoutKeyBack.setText(fontFooter, keyBack);

        updateFooterRects();

        game.batch.begin();

        game.batch.draw(bg, 0, 0, WORLD_W, WORLD_H);

        fontTitle.draw(game.batch, layoutTitle, (WORLD_W - layoutTitle.width) / 2f, 665f);

        fontBody.draw(game.batch, layoutLeft, leftX, topY);
        fontBody.draw(game.batch, layoutRight, rightX, topY);

        fontFooter.getData().setScale(1.45f);
        fontFooter.setColor(hoverTapBack ? Color.WHITE : Color.LIGHT_GRAY);
        fontFooter.draw(game.batch, layoutTapBack, (WORLD_W - layoutTapBack.width) / 2f, 95f);

        fontFooter.getData().setScale(1.20f);
        fontFooter.setColor(hoverKeyBack ? Color.WHITE : Color.LIGHT_GRAY);
        fontFooter.draw(game.batch, layoutKeyBack, (WORLD_W - layoutKeyBack.width) / 2f, 65f);

        fontFooter.setColor(Color.WHITE);

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
        fontBody.dispose();
        fontFooter.dispose();
    }
}
