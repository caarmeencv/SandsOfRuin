package com.carmen.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.carmen.mijuego.Main;
import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.characters.Ayla;
import com.carmen.mijuego.enemies.Cactus;
import com.carmen.mijuego.input.Controls;
import com.carmen.mijuego.ui.LivesHUD;
import com.carmen.mijuego.world.ParallaxBackground;

public class DesertScreen implements Screen {

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    private static final float GROUND_Y = 180f;

    private static final float SCROLL_SPEED_FORWARD = 320f;
    private static final float SCROLL_SPEED_BACK    = 260f;

    private static final float AYLA_SCREEN_X = 220f;

    private static final float MID_FACTOR = 0.30f;
    private static final float PARALLAX_MUL = 0.70f;
    private static final float MID_REAL = MID_FACTOR * PARALLAX_MUL;

    private static final float CACTUS_MIN_DIST = 600f;
    private static final float CACTUS_MAX_DIST = 1100f;
    private static final float CACTUS_SPAWN_MARGIN = 250f;
    private static final float CACTUS_HEIGHT = 90f;

    private final Main game;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture sky, clouds, ruins, mid, near;
    private Texture cactusPink, cactusYellow;

    private Ayla ayla;
    private Controls controls;
    private ParallaxBackground parallax;

    private final Array<Cactus> cactuses = new Array<>();

    private float scrollX = 0f;
    private float nextSpawnCamRight = 0f;

    private LivesHUD livesHUD;

    public DesertScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        // ✅ FitViewport = misma proporción en PC y móvil (sin recortar)
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply();

        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0f);
        camera.update();

        sky = game.assets.get(Assets.SKY);
        clouds = game.assets.get(Assets.CLOUDS);
        ruins = game.assets.get(Assets.RUINS);
        mid = game.assets.get(Assets.MID);
        near = game.assets.get(Assets.NEAR);

        cactusPink = game.assets.get(Assets.CACTUS_PINK);
        cactusYellow = game.assets.get(Assets.CACTUS_YELLOW);

        ayla = new Ayla(
            game.assets.get(Assets.AYLA_RUN),
            game.assets.get(Assets.AYLA_IDLE),
            game.assets.get(Assets.AYLA_JUMP),
            0f,
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

        parallax = new ParallaxBackground(
            camera,
            viewport,
            sky,
            new Texture[]{ clouds, ruins, mid, near },
            new float[]  { 0.08f, 0.15f, 0.30f, 0.45f }
        );
        parallax.setSpeedMul(PARALLAX_MUL);

        livesHUD = new LivesHUD(game.assets.get(Assets.HUD_HEART_FULL));

        float camRight = camera.position.x + viewport.getWorldWidth() / 2f;
        nextSpawnCamRight = camRight + 300f;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        boolean left  = Gdx.input.isKeyPressed(Input.Keys.LEFT)  || controls.leftPressed;
        boolean right = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || controls.rightPressed;
        boolean jump  = Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || controls.jumpPressed;

        boolean moving = (left ^ right);

        if (right) scrollX += SCROLL_SPEED_FORWARD * delta;
        if (left)  scrollX -= SCROLL_SPEED_BACK * delta;
        if (scrollX < 0f) scrollX = 0f;

        float targetCamX = scrollX + viewport.getWorldWidth() / 2f;
        camera.position.x += (targetCamX - camera.position.x) * 10f * delta;
        camera.update();

        float camLeft  = camera.position.x - viewport.getWorldWidth() / 2f;
        float camRight = camera.position.x + viewport.getWorldWidth() / 2f;
        float camTop   = camera.position.y + viewport.getWorldHeight() / 2f;

        ayla.setX(camLeft + AYLA_SCREEN_X);
        ayla.update(delta, left, right, jump, GROUND_Y);

        float midScroll = scrollX * MID_REAL;

        if (camRight >= nextSpawnCamRight) {
            Texture tex = MathUtils.randomBoolean() ? cactusPink : cactusYellow;

            float xDrawSpawn = camRight + CACTUS_SPAWN_MARGIN;
            float cactusWorldX = xDrawSpawn + midScroll;

            cactuses.add(new Cactus(tex, cactusWorldX, GROUND_Y, CACTUS_HEIGHT));

            nextSpawnCamRight = camRight + MathUtils.random(CACTUS_MIN_DIST, CACTUS_MAX_DIST);
        }

        for (int i = cactuses.size - 1; i >= 0; i--) {
            if (cactuses.get(i).isOffScreenLeft(camLeft, midScroll)) {
                cactuses.removeIndex(i);
            }
        }

        game.batch.setProjectionMatrix(camera.combined);
        controls.updateLayout(camera, viewport);

        game.batch.begin();

        parallax.render(game.batch, scrollX);

        for (Cactus c : cactuses) c.draw(game.batch, midScroll);

        ayla.draw(game.batch, moving);

        livesHUD.draw(game.batch, camLeft, camTop);

        controls.draw(game.batch);

        game.batch.end();
    }

    @Override
    public void resize(int w, int h) {
        viewport.update(w, h, true);
        controls.updateLayout(camera, viewport);
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
