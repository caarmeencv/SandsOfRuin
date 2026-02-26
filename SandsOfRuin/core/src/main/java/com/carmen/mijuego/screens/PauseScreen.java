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
import com.carmen.mijuego.ui.Fonts;

public class PauseScreen implements Screen {

    public enum Context {
        DESERT,
        PYRAMID
    }

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

    private static final float HOVER_SCALE = 1.08f;

    private BitmapFont font;
    private GlyphLayout layout;

    public PauseScreen(Main game, Screen returnScreen, Context context) {
        this.game = game;
        this.returnScreen = returnScreen;
        this.context = context;

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0f);
        camera.update();

        bg = (context == Context.DESERT)
            ? game.assets.get(Assets.SCREEN_PAUSE_BG_DESERT)
            : game.assets.get(Assets.SCREEN_PAUSE_BG_PYRAMID);

        btnContinue = game.assets.get(Assets.SCREEN_PAUSE_BTN_CONTINUE);
        btnReset = game.assets.get(Assets.SCREEN_PAUSE_BTN_RESET);
        btnMenu = game.assets.get(Assets.SCREEN_PAUSE_BTN_MENU);

        // ✅ Fuente del juego
        font = Fonts.main(game);
        font.getData().setScale(2.2f);
        layout = new GlyphLayout();

        updateLayout();
    }

    private void updateLayout() {
        float btnW = 520f;
        float btnH = btnW * ((float) btnContinue.getHeight() / (float) btnContinue.getWidth());

        float centerX = (WORLD_W - btnW) / 2f;
        float startY = WORLD_H / 2f + btnH + 40f;

        float gap = 30f;

        rContinue.set(centerX, startY, btnW, btnH);
        rReset.set(centerX, startY - (btnH + gap), btnW, btnH);
        rMenu.set(centerX, startY - 2f * (btnH + gap), btnW, btnH);
    }

    private void updatePointer() {
        pointerWorld.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(pointerWorld);

        hoverContinue = rContinue.contains(pointerWorld.x, pointerWorld.y);
        hoverReset = rReset.contains(pointerWorld.x, pointerWorld.y);
        hoverMenu = rMenu.contains(pointerWorld.x, pointerWorld.y);
    }

    private void drawButton(Texture tex, Rectangle r, boolean hover, String text) {

        float scale = hover ? HOVER_SCALE : 1f;

        float w = r.width * scale;
        float h = r.height * scale;

        float x = r.x + (r.width - w) / 2f;
        float y = r.y + (r.height - h) / 2f;

        game.batch.draw(tex, x, y, w, h);

        float baseFontScale = 2.2f;
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
        game.audio.playMusic(Assets.MUS_PAUSE_THEME, true);
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
                if (context == Context.DESERT) game.setScreen(new DesertScreen(game));
                else game.setScreen(new PyramidScreen(game));
                return;
            }
            if (hoverMenu) {
                game.setScreen(new MenuScreen(game));
                return;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            game.setScreen(returnScreen);
            return;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.draw(bg, 0, 0, WORLD_W, WORLD_H);

        drawButton(btnContinue, rContinue, hoverContinue, game.i18n.t("pause.continue"));
        drawButton(btnReset, rReset, hoverReset, game.i18n.t("pause.reset"));
        drawButton(btnMenu, rMenu, hoverMenu, game.i18n.t("pause.menu"));

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
        // ✅ NO dispose()
    }
}
