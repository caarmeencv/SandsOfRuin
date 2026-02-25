package com.carmen.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.carmen.mijuego.Main;
import com.carmen.mijuego.assets.Assets;

public class PauseScreen implements Screen {

    public enum Context { DESERT, PYRAMID }

    private final Main game;
    private final Screen returnScreen;
    private final Context context;

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture bg;
    private Texture btnContinue, btnReset, btnMenu;

    private final Rectangle rContinue = new Rectangle();
    private final Rectangle rReset = new Rectangle();
    private final Rectangle rMenu = new Rectangle();

    private final Vector2 pointerWorld = new Vector2();

    private boolean hoverContinue, hoverReset, hoverMenu;

    private BitmapFont font;
    private GlyphLayout layout;

    private static final float HOVER_SCALE = 1.12f;

    public PauseScreen(Main game, Screen returnScreen, Context context) {
        this.game = game;
        this.returnScreen = returnScreen;
        this.context = context;

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0f);
        camera.update();

        if (context == Context.DESERT)
            bg = game.assets.get(Assets.SCREEN_PAUSE_BG_DESERT);
        else
            bg = game.assets.get(Assets.SCREEN_PAUSE_BG_PYRAMID);

        btnContinue = game.assets.get(Assets.SCREEN_PAUSE_BTN_CONTINUE);
        btnReset    = game.assets.get(Assets.SCREEN_PAUSE_BTN_RESET);
        btnMenu     = game.assets.get(Assets.SCREEN_PAUSE_BTN_MENU);

        font = new BitmapFont();
        font.getData().setScale(2.6f);
        layout = new GlyphLayout();

        updateLayout();
    }

    private void updateLayout() {
        float btnW = 540f;
        float btnH = btnW * ((float) btnContinue.getHeight() / btnContinue.getWidth());
        float gap = 45f;
        float totalH = btnH * 3f + gap * 2f;

        float x = (WORLD_W - btnW) / 2f;
        float startY = (WORLD_H - totalH) / 2f;

        rMenu.set(x, startY, btnW, btnH);
        rReset.set(x, startY + btnH + gap, btnW, btnH);
        rContinue.set(x, startY + (btnH + gap) * 2f, btnW, btnH);
    }

    private void updatePointer() {
        pointerWorld.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(pointerWorld);

        hoverContinue = rContinue.contains(pointerWorld);
        hoverReset = rReset.contains(pointerWorld);
        hoverMenu = rMenu.contains(pointerWorld);
    }

    private void drawButton(Texture tex, Rectangle r, boolean hover, String text) {

        float scale = hover ? HOVER_SCALE : 1f;

        float w = r.width * scale;
        float h = r.height * scale;

        float x = r.x + (r.width - w) / 2f;
        float y = r.y + (r.height - h) / 2f;

        game.batch.draw(tex, x, y, w, h);

        float baseScale = 2.6f;
        font.getData().setScale(baseScale * scale);

        layout.setText(font, text);

        float iconArea = w * 0.22f;
        float usableWidth = w - iconArea;

        float textX = x + iconArea + (usableWidth - layout.width) / 2f;
        float textY = y + (h + layout.height) / 2f;

        font.setColor(1f, 1f, 1f, 1f);
        font.draw(game.batch, layout, textX, textY);

        font.getData().setScale(baseScale);
    }

    @Override
    public void show() {
        game.audio.playMusic(Assets.MUS_PAUSE_THEME, true);
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {

        updatePointer();

        if (Gdx.input.justTouched()) {

            if (hoverContinue) {
                game.setScreen(returnScreen);
                return;
            }

            if (hoverReset) {
                game.resetRun();
                if (context == Context.DESERT)
                    game.setScreen(new DesertScreen(game));
                else
                    game.setScreen(new PyramidScreen(game));
                return;
            }

            if (hoverMenu) {
                game.setScreen(new MenuScreen(game));
                return;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(returnScreen);
            return;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

        game.batch.draw(bg, 0, 0, WORLD_W, WORLD_H);

        drawButton(btnContinue, rContinue, hoverContinue, "CONTINUAR");
        drawButton(btnReset, rReset, hoverReset, "REINICIAR");
        drawButton(btnMenu, rMenu, hoverMenu, "VOLVER AL MENU");

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        updateLayout();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        font.dispose();
    }
}
