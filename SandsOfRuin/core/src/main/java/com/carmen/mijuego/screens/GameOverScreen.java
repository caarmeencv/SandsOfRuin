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

public class GameOverScreen implements Screen {

    // Tamaño fijo del mundo para esta pantalla
    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    // Esto es el margen desde abajo para el texto de "volver al menú"
    // Si lo bajas, el texto se pega más abajo
    // Si lo subes, el texto se separa más del borde inferior
    private static final float HINT_BOTTOM_PAD = 20f;

    // Escala normal del texto
    private static final float HINT_SCALE = 1.55f;

    // Escala del texto cuando el dedo o ratón está encima
    private static final float HOVER_SCALE = 1.62f;

    // Referencia al juego principal para acceder a assets, audio, settings e i18n
    private final Main game;

    // Cámara y viewport para dibujar en coordenadas de mundo
    private OrthographicCamera camera;
    private Viewport viewport;

    // Fondo de Game Over
    private Texture background;

    // Fuente y layout para medir el texto
    private BitmapFont font;
    private GlyphLayout layout;

    // Rectángulo que marca dónde se puede pulsar el texto
    // Esto sirve para que el texto sea como un botón
    private Rectangle hintBounds;

    // Vectores para pasar de coordenadas de pantalla a coordenadas del mundo
    private Vector2 pointerWorld;
    private Vector2 touch;

    // Dice si el puntero está encima del texto
    // Cuando está encima, se agranda un poco y cambia el color
    private boolean hoverHint;

    public GameOverScreen(Main game) {
        this.game = game;

        // Creo la cámara y el viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        // Centro la cámara
        camera.position.set(WORLD_W * 0.5f, WORLD_H * 0.5f, 0f);
        camera.update();

        // Cargo el fondo desde el AssetManager
        background = game.assets.get(Assets.SCREEN_GAMEOVER_BG);

        // Pido la fuente principal del juego
        // Ojo, esto depende de tu clase Fonts
        font = Fonts.main();
        layout = new GlyphLayout();

        // Creo las cosas para hover y toque
        hintBounds = new Rectangle();
        pointerWorld = new Vector2();
        touch = new Vector2();
        hoverHint = false;

        // Coloco el rectángulo del texto según el texto actual del idioma
        updateHintBounds();
    }

    private void playClickIfAllowed() {
        // Si existe settings y los efectos están activados, suena el click
        if (game.settings != null && game.settings.isSfxEnabled()) {
            game.audio.playSfx(Assets.SFX_BUTTON_CLICKED);
        }
    }

    private void goMenu() {
        // Sonido y vuelta al menú principal
        playClickIfAllowed();
        game.setScreen(new MenuScreen(game));
    }

    private void updatePointer() {
        // Cojo posición del puntero en pantalla
        pointerWorld.set(Gdx.input.getX(), Gdx.input.getY());

        // La paso a coordenadas del mundo
        viewport.unproject(pointerWorld);

        // Miro si el puntero está dentro del rectángulo del texto
        hoverHint = hintBounds.contains(pointerWorld.x, pointerWorld.y);
    }

    private void updateHintBounds() {
        // Aquí calculas el rectángulo clickable del texto
        // Como el texto puede cambiar por idioma, lo calculas cada vez
        String text = game.i18n.t("ui.back_to_menu_hint");

        // Pongo la escala normal para medir con el tamaño correcto
        font.getData().setScale(HINT_SCALE);

        // Mido el texto
        // Esto solo mide, no dibuja
        layout.setText(font, text);

        float textW = layout.width;
        float textH = layout.height;

        // baselineY es la altura donde se dibuja el texto
        // En libGDX cuando dibujas una fuente, le das la línea base, no la esquina inferior
        float baselineY = HINT_BOTTOM_PAD + textH;

        // Centro el texto en X
        float x = (WORLD_W - textW) * 0.5f;

        // Creo el rectángulo clickable justo donde está el texto
        // La y del rectángulo es baselineY - textH porque el texto se dibuja desde la base
        hintBounds.set(x, baselineY - textH, textW, textH);

        // Devuelvo la escala a 1 para no dejar la fuente tocada para otras cosas
        font.getData().setScale(1f);
    }

    @Override
    public void show() {
        // Al entrar, pongo la música de game over y recalculo el rectángulo por si cambió idioma
        game.audio.playMusic(Assets.MUS_GAME_OVER_THEME, true);
        updateHintBounds();
    }

    @Override
    public void render(float delta) {

        // Actualizo hover del texto
        updatePointer();

        // Si le das a escape o back, vuelves al menú
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            goMenu();
            return;
        }

        // Si tocas la pantalla, miro si tocaste encima del texto
        if (Gdx.input.justTouched()) {
            touch.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touch);

            if (hintBounds.contains(touch.x, touch.y)) {
                goMenu();
                return;
            }
        }

        // Limpio pantalla
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Aplico viewport y cámara
        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

        // Dibujo fondo
        game.batch.draw(background, 0f, 0f, WORLD_W, WORLD_H);

        // Texto que aparece abajo como pista para volver al menú
        String text = game.i18n.t("ui.back_to_menu_hint");

        // Si hay hover lo hago un pelín más grande
        float scale;
        if (hoverHint) scale = HOVER_SCALE;
        else scale = HINT_SCALE;

        font.getData().setScale(scale);

        // Mido el alto para colocar el texto pegado abajo con el padding
        layout.setText(font, text);
        float baselineY = HINT_BOTTOM_PAD + layout.height;

        // Color del texto
        font.setColor(Color.WHITE);

        // Dibujo centrado en X usando Align.center
        // Aquí no calculas x porque le dices que use WORLD_W y centre
        font.draw(game.batch, text, 0f, baselineY, WORLD_W, Align.center, true);

        // Reseteo color y escala para no afectar otras pantallas
        Fonts.resetColor(font);
        font.getData().setScale(1f);

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Si cambia el tamaño de ventana, actualizo el viewport y recalculo el rectángulo clickable
        viewport.update(width, height, true);
        updateHintBounds();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override public void dispose() {
        // No hago dispose de font ni background porque vienen del AssetManager
        // Si los liberas aquí puedes romper otras pantallas
    }
}
