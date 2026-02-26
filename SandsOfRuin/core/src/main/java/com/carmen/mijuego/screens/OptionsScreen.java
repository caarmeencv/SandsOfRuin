package com.carmen.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
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

public class OptionsScreen implements Screen {

    private final Main game;

    private OrthographicCamera camera;
    private Viewport viewport;

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    private Texture bg;
    private Texture switchOn;
    private Texture switchOff;
    private Texture switchEN;
    private Texture switchES;

    private Sound clickSound;

    private BitmapFont font;
    private GlyphLayout layout;
    private GlyphLayout backLayout;

    private boolean isSpanish;
    private boolean accelJumpOn;
    private boolean vibrationOn;
    private boolean musicOn;
    private boolean sfxOn;

    private Rectangle langBounds;
    private Rectangle accelJumpBounds;
    private Rectangle vibrationBounds;
    private Rectangle musicBounds;
    private Rectangle sfxBounds;

    private Rectangle backBounds;

    private static final float SWITCH_W = 160f;
    private static final float SWITCH_H = 80f;

    private final Vector2 touch = new Vector2();

    private float switchX;
    private float textX;
    private float startY;
    private float spacing;

    public OptionsScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0);
        camera.update();

        bg = game.assets.get(Assets.SCREEN_OPTIONS_BG);
        switchOn = game.assets.get(Assets.SWITCH_ON);
        switchOff = game.assets.get(Assets.SWITCH_OFF);
        switchEN = game.assets.get(Assets.SWITCH_EN);
        switchES = game.assets.get(Assets.SWITCH_ES);

        clickSound = game.assets.get(Assets.SFX_BUTTON_CLICKED);

        // ✅ Fuente global con helper
        font = Fonts.main(game);

        layout = new GlyphLayout();
        backLayout = new GlyphLayout();

        // Cargar estados desde settings
        isSpanish = game.settings.isLangSpanish();
        accelJumpOn = game.settings.isAccelJumpEnabled();
        vibrationOn = game.settings.isVibrationEnabled();
        musicOn = game.settings.isMusicEnabled();
        sfxOn = game.settings.isSfxEnabled();

        spacing = 85f;

        float centerX = WORLD_W / 2f;
        float offsetLeft = 80f;

        switchX = centerX - 180f - offsetLeft;
        textX = centerX - 10f - offsetLeft;

        startY = WORLD_H - 230f;

        // Filas (sin "gyro")
        langBounds = new Rectangle(switchX, startY, SWITCH_W, SWITCH_H);
        accelJumpBounds = new Rectangle(switchX, startY - spacing, SWITCH_W, SWITCH_H);
        vibrationBounds = new Rectangle(switchX, startY - spacing * 2f, SWITCH_W, SWITCH_H);
        musicBounds = new Rectangle(switchX, startY - spacing * 3f, SWITCH_W, SWITCH_H);
        sfxBounds = new Rectangle(switchX, startY - spacing * 4f, SWITCH_W, SWITCH_H);

        backBounds = new Rectangle();
        updateBackBounds();
    }

    private void updateBackBounds() {
        font.getData().setScale(1.7f);

        String txt = game.i18n.t("options.back");
        backLayout.setText(font, txt);

        float x = 60f;
        float y = 60f;

        backBounds.set(x, y - backLayout.height, backLayout.width, backLayout.height);

        // higiene
        font.getData().setScale(1f);
    }

    @Override
    public void render(float delta) {

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            game.setScreen(new MenuScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

        game.batch.draw(bg, 0, 0, WORLD_W, WORLD_H);

        // =========================
        // TÍTULO
        // =========================
        font.setColor(Color.WHITE);
        font.getData().setScale(3.0f);

        layout.setText(font, game.i18n.t("options.title"));
        font.draw(game.batch, layout, (WORLD_W - layout.width) / 2f, WORLD_H - 60f);

        // =========================
        // FILAS
        // =========================
        font.setColor(Color.BLACK);
        font.getData().setScale(2.3f);

        drawRow(game.i18n.t("options.lang"), langBounds);
        drawRow(game.i18n.t("options.accelJump"), accelJumpBounds);
        drawRow(game.i18n.t("options.vibration"), vibrationBounds);
        drawRow(game.i18n.t("options.music"), musicBounds);
        drawRow(game.i18n.t("options.sfx"), sfxBounds);

        // =========================
        // SWITCHES
        // =========================
        game.batch.draw(isSpanish ? switchES : switchEN, langBounds.x, langBounds.y, SWITCH_W, SWITCH_H);

        game.batch.draw(accelJumpOn ? switchOn : switchOff, accelJumpBounds.x, accelJumpBounds.y, SWITCH_W, SWITCH_H);
        game.batch.draw(vibrationOn ? switchOn : switchOff, vibrationBounds.x, vibrationBounds.y, SWITCH_W, SWITCH_H);
        game.batch.draw(musicOn ? switchOn : switchOff, musicBounds.x, musicBounds.y, SWITCH_W, SWITCH_H);
        game.batch.draw(sfxOn ? switchOn : switchOff, sfxBounds.x, sfxBounds.y, SWITCH_W, SWITCH_H);

        // =========================
        // VOLVER
        // =========================
        font.getData().setScale(1.7f);
        font.setColor(Color.BLACK);

        String backTxt = game.i18n.t("options.back");
        backLayout.setText(font, backTxt);
        font.draw(game.batch, backLayout, backBounds.x, backBounds.y + backBounds.height);

        // ✅ reset de color por higiene (si una pantalla cambia alpha, etc.)
        Fonts.resetColor(font);
        font.getData().setScale(1f);

        game.batch.end();

        handleInput();
    }

    private void drawRow(String text, Rectangle bounds) {
        layout.setText(font, text);
        float textY = bounds.y + (SWITCH_H / 2f) + (layout.height / 2f);
        font.draw(game.batch, layout, textX, textY);
    }

    private void handleInput() {

        if (!Gdx.input.justTouched()) return;

        touch.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(touch);

        if (backBounds.contains(touch.x, touch.y)) {
            if (sfxOn) clickSound.play();
            game.setScreen(new MenuScreen(game));
            return;
        }

        if (langBounds.contains(touch.x, touch.y)) {
            isSpanish = !isSpanish;
            applyAndSave();
            return;
        }

        if (accelJumpBounds.contains(touch.x, touch.y)) {
            accelJumpOn = !accelJumpOn;
            applyAndSave();
            return;
        }

        if (vibrationBounds.contains(touch.x, touch.y)) {
            vibrationOn = !vibrationOn;
            applyAndSave();
            return;
        }

        if (musicBounds.contains(touch.x, touch.y)) {
            musicOn = !musicOn;
            applyAndSave();
            return;
        }

        if (sfxBounds.contains(touch.x, touch.y)) {
            sfxOn = !sfxOn;
            applyAndSave();
        }
    }

    private void applyAndSave() {

        // Click solo si SFX está ON
        if (sfxOn) clickSound.play();

        // Guardar en settings
        game.settings.setLangSpanish(isSpanish);
        game.settings.setAccelJumpEnabled(accelJumpOn);
        game.settings.setVibrationEnabled(vibrationOn);
        game.settings.setMusicEnabled(musicOn);
        game.settings.setSfxEnabled(sfxOn);
        game.settings.save();

        // Aplicar efectos inmediatos
        if (!musicOn) {
            game.audio.stopMusic();
        }

        updateBackBounds();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        updateBackBounds();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        // ✅ NO dispose(): la fuente la gestiona AssetManager
    }
}
