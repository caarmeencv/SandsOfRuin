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

public class CreditsScreen implements Screen {

    // Tamaño fijo del mundo para esta pantalla
    // Todo lo colocas pensando en 1280 por 720
    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    // Escalas de la fuente para el título y el texto largo
    private static final float SCALE_TITLE = 2.2f;
    private static final float SCALE_BODY = 1.35f;

    // Datos del botón back
    // BACK_MARGIN es la distancia a los bordes
    // BACK_W y BACK_H es el tamaño normal del botón
    // HOVER_SCALE es el tamaño cuando el dedo o el ratón pasa por encima
    private static final float BACK_MARGIN = 24f;
    private static final float BACK_W = 150f;
    private static final float BACK_H = 150f;
    private static final float HOVER_SCALE = 1.08f;

    // Referencia al juego principal, para cambiar pantallas, leer assets, settings, i18n, audio, etc
    private final Main game;

    // Cámara y viewport para dibujar en coordenadas del mundo
    private OrthographicCamera camera;
    private Viewport viewport;

    // Fondo y textura del botón back
    private Texture bg;
    private Texture btnBack;

    // Fuente y layout para medir texto
    // layout lo usas para saber cuánto ocupa el texto y para dibujar multilínea centrado
    private BitmapFont font;
    private GlyphLayout layout;

    // Rectángulo que marca la zona clicable del botón back
    private Rectangle backBounds;

    // Dos vectores para convertir la posición de pantalla a posición de mundo
    // pointerWorld lo usas para el hover
    // touch lo usas para el toque normal
    private Vector2 pointerWorld;
    private Vector2 touch;

    // Esto dice si el puntero está encima del botón back
    // si está encima, lo dibujas un poco más grande
    private boolean hoverBack;

    public CreditsScreen(Main game) {
        this.game = game;

        // Creo cámara y viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        // Centro la cámara
        camera.position.set(WORLD_W * 0.5f, WORLD_H * 0.5f, 0f);
        camera.update();

        // Pido texturas al AssetManager
        bg = game.assets.get(Assets.SCREEN_CREDITS_BG);
        btnBack = game.assets.get(Assets.BUTTON_BACK);

        // Fuente principal del juego
        // Importante, aquí depende de cómo tengas tu clase Fonts
        // En tu proyecto tú la usas como Fonts.main()
        font = Fonts.main();
        layout = new GlyphLayout();

        // Creo objetos auxiliares
        backBounds = new Rectangle();
        pointerWorld = new Vector2();
        touch = new Vector2();
        hoverBack = false;

        // Pongo el rectángulo del botón en su sitio
        updateBackBounds();
    }

    private void updateBackBounds() {
        // El botón está en la esquina inferior izquierda con un margen
        backBounds.set(BACK_MARGIN, BACK_MARGIN, BACK_W, BACK_H);
    }

    private void goBack() {
        // Si los efectos están activados, suena el click
        if (game.settings.isSfxEnabled()) {
            game.audio.playSfx(Assets.SFX_BUTTON_CLICKED);
        }

        // Cambio a la pantalla del menú
        game.setScreen(new MenuScreen(game));
    }

    private void updatePointer() {
        // Cojo coordenadas de pantalla del ratón o dedo
        pointerWorld.set(Gdx.input.getX(), Gdx.input.getY());

        // Las convierto a coordenadas del mundo usando el viewport
        viewport.unproject(pointerWorld);

        // Compruebo si el puntero está dentro del rectángulo del botón
        hoverBack = backBounds.contains(pointerWorld.x, pointerWorld.y);
    }

    private void drawBackButton() {
        // Decido el tamaño final según si hay hover o no
        float scale;
        if (hoverBack) scale = HOVER_SCALE;
        else scale = 1f;

        // Calculo ancho y alto aplicando escala
        float w = backBounds.width * scale;
        float h = backBounds.height * scale;

        // Esto centra el botón escalado dentro del rectángulo original
        float x = backBounds.x + (backBounds.width - w) * 0.5f;
        float y = backBounds.y + (backBounds.height - h) * 0.5f;

        // Dibujo el botón
        game.batch.draw(btnBack, x, y, w, h);
    }

    @Override
    public void show() {
        // Cada vez que entras a créditos, pones su música en loop
        game.audio.playMusic(Assets.MUS_CREDITS_THEME, true);
    }

    @Override
    public void render(float delta) {

        // Actualizo si el puntero está encima del botón
        updatePointer();

        // Si pulsas escape en PC o back en Android, vuelves al menú
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            goBack();
            return;
        }

        // Si hay un toque en pantalla, miro si fue dentro del botón back
        if (Gdx.input.justTouched()) {
            touch.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touch);

            if (backBounds.contains(touch.x, touch.y)) {
                goBack();
                return;
            }
        }

        // Limpio la pantalla
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Aplico el viewport y la cámara para dibujar en coordenadas del mundo
        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);

        // Cojo los textos por i18n
        // credits.title es el título (CRÉDITOS o CREDITS)
        // credits.text es el bloque grande con nombres y lo que quieras poner
        String title = game.i18n.t("credits.title");
        String creditsText = game.i18n.t("credits.text");

        // Ancho máximo para el texto largo, para que no ocupe toda la pantalla
        float textWidth = WORLD_W * 0.82f;

        // X desde donde empieza ese bloque, para que quede centrado
        float leftX = (WORLD_W - textWidth) * 0.5f;

        // Separaciones verticales
        float topMargin = 75f;
        float gapTitleBody = 60f;

        game.batch.begin();

        // Fondo a pantalla completa
        game.batch.draw(bg, 0f, 0f, WORLD_W, WORLD_H);

        // Pongo la fuente en blanco
        font.setColor(Color.WHITE);

        // Dibujo el título
        font.getData().setScale(SCALE_TITLE);
        layout.setText(font, title);

        // Y del título, cerca de arriba
        float titleY = WORLD_H - topMargin;

        // Dibujo centrado en toda la pantalla
        font.draw(game.batch, title, 0f, titleY, WORLD_W, Align.center, false);

        // Altura real del título para calcular dónde empieza el texto grande
        float titleH = layout.height;

        // Dibujo el texto largo
        // Aquí le dices al layout que mida con salto de línea automático dentro de textWidth
        font.getData().setScale(SCALE_BODY);
        layout.setText(font, creditsText, Color.WHITE, textWidth, Align.center, true);

        // La parte de arriba del texto largo empieza debajo del título
        float bodyTopY = titleY - titleH - gapTitleBody;

        // Dibujo el texto largo centrado y con word wrap activado
        font.draw(game.batch, creditsText, leftX, bodyTopY, textWidth, Align.center, true);

        // Dibujo el botón back al final para que quede por encima
        drawBackButton();

        // Reseteo color y escala para no romper otras pantallas
        Fonts.resetColor(font);
        font.getData().setScale(1f);

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Cuando cambia el tamaño de pantalla, actualizo el viewport
        viewport.update(width, height, true);

        // Recoloco el botón por si cambia el viewport
        updateBackBounds();
    }

    @Override
    public void pause() {
        // No hago nada aquí porque la pausa la controlas desde fuera con PauseScreen
    }

    @Override
    public void resume() {
        // Igual, no hago nada aquí
    }

    @Override
    public void hide() {
        // Cuando se oculta esta pantalla, tampoco haces nada especial
    }

    @Override
    public void dispose() {
        // No haces dispose porque las texturas y la fuente vienen del AssetManager del juego
        // Si aquí hicieras dispose podrías romper otras pantallas que usan lo mismo
    }
}
