package com.carmen.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
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
import com.carmen.mijuego.ui.LivesHUD;
import com.carmen.mijuego.world.ParallaxBackground;

public class DesertScreen implements Screen {

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    private static final float GROUND_Y = 180f;

    private static final float SCROLL_SPEED_FORWARD = 320f;
    private static final float SCROLL_SPEED_BACK    = 260f;

    private static final float AYLA_SCREEN_X = 220f;

    private static final float PARALLAX_MUL = 0.70f;

    private static final float CACTUS_MIN_DIST = 1400f;
    private static final float CACTUS_MAX_DIST = 2600f;

    private static final float CACTUS_SPAWN_MARGIN = 250f;
    private static final float CACTUS_HEIGHT = 90f;

    // golpe / cooldown
    private static final float HIT_DELAY = 0.55f;

    // knockback (retrocede el mundo)
    private static final float KNOCKBACK_DISTANCE = 140f;
    private static final float KNOCKBACK_SPEED = 900f;

    // SOLDIERS spawn
    private static final float SOLDIER_MIN_DIST = 1800f;
    private static final float SOLDIER_MAX_DIST = 3200f;
    private static final float SOLDIER_SPAWN_MARGIN = 600f;
    private static final float SOLDIER_HEIGHT = CACTUS_HEIGHT;

    // TANK spawn
    private static final float TANK_MIN_DIST = 2400f;
    private static final float TANK_MAX_DIST = 4500f;
    private static final float TANK_SPAWN_MARGIN = 650f;
    private static final float TANK_SPAWN_CHANCE = 0.78f;

    // --- BALAS TANK (grandes) ---
    private static final float TANK_BULLET_W = 90f;
    private static final float TANK_BULLET_H = 45f;
    private static final float TANK_BULLET_SPEED = 700f; // el signo lo ponemos según facing
    private static final float TANK_MUZZLE_Y = 40f;

    private final Main game;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture sky, clouds, ruins, mid, near;
    private Texture cactusPink, cactusYellow;

    private Ayla ayla;
    private Controls controls;
    private ParallaxBackground parallax;

    private final Array<Cactus> cactuses = new Array<>();
    private final Array<Soldier> soldiers = new Array<>();
    private final Array<Tank> tanks = new Array<>();

    // ✅ balas de tanque
    private final Array<Bullet> tankBullets = new Array<>();

    private float nextTankSpawnX = 0f;
    private float scrollX = 0f;
    private float nextSpawnX = 0f;
    private float nextSoldierSpawnX = 0f;

    private LivesHUD livesHUD;

    private float hitCooldown = 0f;
    private float knockRemaining = 0f;

    // debug hitboxes
    private ShapeRenderer shapeRenderer;
    private boolean debugHitboxes = true;

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

        cactusPink   = game.assets.get(Assets.CACTUS_PINK);
        cactusYellow = game.assets.get(Assets.CACTUS_YELLOW);

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

        float camRight = camera.position.x + viewport.getWorldWidth() / 2f;
        nextSpawnX = camRight + 600f;
        nextSoldierSpawnX = camRight + 1200f;
        nextTankSpawnX = camRight + 1600f;

        shapeRenderer = new ShapeRenderer();
    }

    private float scaledHeight(Texture tex) {
        float scale = WORLD_W / (float) tex.getWidth();
        return tex.getHeight() * scale;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        // cooldown golpe
        hitCooldown -= delta;
        if (hitCooldown < 0f) hitCooldown = 0f;

        boolean left  = Gdx.input.isKeyPressed(Input.Keys.LEFT)  || controls.leftPressed;
        boolean right = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || controls.rightPressed;
        boolean jump  = Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || controls.jumpPressed;
        boolean shoot = controls.shootPressed || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);

        boolean moving = (left ^ right);

        // knockback (retroceso del mundo)
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

        float targetCamX = scrollX + viewport.getWorldWidth() / 2f;

        if (moving || knockRemaining > 0f) {
            camera.position.x += (targetCamX - camera.position.x) * 10f * delta;
        } else {
            camera.position.x = targetCamX;
        }
        camera.update();

        float camLeft  = camera.position.x - viewport.getWorldWidth() / 2f;
        float camRight = camera.position.x + viewport.getWorldWidth() / 2f;
        float camTop   = camera.position.y + viewport.getWorldHeight() / 2f;

        // Ayla anclada
        ayla.setX(camLeft + AYLA_SCREEN_X);
        ayla.update(delta, left, right, jump, shoot, GROUND_Y, camLeft, camRight);

        // cactus spawn
        if (camRight >= nextSpawnX) {
            Texture tex = MathUtils.randomBoolean() ? cactusPink : cactusYellow;
            float cactusX = camRight + CACTUS_SPAWN_MARGIN;
            cactuses.add(new Cactus(tex, cactusX, GROUND_Y, CACTUS_HEIGHT));
            nextSpawnX = camRight + MathUtils.random(CACTUS_MIN_DIST, CACTUS_MAX_DIST);
        }

        // SOLDIER spawn
        if (camRight >= nextSoldierSpawnX) {
            float sx = camRight + SOLDIER_SPAWN_MARGIN;

            soldiers.add(new Soldier(
                game.assets.get(Assets.SOLDIER_IDLE),
                game.assets.get(Assets.SOLDIER_RUN),
                game.assets.get(Assets.SOLDIER_HURT),
                game.assets.get(Assets.SOLDIER_DEAD),
                game.assets.get(Assets.BULLET),
                sx,
                GROUND_Y
            ));

            nextSoldierSpawnX = camRight + MathUtils.random(SOLDIER_MIN_DIST, SOLDIER_MAX_DIST);
        }

        // TANK spawn
        if (camRight >= nextTankSpawnX) {
            nextTankSpawnX = camRight + MathUtils.random(TANK_MIN_DIST, TANK_MAX_DIST);

            if (MathUtils.random() < TANK_SPAWN_CHANCE) {
                float tx = camRight + TANK_SPAWN_MARGIN;

                tanks.add(new Tank(
                    game.assets.get(Assets.TANK_IDLE),
                    game.assets.get(Assets.TANK_MOVE),
                    game.assets.get(Assets.TANK_DESTROY),
                    game.assets.get(Assets.TANK_DEAD),
                    tx,
                    GROUND_Y
                ));
            }
        }

        // quitar cactus fuera
        for (int i = cactuses.size - 1; i >= 0; i--) {
            if (cactuses.get(i).isOffScreenLeft(camLeft)) cactuses.removeIndex(i);
        }

        // quitar soldados fuera
        for (int i = soldiers.size - 1; i >= 0; i--) {
            if (soldiers.get(i).isOffScreenLeft(camLeft)) soldiers.removeIndex(i);
        }

        // quitar tanks fuera
        for (int i = tanks.size - 1; i >= 0; i--) {
            if (tanks.get(i).isOffScreenLeft(camLeft)) tanks.removeIndex(i);
        }

        // colisión Ayla - cactus
        if (hitCooldown <= 0f) {
            for (int i = 0; i < cactuses.size; i++) {
                Cactus c = cactuses.get(i);
                if (ayla.getBounds().overlaps(c.getBounds())) {
                    knockRemaining = KNOCKBACK_DISTANCE;
                    hitCooldown = HIT_DELAY;
                    // livesHUD.loseLife();
                    break;
                }
            }
        }

        // colisión Ayla - tank (cuerpo)
        if (hitCooldown <= 0f) {
            for (int i = 0; i < tanks.size; i++) {
                Tank t = tanks.get(i);
                if (!t.isDead() && ayla.getBounds().overlaps(t.getBounds())) {
                    knockRemaining = KNOCKBACK_DISTANCE;
                    hitCooldown = HIT_DELAY;
                    // livesHUD.loseLife();
                    break;
                }
            }
        }

        // actualizar soldados + colisiones
        Array<Bullet> aylaBullets = ayla.getBullets();

        for (int i = 0; i < soldiers.size; i++) {
            Soldier s = soldiers.get(i);

            s.update(delta, ayla.getX(), camLeft, camRight);
            s.setAylaX(ayla.getX());

            // balas de Ayla -> soldado
            for (int b = aylaBullets.size - 1; b >= 0; b--) {
                Bullet ab = aylaBullets.get(b);
                if (ab.getBounds().overlaps(s.getBounds()) && !s.isDead()) {
                    ab.kill();
                    s.hitByAylaBullet();
                }
            }

            // balas del soldado -> Ayla
            if (hitCooldown <= 0f) {
                Array<Bullet> sb = s.getBullets();
                for (int b = sb.size - 1; b >= 0; b--) {
                    Bullet eb = sb.get(b);
                    if (eb.getBounds().overlaps(ayla.getBounds())) {
                        eb.kill();
                        knockRemaining = KNOCKBACK_DISTANCE;
                        hitCooldown = HIT_DELAY;
                        // livesHUD.loseLife();
                        break;
                    }
                }
            }
        }

        // --- TANKS: update + recibir daño + DISPARAR ---
        for (int i = 0; i < tanks.size; i++) {
            Tank t = tanks.get(i);

            t.update(delta, ayla.getX());

            // balas Ayla -> tank (3 hits)
            for (int b = aylaBullets.size - 1; b >= 0; b--) {
                Bullet ab = aylaBullets.get(b);

                if (!ab.isAlive()) continue;
                if (t.isDead() || t.isDestroying()) continue;

                if (ab.getBounds().overlaps(t.getBounds())) {
                    ab.kill();
                    t.hitByAylaBullet();
                }
            }

            // ✅ DISPARO DEL TANK (balas grandes)
            if (t.canShoot(delta)) {

                float muzzleX = t.isFacingRight()
                    ? (t.getX() + t.getWidth())
                    : t.getX();

                float muzzleY = t.getY() + TANK_MUZZLE_Y;

                float velX = t.isFacingRight()
                    ? TANK_BULLET_SPEED
                    : -TANK_BULLET_SPEED;

                // ✅ usa la misma textura que el resto, pero más grande
                // Si tienes otra textura para el tanque, cambia Assets.BULLET por tu Assets.TANK_BULLET
                Bullet tb = new Bullet(
                    game.assets.get(Assets.BULLET),
                    muzzleX,
                    muzzleY,
                    velX,
                    TANK_BULLET_W,
                    TANK_BULLET_H
                );

                tankBullets.add(tb);

                // Debug útil: si no sale esto, no está disparando
                // Gdx.app.log("TANK", "DISPARA: x=" + muzzleX + " y=" + muzzleY + " velX=" + velX);
            }
        }

        // ✅ actualizar balas del tank + colisión con Ayla + borrar fuera
        for (int i = tankBullets.size - 1; i >= 0; i--) {
            Bullet b = tankBullets.get(i);
            b.update(delta);

            if (hitCooldown <= 0f && b.isAlive() && b.getBounds().overlaps(ayla.getBounds())) {
                b.kill();
                knockRemaining = KNOCKBACK_DISTANCE;
                hitCooldown = HIT_DELAY;
                // livesHUD.loseLife();
            }

            // fuera de pantalla respecto a cámara
            if (!b.isAlive() || b.getX() < camLeft - 400f || b.getX() > camRight + 400f) {
                tankBullets.removeIndex(i);
            }
        }

        game.batch.setProjectionMatrix(camera.combined);
        controls.updateLayout(camera, viewport);

        game.batch.begin();

        parallax.render(game.batch, scrollX);

        for (Cactus c : cactuses) c.draw(game.batch);
        for (Soldier s : soldiers) s.draw(game.batch);
        for (Tank t : tanks) t.draw(game.batch);

        // ✅ dibujar balas del tanque (para que SE VEAN)
        for (Bullet b : tankBullets) b.draw(game.batch);

        ayla.draw(game.batch, moving);

        livesHUD.draw(game.batch, camLeft, camTop);
        controls.draw(game.batch);

        game.batch.end();

        if (debugHitboxes) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            // Ayla (rojo)
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(
                ayla.getBounds().x,
                ayla.getBounds().y,
                ayla.getBounds().width,
                ayla.getBounds().height
            );

            // Cactus (verde)
            shapeRenderer.setColor(Color.GREEN);
            for (int i = 0; i < cactuses.size; i++) {
                shapeRenderer.rect(
                    cactuses.get(i).getBounds().x,
                    cactuses.get(i).getBounds().y,
                    cactuses.get(i).getBounds().width,
                    cactuses.get(i).getBounds().height
                );
            }

            // Soldier (cyan) + balas (amarillo)
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

            // Tank (magenta)
            shapeRenderer.setColor(Color.MAGENTA);
            for (int i = 0; i < tanks.size; i++) {
                Tank t = tanks.get(i);
                shapeRenderer.rect(
                    t.getBounds().x,
                    t.getBounds().y,
                    t.getBounds().width,
                    t.getBounds().height
                );
            }

            // ✅ balas del tank (naranja)
            shapeRenderer.setColor(Color.ORANGE);
            for (int i = 0; i < tankBullets.size; i++) {
                Bullet bb = tankBullets.get(i);
                shapeRenderer.rect(
                    bb.getBounds().x,
                    bb.getBounds().y,
                    bb.getBounds().width,
                    bb.getBounds().height
                );
            }

            shapeRenderer.end();
        }
    }

    @Override
    public void resize(int w, int h) {
        viewport.update(w, h, true);
        controls.updateLayout(camera, viewport);
    }

    @Override
    public void show() {
        game.audio.playMusic(
            com.carmen.mijuego.assets.Assets.MUS_DESERT_THEME,
            true
        );
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}
