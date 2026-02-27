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

public class VictoryScreen implements Screen {

    // Tamaño fijo del mundo para dibujar UI en posiciones constantes
    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    // Separación desde el borde inferior del texto “volver al menú”
    // Si quieres el texto más abajo, baja este número
    private static final float HINT_BOTTOM_PAD = 20f;

    // Escala normal del texto
    private static final float HINT_SCALE = 1.55f;

    // Escala cuando el ratón está encima (hover)
    private static final float HOVER_SCALE = 1.62f;

    private final Main game;

    // Cámara y viewport para coordenadas de mundo
    private OrthographicCamera camera;
    private Viewport viewport;

    // Fondo de victoria
    private Texture background;

    // Fuente y layout para medir el texto
    private BitmapFont font;
    private GlyphLayout layout;

    // Rectángulo clicable del texto “volver al menú”
    private Rectangle hintBounds;

    // Input táctil y puntero
    private Vector2 touch;
    private Vector2 pointerWorld;

    // Estado hover del texto clicable
    private boolean hoverHint;

    public VictoryScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        camera.position.set(WORLD_W * 0.5f, WORLD_H * 0.5f, 0f);
        camera.update();

        // Carga fondo desde AssetManager
        background = game.assets.get(Assets.SCREEN_VICTORY_BG);

        // Fuente principal del juego (en tu proyecto recibe game)
        font = Fonts.main();
        layout = new GlyphLayout();

        hintBounds = new Rectangle();
        touch = new Vector2();
        pointerWorld = new Vector2();
        hoverHint = false;

        // Calcula el área clicable inicial
        updateHintBounds();
    }

    @Override
    public void show() {
        // Música al entrar en la pantalla
        game.audio.playMusic(Assets.MUS_VICTORY_THEME, true);

        // Recalcula bounds por si cambió idioma u otra cosa
        updateHintBounds();
    }

    // Reproduce click solo si los efectos están activos
    private void playClickIfAllowed() {
        if (game.settings != null && game.settings.isSfxEnabled()) {
            game.audio.playSfx(Assets.SFX_BUTTON_CLICKED);
        }
    }

    // Vuelve al menú
    private void goMenu() {
        playClickIfAllowed();
        game.setScreen(new MenuScreen(game));
    }

    // Actualiza el hover del texto comprobando la posición del puntero
    private void updatePointer() {
        pointerWorld.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(pointerWorld);
        hoverHint = hintBounds.contains(pointerWorld.x, pointerWorld.y);
    }

    // Calcula el rectángulo clicable del texto basándose en su tamaño real
    private void updateHintBounds() {
        String text = game.i18n.t("ui.back_to_menu_hint");

        // Pone escala normal para medir el texto
        font.getData().setScale(HINT_SCALE);

        // Mide ancho y alto reales
        layout.setText(font, text);

        float textW = layout.width;
        float textH = layout.height;

        // El baseline se coloca a una distancia fija del borde inferior
        float baselineY = HINT_BOTTOM_PAD + textH;

        // Centrado horizontal
        float x = (WORLD_W - textW) * 0.5f;

        // Bounds con el tamaño real del texto para que el toque sea exacto
        hintBounds.set(x, baselineY - textH, textW, textH);

        // Resetea la escala para no afectar otras mediciones
        font.getData().setScale(1f);
    }

    @Override
    public void render(float delta) {
        // Actualiza hover antes de dibujar
        updatePointer();

        // Tecla back o escape para volver al menú
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            goMenu();
            return;
        }

        // Toque en pantalla: si cae dentro del texto, vuelve al menú
        if (Gdx.input.justTouched()) {
            touch.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touch);

            if (hintBounds.contains(touch.x, touch.y)) {
                goMenu();
                return;
            }
        }

        // Limpieza de pantalla
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

        // Dibuja el fondo
        game.batch.draw(background, 0f, 0f, WORLD_W, WORLD_H);

        // Texto a dibujar
        String text = game.i18n.t("ui.back_to_menu_hint");

        // Escala según hover
        float scale = hoverHint ? HOVER_SCALE : HINT_SCALE;
        font.getData().setScale(scale);

        // Mide para colocar baseline abajo correctamente
        layout.setText(font, text);
        float baselineY = HINT_BOTTOM_PAD + layout.height;

        font.setColor(Color.WHITE);

        // Dibuja centrado usando Align.center
        font.draw(game.batch, text, 0f, baselineY, WORLD_W, Align.center, true);

        // Reset para no contaminar otras pantallas
        Fonts.resetColor(font);
        font.getData().setScale(1f);

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Ajusta viewport y recalcula bounds porque el unproject cambia
        viewport.update(width, height, true);
        updateHintBounds();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
