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

public class MenuScreen implements Screen {

    private final Main game;

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture bg;
    private Texture btnGame, btnOptions, btnCredits, btnAchievements;

    private final Rectangle rGame = new Rectangle();
    private final Rectangle rOptions = new Rectangle();
    private final Rectangle rCredits = new Rectangle();
    private final Rectangle rAchievements = new Rectangle();

    private final Vector2 pointerWorld = new Vector2();

    private boolean hoverGame, hoverOptions, hoverCredits, hoverAchievements;

    private static final float HOVER_SCALE = 1.08f;

    private BitmapFont font;
    private GlyphLayout layout;

    public MenuScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0f);
        camera.update();

        // ✅ AssetManager
        bg = game.assets.get(Assets.SCREEN_MENU_BG);
        btnGame = game.assets.get(Assets.SCREEN_MENU_BTN_GAME);
        btnOptions = game.assets.get(Assets.SCREEN_MENU_BTN_OPTIONS);
        btnCredits = game.assets.get(Assets.SCREEN_MENU_BTN_CREDITS);
        btnAchievements = game.assets.get(Assets.SCREEN_MENU_BTN_ACHIEVEMENTS);

        font = new BitmapFont();
        font.getData().setScale(2.0f);
        layout = new GlyphLayout();

        updateLayout();
    }

    private void updateLayout() {
        float panelW = 900f;
        float panelH = 360f;

        float panelX = (WORLD_W - panelW) / 2f;
        float panelY = (WORLD_H - panelH) / 2f - 40f;

        float btnW = 360f;
        float btnH = btnW * ((float) btnGame.getHeight() / btnGame.getWidth());

        float colGap = 30f;
        float rowGap = 50f;

        float totalW = btnW * 2f + colGap;
        float startX = panelX + (panelW - totalW) / 2f;

        float leftX = startX;
        float rightX = startX + btnW + colGap;

        float totalH = btnH * 2f + rowGap;
        float startY = panelY + (panelH - totalH) / 2f;

        float centerOffsetY = -25f;

        float bottomY = startY + centerOffsetY;
        float topY = bottomY + btnH + rowGap;

        rGame.set(leftX, topY, btnW, btnH);
        rOptions.set(rightX, topY, btnW, btnH);
        rCredits.set(leftX, bottomY, btnW, btnH);
        rAchievements.set(rightX, bottomY, btnW, btnH);
    }

    private void updatePointer() {
        pointerWorld.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(pointerWorld);

        hoverGame = rGame.contains(pointerWorld);
        hoverOptions = rOptions.contains(pointerWorld);
        hoverCredits = rCredits.contains(pointerWorld);
        hoverAchievements = rAchievements.contains(pointerWorld);
    }

    private void drawButton(Texture tex, Rectangle r, boolean hover, String text) {
        float scale = hover ? HOVER_SCALE : 1f;

        float w = r.width * scale;
        float h = r.height * scale;

        float x = r.x + (r.width - w) / 2f;
        float y = r.y + (r.height - h) / 2f;

        game.batch.draw(tex, x, y, w, h);

        float baseFontScale = 2.0f;
        font.getData().setScale(baseFontScale * scale);

        layout.setText(font, text);

        float iconOffset = w * 0.12f;
        float textX = x + (w - layout.width) / 2f + iconOffset;
        float textY = y + (h + layout.height) / 2f;

        font.draw(game.batch, layout, textX, textY);

        font.getData().setScale(baseFontScale);
    }

    @Override
    public void show() {
        // ✅ misma música que intro (no reinicia)
        game.audio.playMusic(Assets.MUS_INTRO_THEME, true);
    }

    @Override
    public void render(float delta) {
        updatePointer();

        if (Gdx.input.justTouched()) {
            if (hoverGame) {
                game.setScreen(new DesertScreen(game));
                return;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            Gdx.app.exit();
            return;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

        game.batch.draw(bg, 0, 0, WORLD_W, WORLD_H);

        drawButton(btnGame, rGame, hoverGame, "JUGAR");
        drawButton(btnOptions, rOptions, hoverOptions, "OPCIONES");
        drawButton(btnCredits, rCredits, hoverCredits, "CREDITOS");
        drawButton(btnAchievements, rAchievements, hoverAchievements, "LOGROS");

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
        // ❌ NO disposes bg/botones (AssetManager)
        font.dispose();
    }
}
