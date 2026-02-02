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

public class DesertScreen implements Screen {

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;
    private static final float GROUND_Y = 120f;

    private final Main game;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture sky, clouds, ruins, mid, near;
    private Ayla ayla;
    private Controls controls;

    private float xClouds, xRuins, xMid, xNear;

    public DesertScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();

        // Pantalla completa (sin bandas negras). Puede recortar un poco según el móvil.
        viewport = new FillViewport(WORLD_W, WORLD_H, camera);
        viewport.apply();

        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0f);
        camera.update();

        sky = game.assets.get(Assets.SKY);
        clouds = game.assets.get(Assets.CLOUDS);
        ruins = game.assets.get(Assets.RUINS);
        mid = game.assets.get(Assets.MID);
        near = game.assets.get(Assets.NEAR);

        ayla = new Ayla(game.assets.get(Assets.AYLA_RUN), -200, GROUND_Y);

        controls = new Controls(
            viewport,
            game.assets.get(Assets.UI_LEFT),
            game.assets.get(Assets.UI_RIGHT),
            game.assets.get(Assets.UI_JUMP),
            game.assets.get(Assets.UI_SHOOT),
            game.assets.get(Assets.UI_GRENADE),
            game.assets.get(Assets.UI_PAUSE) // Añade este asset/constante
        );

        Gdx.input.setInputProcessor(controls);
        controls.updateLayout(camera, viewport);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        boolean left = Gdx.input.isKeyPressed(Input.Keys.LEFT) || controls.leftPressed;
        boolean right = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || controls.rightPressed;
        boolean jump = Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || controls.jumpPressed;

        ayla.update(delta, left, right, jump, GROUND_Y);

        // Movimiento del personaje en este frame
        float dx = ayla.getLastDx();

        // Parallax offsets
        xClouds -= dx * 0.15f;
        xRuins  -= dx * 0.30f;
        xMid    -= dx * 0.60f;
        xNear   -= dx * 0.90f;

        // Cámara avanza con el movimiento (simple)
        camera.position.x += dx;

        // Limitar cámara al inicio
        float minCamX = viewport.getWorldWidth() / 2f;
        if (camera.position.x < minCamX) camera.position.x = minCamX;

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        // HUD anclado a la pantalla visible
        controls.updateLayout(camera, viewport);

        game.batch.begin();

        float camLeft = camera.position.x - viewport.getWorldWidth() / 2f;

        // Base sky anclado
        game.batch.draw(sky, camLeft, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        // Capas parallax (de lejos a cerca)
        drawLayer(clouds, camLeft, xClouds);
        drawLayer(ruins,  camLeft, xRuins);
        drawLayer(mid,    camLeft, xMid);
        drawLayer(near,   camLeft, xNear);

        ayla.draw(game.batch);
        controls.draw(game.batch);

        game.batch.end();
    }

    /**
     * Dibuja una capa repitiéndola en X para que no se corte al moverse.
     * Asume capas del tamaño WORLD_W x WORLD_H.
     */
    private void drawLayer(Texture tex, float camLeft, float xOffset) {
        float start = xOffset % WORLD_W;
        if (start > 0) start -= WORLD_W;

        game.batch.draw(tex, camLeft + start, 0, WORLD_W, WORLD_H);
        game.batch.draw(tex, camLeft + start + WORLD_W, 0, WORLD_W, WORLD_H);
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
