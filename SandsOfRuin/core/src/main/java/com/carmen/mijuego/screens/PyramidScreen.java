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
import com.carmen.mijuego.input.AccelerometerJumpDetector;
import com.carmen.mijuego.Main;
import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.characters.Ayla;
import com.carmen.mijuego.enemies.Mummy;
import com.carmen.mijuego.input.Controls;
import com.carmen.mijuego.projectiles.Bullet;
import com.carmen.mijuego.projectiles.MummyBulletSystem;
import com.carmen.mijuego.ui.CurseParticles;
import com.carmen.mijuego.ui.LivesHUD;
import com.carmen.mijuego.world.ParallaxPyramid;

public class PyramidScreen implements Screen {

    private boolean snapCameraOnResume = false;
    private float resumeImmunity = 0f;
    private static final float RESUME_IMMUNITY_TIME = 2.0f;
    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    private static final float GROUND_Y = 90f;
    // ✅ salto por acelerómetro
    private final AccelerometerJumpDetector accelJump = new AccelerometerJumpDetector();
    private static final float INTRO_START_X = -260f;
    private static final float INTRO_TARGET_X = 220f;
    private static final float INTRO_WALK_SPEED = 280f;

    private static final float SCROLL_SPEED_FORWARD = 320f;
    private static final float SCROLL_SPEED_BACK    = 260f;

    private static final float KNOCKBACK_DISTANCE = 140f;
    private static final float KNOCKBACK_SPEED    = 900f;
    private float knockRemaining = 0f;

    private static final float MUMMY_HIT_DELAY = 0.55f;
    private float mummyHitCooldown = 0f;

    private static final float BOSS_SPAWN_AFTER_SCROLL = 650f;
    private static final float MUMMY_SPAWN_OUTSIDE_PAD = 220f;

    private static final float AFTER_BOSS_DISTANCE = 700f;
    private static final float AFTER_BOSS_SPEED    = 240f;

    private static final float TREASURE_WORLD_X = 3050f;
    private static final float TREASURE_W = 220f;
    private static final float TREASURE_H = 220f;
    private static final float TREASURE_Y_OFFSET = -25f;
    private static final float TREASURE_REVEAL_PAD = 20f;

    private static final float AUTO_SCROLL_SPEED = 320f;
    private static final float AYLA_TO_CHEST_SPEED = 240f;

    private static final float MUMMY_VISUAL_W_EST = 320f;
    private static final float OFFSCREEN_MARGIN = 30f;

    private static final float CURSE_TIME = 4.0f;

    private boolean pauseLatch = false;

    // ✅ LÍMITE GLOBAL DE VIDAS
    private static final int MAX_LIVES = 5;

    // =========================
    // ✅ ITEMS: CORAZONES EN SUELO
    // =========================
    private static final float HEART_W = 54f;
    private static final float HEART_H = 54f;
    private static final float HEART_Y = GROUND_Y + 8f;

    // Posiciones en mundo (zona inicial jugable)
    private static final float HEART1_WORLD_X = 520f;
    private static final float HEART2_WORLD_X = 720f;

    private Texture heartItemTex;
    private final Array<HeartItem> heartItems = new Array<>();

    private static class HeartItem {
        float x, y, w, h;
        boolean collected = false;
        Rectangle bounds = new Rectangle();

        HeartItem(float x, float y, float w, float h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            bounds.set(x, y, w, h);
        }

        void updateBounds() {
            bounds.set(x, y, w, h);
        }
    }

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

    private Texture mummyIdle, mummyWalk, mummyHurt, mummyDead;
    private Mummy mummy;
    private boolean mummySpawned = false;

    private MummyBulletSystem mummyBullets;
    private CurseParticles curseParticles;

    private LivesHUD livesHUD;

    private Texture treasureTex;
    private final Rectangle treasureBounds = new Rectangle();

    private float scrollX = 0f;
    private float playStartScrollX = 0f;
    private float afterBossStartScrollX = 0f;

    private float lockedCamX = 0f;

    private float dizzyTime = 0f;
    private float baseCamY;
    private float baseZoom;

    public PyramidScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply();

        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0f);
        camera.update();

        baseCamY = camera.position.y;
        baseZoom = camera.zoom;

        ayla = new Ayla(
            game.audio,
            game.assets.get(Assets.AYLA_RUN),
            game.assets.get(Assets.AYLA_IDLE),
            game.assets.get(Assets.AYLA_JUMP),
            game.assets.get(Assets.BULLET),
            game.assets.get(Assets.BULLET_SPECIAL),
            INTRO_START_X,
            GROUND_Y
        );

        // ✅ NO regalamos vidas aquí. Solo sincronizamos lo que traigas:
        game.vidas = clampLives(game.vidas);
        ayla.setLives(game.vidas);

        controls = new Controls(game.audio, viewport,
            game.assets.get(Assets.UI_LEFT),
            game.assets.get(Assets.UI_RIGHT),
            game.assets.get(Assets.UI_JUMP),
            game.assets.get(Assets.UI_SHOOT),
            game.assets.get(Assets.UI_GRENADE),
            game.assets.get(Assets.UI_PAUSE));
        Gdx.input.setInputProcessor(controls);
        controls.updateLayout(camera, viewport);

        Texture wallTex   = game.assets.get(Assets.PYR_WALL);
        Texture groundTex = game.assets.get(Assets.PYR_GROUND);
        parallax = new ParallaxPyramid(wallTex, groundTex, 0.75f, 0.90f);

        mummyIdle = game.assets.get(Assets.MUMMY_IDLE);
        mummyWalk = game.assets.get(Assets.MUMMY_WALK);
        mummyHurt = game.assets.get(Assets.MUMMY_HURT);
        mummyDead = game.assets.get(Assets.MUMMY_DEAD);

        treasureTex = game.assets.get(Assets.TREASURE);
        float ty = GROUND_Y + TREASURE_Y_OFFSET;
        treasureBounds.set(TREASURE_WORLD_X, ty, TREASURE_W, TREASURE_H);

        mummyBullets = new MummyBulletSystem(game.audio, game.assets.get(Assets.BULLET_MUMMY));
        curseParticles = new CurseParticles();

        livesHUD = new LivesHUD(
            game.assets.get(Assets.HUD_HEART_FULL),
            game.assets.get(Assets.HUD_HEART_EMPTY)
        );

        // ✅ textura del item corazón (reutilizamos el HUD full)
        heartItemTex = game.assets.get(Assets.HUD_HEART_FULL);

        // ✅ spawnea 2 corazones en el suelo
        spawnStartHearts();
    }

    private void spawnStartHearts() {
        heartItems.clear();
        heartItems.add(new HeartItem(HEART1_WORLD_X, HEART_Y, HEART_W, HEART_H));
        heartItems.add(new HeartItem(HEART2_WORLD_X, HEART_Y, HEART_W, HEART_H));
    }

    @Override
    public void show() {
        game.audio.playMusic(Assets.MUS_PYRAMID_THEME, true);
        Gdx.input.setInputProcessor(controls);
        controls.resetAll();
        pauseLatch = false;

        // ✅ NUEVO
        accelJump.reset();

        // ✅ siempre sincroniza al entrar (sin sumar nada)
        game.vidas = clampLives(game.vidas);
        ayla.setLives(game.vidas);
    }

    @Override
    public void render(float delta) {

        if (resumeImmunity > 0f) {
            resumeImmunity -= delta;
            if (resumeImmunity < 0f) resumeImmunity = 0f;
        }

        ScreenUtils.clear(0, 0, 0, 1);

        game.runTimeSeconds += delta;
        controls.setCounterText(formatTime(game.runTimeSeconds));

        // ===================== PAUSA =====================
        boolean pauseKey = Gdx.input.isKeyPressed(Input.Keys.ESCAPE);
        boolean pauseNow = pauseKey || controls.pausePressed;

        if (pauseNow && !pauseLatch) {
            pauseLatch = true;
            controls.resetAll();

            snapCameraOnResume = true;
            resumeImmunity = RESUME_IMMUNITY_TIME;

            ayla.stopAllLoops();
            game.setScreen(new PauseScreen(game, this, PauseScreen.Context.PYRAMID));
            return;
        }
        if (!pauseNow) pauseLatch = false;
        // ================================================

        updateLogic(delta);

        if (snapCameraOnResume) {
            float targetCamX = scrollX + viewport.getWorldWidth() / 2f;
            camera.position.x = targetCamX;
            camera.update();
            snapCameraOnResume = false;
        }

        applyDizzyCamera(delta);

        float camLeft = camera.position.x - viewport.getWorldWidth() / 2f;
        float camTop  = camera.position.y + viewport.getWorldHeight() / 2f;

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        parallax.render(game.batch, camera, viewport.getWorldWidth(), viewport.getWorldHeight());

        drawHeartItems();
        drawTreasure();

        if (mummy != null) mummy.draw(game.batch);

        if (mummy != null && !mummy.isDead()) {
            mummyBullets.draw(game.batch);
        }

        ayla.draw(game.batch, computeMovingVisual());

        float px = ayla.getX() + ayla.getWidth() * 0.50f;
        float py = ayla.getY() + ayla.getHeight() * 0.55f;
        curseParticles.draw(game.batch, px, py);

        livesHUD.draw(game.batch, camLeft, camTop, ayla.getLives(), phase == Phase.PLAY);

        if (phase == Phase.PLAY) {

            // ✅ NUEVO: activar/desactivar UI según settings (oculta salto / baja granada)
            controls.setAccelJumpEnabled(game.settings.isAccelJumpEnabled());

            controls.updateLayout(camera, viewport);
            controls.draw(
                game.batch,
                ayla.getNormalCooldownPercent(),
                ayla.getSpecialCooldownPercent()
            );
        }

        game.batch.end();
    }

    private void drawHeartItems() {
        if (heartItemTex == null) return;
        if (phase == Phase.INTRO_ENTER) return;

        float vw = viewport.getWorldWidth();
        float camLeft = camera.position.x - vw / 2f;
        float camRight = camera.position.x + vw / 2f;

        for (int i = 0; i < heartItems.size; i++) {
            HeartItem it = heartItems.get(i);
            if (it.collected) continue;

            if (it.x + it.w < camLeft - 100f) continue;
            if (it.x > camRight + 100f) continue;

            game.batch.draw(heartItemTex, it.x, it.y, it.w, it.h);
        }
    }

    private void updateHeartItems() {
        if (phase != Phase.PLAY) return;

        Rectangle aylaB = ayla.getBounds();

        for (int i = 0; i < heartItems.size; i++) {
            HeartItem it = heartItems.get(i);
            if (it.collected) continue;

            it.updateBounds();

            if (aylaB.overlaps(it.bounds)) {
                it.collected = true;

                int before = ayla.getLives();
                int after = clampLives(before + 1); // si estaba a 5, se queda en 5

                ayla.setLives(after);
                game.vidas = after;

                // ✅ SFX al coger vida
                game.audio.playSfx(Assets.SFX_LIVE);
            }
        }
    }

    private void applyDizzyCamera(float delta) {
        if (ayla != null && ayla.isCursed()) {
            dizzyTime += delta;

            float rollDeg = 3.5f;
            float swayX   = 10f;
            float swayY   = 6f;
            float zoomAmp = 0.02f;

            float t1 = dizzyTime * 3.5f;
            float t2 = dizzyTime * 6.0f;

            camera.up.set(0, 1, 0);
            camera.rotate((float) Math.sin(t1) * rollDeg);

            camera.position.x += (float) Math.sin(t2) * swayX;
            camera.position.y = baseCamY + (float) Math.cos(t2 * 0.9f) * swayY;

            camera.zoom = baseZoom + (float) Math.sin(t1 * 1.2f) * zoomAmp;

            camera.update();
        } else {
            dizzyTime = 0f;
            camera.up.set(0, 1, 0);
            camera.zoom = baseZoom;
            camera.position.y = baseCamY;
            camera.update();
        }
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

        game.batch.draw(treasureTex, tx, ty, TREASURE_W, TREASURE_H);
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

        boolean grenadeKey = Gdx.input.isKeyJustPressed(Input.Keys.L);

        // ✅ activar/desactivar UI según settings (oculta salto / baja granada)
        boolean accelMode = game.settings.isAccelJumpEnabled();
        controls.setAccelJumpEnabled(accelMode);

        boolean left  = leftKey  || controls.leftPressed;
        boolean right = rightKey || controls.rightPressed;

        boolean shoot = shootKey || controls.shootPressed;
        boolean grenade = grenadeKey || controls.grenadePressed;

// ✅ SALTO: si accelMode => acelerómetro; si no hay acelerómetro (PC) => fallback a normal
        boolean jump;
        if (accelMode) {
            boolean accelJumpNow = accelJump.updateAndConsume(delta);
            boolean fallbackJump = jumpKey || controls.jumpPressed; // para probar en PC
            jump = accelJumpNow || fallbackJump;
        } else {
            jump = jumpKey || controls.jumpPressed;
        }

        mummyHitCooldown -= delta;
        if (mummyHitCooldown < 0f) mummyHitCooldown = 0f;

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

            updateCurseParticles(delta);
            return;
        }

        if (phase == Phase.PLAY) {

            if (ayla.isCursed()) {
                boolean tmp = left;
                left = right;
                right = tmp;
            }

            if (knockRemaining > 0f) {
                float step = KNOCKBACK_SPEED * delta;
                if (step > knockRemaining) step = knockRemaining;

                scrollX -= step;
                if (scrollX < 0f) scrollX = 0f;

                knockRemaining -= step;
                if (knockRemaining < 0f) knockRemaining = 0f;
            } else {
                if (right) scrollX += SCROLL_SPEED_FORWARD * delta;
                if (left)  scrollX -= SCROLL_SPEED_BACK * delta;
                if (scrollX < 0f) scrollX = 0f;
            }

            float targetCamX = scrollX + viewportW / 2f;

            if (snapCameraOnResume) {
                camera.position.x = targetCamX;
                camera.update();
            } else {
                camera.position.x += (targetCamX - camera.position.x) * 10f * delta;
                camera.update();
            }

            baseCamY = WORLD_H / 2f;

            float camLeft  = camera.position.x - viewportW / 2f;
            float camRight = camera.position.x + viewportW / 2f;

            ayla.setX(camLeft + INTRO_TARGET_X);
            ayla.update(delta, left, right, jump, shoot, grenade, GROUND_Y, camLeft, camRight);

            // ✅ recoger corazones
            updateHeartItems();

            if (!mummySpawned && (scrollX - playStartScrollX) >= BOSS_SPAWN_AFTER_SCROLL) {
                spawnMummyOutsideRight();
            }

            if (mummySpawned && mummy != null && !mummy.isDead()) {
                mummy.update(delta, ayla.getX(), GROUND_Y);

                mummyBullets.update(delta, camLeft, camRight, mummy);

                handleAylaBulletsVsMummy();
                handleMummyBulletsVsAyla();

                handleAylaVsMummyBody();

                game.vidas = clampLives(ayla.getLives());
                ayla.setLives(game.vidas);

                if (ayla.isDead()) {
                    game.vidas = clampLives(ayla.getLives());

                    game.audio.playSfx(Assets.SFX_GAME_OVER);

                    ayla.stopAllLoops();
                    game.setScreen(new GameOverScreen(game));
                    return;
                }
            }

            if (mummySpawned && mummy != null && mummy.isDead()) {
                ayla.clearCurse();
                dizzyTime = 0f;

                phase = Phase.AFTER_BOSS_WALK;
                afterBossStartScrollX = scrollX;
                mummyBullets.clear();
            }

            updateCurseParticles(delta);
            return;
        }

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

            ayla.setX(camLeft + INTRO_TARGET_X);
            ayla.update(delta, false, true, false, false, false, GROUND_Y, camLeft,
                camera.position.x + viewportW / 2f);

            despawnMummyIfFullyOffscreenLeft(camLeft);

            updateCurseParticles(delta);
            return;
        }

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

        if (phase == Phase.TREASURE_LOCK) {

            camera.position.x = lockedCamX;
            camera.update();

            float camLeft  = camera.position.x - viewportW / 2f;
            float camRight = camera.position.x + viewportW / 2f;

            ayla.setX(ayla.getX() + AYLA_TO_CHEST_SPEED * delta);
            ayla.update(delta, false, true, false, false, false, GROUND_Y, camLeft, camRight);

            if (ayla.getBounds().overlaps(treasureBounds)) {
                phase = Phase.END;
                game.vidas = clampLives(ayla.getLives());

                game.audio.playSfx(Assets.SFX_VICTORY);

                ayla.stopAllLoops();
                game.setScreen(new VictoryScreen(game));
                return;
            }

            updateCurseParticles(delta);
        }
    }

    private void updateCurseParticles(float delta) {
        float cx = ayla.getX() + ayla.getWidth() * 0.50f;
        float cy = ayla.getY() + ayla.getHeight() * 0.55f;
        curseParticles.update(delta, cx, cy, ayla.isCursed());
    }

    private void handleAylaVsMummyBody() {
        if (mummy == null || mummy.isDead()) return;
        if (mummyHitCooldown > 0f) return;
        if (resumeImmunity > 0f) return;

        if (ayla.getBounds().overlaps(mummy.getBounds())) {
            knockRemaining = KNOCKBACK_DISTANCE;

            boolean damaged = ayla.takeDamage();
            if (damaged) ayla.applyCurse(CURSE_TIME);

            if (damaged) {
                game.vibrateHit(120);
            }

            game.vidas = clampLives(ayla.getLives());
            ayla.setLives(game.vidas);

            mummyHitCooldown = MUMMY_HIT_DELAY;
        }
    }

    private void handleMummyBulletsVsAyla() {
        if (mummy == null || mummy.isDead()) return;

        Array<Bullet> bullets = mummyBullets.getBullets();
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            if (!b.isAlive()) continue;

            if (resumeImmunity > 0f) return;

            if (b.getBounds().overlaps(ayla.getBounds())) {
                b.kill();

                knockRemaining = KNOCKBACK_DISTANCE;

                boolean damaged = ayla.takeDamage();
                if (damaged) ayla.applyCurse(CURSE_TIME);

                if (damaged) {
                    game.vibrateHit(120);
                }

                game.vidas = clampLives(ayla.getLives());
                ayla.setLives(game.vidas);
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
        mummy = new Mummy(game.audio, mummyIdle, mummyWalk, mummyHurt, mummyDead, mummyX, GROUND_Y);
    }

    private void handleAylaBulletsVsMummy() {
        if (mummy == null || mummy.isDead()) return;

        Array<Bullet> bullets = ayla.getBullets();
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            if (!b.isAlive()) continue;

            if (b.getBounds().overlaps(mummy.getBounds())) {
                b.kill();
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

        return (left ^ right);
    }

    private String formatTime(float seconds) {
        int total = (int) seconds;
        int min = total / 60;
        int sec = total % 60;
        return String.format("%02d:%02d", min, sec);
    }

    private int clampLives(int v) {
        if (v < 0) return 0;
        if (v > MAX_LIVES) return MAX_LIVES;
        return v;
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
