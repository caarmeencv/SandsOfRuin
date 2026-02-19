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

public class HowToPlayScreen implements Screen {

    private final Main game;

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture bg;

    private BitmapFont fontTitle;
    private BitmapFont fontBody;
    private BitmapFont fontFooter;

    private GlyphLayout layoutTitle;
    private GlyphLayout layoutLeft;
    private GlyphLayout layoutRight;
    private GlyphLayout layoutTapBack;
    private GlyphLayout layoutKeyBack;

    private final Vector2 pointerWorld = new Vector2();
    private final Rectangle rTapBack = new Rectangle();
    private final Rectangle rKeyBack = new Rectangle();
    private boolean hoverTapBack = false;
    private boolean hoverKeyBack = false;

    public HowToPlayScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0f);
        camera.update();

        // ✅ Fondo desde AssetManager
        bg = game.assets.get(Assets.SCREEN_HOWTOPLAY_BG);

        // Fuentes
        fontTitle = new BitmapFont();
        fontTitle.setColor(Color.WHITE);
        fontTitle.getData().setScale(2.6f);

        fontBody = new BitmapFont();
        fontBody.setColor(Color.WHITE);
        fontBody.getData().setScale(1.8f); // base (grande)

        fontFooter = new BitmapFont();
        fontFooter.setColor(Color.WHITE);
        fontFooter.getData().setScale(1.35f);

        // Layouts
        layoutTitle = new GlyphLayout();
        layoutLeft = new GlyphLayout();
        layoutRight = new GlyphLayout();
        layoutTapBack = new GlyphLayout();
        layoutKeyBack = new GlyphLayout();
    }

    @Override
    public void show() {
        game.audio.playMusic(Assets.MUS_INTRO_THEME, true);
    }

    private void goBack() {
        game.setScreen(new MenuScreen(game));
    }

    private void updatePointer() {
        pointerWorld.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(pointerWorld);

        hoverTapBack = rTapBack.contains(pointerWorld);
        hoverKeyBack = rKeyBack.contains(pointerWorld);
    }

    private void updateFooterRects() {
        // Se calculan con el tamaño real del texto (layout.width/height)
        float tapX = (WORLD_W - layoutTapBack.width) / 2f;
        float tapY = 95f; // baseline del texto

        // Rectangle en coords mundo: x,y es esquina inferior izquierda
        rTapBack.set(tapX, tapY - layoutTapBack.height, layoutTapBack.width, layoutTapBack.height);

        float keyX = (WORLD_W - layoutKeyBack.width) / 2f;
        float keyY = 65f;

        rKeyBack.set(keyX, keyY - layoutKeyBack.height, layoutKeyBack.width, layoutKeyBack.height);
    }

    @Override
    public void render(float delta) {

        // ESC / BACK siempre vuelve
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            goBack();
            return;
        }

        // Actualiza hover de textos clicables
        updatePointer();

        // Click / toque en textos de volver
        if (Gdx.input.justTouched()) {
            if (hoverTapBack || hoverKeyBack) {
                goBack();
                return;
            }
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);

        // ---- TEXTOS ----
        String leftText =
            "CONTROLES MOVIL:\n" +
                " Flechas: moverse\n" +
                " Flecha arriba: saltar (DOBLE SALTO)\n" +
                " Agitar movil: saltar (activar en Ajustes)\n" +
                " Pistola: disparo normal\n" +
                " Bomba: especial (2 disparos, cooldown)\n\n" +

                "CONTROLES ORDENADOR:\n" +
                " A / D: moverse\n" +
                " ESPACIO o W: saltar\n" +
                " K: disparar\n" +
                " L: especial\n\n" +

                "COMBATE:\n" +
                " Soldado: 2 disparos\n" +
                " Tanque: 3 disparos\n" +
                " Momia: 5 disparos\n" +
                " Cactus: no se destruyen";

        String rightText =
            "OBJETIVO:\n\n" +
                "Avanza por el desierto\n" +
                "superando enemigos y\n" +
                "obstaculos.\n\n" +
                "Llega a la piramide\n" +
                "y derrota a la momia\n" +
                "para conseguir\n" +
                "el tesoro.";

        // ---- LAYOUT (2 columnas) ----
        float margin = 100f;
        float columnGap = 60f;
        float columnW = (WORLD_W - margin * 2f - columnGap) / 2f;

        float leftX = margin;
        float rightX = margin + columnW + columnGap;

        float topY = 590f; // debajo del título
        float bottomLimit = 130f; // zona del pie
        float availableH = topY - bottomLimit;

        // Título
        layoutTitle.setText(fontTitle, "COMO JUGAR");

        // Ajuste automático del tamaño del cuerpo: si no cabe, baja un poco
        float scale = 1.9f; // empieza grande
        for (int i = 0; i < 8; i++) {
            fontBody.getData().setScale(scale);

            layoutLeft.setText(fontBody, leftText, Color.WHITE, columnW, Align.left, true);
            layoutRight.setText(fontBody, rightText, Color.WHITE, columnW, Align.left, true);

            float neededH = Math.max(layoutLeft.height, layoutRight.height);
            if (neededH <= availableH) break;

            scale -= 0.10f;
        }

        // Footer (clicable)
        fontFooter.getData().setScale(1.45f);
        String tapBack = hoverTapBack ? "TOCA AQUI PARA VOLVER" : "Toca aqui para volver";
        layoutTapBack.setText(fontFooter, tapBack);

        fontFooter.getData().setScale(1.20f);
        String keyBack = hoverKeyBack ? "PULSA ESC / ATRAS PARA VOLVER" : "Pulsa ESC / Atras para volver";
        layoutKeyBack.setText(fontFooter, keyBack);

        // Actualizar rectángulos clicables del footer
        updateFooterRects();

        // ---- DIBUJO ----
        game.batch.begin();

        // Fondo
        game.batch.draw(bg, 0, 0, WORLD_W, WORLD_H);

        // Título centrado
        fontTitle.draw(game.batch, layoutTitle, (WORLD_W - layoutTitle.width) / 2f, 665f);

        // Columna izquierda (texto grande)
        fontBody.draw(game.batch, layoutLeft, leftX, topY);

        // Columna derecha (objetivo)
        fontBody.draw(game.batch, layoutRight, rightX, topY);

        // Footer (clicable)
        // Tap back
        fontFooter.getData().setScale(1.45f);
        fontFooter.setColor(hoverTapBack ? Color.WHITE : Color.LIGHT_GRAY);
        fontFooter.draw(game.batch, layoutTapBack, (WORLD_W - layoutTapBack.width) / 2f, 95f);

        // Key back
        fontFooter.getData().setScale(1.20f);
        fontFooter.setColor(hoverKeyBack ? Color.WHITE : Color.LIGHT_GRAY);
        fontFooter.draw(game.batch, layoutKeyBack, (WORLD_W - layoutKeyBack.width) / 2f, 65f);

        // restaurar color por si acaso
        fontFooter.setColor(Color.WHITE);

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        fontTitle.dispose();
        fontBody.dispose();
        fontFooter.dispose();
        // bg NO se dispone aquí (AssetManager lo gestiona)
    }
}
