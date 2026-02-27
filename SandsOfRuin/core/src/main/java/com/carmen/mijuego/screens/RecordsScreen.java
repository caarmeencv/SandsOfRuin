package com.carmen.mijuego.screens;

import java.util.List;

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
import com.carmen.mijuego.settings.MummyTimeRecords;
import com.carmen.mijuego.ui.Fonts;

public class RecordsScreen implements Screen {

    // Tamaño fijo del mundo para colocar UI siempre igual
    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    // Posición vertical del título
    private static final float TITLE_Y = WORLD_H - 40f;

    // Panel donde se dibuja el listado
    private static final float PANEL_X = 240f;
    private static final float PANEL_Y = 190f;
    private static final float PANEL_W = 800f;
    private static final float PANEL_H = 360f;

    // Márgenes internos del panel
    private static final float PANEL_PADDING_X = 40f;
    private static final float PANEL_PADDING_TOP = 70f;

    // Número máximo de registros que se muestran
    private static final int MAX_SLOTS = 10;

    // Botón back en esquina inferior izquierda
    private static final float BACK_MARGIN = 24f;
    private static final float BACK_W = 150f;
    private static final float BACK_H = 150f;

    // Escala extra cuando el ratón está encima
    private static final float HOVER_SCALE = 1.08f;

    private final Main game;

    // Cámara y viewport para coordenadas de mundo
    private OrthographicCamera camera;
    private Viewport viewport;

    // Fondo y botón back
    private Texture bg;
    private Texture btnBack;

    // Fuente principal
    private BitmapFont font;

    // Layouts para medir textos (título, filas, y texto temporal)
    private GlyphLayout layoutTitle;
    private GlyphLayout rowLayout;
    private GlyphLayout tmp;

    // Área clicable del botón back
    private Rectangle backBounds;

    // Vectores de input para convertir pantalla a mundo
    private Vector2 pointerWorld;
    private Vector2 touch;

    // Estado hover del botón back
    private boolean hoverBack;

    // Pequeño bloqueo al entrar para evitar toques accidentales
    private float inputBlockTime;

    // Lista de tiempos guardados (en segundos)
    private List<Integer> times;

    public RecordsScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        camera.position.set(WORLD_W * 0.5f, WORLD_H * 0.5f, 0f);
        camera.update();

        // Texturas de esta pantalla
        bg = game.assets.get(Assets.SCREEN_ACHIEVEMENTS_BG);
        btnBack = game.assets.get(Assets.BUTTON_BACK);

        // Fuente del juego
        // En tu proyecto Fonts.main necesita game
        font = Fonts.main();

        layoutTitle = new GlyphLayout();
        rowLayout = new GlyphLayout();
        tmp = new GlyphLayout();

        backBounds = new Rectangle();
        pointerWorld = new Vector2();
        touch = new Vector2();

        hoverBack = false;
        inputBlockTime = 0f;

        updateBackBounds();
    }

    // Coloca el área clicable del botón back
    private void updateBackBounds() {
        backBounds.set(BACK_MARGIN, BACK_MARGIN, BACK_W, BACK_H);
    }

    // Actualiza hover calculando dónde está el cursor en coordenadas del mundo
    private void updatePointer() {
        pointerWorld.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(pointerWorld);
        hoverBack = backBounds.contains(pointerWorld.x, pointerWorld.y);
    }

    // Dibuja el botón back con un pequeño zoom si está en hover
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

    // Vuelve al menú con sonido si SFX está activo
    private void goBack() {
        if (game.settings.isSfxEnabled()) {
            game.audio.playSfx(Assets.SFX_BUTTON_CLICKED);
        }
        game.setScreen(new MenuScreen(game));
    }

    @Override
    public void show() {
        // Música al entrar a records
        game.audio.playMusic(Assets.MUS_ACHIEVEMENTS_THEME, true);

        // Bloqueo corto para que no se pulse back por accidente al entrar
        inputBlockTime = 0.20f;

        // Lee la lista de records guardada
        times = MummyTimeRecords.getTimesSeconds();
    }

    @Override
    public void render(float delta) {
        // Actualiza hover
        updatePointer();

        // Cuenta atrás del bloqueo de input
        if (inputBlockTime > 0f) {
            inputBlockTime -= delta;
            if (inputBlockTime < 0f) inputBlockTime = 0f;
        }

        // Tecla back o escape
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            goBack();
            return;
        }

        // Click táctil solo cuando ya pasó el bloqueo
        if (inputBlockTime == 0f && Gdx.input.justTouched()) {
            touch.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touch);

            if (backBounds.contains(touch.x, touch.y)) {
                goBack();
                return;
            }
        }

        // Limpia pantalla
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);

        String title = game.i18n.t("records.title");

        game.batch.begin();

        // Fondo
        game.batch.draw(bg, 0f, 0f, WORLD_W, WORLD_H);

        // Dibuja título centrado arriba
        font.getData().setScale(3.6f);
        font.setColor(Color.WHITE);

        layoutTitle.setText(font, title);
        font.draw(game.batch, layoutTitle, (WORLD_W - layoutTitle.width) * 0.5f, TITLE_Y);

        // Si no hay tiempos guardados, muestra texto centrado dentro del panel
        if (times == null || times.size() == 0) {
            String empty = game.i18n.t("records.empty");

            font.getData().setScale(2.2f);
            font.setColor(Color.BLACK);

            tmp.setText(font, empty, Color.BLACK, PANEL_W, Align.center, true);

            float textX = PANEL_X;
            float textY = PANEL_Y + (PANEL_H * 0.5f) + (tmp.height * 0.5f);
            font.draw(game.batch, tmp, textX, textY);

        } else {
            // Texto “RECORD” o “RÉCORD” según idioma
            String recordWord = game.i18n.t(" ");

            font.getData().setScale(2.0f);
            font.setColor(Color.WHITE);

            // Área interna del panel con padding lateral
            float innerX = PANEL_X + PANEL_PADDING_X;
            float innerW = PANEL_W - PANEL_PADDING_X * 2f;

            // Dos columnas del mismo ancho
            float colW = innerW * 0.5f;
            float leftColX = innerX;
            float rightColX = innerX + colW;

            // Primera fila desde arriba del panel
            float startY = PANEL_Y + PANEL_H - PANEL_PADDING_TOP;

            // Separación vertical entre filas
            float rowGap = 52f;

            // 10 slots: 5 en columna izquierda, 5 en derecha
            for (int slot = 0; slot < MAX_SLOTS; slot++) {
                boolean hasTime = slot < times.size();
                String line;

                // Si hay tiempo, lo formatea en mm:ss; si no, muestra ---
                if (hasTime) {
                    int secs = times.get(slot);
                    line = (slot + 1) + ". " + recordWord + " " + MummyTimeRecords.formatMMSS(secs);
                } else {
                    line = (slot + 1) + ". ---";
                }

                // slot 0..4 izquierda, slot 5..9 derecha
                int row = slot % 5;
                boolean right = slot >= 5;

                float x;
                if (right) x = rightColX;
                else x = leftColX;

                float y = startY - row * rowGap;

                // Centra el texto dentro de la columna
                rowLayout.setText(font, line, Color.WHITE, colW, Align.center, false);
                font.draw(game.batch, rowLayout, x, y);
            }
        }

        // Botón back arriba de todo lo demás
        drawBackButton();

        // Resetea color y escala para no afectar otras pantallas
        Fonts.resetColor(font);
        font.getData().setScale(1f);

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        updateBackBounds();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
