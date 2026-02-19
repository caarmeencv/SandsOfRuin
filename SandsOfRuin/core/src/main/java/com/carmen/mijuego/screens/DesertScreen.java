package com.carmen.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.carmen.mijuego.Main;
import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.characters.Ayla;
import com.carmen.mijuego.enemies.Cactus;
import com.carmen.mijuego.enemies.Soldier;
import com.carmen.mijuego.enemies.Tank;
import com.carmen.mijuego.input.Controls;
import com.carmen.mijuego.projectiles.Bullet;
import com.carmen.mijuego.projectiles.TankBulletSystem;
import com.carmen.mijuego.ui.LivesHUD;
import com.carmen.mijuego.world.CactusManager;
import com.carmen.mijuego.world.EnemyManager;
import com.carmen.mijuego.world.LevelConfig;
import com.carmen.mijuego.world.ParallaxBackground;
import com.carmen.mijuego.world.SpawnDirector;
import com.carmen.mijuego.combat.CollisionSystem;

public class DesertScreen implements Screen {

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    private static final float GROUND_Y = 180f;

    private static final float SCROLL_SPEED_FORWARD = 320f;
    private static final float SCROLL_SPEED_BACK    = 260f;

    private static final float AYLA_SCREEN_X = 220f;

    private static final float PARALLAX_MUL = 0.70f;

    private static final float KNOCKBACK_DISTANCE = 140f;
    private static final float KNOCKBACK_SPEED    = 900f;

    private static final float GLOBAL_SPAWN_GAP = 260f;

    private final Main game;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture sky, clouds, ruins, mid, near;

    // Decoración final (AssetManager)
    private Texture sphinxTex;
    private Texture entranceTex;

    private float sphinxX;
    private float entranceX;

    private static final float SPHINX_Y   = GROUND_Y;
    private static final float ENTRANCE_Y = GROUND_Y - 10f;

    private static final float SPHINX_SCALE   = 0.55f;
    private static final float ENTRANCE_SCALE = 0.60f;

    private static final float DECOR_GAP = 420f;

    // Cutscene “Ayla entra”
    private boolean inCutscene = false;
    private boolean enteredPhase5Once = false;

    private static final float AUTO_WALK_SPEED = 260f;
    private static final float CUTSCENE_TRIGGER_AHEAD = 520f;

    private Ayla ayla;
    private Controls controls;
    private ParallaxBackground parallax;

    private CactusManager cactusManager;
    private EnemyManager enemyManager;
    private TankBulletSystem tankBulletSystem;
    private CollisionSystem collisionSystem;

    private SpawnDirector spawnDirector;

    private LivesHUD livesHUD;

    private float scrollX = 0f;
    private float knockRemaining = 0f;

    private ShapeRenderer shapeRenderer;
    private boolean debugHitboxes = true;

    // ✅ CONTADOR (tiempo de nivel)
    private float levelTimer = 0f;

    // ✅ Pause (para que no se dispare muchas veces al mantener)
    private boolean pauseLatch = false;

    public DesertScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply();

        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0f);
        camera.update();

        sky    = game.assets.get(Assets.SKY);
        clouds = game.assets.get(Assets.CLOUDS);
        ruins  = game.assets.get(Assets.RUINS);
        mid    = game.assets.get(Assets.MID);
        near   = game.assets.get(Assets.NEAR);

        sphinxTex   = game.assets.get(Assets.SPHINX_PYRAMID);
        entranceTex = game.assets.get(Assets.ENTRANCE_PYRAMID);

        ayla = new Ayla(
            game.assets.get(Assets.AYLA_RUN),
            game.assets.get(Assets.AYLA_IDLE),
            game.assets.get(Assets.AYLA_JUMP),
            game.assets.get(Assets.BULLET),
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

        float cloudsH = scaledHeight(clouds);
        float ruinsH  = scaledHeight(ruins);
        float midH    = scaledHeight(mid);
        float nearH   = scaledHeight(near);

        parallax = new ParallaxBackground(
            camera,
            viewport,
            sky,
            new Texture[]{ clouds, ruins, mid, near },
            new float[]  { 0.08f, 0.15f, 0.30f, 1.00f },
            new float[]  { 0f,    0f,    0f,    0f },
            new float[]  { cloudsH, ruinsH, midH, nearH },
            new boolean[]{ false, false, false, true }
        );
        parallax.setSpeedMul(PARALLAX_MUL);

        livesHUD = new LivesHUD(game.assets.get(Assets.HUD_HEART_FULL));

        cactusManager = new CactusManager(
            game.assets.get(Assets.CACTUS_PINK),
            game.assets.get(Assets.CACTUS_YELLOW)
        );

        enemyManager = new EnemyManager(
            game.assets.get(Assets.SOLDIER_IDLE),
            game.assets.get(Assets.SOLDIER_RUN),
            game.assets.get(Assets.SOLDIER_HURT),
            game.assets.get(Assets.SOLDIER_DEAD),
            game.assets.get(Assets.BULLET),
            game.assets.get(Assets.TANK_IDLE),
            game.assets.get(Assets.TANK_MOVE),
            game.assets.get(Assets.TANK_DESTROY),
            game.assets.get(Assets.TANK_DEAD),
            GROUND_Y
        );

        tankBulletSystem = new TankBulletSystem(game.assets.get(Assets.BULLET));
        collisionSystem = new CollisionSystem();

        shapeRenderer = new ShapeRenderer();

        float viewportW = viewport.getWorldWidth();
        float margin = 500f;

        spawnDirector = new SpawnDirector(
            cactusManager,
            enemyManager,
            viewportW,
            margin,
            GLOBAL_SPAWN_GAP
        );

        float camRight = camera.position.x + viewport.getWorldWidth() / 2f;
        spawnDirector.reset(camRight + 40f);

        sphinxX = LevelConfig.F4_END + 1300f;
        entranceX = sphinxX + DECOR_GAP;
    }

    private float scaledHeight(Texture tex) {
        float scale = WORLD_W / (float) tex.getWidth();
        return tex.getHeight() * scale;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        // ✅ contador (si quieres que NO cuente en cutscene, cambia a: if(!inCutscene) levelTimer += delta;)
        levelTimer += delta;
        controls.setCounterText(formatTime(levelTimer));

        float maxScrollX = LevelConfig.DESERT_LENGTH - viewport.getWorldWidth();
        if (maxScrollX < 0f) maxScrollX = 0f;

        // ==========================================================
        // INPUT: móvil (controls.*) + ordenador (A/D/W/SPACE/K/L)
        // ==========================================================

        boolean leftKey  = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean rightKey = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);

        // salto: SPACE o W o Flecha Arriba (si quieres)
        boolean jumpKey = Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.W)
            || Gdx.input.isKeyJustPressed(Input.Keys.UP);

        // disparo: K (ordenador) o CTRL (si lo usabas)
        boolean shootKey = Gdx.input.isKeyPressed(Input.Keys.K)
            || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);

        // especial: L
        boolean grenadeKey = Gdx.input.isKeyJustPressed(Input.Keys.L);

        // pause: botón móvil o ESC
        boolean pauseKey = Gdx.input.isKeyPressed(Input.Keys.ESCAPE);

        // combinamos con móvil
        boolean left  = leftKey  || controls.leftPressed;
        boolean right = rightKey || controls.rightPressed;

        // ✅ jumpPressed de móvil suele ser “hold”; lo convertimos a “just pressed” con latch
        // (si prefieres hold, quita este latch y usa: jumpKey || controls.jumpPressed)
        boolean jump = jumpKey || controls.jumpPressed;

        boolean shoot = shootKey || controls.shootPressed;
        boolean grenade = grenadeKey || controls.grenadePressed;

        boolean pauseNow = pauseKey || controls.pausePressed;

        // ✅ latch para pause (para que no se active 60 veces por segundo)
        if (pauseNow && !pauseLatch) {
            pauseLatch = true;
            // Aquí tu lógica de pausa: abrir pantalla pause / toggle, etc.
            // Ejemplo: game.setScreen(new PauseScreen(game, this));  (si tienes)
            // O un boolean paused = !paused;
        }
        if (!pauseNow) {
            pauseLatch = false;
        }

        // cámara bounds
        float camLeft  = camera.position.x - viewport.getWorldWidth() / 2f;
        float camRight = camera.position.x + viewport.getWorldWidth() / 2f;

        // fase actual
        LevelConfig.Phase phase = LevelConfig.phaseFor(camRight);

        // al entrar en F5: limpiar TODO
        if (phase == LevelConfig.Phase.F5_DECOR_NO_ENEMIES && !enteredPhase5Once) {
            enteredPhase5Once = true;

            cactusManager.clear();
            enemyManager.clear();
            tankBulletSystem.clear();

            knockRemaining = 0f;
        }

        // activar cutscene cuando la entrada entra en rango
        if (!inCutscene && phase == LevelConfig.Phase.F5_DECOR_NO_ENEMIES) {
            if (entranceX < camRight + CUTSCENE_TRIGGER_AHEAD) {
                inCutscene = true;

                controls.leftPressed = false;
                controls.rightPressed = false;
                controls.jumpPressed = false;
                controls.shootPressed = false;
                controls.grenadePressed = false;
                controls.pausePressed = false;
            }
        }

        // ===================== MOVIMIENTO (normal vs cutscene) =====================
        if (inCutscene) {
            scrollX += AUTO_WALK_SPEED * delta;
            if (scrollX > maxScrollX) scrollX = maxScrollX;

        } else {
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

            if (scrollX > maxScrollX) scrollX = maxScrollX;
        }

        // cámara sigue a scrollX
        float targetCamX = scrollX + viewport.getWorldWidth() / 2f;
        camera.position.x += (targetCamX - camera.position.x) * 10f * delta;
        camera.update();

        camLeft  = camera.position.x - viewport.getWorldWidth() / 2f;
        camRight = camera.position.x + viewport.getWorldWidth() / 2f;
        float camTop = camera.position.y + viewport.getWorldHeight() / 2f;

        controls.updateLayout(camera, viewport);

        // fase actual actualizada
        phase = LevelConfig.phaseFor(camRight);

        // Ayla siempre anclada a pantalla
        ayla.setX(camLeft + AYLA_SCREEN_X);

        boolean movingVisual;
        if (inCutscene) {
            movingVisual = true;
            ayla.update(delta, false, true, false, false, GROUND_Y, camLeft, camRight);
        } else {
            movingVisual = (left ^ right);

            // ✅ granada: si tu Ayla.update no tiene parámetro, ignora "grenade"
            // aquí no lo paso porque tu firma actual es: update(delta,left,right,jump,shoot,...)
            ayla.update(delta, left, right, jump, shoot, GROUND_Y, camLeft, camRight);
        }

        // ===================== SPAWNS / UPDATES =====================
        if (!inCutscene) {
            spawnDirector.update(phase, camLeft, camRight);
        }

        cactusManager.update(camLeft);
        enemyManager.update(delta, ayla.getX(), camLeft, camRight);
        tankBulletSystem.update(delta, camLeft, camRight, enemyManager.getTanks());

        if (!inCutscene) {
            collisionSystem.update(
                delta,
                ayla,
                cactusManager.getCactuses(),
                enemyManager.getSoldiers(),
                enemyManager.getTanks(),
                tankBulletSystem.getBullets(),
                () -> knockRemaining = KNOCKBACK_DISTANCE
            );
        }

        // ===================== DRAW =====================
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        parallax.render(game.batch, scrollX);

        if (phase == LevelConfig.Phase.F5_DECOR_NO_ENEMIES) {
            drawSphinxIfVisible(camLeft, camRight);
        }

        for (Cactus c : cactusManager.getCactuses()) c.draw(game.batch);
        for (Soldier s : enemyManager.getSoldiers()) s.draw(game.batch);
        for (Tank t : enemyManager.getTanks()) t.draw(game.batch);

        tankBulletSystem.draw(game.batch);

        ayla.draw(game.batch, movingVisual);

        if (phase == LevelConfig.Phase.F5_DECOR_NO_ENEMIES) {
            drawEntranceIfVisible(camLeft, camRight);
        }

        livesHUD.draw(game.batch, camLeft, camTop);

        // ✅ controles visibles fuera de cutscene
        if (!inCutscene) {
            controls.draw(game.batch);
        }

        game.batch.end();

        if (debugHitboxes) debugRender();
    }

    private String formatTime(float seconds) {
        int total = (int) seconds;
        int min = total / 60;
        int sec = total % 60;
        return String.format("%02d:%02d", min, sec);
    }

    private void drawSphinxIfVisible(float camLeft, float camRight) {
        if (sphinxTex == null) return;

        float w = sphinxTex.getWidth() * SPHINX_SCALE;
        float h = sphinxTex.getHeight() * SPHINX_SCALE;

        if (sphinxX + w < camLeft - 300f) return;
        if (sphinxX > camRight + 300f) return;

        game.batch.draw(sphinxTex, sphinxX, SPHINX_Y, w, h);
    }

    private void drawEntranceIfVisible(float camLeft, float camRight) {
        if (entranceTex == null) return;

        float w = entranceTex.getWidth() * ENTRANCE_SCALE;
        float h = entranceTex.getHeight() * ENTRANCE_SCALE;

        if (entranceX + w < camLeft - 300f) return;
        if (entranceX > camRight + 300f) return;

        game.batch.draw(entranceTex, entranceX, ENTRANCE_Y, w, h);
    }

    private void debugRender() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(
            ayla.getBounds().x,
            ayla.getBounds().y,
            ayla.getBounds().width,
            ayla.getBounds().height
        );

        shapeRenderer.setColor(Color.GREEN);
        Array<Cactus> cactuses = cactusManager.getCactuses();
        for (int i = 0; i < cactuses.size; i++) {
            shapeRenderer.rect(
                cactuses.get(i).getBounds().x,
                cactuses.get(i).getBounds().y,
                cactuses.get(i).getBounds().width,
                cactuses.get(i).getBounds().height
            );
        }

        Array<Soldier> soldiers = enemyManager.getSoldiers();
        for (int i = 0; i < soldiers.size; i++) {
            Soldier s = soldiers.get(i);

            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(
                s.getBounds().x,
                s.getBounds().y,
                s.getBounds().width,
                s.getBounds().height
            );

            shapeRenderer.setColor(Color.YELLOW);
            Array<Bullet> sb = s.getBullets();
            for (int b = 0; b < sb.size; b++) {
                Bullet bb = sb.get(b);
                shapeRenderer.rect(
                    bb.getBounds().x,
                    bb.getBounds().y,
                    bb.getBounds().width,
                    bb.getBounds().height
                );
            }
        }

        shapeRenderer.setColor(Color.MAGENTA);
        Array<Tank> tanks = enemyManager.getTanks();
        for (int i = 0; i < tanks.size; i++) {
            Tank t = tanks.get(i);
            shapeRenderer.rect(
                t.getBounds().x,
                t.getBounds().y,
                t.getBounds().width,
                t.getBounds().height
            );
        }

        shapeRenderer.setColor(Color.ORANGE);
        Array<Bullet> tb = tankBulletSystem.getBullets();
        for (int i = 0; i < tb.size; i++) {
            Bullet bb = tb.get(i);
            shapeRenderer.rect(
                bb.getBounds().x,
                bb.getBounds().y,
                bb.getBounds().width,
                bb.getBounds().height
            );
        }

        shapeRenderer.end();
    }

    @Override
    public void resize(int w, int h) {
        viewport.update(w, h, true);
        controls.updateLayout(camera, viewport);
    }

    @Override
    public void show() {
        game.audio.playMusic(Assets.MUS_DESERT_THEME, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
        // ✅ NO disposes textures del AssetManager
        // Si quieres liberar font del Controls al salir:
        // controls.dispose();
    }
}
