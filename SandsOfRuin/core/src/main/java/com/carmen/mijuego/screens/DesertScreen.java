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
import com.carmen.mijuego.input.AccelerometerJumpDetector;
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

    // Tamaño del mundo del desierto
    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    // Altura del suelo del desierto
    private static final float GROUND_Y = 180f;

    // Velocidad de avance y retroceso del scroll
    private static final float SCROLL_SPEED_FORWARD = 320f;
    private static final float SCROLL_SPEED_BACK = 260f;

    // Posición fija de Ayla en pantalla
    // En realidad Ayla se queda casi quieta y lo que se mueve es el mundo mediante scrollX
    private static final float AYLA_SCREEN_X = 220f;

    // Multiplicador del parallax para que el fondo se mueva más lento que el suelo
    private static final float PARALLAX_MUL = 0.70f;

    // Retroceso cuando Ayla recibe daño
    private static final float KNOCKBACK_DISTANCE = 140f;
    private static final float KNOCKBACK_SPEED = 900f;

    // Distancia mínima entre spawns para que no aparezcan enemigos pegados
    private static final float GLOBAL_SPAWN_GAP = 260f;

    // Zona antes de la entrada a la pirámide donde ya no quieres que spawneen enemigos
    private static final float NO_SPAWN_ZONE_BEFORE_F5 = 2800f;

    // Velocidades para la escena final donde vas hacia la pirámide
    private static final float AUTO_SCROLL_SPEED = 320f;
    private static final float AYLA_WALK_OFF_SPEED = 280f;
    private static final float EXIT_MARGIN = 80f;

    // Punto donde quieres parar el auto scroll para que la entrada se vea en un sitio bonito de la pantalla
    private static final float ENTRANCE_TARGET_SCREEN_X = 520f;

    // Alturas base donde se dibujan los decorados especiales
    private static final float SPHINX_Y = GROUND_Y - 10f;
    private static final float ENTRANCE_Y = GROUND_Y - 20f;

    // Escalas de los decorados porque seguramente los png son enormes
    private static final float SPHINX_SCALE = 0.55f;
    private static final float ENTRANCE_SCALE = 0.60f;

    // Posición de la esfinge dentro del tramo final y separación entre esfinge y entrada
    private static final float SPHINX_OFFSET_IN_F5 = 300f;
    private static final float DECOR_GAP = 1200f;

    // Inmunidad al volver de pausa, igual que en la pirámide
    private static final float RESUME_IMMUNITY_TIME = 2.0f;

    // Esto sirve para que cuando empieza la cinemática sigas viendo enemigos un momento y luego desaparezcan
    private static final float HIDE_ENEMIES_DELAY = 0.8f;

    // Estado de la cinemática final
    // NONE es juego normal
    // AUTO_SCROLL_SHOW_DECOR es auto scroll para enseñar esfinge y entrada
    // FREEZE_AND_AYLA_WALKS_OFF es cámara quieta y Ayla sale caminando para cambiar de pantalla
    private enum CutsceneState {
        NONE,
        AUTO_SCROLL_SHOW_DECOR,
        FREEZE_AND_AYLA_WALKS_OFF
    }

    // Referencia al juego principal
    private final Main game;

    // Cámara y viewport
    private OrthographicCamera camera;
    private Viewport viewport;

    // Capas de fondo
    private Texture sky;
    private Texture clouds;
    private Texture ruins;
    private Texture mid;
    private Texture near;

    // Decorados especiales del final
    private Texture sphinxTex;
    private Texture entranceTex;

    // Personaje y controles
    private Ayla ayla;
    private Controls controls;

    // Sistema de parallax del desierto
    private ParallaxBackground parallax;

    // HUD de vidas
    private LivesHUD livesHUD;

    // Managers de enemigos y cactus
    private CactusManager cactusManager;
    private EnemyManager enemyManager;

    // Balas de tanque
    private TankBulletSystem tankBulletSystem;

    // Sistema de colisiones general
    private CollisionSystem collisionSystem;

    // Director de spawns que decide cuándo crear enemigos según la fase del nivel
    private SpawnDirector spawnDirector;

    // Para dibujar hitboxes en modo debug
    private ShapeRenderer shapeRenderer;
    private boolean debugHitboxes;

    // Variables para la cinemática
    private CutsceneState cutsceneState;
    private boolean cutsceneStarted;

    // scrollX es cuánto has avanzado en el nivel
    // knockRemaining es cuánto retroceso queda por aplicar
    private float scrollX;
    private float knockRemaining;

    // Posiciones X del mundo para esfinge y entrada
    private float sphinxX;
    private float entranceX;

    // Para volver de pausa con cámara bien puesta e inmunidad
    private boolean snapCameraOnResume;
    private float resumeImmunity;

    // Para que el botón de pausa no se dispare muchas veces
    private boolean pauseLatch;

    // Temporizador para ocultar enemigos durante la cinemática
    private float hideEnemiesTimer;

    // Detector del acelerómetro para salto
    private AccelerometerJumpDetector accelJump;

    public DesertScreen(Main game) {
        this.game = game;

        // Estado inicial de pausa
        snapCameraOnResume = false;
        resumeImmunity = 0f;

        // Estado inicial de cinemática
        cutsceneState = CutsceneState.NONE;
        cutsceneStarted = false;

        // Estado inicial del scroll
        scrollX = 0f;
        knockRemaining = 0f;

        // Bloqueo para la pausa
        pauseLatch = false;

        // Temporizador de ocultar enemigos
        hideEnemiesTimer = 0f;

        // Debug apagado
        debugHitboxes = false;

        // Creo cámara y viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        camera.position.set(WORLD_W * 0.5f, WORLD_H * 0.5f, 0f);
        camera.update();

        // Cargo texturas de fondo desde el asset manager
        sky = game.assets.get(Assets.SKY);
        clouds = game.assets.get(Assets.CLOUDS);
        ruins = game.assets.get(Assets.RUINS);
        mid = game.assets.get(Assets.MID);
        near = game.assets.get(Assets.NEAR);

        // Cargo decorados
        sphinxTex = game.assets.get(Assets.SPHINX_PYRAMID);
        entranceTex = game.assets.get(Assets.ENTRANCE_PYRAMID);

        // Creo a Ayla en el desierto
        // Empieza en x 0 porque luego se recoloca con la cámara
        ayla = new Ayla(
            game.audio,
            game.assets.get(Assets.AYLA_RUN),
            game.assets.get(Assets.AYLA_IDLE),
            game.assets.get(Assets.AYLA_JUMP),
            game.assets.get(Assets.BULLET),
            game.assets.get(Assets.BULLET_SPECIAL),
            0f,
            GROUND_Y
        );

        // Le pongo las vidas que venían del juego
        ayla.setLives(game.vidas);

        // Creo los controles táctiles y los pongo como inputProcessor
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

        // Calculo alturas escaladas para las capas del parallax
        // scaledHeight hace que cada textura se adapte al ancho del mundo
        float cloudsH = scaledHeight(clouds);
        float ruinsH = scaledHeight(ruins);
        float midH = scaledHeight(mid);
        float nearH = scaledHeight(near);

        // repeat dice si la capa se repite en horizontal o no
        // ignoreSpeedMul te permite que una capa ignore el multiplicador global
        boolean[] repeat = new boolean[]{false, false, false, true};
        boolean[] ignoreSpeedMul = new boolean[]{false, false, false, true};

        // Creo el parallax background con todas las capas y sus velocidades relativas
        parallax = new ParallaxBackground(
            camera,
            viewport,
            sky,
            new Texture[]{clouds, ruins, mid, near},
            new float[]{0.08f, 0.15f, 0.30f, 1.00f},
            new float[]{0f, 0f, 0f, 0f},
            new float[]{cloudsH, ruinsH, midH, nearH},
            repeat,
            ignoreSpeedMul
        );
        parallax.setSpeedMul(PARALLAX_MUL);

        // HUD de vidas
        livesHUD = new LivesHUD(
            game.assets.get(Assets.HUD_HEART_FULL),
            game.assets.get(Assets.HUD_HEART_EMPTY)
        );

        // Manager de cactus que sabe crear cactus rosas y amarillos
        cactusManager = new CactusManager(
            game.assets.get(Assets.CACTUS_PINK),
            game.assets.get(Assets.CACTUS_YELLOW)
        );

        // Manager de enemigos con soldados y tanques
        enemyManager = new EnemyManager(
            game.audio,
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

        // Sistema de balas del tanque
        tankBulletSystem = new TankBulletSystem(game.assets.get(Assets.BULLET));

        // Sistema de colisiones
        collisionSystem = new CollisionSystem();

        // ShapeRenderer para debug de hitboxes
        shapeRenderer = new ShapeRenderer();

        // Director de spawn, usa el ancho del viewport y un margen para spawnear fuera de cámara
        float viewportW = viewport.getWorldWidth();
        float margin = 500f;

        spawnDirector = new SpawnDirector(
            cactusManager,
            enemyManager,
            viewportW,
            margin,
            GLOBAL_SPAWN_GAP
        );

        // Inicializo el spawn director con una posición de inicio
        float camRight = camera.position.x + viewport.getWorldWidth() * 0.5f;
        spawnDirector.reset(camRight + 40f);

        // Coloco los decorados del final basándome en LevelConfig
        sphinxX = LevelConfig.F4_END + SPHINX_OFFSET_IN_F5;
        entranceX = sphinxX + DECOR_GAP;

        // Acelerómetro y modo de salto según settings
        accelJump = new AccelerometerJumpDetector();
        controls.setAccelJumpEnabled(game.settings.isAccelJumpEnabled());
    }

    private float scaledHeight(Texture tex) {

        // Esto calcula qué alto queda la textura si la estiras para ocupar todo el ancho del mundo
        float scale = WORLD_W / (float) tex.getWidth();
        return tex.getHeight() * scale;
    }

    private String formatTime(float seconds) {

        // Pasa segundos a texto mm:ss
        int total = (int) seconds;
        int min = total / 60;
        int sec = total % 60;

        // Aquí haces el formato para que siempre haya dos dígitos
        // Si sec es menor que 10, le pones un cero delante
        if (sec < 10) return String.format("%02d:0%d", min, sec);
        return String.format("%02d:%d", min, sec);
    }

    private void drawSphinxIfVisible(float camLeft, float camRight) {

        // Dibuja la esfinge solo si está cerca de la cámara
        if (sphinxTex == null) return;

        float w = sphinxTex.getWidth() * SPHINX_SCALE;
        float h = sphinxTex.getHeight() * SPHINX_SCALE;

        // Si está muy a la izquierda o derecha, no la dibujo
        if (sphinxX + w < camLeft - 300f) return;
        if (sphinxX > camRight + 300f) return;

        game.batch.draw(sphinxTex, sphinxX, SPHINX_Y, w, h);
    }

    private void drawEntranceIfVisible(float camLeft, float camRight) {

        // Dibuja la entrada solo si está cerca de la cámara
        if (entranceTex == null) return;

        float w = entranceTex.getWidth() * ENTRANCE_SCALE;
        float h = entranceTex.getHeight() * ENTRANCE_SCALE;

        if (entranceX + w < camLeft - 300f) return;
        if (entranceX > camRight + 300f) return;

        game.batch.draw(entranceTex, entranceX, ENTRANCE_Y, w, h);
    }

    private void debugRender() {

        // Esto dibuja rectángulos de hitbox con colores para ver si colisiona bien
        // Solo se llama si debugHitboxes está activo
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(ayla.getBounds().x, ayla.getBounds().y, ayla.getBounds().width, ayla.getBounds().height);

        shapeRenderer.setColor(Color.GREEN);
        Array<Cactus> cactuses = cactusManager.getCactuses();
        for (int i = 0; i < cactuses.size; i++) {
            Cactus c = cactuses.get(i);
            shapeRenderer.rect(c.getBounds().x, c.getBounds().y, c.getBounds().width, c.getBounds().height);
        }

        Array<Soldier> soldiers = enemyManager.getSoldiers();
        for (int i = 0; i < soldiers.size; i++) {
            Soldier s = soldiers.get(i);

            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(s.getBounds().x, s.getBounds().y, s.getBounds().width, s.getBounds().height);

            shapeRenderer.setColor(Color.YELLOW);
            Array<Bullet> sb = s.getBullets();
            for (int b = 0; b < sb.size; b++) {
                Bullet bb = sb.get(b);
                shapeRenderer.rect(bb.getBounds().x, bb.getBounds().y, bb.getBounds().width, bb.getBounds().height);
            }
        }

        shapeRenderer.setColor(Color.MAGENTA);
        Array<Tank> tanks = enemyManager.getTanks();
        for (int i = 0; i < tanks.size; i++) {
            Tank t = tanks.get(i);
            shapeRenderer.rect(t.getBounds().x, t.getBounds().y, t.getBounds().width, t.getBounds().height);
        }

        shapeRenderer.setColor(Color.ORANGE);
        Array<Bullet> tb = tankBulletSystem.getBullets();
        for (int i = 0; i < tb.size; i++) {
            Bullet bb = tb.get(i);
            shapeRenderer.rect(bb.getBounds().x, bb.getBounds().y, bb.getBounds().width, bb.getBounds().height);
        }

        shapeRenderer.end();
    }

    @Override
    public void show() {

        // Cuando entras al desierto, pones música, activas input y reseteas estados
        game.audio.playMusic(Assets.MUS_DESERT_THEME, true);
        Gdx.input.setInputProcessor(controls);
        controls.resetAll();
        pauseLatch = false;
        accelJump.reset();
    }

    @Override
    public void render(float delta) {

        // Bajo inmunidad al volver de pausa
        if (resumeImmunity > 0f) {
            resumeImmunity -= delta;
            if (resumeImmunity < 0f) resumeImmunity = 0f;
        }

        // Limpio pantalla
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        // Tiempo global del juego que continúa al pasar a la pirámide
        game.runTimeSeconds += delta;
        controls.setCounterText(formatTime(game.runTimeSeconds));

        // Activo o desactivo botón de salto según settings
        boolean accelMode = game.settings.isAccelJumpEnabled();
        controls.setAccelJumpEnabled(accelMode);

        // Límites del scroll para no salirte del nivel
        float viewportW = viewport.getWorldWidth();
        float maxScrollX = LevelConfig.DESERT_LENGTH - viewportW;
        if (maxScrollX < 0f) maxScrollX = 0f;

        // Teclado para pruebas en PC
        boolean leftKey = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean rightKey = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);

        boolean jumpKey = Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.W)
            || Gdx.input.isKeyJustPressed(Input.Keys.UP);

        boolean shootKey = Gdx.input.isKeyPressed(Input.Keys.K)
            || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);

        boolean grenadeKey = Gdx.input.isKeyJustPressed(Input.Keys.L);
        boolean pauseKey = Gdx.input.isKeyPressed(Input.Keys.ESCAPE);

        // Mezcla de teclado y táctil
        boolean left = leftKey || controls.leftPressed;
        boolean right = rightKey || controls.rightPressed;
        boolean shoot = shootKey || controls.shootPressed;
        boolean grenade = grenadeKey || controls.grenadePressed;

        // Salto cambia según modo acelerómetro
        boolean jump;
        if (accelMode) {
            boolean a = accelJump.updateAndConsume(delta);
            boolean f = jumpKey || controls.jumpPressed;
            if (a) jump = true;
            else jump = f;
        } else {
            jump = jumpKey || controls.jumpPressed;
        }

        // Pausa
        boolean pauseNow = pauseKey || controls.pausePressed;

        if (pauseNow && !pauseLatch) {
            pauseLatch = true;
            controls.resetAll();

            snapCameraOnResume = true;
            resumeImmunity = RESUME_IMMUNITY_TIME;

            ayla.stopAllLoops();

            game.setScreen(new PauseScreen(game, this, PauseScreen.Context.DESERT));
            return;
        }
        if (!pauseNow) pauseLatch = false;

        // Movimiento del scroll según si estás jugando o en cinemática
        if (cutsceneState == CutsceneState.NONE) {

            // Si hay retroceso por daño, lo aplico primero
            if (knockRemaining > 0f) {
                float step = KNOCKBACK_SPEED * delta;
                if (step > knockRemaining) step = knockRemaining;

                scrollX -= step;
                if (scrollX < 0f) scrollX = 0f;

                knockRemaining -= step;
                if (knockRemaining < 0f) knockRemaining = 0f;
            } else {

                // Scroll normal con controles
                if (right) scrollX += SCROLL_SPEED_FORWARD * delta;
                if (left) scrollX -= SCROLL_SPEED_BACK * delta;
                if (scrollX < 0f) scrollX = 0f;
            }

            // Nunca paso el final del nivel
            if (scrollX > maxScrollX) scrollX = maxScrollX;

        } else if (cutsceneState == CutsceneState.AUTO_SCROLL_SHOW_DECOR) {

            // Auto scroll para enseñar decorado y acercarte a la entrada
            scrollX += AUTO_SCROLL_SPEED * delta;
            if (scrollX > maxScrollX) scrollX = maxScrollX;
        }

        // Cámara sigue al scroll, salvo en la parte congelada
        float targetCamX = scrollX + viewportW * 0.5f;
        if (cutsceneState != CutsceneState.FREEZE_AND_AYLA_WALKS_OFF) {
            if (snapCameraOnResume) {
                camera.position.x = targetCamX;
                camera.update();
                snapCameraOnResume = false;
            } else {
                camera.position.x += (targetCamX - camera.position.x) * 10f * delta;
                camera.update();
            }
        } else {
            camera.update();
        }

        float camLeft = camera.position.x - viewportW * 0.5f;
        float camRight = camera.position.x + viewportW * 0.5f;
        float camTop = camera.position.y + viewport.getWorldHeight() * 0.5f;

        // Para lógica de spawns usas el scroll como si fuera la cámara lógica
        float logicCamLeft = scrollX;
        float logicCamRight = scrollX + viewportW;

        // Averiguas la fase del nivel según dónde estás
        LevelConfig.Phase phase = LevelConfig.phaseFor(logicCamRight);

        // Aquí decides cuándo empieza la cinemática final
        if (!cutsceneStarted) {
            if (phase == LevelConfig.Phase.F5_PYRAMID_FREEZE) {
                cutsceneStarted = true;
                cutsceneState = CutsceneState.AUTO_SCROLL_SHOW_DECOR;

                // Empiezas a ocultar enemigos y limpias inputs y balas
                hideEnemiesTimer = HIDE_ENEMIES_DELAY;
                controls.resetAll();
                tankBulletSystem.clear();
            }
        }

        // Cuando la entrada llega al punto que quieres en pantalla, congelas la cámara y haces caminar a Ayla
        if (cutsceneState == CutsceneState.AUTO_SCROLL_SHOW_DECOR) {
            float entranceScreenX = entranceX - camLeft;
            if (entranceScreenX <= ENTRANCE_TARGET_SCREEN_X) {
                cutsceneState = CutsceneState.FREEZE_AND_AYLA_WALKS_OFF;
                parallax.setSpeedMul(0f);
            }
        }

        // En cinemática, el temporizador para ocultar enemigos va bajando
        if (cutsceneState != CutsceneState.NONE) {
            hideEnemiesTimer -= delta;
            if (hideEnemiesTimer < 0f) hideEnemiesTimer = 0f;
        }

        // Parallax parado o activo según estado
        if (cutsceneState == CutsceneState.FREEZE_AND_AYLA_WALKS_OFF) parallax.setSpeedMul(0f);
        else parallax.setSpeedMul(PARALLAX_MUL);

        // Esto es solo para animación visual de Ayla, para saber si debe verse corriendo
        boolean movingVisual = false;

        if (cutsceneState == CutsceneState.NONE) {

            // Juego normal, Ayla va fija en pantalla y el mundo se mueve
            ayla.setX(camLeft + AYLA_SCREEN_X);

            // Aquí estás usando un símbolo raro para saber si solo se pulsa una dirección
            // Si no lo quieres, lo puedes cambiar por un if que compruebe si left y right son distintos
            movingVisual = left ^ right;

            ayla.update(delta, left, right, jump, shoot, grenade, GROUND_Y, camLeft, camRight);

        } else if (cutsceneState == CutsceneState.AUTO_SCROLL_SHOW_DECOR) {

            // En auto scroll, Ayla camina sola hacia delante
            ayla.setX(camLeft + AYLA_SCREEN_X);
            movingVisual = true;
            ayla.update(delta, false, true, false, false, false, GROUND_Y, camLeft, camRight);

        } else if (cutsceneState == CutsceneState.FREEZE_AND_AYLA_WALKS_OFF) {

            // Aquí la cámara está quieta y Ayla se mueve de verdad hacia la derecha hasta salir de pantalla
            movingVisual = true;

            float newX = ayla.getX() + AYLA_WALK_OFF_SPEED * delta;
            ayla.setX(newX);

            // forceRun true hace que la animación sea correr aunque no haya input
            ayla.update(delta, false, false, false, false, false, GROUND_Y, camLeft, camRight, true);

            // Cuando sale de pantalla, guardas vidas y cambias a la pantalla de la pirámide
            if (ayla.getX() > camRight + EXIT_MARGIN) {
                game.vidas = ayla.getLives();
                ayla.stopAllLoops();
                game.setScreen(new PyramidScreen(game));
                return;
            }
        }

        // Actualización de enemigos, spawns y colisiones solo si estás jugando normal
        if (cutsceneState == CutsceneState.NONE) {

            // Zona sin spawns antes del final
            boolean noSpawnZone = logicCamRight >= (LevelConfig.F4_END - NO_SPAWN_ZONE_BEFORE_F5);
            if (!noSpawnZone) spawnDirector.update(phase, logicCamLeft, logicCamRight);

            // Actualizo cactus y enemigos
            cactusManager.update(logicCamLeft);
            enemyManager.update(delta, ayla.getX(), logicCamLeft, logicCamRight);

            // Actualizo balas de tanque
            tankBulletSystem.update(delta, logicCamLeft, logicCamRight, enemyManager.getTanks());

            // Colisiones centralizadas
            // onHit es lo que ocurre cuando algo hace daño a Ayla
            collisionSystem.update(
                delta,
                ayla,
                cactusManager.getCactuses(),
                enemyManager.getSoldiers(),
                enemyManager.getTanks(),
                tankBulletSystem.getBullets(),
                new Runnable() {
                    @Override
                    public void run() {

                        // Aplico retroceso siempre
                        knockRemaining = KNOCKBACK_DISTANCE;

                        // Si estás en inmunidad por volver de pausa, no aplico daño
                        if (resumeImmunity <= 0f) {
                            boolean damaged = ayla.takeDamage();
                            game.vidas = ayla.getLives();
                            if (damaged) game.vibrateHit(120);
                        }
                    }
                }
            );

            // Si muere, vas a game over
            if (ayla.isDead()) {
                game.vidas = ayla.getLives();
                game.audio.playSfx(Assets.SFX_GAME_OVER);
                ayla.stopAllLoops();
                game.setScreen(new GameOverScreen(game));
                return;
            }
        } else {

            // En cinemática solo actualizo cactus para que se limpien si quedan atrás
            cactusManager.update(logicCamLeft);
        }

        // Dibujado del mundo
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // Fondo parallax
        parallax.render(game.batch);

        // En cinemática puedes ocultar enemigos tras un rato
        boolean canDrawEnemies = true;
        if (cutsceneState != CutsceneState.NONE) {
            if (hideEnemiesTimer <= 0f) canDrawEnemies = false;
        }

        if (canDrawEnemies) {

            // Dibujo cactus
            Array<Cactus> cactuses = cactusManager.getCactuses();
            for (int i = 0; i < cactuses.size; i++) cactuses.get(i).draw(game.batch);

            // Dibujo soldados
            Array<Soldier> soldiers = enemyManager.getSoldiers();
            for (int i = 0; i < soldiers.size; i++) soldiers.get(i).draw(game.batch);

            // Dibujo tanques
            Array<Tank> tanks = enemyManager.getTanks();
            for (int i = 0; i < tanks.size; i++) tanks.get(i).draw(game.batch);

            // Dibujo balas de tanque
            tankBulletSystem.draw(game.batch);
        }

        // Si estás en cinemática, dibujo la esfinge
        if (cutsceneState != CutsceneState.NONE) drawSphinxIfVisible(camLeft, camRight);

        // Dibujo Ayla
        ayla.draw(game.batch, movingVisual);

        // Si estás en cinemática, dibujo la entrada
        if (cutsceneState != CutsceneState.NONE) drawEntranceIfVisible(camLeft, camRight);

        // Dibujo HUD de vidas, y solo muestro HUD como activo durante juego normal
        livesHUD.draw(game.batch, camLeft, camTop, ayla.getLives(), cutsceneState == CutsceneState.NONE);

        // Controles en pantalla solo durante juego normal
        if (cutsceneState == CutsceneState.NONE) {
            controls.beginUI(game.batch);
            controls.draw(game.batch, ayla.getNormalCooldownPercent(), ayla.getSpecialCooldownPercent());
        }

        game.batch.end();

        // Debug de hitboxes si está activado
        if (debugHitboxes) debugRender();
    }

    @Override
    public void resize(int w, int h) {

        // Ajusto viewport del mundo y de la UI
        viewport.update(w, h, true);
        controls.resize(w, h);
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

        // Libero el shapeRenderer porque lo creé con new
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}
