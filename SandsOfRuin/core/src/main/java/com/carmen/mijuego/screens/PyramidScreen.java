package com.carmen.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.carmen.mijuego.Main;
import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.characters.Ayla;
import com.carmen.mijuego.enemies.Mummy;
import com.carmen.mijuego.input.Controls;
import com.carmen.mijuego.projectiles.Bullet;
import com.carmen.mijuego.world.ParallaxPyramid;

public class PyramidScreen implements Screen {

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    private static final float GROUND_Y = 90f;

    // Intro: entra sola desde fuera
    private static final float INTRO_START_X = -260f;
    private static final float INTRO_TARGET_X = 220f;
    private static final float INTRO_WALK_SPEED = 280f;

    // Play scroll
    private static final float SCROLL_SPEED_FORWARD = 320f;
    private static final float SCROLL_SPEED_BACK    = 260f;

    // Cuánto avanzar (en scroll) tras tomar control para spawnear momia
    private static final float BOSS_SPAWN_AFTER_SCROLL = 650f;

    // Auto al final
    private static final float AUTO_SCROLL_SPEED = 320f;
    private static final float AYLA_AUTO_WALK_SPEED = 280f;
    private static final float TREASURE_SCROLL_X = 1900f;

    private enum Phase { INTRO_ENTER, PLAY, AUTO_TO_TREASURE, END }
    private Phase phase = Phase.INTRO_ENTER;

    private final Main game;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Controls controls;
    private Ayla ayla;

    private ParallaxPyramid parallax;

    // Boss
    private Texture mummyIdle, mummyWalk, mummyHurt, mummyDead;
    private Mummy mummy;
    private boolean mummySpawned = false;

    // Mundo
    private float scrollX = 0f;
    private float playStartScrollX = 0f;

    public PyramidScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply();
        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0f);
        camera.update();

        // Ayla
        ayla = new Ayla(
            game.assets.get(Assets.AYLA_RUN),
            game.assets.get(Assets.AYLA_IDLE),
            game.assets.get(Assets.AYLA_JUMP),
            game.assets.get(Assets.BULLET),
            INTRO_START_X,
            GROUND_Y
        );

        // Controls
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

        // Parallax
        Texture wallTex   = game.assets.get(Assets.PYR_WALL);
        Texture groundTex = game.assets.get(Assets.PYR_GROUND);
        parallax = new ParallaxPyramid(wallTex, groundTex, 0.75f, 0.90f);

        // Mummy textures manual (si lo metes en AssetManager, quita esto y NO dispose aquí)
        mummyIdle = new Texture(Gdx.files.internal("enemies/mummy/mummy_idle.png"));
        mummyWalk = new Texture(Gdx.files.internal("enemies/mummy/mummy_walk.png"));
        mummyHurt = new Texture(Gdx.files.internal("enemies/mummy/mummy_hurt.png"));
        mummyDead = new Texture(Gdx.files.internal("enemies/mummy/mummy_dead.png"));
    }

    @Override
    public void show() {
        game.audio.playMusic(Assets.MUS_PYRAMID_THEME, true);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        updateLogic(delta);

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // Parallax fluido (basado en cámara)
        parallax.render(game.batch, camera, viewport.getWorldWidth(), viewport.getWorldHeight());

        // Boss
        if (mummySpawned && mummy != null && !mummy.isGone()) mummy.draw(game.batch);

        // Ayla
        ayla.draw(game.batch, computeMovingVisual());

        // UI
        controls.updateLayout(camera, viewport);
        controls.draw(game.batch);

        game.batch.end();
    }

    private void updateLogic(float delta) {
        float viewportW = viewport.getWorldWidth();

        boolean leftKey  = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean rightKey = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);

        boolean jumpKey = Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.W)
            || Gdx.input.isKeyJustPressed(Input.Keys.UP);

        boolean shootKey = Gdx.input.isKeyPressed(Input.Keys.K)
            || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);

        boolean left  = leftKey  || controls.leftPressed;
        boolean right = rightKey || controls.rightPressed;

        boolean jump  = jumpKey  || controls.jumpPressed;
        boolean shoot = shootKey || controls.shootPressed;

        float camLeft  = camera.position.x - viewportW / 2f;
        float camRight = camera.position.x + viewportW / 2f;

        if (phase == Phase.INTRO_ENTER) {
            // Cámara y mundo quietos (sin scroll)
            scrollX = 0f;
            camera.position.x = viewportW / 2f;
            camera.update();

            float ax = ayla.getX();
            if (ax < INTRO_TARGET_X) {
                ax += INTRO_WALK_SPEED * delta;
                if (ax > INTRO_TARGET_X) ax = INTRO_TARGET_X;
                ayla.setX(ax);
                ayla.update(delta, false, true, false, false, GROUND_Y, camLeft, camRight);
            } else {
                phase = Phase.PLAY;
                playStartScrollX = scrollX;
            }
            return;
        }

        if (phase == Phase.PLAY) {
            // Scroll normal mientras juegas
            if (right) scrollX += SCROLL_SPEED_FORWARD * delta;
            if (left)  scrollX -= SCROLL_SPEED_BACK * delta;
            if (scrollX < 0f) scrollX = 0f;

            // Cámara sigue al scroll (suave)
            float targetCamX = scrollX + viewportW / 2f;
            camera.position.x += (targetCamX - camera.position.x) * 10f * delta;
            camera.update();

            camLeft  = camera.position.x - viewportW / 2f;
            camRight = camera.position.x + viewportW / 2f;

            // Ayla fija en pantalla (runner)
            ayla.setX(camLeft + INTRO_TARGET_X);
            ayla.update(delta, left, right, jump, shoot, GROUND_Y, camLeft, camRight);

            // Spawn momia tras avanzar un poco
            if (!mummySpawned && (scrollX - playStartScrollX) >= BOSS_SPAWN_AFTER_SCROLL) {
                spawnMummyAtRightEdge();
            }

            // Si existe momia, se actualiza mientras juegas normal (NO pantalla fija)
            if (mummySpawned && mummy != null && !mummy.isGone()) {
                mummy.update(delta, ayla.getX(), GROUND_Y);
                handleAylaBulletsVsMummy();
            }

            // Si la matas -> auto
            if (mummySpawned && mummy != null && mummy.isGone()) {
                phase = Phase.AUTO_TO_TREASURE;
            }

            return;
        }

        if (phase == Phase.AUTO_TO_TREASURE) {
            // Scroll auto hasta tesoro
            if (scrollX < TREASURE_SCROLL_X) {
                scrollX += AUTO_SCROLL_SPEED * delta;
                if (scrollX > TREASURE_SCROLL_X) scrollX = TREASURE_SCROLL_X;
            } else {
                phase = Phase.END;
            }

            float targetCamX = scrollX + viewportW / 2f;
            camera.position.x += (targetCamX - camera.position.x) * 10f * delta;
            camera.update();

            camLeft  = camera.position.x - viewportW / 2f;
            camRight = camera.position.x + viewportW / 2f;

            // Ayla camina sola (visual)
            ayla.setX(ayla.getX() + AYLA_AUTO_WALK_SPEED * delta);
            ayla.update(delta, false, true, false, false, GROUND_Y, camLeft, camRight);

            return;
        }

        // END
        // Aquí luego pones pantalla de tesoro cuando tengas assets
        // game.setScreen(new TreasureScreen(game));
    }

    private void spawnMummyAtRightEdge() {
        mummySpawned = true;

        float viewportW = viewport.getWorldWidth();
        float camRight = camera.position.x + viewportW / 2f;

        // aparece en el borde derecho (un poco dentro)
        float mummyX = camRight - 260f;
        mummy = new Mummy(mummyIdle, mummyWalk, mummyHurt, mummyDead, mummyX, GROUND_Y);
        mummy.startFight();
    }

    private void handleAylaBulletsVsMummy() {
        if (mummy == null || mummy.isDead() || mummy.isGone()) return;

        Array<Bullet> bullets = ayla.getBullets();
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            if (!b.isAlive()) continue;

            if (b.getBounds().overlaps(mummy.getBounds())) {
                b.kill();
                mummy.hitByAylaBullet();
            }
        }
    }

    private boolean computeMovingVisual() {
        if (phase == Phase.INTRO_ENTER) return true;
        if (phase == Phase.AUTO_TO_TREASURE) return true;

        boolean leftKey  = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean rightKey = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);

        boolean left  = leftKey  || controls.leftPressed;
        boolean right = rightKey || controls.rightPressed;

        return (left ^ right);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        controls.updateLayout(camera, viewport);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        // wall/ground vienen del AssetManager -> NO dispose aquí

        // momia manual -> dispose
        if (mummyIdle != null) mummyIdle.dispose();
        if (mummyWalk != null) mummyWalk.dispose();
        if (mummyHurt != null) mummyHurt.dispose();
        if (mummyDead != null) mummyDead.dispose();
    }
}
