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

public class HowToPlayScreen implements Screen {

    // Tamaño fijo del mundo para esta pantalla
    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    // Datos del botón back, igual que en otras pantallas
    // BACK_MARGIN es la separación con el borde
    // BACK_W y BACK_H es el tamaño del botón
    // HOVER_SCALE es el tamaño cuando pasas el dedo o el ratón por encima
    private static final float BACK_MARGIN = 24f;
    private static final float BACK_W = 150f;
    private static final float BACK_H = 150f;
    private static final float HOVER_SCALE = 1.08f;

    // Escala de la letra para la parte de navegación (prev, next y página)
    private static final float NAV_SCALE = 1.35f;

    // Número máximo de páginas
    // En tu caso tienes dos páginas de instrucciones
    private static final int PAGE_MAX = 2;

    // Escala base para el texto grande de instrucciones
    // Luego tú intentas bajarla si no cabe
    private static final float BASE_SCALE = 1.75f;

    // Referencia al juego principal para poder cambiar pantallas, usar audio, settings, i18n, assets
    private final Main game;

    // Cámara y viewport para dibujar en coordenadas del mundo
    private OrthographicCamera camera;
    private Viewport viewport;

    // Fondo y textura del botón back
    private Texture bg;
    private Texture btnBack;

    // Fuente principal del juego
    private BitmapFont font;

    // Layouts para medir textos
    // layoutTitle para el título
    // layoutLeft y layoutRight para medir el contenido de columnas
    // navLayout para medir prev y next
    // pageLayout para medir el texto de página 1/2
    private GlyphLayout layoutTitle;
    private GlyphLayout layoutLeft;
    private GlyphLayout layoutRight;
    private GlyphLayout navLayout;
    private GlyphLayout pageLayout;

    // Rectángulos clicables
    // backBounds para botón back
    // nextBounds para texto next
    // prevBounds para texto prev
    private Rectangle backBounds;
    private Rectangle nextBounds;
    private Rectangle prevBounds;

    // Vectores para convertir coordenadas de pantalla a mundo
    private Vector2 pointerWorld;
    private Vector2 touch;

    // Hover para saber si estás encima de cada cosa
    private boolean hoverBack;
    private boolean hoverNext;
    private boolean hoverPrev;

    // Página actual
    private int page;

    // Esto guarda la escala calculada para la página 1
    // Lo haces porque el texto en cada idioma puede ser más largo y no caber
    private float page1Scale;
    private boolean page1ScaleComputed;

    public HowToPlayScreen(Main game) {
        this.game = game;

        // Creo cámara y viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        // Centro cámara
        camera.position.set(WORLD_W * 0.5f, WORLD_H * 0.5f, 0f);
        camera.update();

        // Cargo assets
        bg = game.assets.get(Assets.SCREEN_HOWTOPLAY_BG);
        btnBack = game.assets.get(Assets.BUTTON_BACK);

        // Pido la fuente principal del juego y la pongo en blanco
        // Ojo con Fonts.main, depende de tu Fonts real
        font = Fonts.main();
        font.setColor(Color.WHITE);

        // Creo los layouts para medir texto
        layoutTitle = new GlyphLayout();
        layoutLeft = new GlyphLayout();
        layoutRight = new GlyphLayout();
        navLayout = new GlyphLayout();
        pageLayout = new GlyphLayout();

        // Creo rectángulos
        backBounds = new Rectangle();
        nextBounds = new Rectangle();
        prevBounds = new Rectangle();

        // Creo vectores
        pointerWorld = new Vector2();
        touch = new Vector2();

        // Inicializo hover
        hoverBack = false;
        hoverNext = false;
        hoverPrev = false;

        // Empiezo en página 1
        page = 1;

        // La escala inicial es la base, y luego tú la ajustas si hace falta
        page1Scale = BASE_SCALE;
        page1ScaleComputed = false;

        // Coloco los rectángulos en pantalla
        updateBounds();
    }

    private void updateBounds() {
        // Botón back abajo a la izquierda
        backBounds.set(BACK_MARGIN, BACK_MARGIN, BACK_W, BACK_H);

        // Botón prev al lado del back
        // Es un rectángulo que engloba el texto "prev"
        prevBounds.set(BACK_MARGIN + BACK_W + 18f, BACK_MARGIN + 18f, 220f, 60f);

        // Botón next abajo a la derecha
        nextBounds.set(WORLD_W - BACK_MARGIN - 220f, BACK_MARGIN + 18f, 220f, 60f);
    }

    private void playClickIfAllowed() {
        // Si los efectos están activados, suena el click
        if (game.settings != null && game.settings.isSfxEnabled()) {
            game.audio.playSfx(Assets.SFX_BUTTON_CLICKED);
        }
    }

    private void goBack() {
        // Vuelvo al menú
        playClickIfAllowed();
        game.setScreen(new MenuScreen(game));
    }

    private void nextPage() {
        // Paso a la siguiente página si no estoy ya en la última
        if (page < PAGE_MAX) {
            playClickIfAllowed();
            page++;
        }
    }

    private void prevPage() {
        // Vuelvo a la anterior si no estoy ya en la primera
        if (page > 1) {
            playClickIfAllowed();
            page--;
        }
    }

    private void updatePointer() {
        // Cojo posición de pantalla y la convierto a mundo
        pointerWorld.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(pointerWorld);

        // Compruebo si estoy encima de cada rectángulo
        hoverBack = backBounds.contains(pointerWorld.x, pointerWorld.y);
        hoverNext = nextBounds.contains(pointerWorld.x, pointerWorld.y);
        hoverPrev = prevBounds.contains(pointerWorld.x, pointerWorld.y);
    }

    private void drawBackButton() {
        // Si hay hover, lo dibujo un poco más grande
        float scale;
        if (hoverBack) scale = HOVER_SCALE;
        else scale = 1f;

        float w = backBounds.width * scale;
        float h = backBounds.height * scale;

        // Centro el botón escalado dentro del rectángulo base
        float x = backBounds.x + (backBounds.width - w) * 0.5f;
        float y = backBounds.y + (backBounds.height - h) * 0.5f;

        game.batch.draw(btnBack, x, y, w, h);
    }

    private void drawNav() {
        // Dibujo los textos de navegación: página actual, prev y next
        font.setColor(Color.WHITE);
        font.getData().setScale(NAV_SCALE);

        // Texto del tipo "PÁGINA 1/2"
        String pageTxt = game.i18n.t("howto.page") + " " + page + "/" + PAGE_MAX;
        pageLayout.setText(font, pageTxt);

        // Lo dibujo centrado cerca de abajo
        font.draw(game.batch, pageLayout, (WORLD_W - pageLayout.width) * 0.5f, 70f);

        // Si no estoy en la primera página, dibujo el texto prev
        if (page > 1) {
            String prevTxt = game.i18n.t("howto.prev");

            // Aquí haces un truco para el hover sin tener dos claves de texto
            // Si estás encima, lo pones en mayúsculas para que se note
            if (hoverPrev) prevTxt = prevTxt.toUpperCase();

            navLayout.setText(font, prevTxt);
            font.draw(game.batch, navLayout, prevBounds.x, prevBounds.y + prevBounds.height);
        }

        // Si no estoy en la última página, dibujo el texto next
        if (page < PAGE_MAX) {
            String nextTxt = game.i18n.t("howto.next");

            // Igual que con prev, lo pones en mayúsculas si hay hover
            if (hoverNext) nextTxt = nextTxt.toUpperCase();

            navLayout.setText(font, nextTxt);
            font.draw(game.batch, navLayout, nextBounds.x, nextBounds.y + nextBounds.height);
        }

        // Vuelvo la escala a 1 y reseteo color para no dejar la fuente cambiada
        font.getData().setScale(1f);
        Fonts.resetColor(font);
    }

    private void computePage1Scale(float colW, float topY) {
        // Aquí calculas una escala para que el texto de la página 1 quepa sin salirse por abajo
        // Lo haces midiendo la altura que ocupa en cada columna y bajando la escala si hace falta

        String left1 = game.i18n.t("howto.page1.left");
        String right1 = game.i18n.t("howto.page1.right");

        // Este es el límite inferior, para no chocar con el botón back y la zona de navegación
        float bottomLimit = BACK_MARGIN + BACK_H + 40f;

        // Altura disponible desde el top del contenido hasta ese límite
        float availableH = topY - bottomLimit;

        float scale = BASE_SCALE;

        // Repites varias veces, cada vez bajando un poco la escala si no cabe
        for (int i = 0; i < 12; i++) {
            font.getData().setScale(scale);

            // Mides el texto de la columna izquierda y derecha con word wrap activado
            layoutLeft.setText(font, left1, Color.WHITE, colW, Align.left, true);
            layoutRight.setText(font, right1, Color.WHITE, colW, Align.left, true);

            // Te quedas con la columna que sea más alta, porque esa es la que manda
            float neededH;
            if (layoutLeft.height > layoutRight.height) neededH = layoutLeft.height;
            else neededH = layoutRight.height;

            // Si la altura necesaria cabe en la altura disponible, paras
            if (neededH <= availableH) break;

            // Si no cabe, reduces la escala un poco
            scale -= 0.10f;

            // Pones un mínimo para que no se quede enana
            if (scale < 1.05f) {
                scale = 1.05f;
                break;
            }
        }

        // Guardas la escala final calculada
        page1Scale = scale;
        page1ScaleComputed = true;

        // Devuelves la escala a 1 para no ensuciar cosas
        font.getData().setScale(1f);
    }

    @Override
    public void show() {
        // Al entrar, pones música (aquí usas la de intro)
        game.audio.playMusic(Assets.MUS_INTRO_THEME, true);

        // Reinicias a la página 1
        page = 1;

        // Haces que se vuelva a calcular la escala por si cambió idioma o tamaño
        page1ScaleComputed = false;
        page1Scale = BASE_SCALE;
    }

    @Override
    public void render(float delta) {

        // Actualizas hover de botones y textos
        updatePointer();

        // Escape o back vuelve al menú
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            goBack();
            return;
        }

        // Flecha derecha o D cambia a la siguiente página
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            nextPage();
        }

        // Flecha izquierda o A vuelve a la anterior
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            prevPage();
        }

        // Control por toque
        if (Gdx.input.justTouched()) {
            touch.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touch);

            // Si tocas el back, vuelves al menú
            if (backBounds.contains(touch.x, touch.y)) {
                goBack();
                return;
            }

            // Si estás en una página que tiene next, y tocas next, avanzas
            if (page < PAGE_MAX && nextBounds.contains(touch.x, touch.y)) {
                nextPage();
                return;
            }

            // Si estás en una página que tiene prev, y tocas prev, retrocedes
            if (page > 1 && prevBounds.contains(touch.x, touch.y)) {
                prevPage();
                return;
            }
        }

        // Limpio pantalla
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Aplico viewport y cámara
        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);

        // Aquí defines el sistema de dos columnas
        // 100 de margen a la izquierda y derecha
        // 60 de espacio entre columnas
        float colW = (WORLD_W - 100f * 2f - 60f) * 0.5f;
        float leftX = 100f;
        float rightX = 100f + colW + 60f;

        // Y desde donde empieza el contenido de texto
        float contentTopY = 590f;

        // Si todavía no calculaste la escala para que la página 1 quepa, lo haces aquí
        if (!page1ScaleComputed) {
            computePage1Scale(colW, contentTopY);
        }

        game.batch.begin();

        // Dibujo el fondo
        game.batch.draw(bg, 0f, 0f, WORLD_W, WORLD_H);

        // Dibujo el título
        font.setColor(Color.WHITE);
        font.getData().setScale(2.6f);
        layoutTitle.setText(font, game.i18n.t("howto.title"));
        font.draw(game.batch, layoutTitle, (WORLD_W - layoutTitle.width) * 0.5f, 665f);

        // El texto cambia según página
        String left;
        String right;

        if (page == 1) {
            left = game.i18n.t("howto.page1.left");
            right = game.i18n.t("howto.page1.right");
        } else {
            left = game.i18n.t("howto.page2.left");
            right = game.i18n.t("howto.page2.right");
        }

        // Aquí usas la escala calculada
        // Ahora mismo siempre usas page1Scale aunque estés en página 2
        // Si la página 2 tiene menos texto, no pasa nada, solo se verá un poco más pequeño de lo necesario
        float scaleToUse = page1Scale;

        font.getData().setScale(scaleToUse);

        // Mides el texto para que haga wrap en el ancho de columna
        layoutLeft.setText(font, left, Color.WHITE, colW, Align.left, true);
        layoutRight.setText(font, right, Color.WHITE, colW, Align.left, true);

        // Dibujas las dos columnas desde arriba hacia abajo
        font.draw(game.batch, layoutLeft, leftX, contentTopY);
        font.draw(game.batch, layoutRight, rightX, contentTopY);

        // Dibujo botón back y navegación
        drawBackButton();
        drawNav();

        // Reseteo color y escala para no romper otras pantallas
        Fonts.resetColor(font);
        font.getData().setScale(1f);

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Si cambia tamaño, actualizo viewport y recalculo la escala porque cambia el layout real
        viewport.update(width, height, true);
        updateBounds();
        page1ScaleComputed = false;
        page1Scale = BASE_SCALE;
    }

    @Override
    public void pause() {
        // No haces nada aquí porque controlas pausa con tu sistema de PauseScreen
    }

    @Override
    public void resume() {
        // No haces nada aquí
    }

    @Override
    public void hide() {
        // No haces nada aquí
    }

    @Override
    public void dispose() {
        // No haces dispose de font ni texturas porque vienen del AssetManager
    }
}
