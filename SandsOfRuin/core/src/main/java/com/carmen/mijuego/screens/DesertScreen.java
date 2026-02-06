package com.carmen.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.carmen.mijuego.Main;
import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.characters.Ayla;
import com.carmen.mijuego.input.Controls;
import com.carmen.mijuego.world.ParallaxBackground;

public class DesertScreen implements Screen {

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;
    private static final float GROUND_Y = 120f;

    // Auto-scroll: velocidad del nivel (ajusta a gusto)
    private static final float LEVEL_SPEED = 260f; // similar al SPEED de Ayla para que “se note”

    // Margen mínimo: Ayla no puede quedarse más atrás de este punto del avance
    private static final float BACK_MARGIN = 140f;

    // Cámara: cuánto “adelantada” va respecto a scrollX
    private static final float CAMERA_AHEAD = 500f;

    private final Main game;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture sky, clouds, ruins, mid, near;
    private Ayla ayla;
    private Controls controls;

    private ParallaxBackground parallax;

    // Avance del nivel
    private float scrollX = 0f;

    public DesertScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FillViewport(WORLD_W, WORLD_H, camera);
        viewport.apply();

        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0f);
        camera.update();

        sky = game.assets.get(Assets.SKY);
        clouds = game.assets.get(Assets.CLOUDS);
        ruins = game.assets.get(Assets.RUINS);
        mid = game.assets.get(Assets.MID);
        near = game.assets.get(Assets.NEAR);

        ayla = new Ayla(
            game.assets.get(Assets.AYLA_RUN),
            game.assets.get(Assets.AYLA_IDLE),
            game.assets.get(Assets.AYLA_JUMP),
            120f,
            GROUND_Y
        );

        controls = new Controls(
            viewport,
            game.assets.get(Assets.UI_LEFT),
            game.assets.get(Assets.UI_RIGHT),
            game.assets.get(Assets.UI_JUMP),
            game.assets.get(Assets.UI_SHOOT),
            game.assets.get(Assets.UI_GRENADE),
            game.assets.get(Assets.UI_PAUSE)
        );

        Gdx.input.setInputProcessor(controls);
        controls.updateLayout(camera, viewport);

        // Parallax: sky fijo + capas con factor
        parallax = new ParallaxBackground(
            camera,
            viewport,
            sky,
            new Texture[] { clouds, ruins, mid, near },
            new float[]   { 0.20f,  0.35f, 0.60f, 0.85f }
        );
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        // Input
        boolean left = Gdx.input.isKeyPressed(Input.Keys.LEFT) || controls.leftPressed;
        boolean right = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || controls.rightPressed;
        boolean jump = Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || controls.jumpPressed;

        boolean moving = (left ^ right);

        // 1) Avanza el nivel automáticamente
        scrollX += LEVEL_SPEED * delta;

        // 2) Actualiza Ayla con input normal
        ayla.update(delta, left, right, jump, GROUND_Y);

        // 3) El scroll “arrastra” a Ayla: no puede quedarse atrás
        float minAylaX = scrollX + BACK_MARGIN;
        ayla.pushMinX(minAylaX);

        // Si prefieres “Mario”: si se queda atrás, game over (en vez de empujar)
        // if (ayla.getX() < minAylaX) { gameOver(); return; }

        // 4) Cámara basada en scroll (no en Ayla)
        float minCamX = viewport.getWorldWidth() / 2f;
        float targetCamX = scrollX + CAMERA_AHEAD;

        if (targetCamX < minCamX) targetCamX = minCamX;

        camera.position.x += (targetCamX - camera.position.x) * 10f * delta;
        camera.update();

        game.batch.setProjectionMatrix(camera.combined);
        controls.updateLayout(camera, viewport);

        // Render
        game.batch.begin();

        // Fondo parallax independiente del jugador
        parallax.render(game.batch, scrollX);

        // Mundo (Ayla) en coordenadas de mundo
        ayla.draw(game.batch, moving);

        // UI (se dibuja con la misma cámara porque tu Controls ya calcula posiciones mundo)
        controls.draw(game.batch);

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        controls.updateLayout(camera, viewport);
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
