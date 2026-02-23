package com.carmen.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
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
import com.carmen.mijuego.projectiles.MummyBulletSystem;
import com.carmen.mijuego.ui.CurseParticles;
import com.carmen.mijuego.world.ParallaxPyramid;

public class PyramidScreen implements Screen {

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    private static final float GROUND_Y = 90f;

    // ===== INTRO =====
    private static final float INTRO_START_X = -260f;
    private static final float INTRO_TARGET_X = 220f;
    private static final float INTRO_WALK_SPEED = 280f;

    // ===== PLAY SCROLL =====
    private static final float SCROLL_SPEED_FORWARD = 320f;
    private static final float SCROLL_SPEED_BACK    = 260f;

    // ===== BOSS SPAWN =====
    private static final float BOSS_SPAWN_AFTER_SCROLL = 650f;
    private static final float MUMMY_SPAWN_OUTSIDE_PAD = 220f;

    // ✅ Ayla anda MÁS tras matar (para dejar la momia atrás)
    private static final float AFTER_BOSS_DISTANCE = 700f;
    private static final float AFTER_BOSS_SPEED    = 240f;

    // ===== TREASURE (mundo) =====
    private static final float TREASURE_WORLD_X = 3050f;

    private static final float TREASURE_W = 380f;
    private static final float TREASURE_H = 380f;

    private static final float TREASURE_Y_OFFSET = -75f;

    private static final float TREASURE_REVEAL_PAD = 20f;

    // ===== AUTO hacia el cofre (antes del lock) =====
    private static final float AUTO_SCROLL_SPEED = 320f;

    // ===== CAMINAR HACIA EL COFRE CON CÁMARA FIJA =====
    private static final float AYLA_TO_CHEST_SPEED = 240f;

    // ===== DESPAWN MOMIA SUAVE =====
    private static final float MUMMY_VISUAL_W_EST = 320f;
    private static final float OFFSCREEN_MARGIN = 30f;

    // ✅ maldición
    private static final float CURSE_TIME = 4.0f;

    private enum Phase {
        INTRO_ENTER,
        PLAY,
        AFTER_BOSS_WALK,
        TREASURE_APPROACH,
        TREASURE_LOCK,
        END
    }
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

    // ✅ Balas momia
    private MummyBulletSystem mummyBullets;

    // ✅ Partículas maldición
    private CurseParticles curseParticles;

    // Treasure
    private Texture treasureTex;
    private final Rectangle treasureBounds = new Rectangle();

    // Mundo
    private float scrollX = 0f;
    private float playStartScrollX = 0f;
    private float afterBossStartScrollX = 0f;

    // lock cámara
    private float lockedCamX = 0f;

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
            game.assets.get(Assets.BULLET_SPECIAL),
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

        // Mummy
        mummyIdle = game.assets.get(Assets.MUMMY_IDLE);
        mummyWalk = game.assets.get(Assets.MUMMY_WALK);
        mummyHurt = game.assets.get(Assets.MUMMY_HURT);
        mummyDead = game.assets.get(Assets.MUMMY_DEAD);

        // Treasure
        treasureTex = game.assets.get(Assets.TREASURE);
        float ty = GROUND_Y + TREASURE_Y_OFFSET;
        treasureBounds.set(TREASURE_WORLD_X, ty, TREASURE_W, TREASURE_H);

        // ✅ sistemas nuevos
        mummyBullets = new MummyBulletSystem(game.assets.get(Assets.BULLET_MUMMY));
        curseParticles = new CurseParticles();
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

        parallax.render(game.batch, camera, viewport.getWorldWidth(), viewport.getWorldHeight());

        drawTreasure();

        if (mummy != null) mummy.draw(game.batch);

        // ✅ dibujar balas momia
        if (mummy != null && !mummy.isDead()) {
            mummyBullets.draw(game.batch);
        }

        ayla.draw(game.batch, computeMovingVisual());

        // ✅ partículas (si hay maldición, se ven orbitando)
        float px = ayla.getX() + ayla.getWidth() * 0.50f;
        float py = ayla.getY() + ayla.getHeight() * 0.55f;
        curseParticles.draw(game.batch, px, py);

        // Controles
        if (phase == Phase.PLAY) {
            controls.updateLayout(camera, viewport);
            controls.draw(
                game.batch,
                ayla.getNormalCooldownPercent(),
                ayla.getSpecialCooldownPercent()
            );
        }

        game.batch.end();
    }

    private void drawTreasure() {
        if (phase != Phase.TREASURE_APPROACH && phase != Phase.TREASURE_LOCK && phase != Phase.END) return;

        float vw = viewport.getWorldWidth();
        float camRight = camera.position.x + vw / 2f;

        if (phase == Phase.TREASURE_APPROACH) {
            if (camRight < (TREASURE_WORLD_X - TREASURE_REVEAL_PAD)) return;
        }

        float tx = TREASURE_WORLD_X;
        float ty = GROUND_Y + TREASURE_Y_OFFSET;

        // invertido (mira izquierda)
        game.batch.draw(treasureTex, tx + TREASURE_W, ty, -TREASURE_W, TREASURE_H);
    }

    private void updateLogic(float delta) {
        float viewportW = viewport.getWorldWidth();

        // Inputs
        boolean leftKey  = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean rightKey = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);

        boolean jumpKey = Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.W)
            || Gdx.input.isKeyJustPressed(Input.Keys.UP);

        boolean shootKey = Gdx.input.isKeyPressed(Input.Keys.K)
            || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);

        boolean grenadeKey = Gdx.input.isKeyJustPressed(Input.Keys.L);

        boolean left  = leftKey  || controls.leftPressed;
        boolean right = rightKey || controls.rightPressed;

        boolean jump  = jumpKey  || controls.jumpPressed;
        boolean shoot = shootKey || controls.shootPressed;

        boolean grenade = grenadeKey || controls.grenadePressed;

        // =========================
        // INTRO (cámara fija)
        // =========================
        if (phase == Phase.INTRO_ENTER) {

            scrollX = 0f;

            camera.position.x = viewportW / 2f;
            camera.update();

            float camLeft  = camera.position.x - viewportW / 2f;
            float camRight = camera.position.x + viewportW / 2f;

            float ax = ayla.getX();
            if (ax < INTRO_TARGET_X) {
                ax += INTRO_WALK_SPEED * delta;
                if (ax > INTRO_TARGET_X) ax = INTRO_TARGET_X;

                ayla.setX(ax);
                ayla.update(delta, false, true, false, false, false, GROUND_Y, camLeft, camRight);

            } else {
                phase = Phase.PLAY;
                playStartScrollX = scrollX;
            }

            // partículas
            updateCurseParticles(delta);
            return;
        }

        // =========================
        // PLAY (jugador controla)
        // =========================
        if (phase == Phase.PLAY) {

            if (right) scrollX += SCROLL_SPEED_FORWARD * delta;
            if (left)  scrollX -= SCROLL_SPEED_BACK * delta;
            if (scrollX < 0f) scrollX = 0f;

            float targetCamX = scrollX + viewportW / 2f;
            camera.position.x += (targetCamX - camera.position.x) * 10f * delta;
            camera.update();

            float camLeft  = camera.position.x - viewportW / 2f;
            float camRight = camera.position.x + viewportW / 2f;

            // ✅ invertir controles si está maldita
            if (ayla.isCursed()) {
                boolean tmp = left;
                left = right;
                right = tmp;
            }

            ayla.setX(camLeft + INTRO_TARGET_X);
            ayla.update(delta, left, right, jump, shoot, grenade, GROUND_Y, camLeft, camRight);

            // Spawn boss
            if (!mummySpawned && (scrollX - playStartScrollX) >= BOSS_SPAWN_AFTER_SCROLL) {
                spawnMummyOutsideRight();
            }

            if (mummySpawned && mummy != null && !mummy.isDead()) {
                mummy.update(delta, ayla.getX(), GROUND_Y);

                // ✅ balas momia
                mummyBullets.update(delta, camLeft, camRight, mummy);

                // colisiones
                handleAylaBulletsVsMummy();
                handleMummyBulletsVsAyla();
            }

            if (mummySpawned && mummy != null && mummy.isDead()) {
                phase = Phase.AFTER_BOSS_WALK;
                afterBossStartScrollX = scrollX;

                // limpiar balas
                mummyBullets.clear();
            }

            updateCurseParticles(delta);
            return;
        }

        // =========================
        // AFTER_BOSS_WALK
        // =========================
        if (phase == Phase.AFTER_BOSS_WALK) {

            float targetScroll = afterBossStartScrollX + AFTER_BOSS_DISTANCE;
            if (scrollX < targetScroll) {
                scrollX += AFTER_BOSS_SPEED * delta;
                if (scrollX > targetScroll) scrollX = targetScroll;
            } else {
                phase = Phase.TREASURE_APPROACH;
            }

            float targetCamX = scrollX + viewportW / 2f;
            camera.position.x += (targetCamX - camera.position.x) * 10f * delta;
            camera.update();

            float camLeft  = camera.position.x - viewportW / 2f;
            float camRight = camera.position.x + viewportW / 2f;

            ayla.setX(camLeft + INTRO_TARGET_X);
            ayla.update(delta, false, true, false, false, false, GROUND_Y, camLeft, camRight);

            despawnMummyIfFullyOffscreenLeft(camLeft);

            updateCurseParticles(delta);
            return;
        }

        // =========================
        // TREASURE_APPROACH
        // =========================
        if (phase == Phase.TREASURE_APPROACH) {

            scrollX += AUTO_SCROLL_SPEED * delta;

            float targetCamX = scrollX + viewportW / 2f;
            camera.position.x += (targetCamX - camera.position.x) * 10f * delta;
            camera.update();

            float camLeft  = camera.position.x - viewportW / 2f;
            float camRight = camera.position.x + viewportW / 2f;

            ayla.setX(camLeft + INTRO_TARGET_X);
            ayla.update(delta, false, true, false, false, false, GROUND_Y, camLeft, camRight);

            despawnMummyIfFullyOffscreenLeft(camLeft);

            boolean treasureFullyVisible =
                (camLeft <= TREASURE_WORLD_X) &&
                    (camRight >= (TREASURE_WORLD_X + TREASURE_W));

            if (treasureFullyVisible) {
                phase = Phase.TREASURE_LOCK;
                lockedCamX = camera.position.x;
            }

            updateCurseParticles(delta);
            return;
        }

        // =========================
        // TREASURE_LOCK
        // =========================
        if (phase == Phase.TREASURE_LOCK) {

            camera.position.x = lockedCamX;
            camera.update();

            float camLeft  = camera.position.x - viewportW / 2f;
            float camRight = camera.position.x + viewportW / 2f;

            ayla.setX(ayla.getX() + AYLA_TO_CHEST_SPEED * delta);
            ayla.update(delta, false, true, false, false, false, GROUND_Y, camLeft, camRight);

            if (ayla.getBounds().overlaps(treasureBounds)) {
                phase = Phase.END;
                game.setScreen(new VictoryScreen(game));
            }

            updateCurseParticles(delta);
            return;
        }
    }

    private void updateCurseParticles(float delta) {
        float cx = ayla.getX() + ayla.getWidth() * 0.50f;
        float cy = ayla.getY() + ayla.getHeight() * 0.55f;
        curseParticles.update(delta, cx, cy, ayla.isCursed());
    }

    private void handleMummyBulletsVsAyla() {
        if (mummy == null || mummy.isDead()) return;

        Array<Bullet> bullets = mummyBullets.getBullets();
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            if (!b.isAlive()) continue;

            if (b.getBounds().overlaps(ayla.getBounds())) {
                b.kill();

                // ✅ aplica maldición
                ayla.applyCurse(CURSE_TIME);
            }
        }
    }

    private void despawnMummyIfFullyOffscreenLeft(float camLeft) {
        if (mummy == null) return;

        float mummyRight = mummy.getX() + MUMMY_VISUAL_W_EST;
        if (mummyRight < (camLeft - OFFSCREEN_MARGIN)) {
            mummy = null;
        }
    }

    private void spawnMummyOutsideRight() {
        mummySpawned = true;

        float viewportW = viewport.getWorldWidth();
        float camRight = camera.position.x + viewportW / 2f;

        float mummyX = camRight + MUMMY_SPAWN_OUTSIDE_PAD;
        mummy = new Mummy(mummyIdle, mummyWalk, mummyHurt, mummyDead, mummyX, GROUND_Y);
        mummy.startFight();
    }

    private void handleAylaBulletsVsMummy() {
        if (mummy == null || mummy.isDead()) return;

        Array<Bullet> bullets = ayla.getBullets();
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            if (!b.isAlive()) continue;

            if (b.getBounds().overlaps(mummy.getBounds())) {
                b.kill();

                // ✅ daño: normal 1 / especial 2
                for (int d = 0; d < b.getDamage(); d++) {
                    mummy.hitByAylaBullet();
                }
            }
        }
    }

    private boolean computeMovingVisual() {
        if (phase == Phase.INTRO_ENTER) return true;
        if (phase == Phase.AFTER_BOSS_WALK) return true;
        if (phase == Phase.TREASURE_APPROACH) return true;
        if (phase == Phase.TREASURE_LOCK) return true;

        boolean leftKey  = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean rightKey = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);

        boolean left  = leftKey  || controls.leftPressed;
        boolean right = rightKey || controls.rightPressed;

        // (visual) si quieres que el “curse” afecte a animación, no hace falta
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
        if (controls != null) controls.dispose();
        if (curseParticles != null) curseParticles.dispose();
    }
}
