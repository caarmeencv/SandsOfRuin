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

    // Contexto para saber qué fondo de pausa usar
    public enum Context {
        DESERT,
        PYRAMID
    }

    // Resolución de mundo fija para posicionar UI siempre igual
    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    // Cuánto crece el botón al pasar el ratón encima
    private static final float HOVER_SCALE = 1.08f;

    // Referencia al juego principal
    private final Main game;

    // Pantalla a la que vuelves al pulsar continuar
    private final Screen returnScreen;

    // Indica si vienes del desierto o de la pirámide
    private final Context context;

    // Cámara y viewport para dibujar en coordenadas del mundo
    private OrthographicCamera camera;
    private Viewport viewport;

    // Texturas de fondo y botones
    private Texture bg;
    private Texture btnContinue;
    private Texture btnReset;
    private Texture btnMenu;

    // Áreas clicables de cada botón
    private Rectangle rContinue;
    private Rectangle rReset;
    private Rectangle rMenu;

    // Vectores para convertir input de pantalla a mundo
    private Vector2 pointerWorld;
    private Vector2 touch;

    // Estados de hover para cada botón
    private boolean hoverContinue;
    private boolean hoverReset;
    private boolean hoverMenu;

    // Fuente para escribir el texto sobre los botones
    private BitmapFont font;
    private GlyphLayout layout;

    public PauseScreen(Main game, Screen returnScreen, Context context) {
        this.game = game;
        this.returnScreen = returnScreen;
        this.context = context;

        // Cámara y viewport de UI fija
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        camera.position.set(WORLD_W * 0.5f, WORLD_H * 0.5f, 0f);
        camera.update();

        // Fondo distinto según el contexto
        if (context == Context.DESERT) bg = game.assets.get(Assets.SCREEN_PAUSE_BG_DESERT);
        else bg = game.assets.get(Assets.SCREEN_PAUSE_BG_PYRAMID);

        // Botones de pausa
        btnContinue = game.assets.get(Assets.SCREEN_PAUSE_BTN_CONTINUE);
        btnReset = game.assets.get(Assets.SCREEN_PAUSE_BTN_RESET);
        btnMenu = game.assets.get(Assets.SCREEN_PAUSE_BTN_MENU);

        // Rectángulos clicables
        rContinue = new Rectangle();
        rReset = new Rectangle();
        rMenu = new Rectangle();

        // Helpers de input
        pointerWorld = new Vector2();
        touch = new Vector2();

        // Hover inicial apagado
        hoverContinue = false;
        hoverReset = false;
        hoverMenu = false;

        // Fuente principal del juego
        font = Fonts.main();
        font.getData().setScale(2.2f);
        layout = new GlyphLayout();

        // Calcula posiciones de los botones
        updateLayout();
    }

    // Reproduce click solo si SFX está activado
    private void playClickIfAllowed() {
        if (game.settings != null && game.settings.isSfxEnabled()) {
            game.audio.playSfx(Assets.SFX_BUTTON_CLICKED);
        }
    }

    // Posiciona los botones en columna centrada
    private void updateLayout() {
        float btnW = 520f;

        // Mantiene la proporción del botón usando su textura
        float btnH = btnW * ((float) btnContinue.getHeight() / (float) btnContinue.getWidth());

        // X centrado
        float centerX = (WORLD_W - btnW) * 0.5f;

        // Empieza un poco por encima del centro y apila hacia abajo
        float startY = WORLD_H * 0.5f + btnH + 40f;

        float gap = 30f;

        rContinue.set(centerX, startY, btnW, btnH);
        rReset.set(centerX, startY - (btnH + gap), btnW, btnH);
        rMenu.set(centerX, startY - 2f * (btnH + gap), btnW, btnH);
    }

    // Convierte coordenadas del ratón a mundo y actualiza hover por botón
    private void updatePointer() {
        pointerWorld.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(pointerWorld);

        hoverContinue = rContinue.contains(pointerWorld.x, pointerWorld.y);
        hoverReset = rReset.contains(pointerWorld.x, pointerWorld.y);
        hoverMenu = rMenu.contains(pointerWorld.x, pointerWorld.y);
    }

    // Dibuja un botón con escala por hover y texto centrado con pequeño offset
    private void drawButton(Texture tex, Rectangle r, boolean hover, String text) {
        float scale;
        if (hover) scale = HOVER_SCALE;
        else scale = 1f;

        float w = r.width * scale;
        float h = r.height * scale;

        // Centrado del botón escalado dentro del rectángulo original
        float x = r.x + (r.width - w) * 0.5f;
        float y = r.y + (r.height - h) * 0.5f;

        game.batch.draw(tex, x, y, w, h);

        // Escala de fuente que acompaña al hover del botón
        float baseFontScale = 2.2f;
        font.getData().setScale(baseFontScale * scale);

        // Mide el texto para centrarlo correctamente
        layout.setText(font, text);

        // Offset opcional por si el botón tiene un icono pintado a la izquierda
        float iconOffset = w * 0.12f;

        float textX = x + (w - layout.width) * 0.5f + iconOffset;
        float textY = y + (h + layout.height) * 0.5f;

        font.draw(game.batch, layout, textX, textY);

        // Devuelve la escala a la base para no contaminar otros draws
        font.getData().setScale(baseFontScale);
    }

    @Override
    public void show() {
        // Música de pausa al entrar
        game.audio.playMusic(Assets.MUS_PAUSE_THEME, true);
    }

    @Override
    public void render(float delta) {
        // Actualiza hover
        updatePointer();

        // Entrada táctil
        if (Gdx.input.justTouched()) {
            touch.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touch);

            // Continuar vuelve a la pantalla anterior
            if (rContinue.contains(touch.x, touch.y)) {
                playClickIfAllowed();
                game.setScreen(returnScreen);
                return;
            }

            // Reset reinicia run y empieza desde el desierto
            if (rReset.contains(touch.x, touch.y)) {
                playClickIfAllowed();
                game.resetRun();
                game.setScreen(new DesertScreen(game));
                return;
            }

            // Menú principal
            if (rMenu.contains(touch.x, touch.y)) {
                playClickIfAllowed();
                game.setScreen(new MenuScreen(game));
                return;
            }
        }

        // Escape o Back actúa como continuar
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            playClickIfAllowed();
            game.setScreen(returnScreen);
            return;
        }

        // Limpia pantalla
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Aplica viewport y cámara
        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

        // Fondo
        game.batch.draw(bg, 0f, 0f, WORLD_W, WORLD_H);

        // Botones con textos traducidos
        drawButton(btnContinue, rContinue, hoverContinue, game.i18n.t("pause.continue"));
        drawButton(btnReset, rReset, hoverReset, game.i18n.t("pause.reset"));
        drawButton(btnMenu, rMenu, hoverMenu, game.i18n.t("pause.menu"));

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Ajusta viewport al cambiar tamaño de ventana
        viewport.update(width, height, true);

        // Recalcula el layout por si cambia la relación de aspecto
        updateLayout();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        // No se hace dispose de assets ni font si están gestionados por AssetManager
    }
}
