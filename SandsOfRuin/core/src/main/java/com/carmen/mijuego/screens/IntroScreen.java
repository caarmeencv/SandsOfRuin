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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.carmen.mijuego.Main;
import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.ui.Fonts;

public class IntroScreen implements Screen {

    // Tamaño fijo del mundo
    // Todo se dibuja pensando en 1280 por 720
    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    // Altura donde se dibuja el texto de "toca para empezar"
    private static final float TEXT_Y = 80f;

    // Ajustes del efecto de parpadeo del texto
    // BLINK_SPEED controla la velocidad del efecto
    // MIN_ALPHA es la transparencia mínima, nunca desaparece del todo
    private static final float BLINK_SPEED = 2.2f;
    private static final float MIN_ALPHA = 0.35f;

    // Referencia al juego principal
    private final Main game;

    // Cámara y viewport
    private OrthographicCamera camera;
    private Viewport viewport;

    // Imagen de fondo de la intro
    private Texture introImage;

    // Fuente y layout para medir el texto
    private BitmapFont font;
    private GlyphLayout layout;

    // Rectángulo clicable del texto
    private Rectangle tapBounds;

    // Vector para convertir coordenadas de pantalla a mundo
    private Vector2 touch;

    // Tiempo acumulado para el efecto de parpadeo
    private float blinkTime;

    public IntroScreen(Main game) {
        this.game = game;

        // Creo cámara y viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        // Centro la cámara
        camera.position.set(WORLD_W * 0.5f, WORLD_H * 0.5f, 0f);
        camera.update();

        // Cargo la imagen de fondo desde el AssetManager
        introImage = game.assets.get(Assets.SCREEN_INTRO);

        // Pido la fuente principal del juego
        // Ojo, esto depende de cómo tengas tu clase Fonts
        font = Fonts.main();

        // Escala inicial del texto
        font.getData().setScale(1.5f);

        layout = new GlyphLayout();

        // Creo rectángulo y vector de toque
        tapBounds = new Rectangle();
        touch = new Vector2();

        // Empiezo el contador de parpadeo en cero
        blinkTime = 0f;

        // Calculo el rectángulo del texto
        updateTapBounds();
    }

    private void playClickIfAllowed() {
        // Si los efectos están activados, suena el click
        if (game.settings != null && game.settings.isSfxEnabled()) {
            game.audio.playSfx(Assets.SFX_BUTTON_CLICKED);
        }
    }

    private void goMenuWithClick() {
        // Reproduce sonido y cambia a la pantalla del menú
        playClickIfAllowed();
        game.setScreen(new MenuScreen(game));
    }

    private void updateTapBounds() {
        // Cojo el texto traducido
        String text = game.i18n.t("intro.tap");

        // Mido el texto con la fuente actual
        layout.setText(font, text);

        // Lo centro en horizontal
        float x = (WORLD_W - layout.width) * 0.5f;

        // Alto real del texto
        float h = layout.height;

        // Creo el rectángulo clicable justo donde está el texto
        // Recuerda que la fuente se dibuja desde la línea base
        tapBounds.set(x, TEXT_Y - h, layout.width, h);
    }

    @Override
    public void show() {
        // Cuando se muestra la intro, suena su música
        game.audio.playMusic(Assets.MUS_INTRO_THEME, true);

        // Recalculo el rectángulo por si cambió idioma
        updateTapBounds();
    }

    @Override
    public void render(float delta) {

        // Aumento el tiempo para el efecto de parpadeo
        blinkTime += delta;

        // Si pulsas enter o espacio en PC, pasas al menú
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
            Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            goMenuWithClick();
            return;
        }

        // Si tocas la pantalla, miro si tocaste encima del texto
        if (Gdx.input.justTouched()) {
            touch.set(Gdx.input.getX(), Gdx.input.getY());

            // Paso coordenadas de pantalla a mundo
            viewport.unproject(touch);

            if (tapBounds.contains(touch.x, touch.y)) {
                goMenuWithClick();
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

        // Dibujo la imagen de fondo
        game.batch.draw(introImage, 0f, 0f, WORLD_W, WORLD_H);

        // Texto que aparece abajo
        String text = game.i18n.t("intro.tap");

        // Mido el texto para centrarlo
        layout.setText(font, text);
        float x = (WORLD_W - layout.width) * 0.5f;

        // Aquí haces el efecto de parpadeo suave
        // MathUtils.sin devuelve valores entre menos uno y uno
        // Primero lo llevas al rango de cero a uno
        float t = (MathUtils.sin(blinkTime * BLINK_SPEED) + 1f) * 0.5f;

        // Luego lo llevas al rango entre MIN_ALPHA y uno
        float alpha = MIN_ALPHA + (1f - MIN_ALPHA) * t;

        // Aplicas el alpha al color blanco
        font.setColor(1f, 1f, 1f, alpha);

        // Dibujas el texto centrado
        font.draw(game.batch, layout, x, TEXT_Y);

        // Reseteas el color para no afectar otras pantallas
        Fonts.resetColor(font);

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Si cambia el tamaño de pantalla, actualizas viewport
        viewport.update(width, height, true);

        // Recalculas el rectángulo del texto
        updateTapBounds();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        // No haces dispose porque la textura y la fuente vienen del AssetManager
    }
}
