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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.carmen.mijuego.Main;
import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.ui.Fonts;

public class OptionsScreen implements Screen {

    // Tamaño de mundo fijo para dibujar la pantalla de opciones
    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    // Tamaño visual de cada interruptor
    private static final float SWITCH_W = 160f;
    private static final float SWITCH_H = 80f;

    // Margen superior del título
    private static final float TITLE_TOP_MARGIN = 25f;

    // Ajuste del bloque de opciones para moverlo a izquierda y arriba/abajo
    private static final float BLOCK_SHIFT_X = 210f;
    private static final float BLOCK_SHIFT_Y = -10f;

    // Ajustes del botón de volver
    private static final float BACK_MARGIN = 24f;
    private static final float BACK_W = 150f;
    private static final float BACK_H = 150f;
    private static final float HOVER_SCALE = 1.08f;

    // Referencia al juego principal
    private final Main game;

    // Cámara y viewport para coordenadas del mundo fijo
    private OrthographicCamera camera;
    private Viewport viewport;

    // Fondos y texturas de los switches
    private Texture bg;
    private Texture switchOn;
    private Texture switchOff;
    private Texture switchEN;
    private Texture switchES;
    private Texture btnBack;

    // Fuente y helper para medir texto
    private BitmapFont font;
    private GlyphLayout layout;

    // Estados actuales de cada opción
    private boolean isSpanish;
    private boolean accelJumpOn;
    private boolean vibrationOn;
    private boolean musicOn;
    private boolean sfxOn;

    // Rectángulos clicables de cada switch
    private Rectangle langBounds;
    private Rectangle accelJumpBounds;
    private Rectangle vibrationBounds;
    private Rectangle musicBounds;
    private Rectangle sfxBounds;

    // Rectángulo del botón back y estado hover
    private Rectangle backBounds;
    private boolean hoverBack;

    // Vectores para input
    private Vector2 touch;
    private Vector2 pointerWorld;

    // Posiciones calculadas para dibujar texto y switches
    private float switchX;
    private float textX;
    private float startY;
    private float spacing;

    public OptionsScreen(Main game) {
        this.game = game;

        // Aquí pones todo a null o a valores iniciales
        // En show() se crean cámara, viewport, se piden assets y se calculan bounds
        camera = null;
        viewport = null;

        bg = null;
        switchOn = null;
        switchOff = null;
        switchEN = null;
        switchES = null;
        btnBack = null;

        font = null;
        layout = null;

        isSpanish = false;
        accelJumpOn = false;
        vibrationOn = false;
        musicOn = false;
        sfxOn = false;

        langBounds = null;
        accelJumpBounds = null;
        vibrationBounds = null;
        musicBounds = null;
        sfxBounds = null;

        backBounds = new Rectangle();
        hoverBack = false;

        touch = new Vector2();
        pointerWorld = new Vector2();

        switchX = 0f;
        textX = 0f;
        startY = 0f;
        spacing = 0f;
    }

    // Recalcula el rectángulo del botón de volver
    private void updateBackBounds() {
        backBounds.set(BACK_MARGIN, BACK_MARGIN, BACK_W, BACK_H);
    }

    // Convierte coordenadas de pantalla a mundo y actualiza si el ratón está encima del back
    private void updatePointer() {
        pointerWorld.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(pointerWorld);
        hoverBack = backBounds.contains(pointerWorld.x, pointerWorld.y);
    }

    // Dibuja el botón back con un pequeño zoom si hay hover
    private void drawBackButton() {
        float scale;
        if (hoverBack) scale = HOVER_SCALE;
        else scale = 1f;

        float w = backBounds.width * scale;
        float h = backBounds.height * scale;

        float x = backBounds.x + (backBounds.width - w) * 0.5f;
        float y = backBounds.y + (backBounds.height - h) * 0.5f;

        game.batch.draw(btnBack, x, y, w, h);
    }

    // Sonido de click solo si SFX está activo
    private void clickIfAllowed() {
        if (game.settings.isSfxEnabled()) {
            game.audio.playSfx(Assets.SFX_BUTTON_CLICKED);
        }
    }

    // Dibuja el texto de una fila alineado verticalmente con el switch
    private void drawRow(String text, Rectangle bounds) {
        layout.setText(font, text);

        float textY = bounds.y + (SWITCH_H * 0.5f) + (layout.height * 0.5f);
        font.draw(game.batch, layout, textX, textY);
    }

    // Gestiona los toques y actualiza estados según qué rectángulo se tocó
    private void handleInput() {
        if (!Gdx.input.justTouched()) return;

        touch.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(touch);

        // Si tocas back vuelves al menú
        if (backBounds.contains(touch.x, touch.y)) {
            clickIfAllowed();
            game.setScreen(new MenuScreen(game));
            return;
        }

        // Idioma
        if (langBounds.contains(touch.x, touch.y)) {
            isSpanish = !isSpanish;
            applyAndSave();
            return;
        }

        // Salto por acelerómetro
        if (accelJumpBounds.contains(touch.x, touch.y)) {
            accelJumpOn = !accelJumpOn;
            applyAndSave();
            return;
        }

        // Vibración
        if (vibrationBounds.contains(touch.x, touch.y)) {
            vibrationOn = !vibrationOn;
            applyAndSave();
            return;
        }

        // Música
        if (musicBounds.contains(touch.x, touch.y)) {
            musicOn = !musicOn;
            applyAndSave();
            return;
        }

        // Efectos
        if (sfxBounds.contains(touch.x, touch.y)) {
            sfxOn = !sfxOn;
            applyAndSave();
        }
    }

    // Aplica al GameSettings y guarda en preferencias
    private void applyAndSave() {
        // Guardamos si antes estaban activados los SFX para decidir si suena el click
        boolean sfxWasOn = game.settings.isSfxEnabled();
        if (sfxWasOn) game.audio.playSfx(Assets.SFX_BUTTON_CLICKED);

        game.settings.setLangSpanish(isSpanish);
        game.settings.setAccelJumpEnabled(accelJumpOn);
        game.settings.setVibrationEnabled(vibrationOn);
        game.settings.setMusicEnabled(musicOn);
        game.settings.setSfxEnabled(sfxOn);
        game.settings.save();

        // Si la música queda desactivada, la paro inmediatamente
        if (!musicOn) game.audio.stopMusic();
    }

    @Override
    public void show() {
        // Se llama al entrar en la pantalla
        // Aquí es donde creas cámara y viewport, y coges assets
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        camera.position.set(WORLD_W * 0.5f, WORLD_H * 0.5f, 0f);
        camera.update();

        bg = game.assets.get(Assets.SCREEN_OPTIONS_BG);
        switchOn = game.assets.get(Assets.SWITCH_ON);
        switchOff = game.assets.get(Assets.SWITCH_OFF);
        switchEN = game.assets.get(Assets.SWITCH_EN);
        switchES = game.assets.get(Assets.SWITCH_ES);
        btnBack = game.assets.get(Assets.BUTTON_BACK);

        // Importante: si tu Fonts es Fonts.main(Main game), aquí debe ir game
        font = Fonts.main();
        layout = new GlyphLayout();

        updateBackBounds();

        // Leo el estado actual desde settings
        isSpanish = game.settings.isLangSpanish();
        accelJumpOn = game.settings.isAccelJumpEnabled();
        vibrationOn = game.settings.isVibrationEnabled();
        musicOn = game.settings.isMusicEnabled();
        sfxOn = game.settings.isSfxEnabled();

        // Separación vertical entre filas
        spacing = 85f;

        // Centro de pantalla, y desplazamiento del bloque para colocarlo
        float centerX = WORLD_W * 0.5f;
        float offsetLeft = BLOCK_SHIFT_X;

        // Posición X de switches y textos
        switchX = centerX - 180f - offsetLeft;
        textX = centerX - 10f - offsetLeft;

        // Y inicial de la primera fila
        startY = (WORLD_H - 230f) + BLOCK_SHIFT_Y;

        // Creo los rectángulos clicables de cada switch
        langBounds = new Rectangle(switchX, startY, SWITCH_W, SWITCH_H);
        accelJumpBounds = new Rectangle(switchX, startY - spacing, SWITCH_W, SWITCH_H);
        vibrationBounds = new Rectangle(switchX, startY - spacing * 2f, SWITCH_W, SWITCH_H);
        musicBounds = new Rectangle(switchX, startY - spacing * 3f, SWITCH_W, SWITCH_H);
        sfxBounds = new Rectangle(switchX, startY - spacing * 4f, SWITCH_W, SWITCH_H);
    }

    @Override
    public void render(float delta) {
        // Actualizo hover del botón back
        updatePointer();

        // Back físico o escape vuelve al menú
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            clickIfAllowed();
            game.setScreen(new MenuScreen(game));
            return;
        }

        // Limpio pantalla
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Aplico viewport y cámara
        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

        // Fondo
        game.batch.draw(bg, 0f, 0f, WORLD_W, WORLD_H);

        // Título en blanco grande
        font.setColor(Color.WHITE);
        font.getData().setScale(3.0f);

        layout.setText(font, game.i18n.t("options.title"));
        float titleY = WORLD_H - TITLE_TOP_MARGIN;
        font.draw(game.batch, layout, (WORLD_W - layout.width) * 0.5f, titleY);

        // Textos de opciones en negro
        font.setColor(Color.BLACK);
        font.getData().setScale(2.3f);

        drawRow(game.i18n.t("options.lang"), langBounds);
        drawRow(game.i18n.t("options.accelJump"), accelJumpBounds);
        drawRow(game.i18n.t("options.vibration"), vibrationBounds);
        drawRow(game.i18n.t("options.music"), musicBounds);
        drawRow(game.i18n.t("options.sfx"), sfxBounds);

        // Dibujo switches según estado
        if (isSpanish) game.batch.draw(switchES, langBounds.x, langBounds.y, SWITCH_W, SWITCH_H);
        else game.batch.draw(switchEN, langBounds.x, langBounds.y, SWITCH_W, SWITCH_H);

        if (accelJumpOn) game.batch.draw(switchOn, accelJumpBounds.x, accelJumpBounds.y, SWITCH_W, SWITCH_H);
        else game.batch.draw(switchOff, accelJumpBounds.x, accelJumpBounds.y, SWITCH_W, SWITCH_H);

        if (vibrationOn) game.batch.draw(switchOn, vibrationBounds.x, vibrationBounds.y, SWITCH_W, SWITCH_H);
        else game.batch.draw(switchOff, vibrationBounds.x, vibrationBounds.y, SWITCH_W, SWITCH_H);

        if (musicOn) game.batch.draw(switchOn, musicBounds.x, musicBounds.y, SWITCH_W, SWITCH_H);
        else game.batch.draw(switchOff, musicBounds.x, musicBounds.y, SWITCH_W, SWITCH_H);

        if (sfxOn) game.batch.draw(switchOn, sfxBounds.x, sfxBounds.y, SWITCH_W, SWITCH_H);
        else game.batch.draw(switchOff, sfxBounds.x, sfxBounds.y, SWITCH_W, SWITCH_H);

        // Back
        drawBackButton();

        // Reseteo color y escala para no contaminar otras pantallas
        Fonts.resetColor(font);
        font.getData().setScale(1f);

        game.batch.end();

        // El input lo gestiono al final para no mezclarlo con el batch
        handleInput();
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
        // No hago dispose de fonts ni assets aquí, los gestiona el AssetManager
    }
}
