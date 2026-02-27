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

public class MenuScreen implements Screen {

    // Tamaño fijo del mundo para dibujar el menú siempre igual
    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    // Cuando pasas el ratón por encima, el botón se hace un poco más grande
    private static final float HOVER_SCALE = 1.08f;

    // Referencia al juego principal para poder cambiar de pantalla y usar audio, assets, etc
    private final Main game;

    // Cámara y viewport para dibujar en modo “mundo fijo”
    private OrthographicCamera camera;
    private Viewport viewport;

    // Texturas del fondo y de los botones
    private Texture bg;
    private Texture btnGame;
    private Texture btnOptions;
    private Texture btnCredits;
    private Texture btnAchievements;

    // Rectángulos donde detecto si el ratón o el dedo está encima de cada botón
    private Rectangle rGame;
    private Rectangle rOptions;
    private Rectangle rCredits;
    private Rectangle rAchievements;
    private Rectangle rHowTo;

    // Vector para convertir coordenadas de pantalla a coordenadas del mundo
    private Vector2 pointerWorld;

    // Flags para saber si el ratón está encima de cada cosa
    private boolean hoverGame;
    private boolean hoverOptions;
    private boolean hoverCredits;
    private boolean hoverAchievements;
    private boolean hoverHowTo;

    // Fuente y layouts para medir y dibujar los textos
    private BitmapFont font;
    private GlyphLayout layout;
    private GlyphLayout howToLayout;

    public MenuScreen(Main game) {
        this.game = game;

        // Creo la cámara y el viewport con tamaño fijo
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        // Centro la cámara
        camera.position.set(WORLD_W * 0.5f, WORLD_H * 0.5f, 0f);
        camera.update();

        // Cargo las texturas desde el AssetManager
        bg = game.assets.get(Assets.SCREEN_MENU_BG);
        btnGame = game.assets.get(Assets.SCREEN_MENU_BTN_GAME);
        btnOptions = game.assets.get(Assets.SCREEN_MENU_BTN_OPTIONS);
        btnCredits = game.assets.get(Assets.SCREEN_MENU_BTN_CREDITS);
        btnAchievements = game.assets.get(Assets.SCREEN_MENU_BTN_ACHIEVEMENTS);

        // Creo los rectángulos vacíos, luego los relleno con tamaños reales
        rGame = new Rectangle();
        rOptions = new Rectangle();
        rCredits = new Rectangle();
        rAchievements = new Rectangle();
        rHowTo = new Rectangle();

        // Vector para saber dónde está el puntero dentro del mundo
        pointerWorld = new Vector2();

        // Empiezo sin hover
        hoverGame = false;
        hoverOptions = false;
        hoverCredits = false;
        hoverAchievements = false;
        hoverHowTo = false;

        // Cojo la fuente principal y le pongo escala para los textos de los botones
        font = Fonts.main();
        font.getData().setScale(2.0f);

        // Layouts para medir textos
        layout = new GlyphLayout();
        howToLayout = new GlyphLayout();

        // Calculo dónde van los botones
        updateLayout();

        // Calculo el rectángulo clicable del “cómo jugar”
        updateHowToLayout();
    }

    private void playClickIfAllowed() {
        // Solo suena el click si los efectos están activados
        if (game.settings != null && game.settings.isSfxEnabled()) {
            game.audio.playSfx(Assets.SFX_BUTTON_CLICKED);
        }
    }

    private void updateLayout() {
        // Esto es como un panel invisible para colocar los botones centrados
        float panelW = 900f;
        float panelH = 360f;

        float panelX = (WORLD_W - panelW) * 0.5f;
        float panelY = (WORLD_H - panelH) * 0.5f - 40f;

        // Tamaño base de cada botón
        float btnW = 360f;

        // Mantengo proporción usando el tamaño real de la imagen del botón
        float btnH = btnW * ((float) btnGame.getHeight() / (float) btnGame.getWidth());

        // Separaciones entre columnas y filas
        float colGap = 30f;
        float rowGap = 50f;

        // Calculo ancho total de dos botones + hueco
        float totalW = btnW * 2f + colGap;
        float startX = panelX + (panelW - totalW) * 0.5f;

        float leftX = startX;
        float rightX = startX + btnW + colGap;

        // Calculo alto total de dos filas + hueco
        float totalH = btnH * 2f + rowGap;
        float startY = panelY + (panelH - totalH) * 0.5f;

        // Pequeño ajuste para subir o bajar el bloque de botones
        float centerOffsetY = -25f;

        float bottomY = startY + centerOffsetY;
        float topY = bottomY + btnH + rowGap;

        // Asigno los rectángulos de cada botón
        rGame.set(leftX, topY, btnW, btnH);
        rOptions.set(rightX, topY, btnW, btnH);
        rCredits.set(leftX, bottomY, btnW, btnH);
        rAchievements.set(rightX, bottomY, btnW, btnH);
    }

    private void updateHowToLayout() {
        // Escala más pequeña porque es un texto suelto, no un botón grande
        float baseScale = 1.3f;
        font.getData().setScale(baseScale);

        // Texto traducido
        String txt = game.i18n.t("menu.howto");

        // Lo mido para sacar su ancho y alto
        howToLayout.setText(font, txt);

        // Centro el texto
        float x = (WORLD_W - howToLayout.width) * 0.5f;

        // Altura donde lo quiero dibujar
        float y = 70f;

        // Creo el rectángulo clicable justo donde está el texto
        rHowTo.set(x, y - howToLayout.height, howToLayout.width, howToLayout.height);

        // Vuelvo a la escala normal del menú
        font.getData().setScale(2.0f);
    }

    private void updatePointer() {
        // Cojo la posición del ratón o toque en coordenadas de pantalla
        pointerWorld.set(Gdx.input.getX(), Gdx.input.getY());

        // La convierto a coordenadas del mundo del menú
        viewport.unproject(pointerWorld);

        // Compruebo si está encima de cada rectángulo
        hoverGame = rGame.contains(pointerWorld.x, pointerWorld.y);
        hoverOptions = rOptions.contains(pointerWorld.x, pointerWorld.y);
        hoverCredits = rCredits.contains(pointerWorld.x, pointerWorld.y);
        hoverAchievements = rAchievements.contains(pointerWorld.x, pointerWorld.y);
        hoverHowTo = rHowTo.contains(pointerWorld.x, pointerWorld.y);
    }

    private void drawButton(Texture tex, Rectangle r, boolean hover, String text) {
        // Si hay hover, agrando un poco el botón
        float scale;
        if (hover) scale = HOVER_SCALE;
        else scale = 1f;

        // Tamaño dibujado
        float w = r.width * scale;
        float h = r.height * scale;

        // Recoloco para que el escalado sea desde el centro
        float x = r.x + (r.width - w) * 0.5f;
        float y = r.y + (r.height - h) * 0.5f;

        // Dibujo la imagen del botón
        game.batch.draw(tex, x, y, w, h);

        // Escala de la fuente para que el texto acompañe el hover
        float baseFontScale = 2.0f;
        font.getData().setScale(baseFontScale * scale);

        // Mido el texto para centrarlo dentro del botón
        layout.setText(font, text);

        // Esto es un pequeño desplazamiento para evitar que el texto se coma el icono del botón
        float iconOffset = w * 0.12f;

        // Coordenadas del texto centrado
        float textX = x + (w - layout.width) * 0.5f + iconOffset;
        float textY = y + (h + layout.height) * 0.5f;

        // Dibujo el texto
        font.draw(game.batch, layout, textX, textY);

        // Devuelvo la escala de la fuente a la normal del menú
        font.getData().setScale(baseFontScale);
    }

    private void drawHowTo() {
        // Dibujo el texto suelto de “cómo jugar” abajo
        float baseScale = 1.3f;

        float scale;
        if (hoverHowTo) scale = 1.08f;
        else scale = 1f;

        String txt = game.i18n.t("menu.howto");

        // Escalo la fuente para el hover del texto
        font.getData().setScale(baseScale * scale);

        // Mido el texto para centrarlo
        howToLayout.setText(font, txt);

        float x = (WORLD_W - howToLayout.width) * 0.5f;
        float y = 70f;

        // Dibujo el texto
        font.draw(game.batch, howToLayout, x, y);

        // Vuelvo a la escala normal del menú
        font.getData().setScale(2.0f);
    }

    @Override
    public void show() {
        // Música del menú
        game.audio.playMusic(Assets.MUS_INTRO_THEME, true);
    }

    @Override
    public void render(float delta) {
        // Actualizo los hovers del ratón
        updatePointer();

        // Si tocas la pantalla, miro qué botón estabas tocando
        // Como ya actualicé hover, uso esos booleanos
        if (Gdx.input.justTouched()) {

            if (hoverGame) {
                playClickIfAllowed();
                game.resetRun();
                game.setScreen(new DesertScreen(game));
                return;
            }

            if (hoverOptions) {
                playClickIfAllowed();
                game.setScreen(new OptionsScreen(game));
                return;
            }

            if (hoverCredits) {
                playClickIfAllowed();
                game.setScreen(new CreditsScreen(game));
                return;
            }

            if (hoverAchievements) {
                playClickIfAllowed();
                game.setScreen(new RecordsScreen(game));
                return;
            }

            if (hoverHowTo) {
                playClickIfAllowed();
                game.setScreen(new HowToPlayScreen(game));
                return;
            }
        }

        // Si le das a escape o back, se cierra el juego
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            Gdx.app.exit();
            return;
        }

        // Limpio pantalla
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Preparo cámara y batch para dibujar
        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

        // Fondo del menú
        game.batch.draw(bg, 0f, 0f, WORLD_W, WORLD_H);

        // Botones principales
        drawButton(btnGame, rGame, hoverGame, game.i18n.t("menu.play"));
        drawButton(btnOptions, rOptions, hoverOptions, game.i18n.t("menu.options"));
        drawButton(btnCredits, rCredits, hoverCredits, game.i18n.t("menu.credits"));
        drawButton(btnAchievements, rAchievements, hoverAchievements, game.i18n.t("menu.achievements"));

        // Texto de “cómo jugar”
        drawHowTo();

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Actualizo viewport al cambiar tamaño
        viewport.update(width, height, true);

        // Recalculo el rectángulo clicable del texto de abajo
        updateHowToLayout();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        // No hago dispose porque assets y fuentes los gestiona el AssetManager del juego
    }
}
