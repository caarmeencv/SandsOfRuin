package com.carmen.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.carmen.mijuego.Main;
import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.characters.Ayla;
import com.carmen.mijuego.enemies.Mummy;
import com.carmen.mijuego.input.AccelerometerJumpDetector;
import com.carmen.mijuego.input.Controls;
import com.carmen.mijuego.projectiles.Bullet;
import com.carmen.mijuego.projectiles.MummyBulletSystem;
import com.carmen.mijuego.settings.MummyTimeRecords;
import com.carmen.mijuego.ui.CurseParticles;
import com.carmen.mijuego.ui.LivesHUD;
import com.carmen.mijuego.world.ParallaxPyramid;

public class PyramidScreen implements Screen {

    // Tamaño del mundo que usa esta pantalla
    // Es el mismo tamaño que estás usando en el resto del juego para que todo encaje bien
    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    // Altura del suelo dentro de la pirámide
    private static final float GROUND_Y = 90f;

    // Posición inicial de Ayla cuando entra en la pirámide
    // Empieza fuera de pantalla a la izquierda y camina hasta el punto donde ya empieza el juego
    private static final float INTRO_START_X = -260f;
    private static final float INTRO_TARGET_X = 220f;
    private static final float INTRO_WALK_SPEED = 280f;

    // Velocidad del scroll cuando avanzas y cuando retrocedes
    // Avanzar es un poco más rápido que volver para que el nivel empuje hacia delante
    private static final float SCROLL_SPEED_FORWARD = 320f;
    private static final float SCROLL_SPEED_BACK = 260f;

    // Retroceso cuando Ayla recibe daño de la momia
    // knockbackDistance es cuánto se echa hacia atrás y knockbackSpeed es lo rápido que ocurre
    private static final float KNOCKBACK_DISTANCE = 140f;
    private static final float KNOCKBACK_SPEED = 900f;

    // Tiempo mínimo entre golpes del cuerpo de la momia a Ayla
    // Sirve para que no te quite muchas vidas por quedarte pegada a la momia
    private static final float MUMMY_HIT_DELAY = 0.55f;

    // Cuánto tienes que avanzar desde que empieza el play para que aparezca la momia
    private static final float BOSS_SPAWN_AFTER_SCROLL = 650f;

    // La momia aparece un poco fuera de pantalla por la derecha para que no aparezca de golpe
    private static final float MUMMY_SPAWN_OUTSIDE_PAD = 220f;

    // Después de derrotar a la momia, Ayla camina un tramo automático hacia el cofre
    private static final float AFTER_BOSS_DISTANCE = 700f;
    private static final float AFTER_BOSS_SPEED = 240f;

    // Posición del cofre en el mundo y su tamaño
    // Reveal pad sirve para no dibujarlo hasta que esté casi entrando en cámara
    private static final float TREASURE_WORLD_X = 3050f;
    private static final float TREASURE_W = 220f;
    private static final float TREASURE_H = 220f;
    private static final float TREASURE_Y_OFFSET = -25f;
    private static final float TREASURE_REVEAL_PAD = 20f;

    // Auto scroll hacia el cofre en la fase final
    private static final float AUTO_SCROLL_SPEED = 320f;

    // Velocidad a la que Ayla se acerca al cofre cuando la cámara se bloquea
    private static final float AYLA_TO_CHEST_SPEED = 240f;

    // Esto se usa para saber cuándo borrar a la momia cuando se queda muy atrás
    // Es una estimación del ancho visual y un margen extra
    private static final float MUMMY_VISUAL_W_EST = 320f;
    private static final float OFFSCREEN_MARGIN = 30f;

    // Tiempo de la maldición que se aplica cuando la momia te da
    private static final float CURSE_TIME = 4.0f;

    // Tiempo de inmunidad al volver de pausa para que no te golpeen justo al volver
    private static final float RESUME_IMMUNITY_TIME = 2.0f;

    // Máximo de vidas permitido en esta pantalla
    private static final int MAX_LIVES = 5;

    // Tamaño y altura de los corazones coleccionables
    private static final float HEART_W = 54f;
    private static final float HEART_H = 54f;
    private static final float HEART_Y = GROUND_Y + 8f;

    // Posiciones X donde aparecen dos corazones al principio del nivel
    private static final float HEART1_WORLD_X = 520f;
    private static final float HEART2_WORLD_X = 720f;

    // Fases del nivel, es como un guion
    // INTRO_ENTER es la entrada automática
    // PLAY es el juego normal
    // AFTER_BOSS_WALK es caminar automático tras el boss
    // TREASURE_APPROACH es el auto scroll hasta que el cofre esté bien en pantalla
    // TREASURE_LOCK es cámara bloqueada y Ayla se acerca al cofre
    // END es cuando ya termina y cambias de pantalla
    private enum Phase {
        INTRO_ENTER,
        PLAY,
        AFTER_BOSS_WALK,
        TREASURE_APPROACH,
        TREASURE_LOCK,
        END
    }

    // Esto representa un corazón coleccionable
    // collected indica si ya lo cogiste
    // bounds es su rectángulo para colisiones con Ayla
    private static class HeartItem {
        float x, y, w, h;
        boolean collected;
        Rectangle bounds;

        HeartItem(float x, float y, float w, float h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            collected = false;
            bounds = new Rectangle(x, y, w, h);
        }

        void updateBounds() {
            bounds.set(x, y, w, h);
        }
    }

    // Referencia al juego principal para acceder a assets, batch, settings, audio y cambiar de pantalla
    private final Main game;

    // Cámara y viewport del mundo
    private OrthographicCamera camera;
    private Viewport viewport;

    // Controles táctiles y personaje
    private Controls controls;
    private Ayla ayla;

    // Fondo con parallax para la pirámide
    private ParallaxPyramid parallax;

    // Texturas del boss momia
    private Texture mummyIdle;
    private Texture mummyWalk;
    private Texture mummyHurt;
    private Texture mummyDead;

    // Objeto momia y flag para saber si ya apareció
    private Mummy mummy;
    private boolean mummySpawned;

    // Sistema de balas de la momia y partículas de maldición
    private MummyBulletSystem mummyBullets;
    private CurseParticles curseParticles;

    // HUD de vidas en pantalla
    private LivesHUD livesHUD;

    // Cofre final
    private Texture treasureTex;
    private Rectangle treasureBounds;

    // Corazones coleccionables al principio
    private Texture heartItemTex;
    private Array<HeartItem> heartItems;

    // Fase actual
    private Phase phase;

    // scrollX es cuánto has avanzado en el nivel
    // playStartScrollX sirve para medir cuánto has avanzado desde que empieza el juego
    // afterBossStartScrollX sirve para medir el tramo automático tras matar al boss
    private float scrollX;
    private float playStartScrollX;
    private float afterBossStartScrollX;

    // Esto guarda la X de la cámara cuando bloqueas en el cofre
    private float lockedCamX;

    // knockRemaining es cuánto retroceso queda por aplicar
    // mummyHitCooldown es un cooldown para golpes del cuerpo de la momia
    private float knockRemaining;
    private float mummyHitCooldown;

    // Variables para el efecto de cámara mareada cuando Ayla está maldita
    private float dizzyTime;
    private float baseCamY;
    private float baseZoom;

    // pauseLatch evita que al mantener pulsado pause se abra y cierre muchas veces
    private boolean pauseLatch;

    // snapCameraOnResume sirve para que al volver de la pausa la cámara se coloque bien
    private boolean snapCameraOnResume;

    // inmunidad al volver de pausa
    private float resumeImmunity;

    // Esto evita guardar el tiempo más de una vez cuando muere la momia
    private boolean mummyTimeSaved;

    // Detector de salto por acelerómetro
    private AccelerometerJumpDetector accelJump;

    public PyramidScreen(Main game) {
        this.game = game;

        // Creo cámara y viewport y los dejo centrados
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply();

        camera.position.set(WORLD_W * 0.5f, WORLD_H * 0.5f, 0f);
        camera.update();

        // Guardo valores base para restaurar la cámara cuando no hay mareo
        baseCamY = camera.position.y;
        baseZoom = camera.zoom;

        // Creo detector de acelerómetro
        accelJump = new AccelerometerJumpDetector();

        // Creo a Ayla con sus texturas y posición inicial de intro
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

        // Sincronizo vidas del game con Ayla
        game.vidas = clampLives(game.vidas);
        ayla.setLives(game.vidas);

        // Creo los controles y los activo como inputProcessor
        controls = new Controls(
            game.audio,
            game.assets,
            game.assets.get(Assets.UI_LEFT),
            game.assets.get(Assets.UI_RIGHT),
            game.assets.get(Assets.UI_JUMP),
            game.assets.get(Assets.UI_SHOOT),
            game.assets.get(Assets.UI_GRENADE),
            game.assets.get(Assets.UI_PAUSE)
        );
        Gdx.input.setInputProcessor(controls);

        // Creo el parallax con las texturas de pared y suelo
        Texture wallTex = game.assets.get(Assets.PYR_WALL);
        Texture groundTex = game.assets.get(Assets.PYR_GROUND);
        parallax = new ParallaxPyramid(wallTex, groundTex, 0.75f, 0.90f);

        // Cargo texturas de la momia desde assets
        mummyIdle = game.assets.get(Assets.MUMMY_IDLE);
        mummyWalk = game.assets.get(Assets.MUMMY_WALK);
        mummyHurt = game.assets.get(Assets.MUMMY_HURT);
        mummyDead = game.assets.get(Assets.MUMMY_DEAD);

        // Cofre final y su rectángulo de colisión
        treasureTex = game.assets.get(Assets.TREASURE);
        treasureBounds = new Rectangle();
        treasureBounds.set(TREASURE_WORLD_X, GROUND_Y + TREASURE_Y_OFFSET, TREASURE_W, TREASURE_H);

        // Sistema de balas de la momia y partículas
        mummyBullets = new MummyBulletSystem(game.audio, game.assets.get(Assets.BULLET_MUMMY));
        curseParticles = new CurseParticles();

        // HUD de vidas
        livesHUD = new LivesHUD(
            game.assets.get(Assets.HUD_HEART_FULL),
            game.assets.get(Assets.HUD_HEART_EMPTY)
        );

        // Corazones coleccionables usan la misma textura del corazón lleno
        heartItemTex = game.assets.get(Assets.HUD_HEART_FULL);
        heartItems = new Array<HeartItem>();
        spawnStartHearts();

        // Empiezo en fase de intro
        phase = Phase.INTRO_ENTER;

        // Inicializo valores de scroll y control
        scrollX = 0f;
        playStartScrollX = 0f;
        afterBossStartScrollX = 0f;
        lockedCamX = 0f;

        knockRemaining = 0f;
        mummyHitCooldown = 0f;

        dizzyTime = 0f;

        pauseLatch = false;
        snapCameraOnResume = false;
        resumeImmunity = 0f;

        mummySpawned = false;
        mummyTimeSaved = false;
    }

    private void spawnStartHearts() {

        // Borro cualquier corazón anterior y creo dos al principio
        heartItems.clear();
        heartItems.add(new HeartItem(HEART1_WORLD_X, HEART_Y, HEART_W, HEART_H));
        heartItems.add(new HeartItem(HEART2_WORLD_X, HEART_Y, HEART_W, HEART_H));
    }

    @Override
    public void show() {

        // Cuando se muestra la pantalla pongo música y preparo input
        game.audio.playMusic(Assets.MUS_PYRAMID_THEME, true);
        Gdx.input.setInputProcessor(controls);
        controls.resetAll();
        pauseLatch = false;

        // Reinicio el detector de salto por acelerómetro
        accelJump.reset();

        // Re-sincronizo vidas
        game.vidas = clampLives(game.vidas);
        ayla.setLives(game.vidas);

        // Permito guardar el tiempo otra vez en esta visita
        mummyTimeSaved = false;
    }

    @Override
    public void render(float delta) {

        // Bajo la inmunidad de reanudar, si existe
        if (resumeImmunity > 0f) {
            resumeImmunity -= delta;
            if (resumeImmunity < 0f) resumeImmunity = 0f;
        }

        // Limpio pantalla
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        // Aumento el contador global del juego y lo pinto en el HUD
        game.runTimeSeconds += delta;
        controls.setCounterText(formatTime(game.runTimeSeconds));

        // Compruebo si se quiere pausar
        boolean pauseKey = Gdx.input.isKeyPressed(Input.Keys.ESCAPE);
        boolean pauseNow = pauseKey || controls.pausePressed;

        // pauseLatch evita que con un toque largo se abra muchas veces la pausa
        if (pauseNow && !pauseLatch) {
            pauseLatch = true;
            controls.resetAll();

            // Al volver quiero que la cámara se coloque correctamente y doy un momento de inmunidad
            snapCameraOnResume = true;
            resumeImmunity = RESUME_IMMUNITY_TIME;

            // Paro loops de sonido para que la pausa no deje sonido raro
            if (mummy != null) mummy.stopAllLoops();
            ayla.stopAllLoops();

            // Cambio a la pantalla de pausa, diciéndole que al volver es esta pantalla
            game.setScreen(new PauseScreen(game, this, PauseScreen.Context.PYRAMID));
            return;
        }
        if (!pauseNow) pauseLatch = false;

        // Actualizo toda la lógica del nivel y personajes
        updateLogic(delta);

        // Si al volver de pausa pedí snap, coloco cámara exacta al scroll
        if (snapCameraOnResume) {
            float targetCamX = scrollX + viewport.getWorldWidth() * 0.5f;
            camera.position.x = targetCamX;
            camera.update();
            snapCameraOnResume = false;
        }

        // Efecto de cámara si Ayla está maldita
        applyDizzyCamera(delta);

        // Estos valores sirven para dibujar HUD y cosas en función de cámara
        float camLeft = camera.position.x - viewport.getWorldWidth() * 0.5f;
        float camTop = camera.position.y + viewport.getWorldHeight() * 0.5f;

        // Dibujo el mundo con la cámara del mundo
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // Fondo parallax
        parallax.render(game.batch, camera, viewport.getWorldWidth(), viewport.getWorldHeight());

        // Coleccionables y cofre
        drawHeartItems();
        drawTreasure();

        // Boss y balas del boss
        if (mummy != null) mummy.draw(game.batch);
        if (mummy != null && !mummy.isDead()) mummyBullets.draw(game.batch);

        // Ayla
        ayla.draw(game.batch, computeMovingVisual());

        // Partículas de maldición encima de Ayla
        float px = ayla.getX() + ayla.getWidth() * 0.50f;
        float py = ayla.getY() + ayla.getHeight() * 0.55f;
        curseParticles.draw(game.batch, px, py);

        // HUD de vidas
        // Solo enseñas HUD de vidas durante la fase de play, el resto puedes decidirlo
        livesHUD.draw(game.batch, camLeft, camTop, ayla.getLives(), phase == Phase.PLAY);

        // Controles táctiles solo se dibujan durante el juego normal
        if (phase == Phase.PLAY) {
            controls.setAccelJumpEnabled(game.settings.isAccelJumpEnabled());
            controls.beginUI(game.batch);
            controls.draw(game.batch, ayla.getNormalCooldownPercent(), ayla.getSpecialCooldownPercent());
            game.batch.setProjectionMatrix(camera.combined);
        }

        game.batch.end();
    }

    private void drawHeartItems() {

        // Si no hay textura no dibujo
        if (heartItemTex == null) return;

        // En la intro no dibujo coleccionables para que no se vean raro
        if (phase == Phase.INTRO_ENTER) return;

        // Solo dibujo si están más o menos en cámara, para ahorrar
        float vw = viewport.getWorldWidth();
        float camLeft = camera.position.x - vw * 0.5f;
        float camRight = camera.position.x + vw * 0.5f;

        for (int i = 0; i < heartItems.size; i++) {
            HeartItem it = heartItems.get(i);
            if (it.collected) continue;
            if (it.x + it.w < camLeft - 100f) continue;
            if (it.x > camRight + 100f) continue;
            game.batch.draw(heartItemTex, it.x, it.y, it.w, it.h);
        }
    }

    private void updateHeartItems() {

        // Solo se pueden coger corazones en play
        if (phase != Phase.PLAY) return;

        Rectangle aylaB = ayla.getBounds();

        for (int i = 0; i < heartItems.size; i++) {
            HeartItem it = heartItems.get(i);
            if (it.collected) continue;

            // Actualizo bounds por si algún día cambias su tamaño o posición
            it.updateBounds();

            // Si Ayla toca el corazón, lo marco como recogido y sumo vida
            if (aylaB.overlaps(it.bounds)) {
                it.collected = true;

                int before = ayla.getLives();
                int after = clampLives(before + 1);

                ayla.setLives(after);
                game.vidas = after;

                game.audio.playSfx(Assets.SFX_LIVE);
            }
        }
    }

    private void applyDizzyCamera(float delta) {

        // Si Ayla está maldita, hago un movimiento de cámara para marear
        if (ayla != null && ayla.isCursed()) {
            dizzyTime += delta;

            float rollDeg = 3.5f;
            float swayX = 10f;
            float swayY = 6f;
            float zoomAmp = 0.02f;

            float t1 = dizzyTime * 3.5f;
            float t2 = dizzyTime * 6.0f;

            // Reseteo el vector up antes de rotar para evitar acumulación rara
            camera.up.set(0, 1, 0);
            camera.rotate((float) Math.sin(t1) * rollDeg);

            // Pequeño vaivén y zoom
            camera.position.x += (float) Math.sin(t2) * swayX;
            camera.position.y = baseCamY + (float) Math.cos(t2 * 0.9f) * swayY;
            camera.zoom = baseZoom + (float) Math.sin(t1 * 1.2f) * zoomAmp;

            camera.update();
        } else {

            // Si no hay maldición, restauro cámara normal
            dizzyTime = 0f;
            camera.up.set(0, 1, 0);
            camera.zoom = baseZoom;
            camera.position.y = baseCamY;
            camera.update();
        }
    }

    private void drawTreasure() {

        // El cofre solo se dibuja en estas fases
        if (phase != Phase.TREASURE_APPROACH && phase != Phase.TREASURE_LOCK && phase != Phase.END) return;

        float vw = viewport.getWorldWidth();
        float camRight = camera.position.x + vw * 0.5f;

        // En approach solo lo dibujo cuando está entrando en pantalla para que quede más bonito
        if (phase == Phase.TREASURE_APPROACH) {
            if (camRight < (TREASURE_WORLD_X - TREASURE_REVEAL_PAD)) return;
        }

        game.batch.draw(treasureTex, TREASURE_WORLD_X, GROUND_Y + TREASURE_Y_OFFSET, TREASURE_W, TREASURE_H);
    }

    private void updateLogic(float delta) {

        float viewportW = viewport.getWorldWidth();

        // Controles de teclado para PC y para depurar
        boolean leftKey = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean rightKey = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);

        boolean jumpKey = Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.W)
            || Gdx.input.isKeyJustPressed(Input.Keys.UP);

        boolean shootKey = Gdx.input.isKeyPressed(Input.Keys.K)
            || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);

        boolean grenadeKey = Gdx.input.isKeyJustPressed(Input.Keys.L);

        // Modo acelerómetro
        boolean accelMode = game.settings.isAccelJumpEnabled();
        controls.setAccelJumpEnabled(accelMode);

        // Mezclo teclado y táctil
        boolean left = leftKey || controls.leftPressed;
        boolean right = rightKey || controls.rightPressed;
        boolean shoot = shootKey || controls.shootPressed;
        boolean grenade = grenadeKey || controls.grenadePressed;

        // El salto cambia según si el modo acelerómetro está activado
        boolean jump;
        if (accelMode) {
            boolean accelJumpNow = accelJump.updateAndConsume(delta);
            boolean fallbackJump = jumpKey || controls.jumpPressed;
            jump = accelJumpNow || fallbackJump;
        } else {
            jump = jumpKey || controls.jumpPressed;
        }

        // Cooldown de choque con momia
        mummyHitCooldown -= delta;
        if (mummyHitCooldown < 0f) mummyHitCooldown = 0f;

        // Fase de intro, Ayla camina sola hasta el punto de inicio
        if (phase == Phase.INTRO_ENTER) {
            scrollX = 0f;

            camera.position.x = viewportW * 0.5f;
            camera.update();

            float camLeft = camera.position.x - viewportW * 0.5f;
            float camRight = camera.position.x + viewportW * 0.5f;

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

        // Fase de juego normal
        if (phase == Phase.PLAY) {

            // Si Ayla está maldita, inviertes controles izquierda y derecha
            if (ayla.isCursed()) {
                boolean tmp = left;
                left = right;
                right = tmp;
            }

            // Si hay retroceso activo por daño, lo aplico y bloqueo el movimiento normal
            if (knockRemaining > 0f) {
                float step = KNOCKBACK_SPEED * delta;
                if (step > knockRemaining) step = knockRemaining;

                scrollX -= step;
                if (scrollX < 0f) scrollX = 0f;

                knockRemaining -= step;
                if (knockRemaining < 0f) knockRemaining = 0f;
            } else {

                // Scroll normal según controles
                if (right) scrollX += SCROLL_SPEED_FORWARD * delta;
                if (left) scrollX -= SCROLL_SPEED_BACK * delta;
                if (scrollX < 0f) scrollX = 0f;
            }

            // Cámara sigue el scroll con suavidad
            float targetCamX = scrollX + viewportW * 0.5f;

            if (snapCameraOnResume) {
                camera.position.x = targetCamX;
                camera.update();
            } else {
                camera.position.x += (targetCamX - camera.position.x) * 10f * delta;
                camera.update();
            }

            // Esto se usa para el mareo, para volver al centro
            baseCamY = WORLD_H * 0.5f;

            float camLeft = camera.position.x - viewportW * 0.5f;
            float camRight = camera.position.x + viewportW * 0.5f;

            // Ayla se mantiene en una posición fija relativa a la cámara
            ayla.setX(camLeft + INTRO_TARGET_X);

            // Actualizo Ayla con inputs
            ayla.update(delta, left, right, jump, shoot, grenade, GROUND_Y, camLeft, camRight);

            // Corazones
            updateHeartItems();

            // Spawneo el boss cuando avanzas suficiente
            if (!mummySpawned && (scrollX - playStartScrollX) >= BOSS_SPAWN_AFTER_SCROLL) {
                spawnMummyOutsideRight();
            }

            // Lógica del boss y colisiones mientras el boss está vivo
            if (mummySpawned && mummy != null && !mummy.isDead()) {
                mummy.update(delta, ayla.getX(), GROUND_Y);

                mummyBullets.update(delta, camLeft, camRight, mummy);

                handleAylaBulletsVsMummy();
                handleMummyBulletsVsAyla();
                handleAylaVsMummyBody();

                // Sincronizo vidas con game para que persistan al cambiar de pantalla
                game.vidas = clampLives(ayla.getLives());
                ayla.setLives(game.vidas);

                // Si Ayla muere, voy a game over
                if (ayla.isDead()) {
                    game.vidas = clampLives(ayla.getLives());
                    game.audio.playSfx(Assets.SFX_GAME_OVER);
                    ayla.stopAllLoops();
                    game.setScreen(new GameOverScreen(game));
                    return;
                }
            }

            // Si el boss murió, guardo tiempo y paso a la siguiente fase
            if (mummySpawned && mummy != null && mummy.isDead()) {
                if (!mummyTimeSaved) {
                    int secs = (int) Math.ceil(game.runTimeSeconds);
                    if (secs < 1) secs = 1;
                    MummyTimeRecords.addTimeSeconds(secs);
                    mummyTimeSaved = true;
                }

                // Quito maldición y reset de mareo
                ayla.clearCurse();
                dizzyTime = 0f;

                phase = Phase.AFTER_BOSS_WALK;
                afterBossStartScrollX = scrollX;
                mummyBullets.clear();
            }

            updateCurseParticles(delta);
            return;
        }

        // Después del boss, caminata automática un tramo
        if (phase == Phase.AFTER_BOSS_WALK) {
            float targetScroll = afterBossStartScrollX + AFTER_BOSS_DISTANCE;
            if (scrollX < targetScroll) {
                scrollX += AFTER_BOSS_SPEED * delta;
                if (scrollX > targetScroll) scrollX = targetScroll;
            } else {
                phase = Phase.TREASURE_APPROACH;
            }

            float targetCamX = scrollX + viewportW * 0.5f;
            camera.position.x += (targetCamX - camera.position.x) * 10f * delta;
            camera.update();

            float camLeft = camera.position.x - viewportW * 0.5f;

            ayla.setX(camLeft + INTRO_TARGET_X);
            ayla.update(delta, false, true, false, false, false, GROUND_Y, camLeft, camera.position.x + viewportW * 0.5f);

            // Borro momia cuando queda lejos a la izquierda
            despawnMummyIfFullyOffscreenLeft(camLeft);

            updateCurseParticles(delta);
            return;
        }

        // Aquí el juego hace auto scroll hasta que el cofre esté bien visible en cámara
        if (phase == Phase.TREASURE_APPROACH) {
            scrollX += AUTO_SCROLL_SPEED * delta;

            float targetCamX = scrollX + viewportW * 0.5f;
            camera.position.x += (targetCamX - camera.position.x) * 10f * delta;
            camera.update();

            float camLeft = camera.position.x - viewportW * 0.5f;
            float camRight = camera.position.x + viewportW * 0.5f;

            ayla.setX(camLeft + INTRO_TARGET_X);
            ayla.update(delta, false, true, false, false, false, GROUND_Y, camLeft, camRight);

            despawnMummyIfFullyOffscreenLeft(camLeft);

            boolean treasureFullyVisible = camLeft <= TREASURE_WORLD_X && camRight >= (TREASURE_WORLD_X + TREASURE_W);

            // Cuando el cofre está totalmente visible, bloqueo la cámara para hacer la escena final
            if (treasureFullyVisible) {
                phase = Phase.TREASURE_LOCK;
                lockedCamX = camera.position.x;
            }

            updateCurseParticles(delta);
            return;
        }

        // En esta fase la cámara se queda fija y Ayla camina al cofre para activar la victoria
        if (phase == Phase.TREASURE_LOCK) {
            camera.position.x = lockedCamX;
            camera.update();

            float camLeft = camera.position.x - viewportW * 0.5f;
            float camRight = camera.position.x + viewportW * 0.5f;

            ayla.setX(ayla.getX() + AYLA_TO_CHEST_SPEED * delta);
            ayla.update(delta, false, true, false, false, false, GROUND_Y, camLeft, camRight);

            // Cuando Ayla toca el cofre, suena victoria y cambias de pantalla
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

        // Actualizo partículas usando el centro de Ayla para que se vean encima de ella
        float cx = ayla.getX() + ayla.getWidth() * 0.50f;
        float cy = ayla.getY() + ayla.getHeight() * 0.55f;
        curseParticles.update(delta, cx, cy, ayla.isCursed());
    }

    private void handleAylaVsMummyBody() {

        // Si no hay momia o ya está muerta, no hago nada
        if (mummy == null) return;
        if (mummy.isDead()) return;

        // Cooldown para choques
        if (mummyHitCooldown > 0f) return;

        // Inmunidad al volver de pausa
        if (resumeImmunity > 0f) return;

        // Si chocan los hitbox, aplico retroceso, daño y maldición
        if (ayla.getBounds().overlaps(mummy.getBounds())) {
            knockRemaining = KNOCKBACK_DISTANCE;

            boolean damaged = ayla.takeDamage();
            if (damaged) ayla.applyCurse(CURSE_TIME);

            if (damaged) game.vibrateHit(120);

            game.vidas = clampLives(ayla.getLives());
            ayla.setLives(game.vidas);

            mummyHitCooldown = MUMMY_HIT_DELAY;
        }
    }

    private void handleMummyBulletsVsAyla() {

        // Si no hay momia o está muerta, no hay balas que hacer daño
        if (mummy == null) return;
        if (mummy.isDead()) return;

        Array<Bullet> bullets = mummyBullets.getBullets();
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            if (!b.isAlive()) continue;

            // Si hay inmunidad por volver de pausa, ni siquiera miro impactos
            if (resumeImmunity > 0f) return;

            // Si bala toca Ayla, mato bala y aplico daño y maldición
            if (b.getBounds().overlaps(ayla.getBounds())) {
                b.kill();

                knockRemaining = KNOCKBACK_DISTANCE;

                boolean damaged = ayla.takeDamage();
                if (damaged) ayla.applyCurse(CURSE_TIME);

                if (damaged) game.vibrateHit(120);

                game.vidas = clampLives(ayla.getLives());
                ayla.setLives(game.vidas);
            }
        }
    }

    private void despawnMummyIfFullyOffscreenLeft(float camLeft) {

        // Borro referencia a la momia cuando se queda muy atrás a la izquierda
        // Esto es para ahorrar y para que no quede ahí invisible gastando
        if (mummy == null) return;

        float mummyRight = mummy.getX() + MUMMY_VISUAL_W_EST;
        if (mummyRight < (camLeft - OFFSCREEN_MARGIN)) {
            mummy = null;
        }
    }

    private void spawnMummyOutsideRight() {

        // Marco que ya la he spawneado para no crearla dos veces
        mummySpawned = true;

        float viewportW = viewport.getWorldWidth();
        float camRight = camera.position.x + viewportW * 0.5f;

        // La pongo fuera de pantalla por la derecha
        float mummyX = camRight + MUMMY_SPAWN_OUTSIDE_PAD;

        // Creo el boss
        mummy = new Mummy(game.audio, mummyIdle, mummyWalk, mummyHurt, mummyDead, mummyX, GROUND_Y);

        mummyTimeSaved = false;
    }

    private void handleAylaBulletsVsMummy() {

        // Balas de Ayla contra momia
        if (mummy == null) return;
        if (mummy.isDead()) return;

        Array<Bullet> bullets = ayla.getBullets();
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            if (!b.isAlive()) continue;

            if (b.getBounds().overlaps(mummy.getBounds())) {
                b.kill();

                // Si la bala hace más de 1 de daño, aplico varios hits seguidos
                for (int d = 0; d < b.getDamage(); d++) {
                    mummy.hitByAylaBullet();
                }
            }
        }
    }

    private boolean computeMovingVisual() {

        // Esto es solo para decidir si Ayla debe verse corriendo o no
        // En fases automáticas siempre se ve corriendo
        if (phase == Phase.INTRO_ENTER) return true;
        if (phase == Phase.AFTER_BOSS_WALK) return true;
        if (phase == Phase.TREASURE_APPROACH) return true;
        if (phase == Phase.TREASURE_LOCK) return true;

        // En play depende del input
        boolean leftKey = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean rightKey = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);

        boolean left = leftKey || controls.leftPressed;
        boolean right = rightKey || controls.rightPressed;

        // Devuelve true cuando solo se pulsa una dirección
        return left ^ right;
    }

    private String formatTime(float seconds) {

        // Paso segundos a un texto mm:ss
        int total = (int) seconds;
        int min = total / 60;
        int sec = total % 60;
        return String.format("%02d:%02d", min, sec);
    }

    private int clampLives(int v) {

        // Esto asegura que las vidas siempre estén entre 0 y MAX_LIVES
        if (v < 0) return 0;
        if (v > MAX_LIVES) return MAX_LIVES;
        return v;
    }

    @Override
    public void resize(int width, int height) {

        // Actualizo viewport del mundo y de la UI de controles
        viewport.update(width, height, true);
        controls.resize(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {

        // Libero cosas que he creado manualmente en esta pantalla
        if (controls != null) controls.dispose();
        if (curseParticles != null) curseParticles.dispose();
    }
}
