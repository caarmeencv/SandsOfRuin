package com.carmen.mijuego.world;

import com.badlogic.gdx.math.MathUtils;

public class SpawnDirector {

    // Tipos de spawns posibles (cactus, soldado o tanque)
    public enum SpawnType { CACTUS, SOLDIER, TANK }

    // Manager que controla la aparición y lista de cactus
    private final CactusManager cactusManager;

    // Manager que controla la aparición y lista de enemigos
    private final EnemyManager enemyManager;

    // Separación mínima entre cactus para que no aparezcan pegados
    private final float cactusMinSep = 140f;

    // Separación mínima entre enemigos (se calcula según el ancho de pantalla)
    private final float enemyMinSep;

    // Próxima X a partir de la cual se permite spawnear cactus
    private float nextCactusSpawnX;

    // Próxima X a partir de la cual se permite spawnear enemigos
    private float nextEnemySpawnX;

    // Última X en la que se puso un cactus
    private float lastCactusX = -999999f;

    // Última X en la que se puso un enemigo
    private float lastEnemyX = -999999f;

    // Alterna el color del cactus para que no se repita siempre el mismo
    private boolean cactusColorFlip;

    // Probabilidad de que salga un cactus doble
    private static final float DOUBLE_CACTUS_CHANCE = 0.08f;

    // Distancia mínima y máxima para colocar el segundo cactus
    private static final float DOUBLE_MIN_OFFSET = 180f;
    private static final float DOUBLE_MAX_OFFSET = 260f;

    // En fase mixta, controla si toca oleada de tanque o de soldado
    private boolean f4WaveIsTank;

    // Guardamos la fase anterior para detectar cambios
    private LevelConfig.Phase lastPhase;

    // Máximo de soldados a la vez en pantalla
    private static final int MAX_SOLDIERS_ON_SCREEN = 3;

    // Máximo de tanques a la vez en pantalla
    private static final int MAX_TANKS_ON_SCREEN = 2;

    // Detecta cuando se pasa de "había enemigos" a "ya no hay enemigos"
    private boolean enemyWasAliveLastFrame;

    // Activa un hueco extra después de eliminar enemigos
    private boolean extraCooldownArmed;

    // Constructor que recibe los managers y calcula la separación mínima de enemigos
    public SpawnDirector(CactusManager cactusManager, EnemyManager enemyManager,
                         float viewportWidth, float margin, float globalGap) {

        this.cactusManager = cactusManager;
        this.enemyManager = enemyManager;

        // La separación mínima depende del tamaño de la pantalla y del gap global
        this.enemyMinSep = Math.max(viewportWidth * 0.60f, globalGap);
    }

    // Reinicia el sistema de spawns al empezar el nivel
    public void reset(float firstSpawnX) {

        // Primer cactus un poco antes para que ya haya ambiente al moverte
        nextCactusSpawnX = firstSpawnX - 600f;

        // Primer enemigo a partir del inicio
        nextEnemySpawnX = firstSpawnX;

        // Reseteo de últimas posiciones
        lastCactusX = lastEnemyX = -999999f;

        // Empezamos sin alternancia de color
        cactusColorFlip = false;

        // En fase mixta empezamos por soldados
        f4WaveIsTank = false;

        // Forzamos detección de fase en el primer update
        lastPhase = null;

        // Reseteo del control de enemigos vivos
        enemyWasAliveLastFrame = false;
        extraCooldownArmed = false;
    }

    // Actualiza spawns según la fase y lo que se ve en cámara
    public void update(LevelConfig.Phase phase, float camLeft, float camRight) {

        // En esta fase no queremos que aparezca nada
        if (phase == LevelConfig.Phase.F5_PYRAMID_FREEZE) return;

        // Si cambia la fase, reseteamos ciertas cosas
        if (phase != lastPhase) {
            if (phase == LevelConfig.Phase.F4_CACTUS_SOLDIERS_TANKS) f4WaveIsTank = false;
            lastPhase = phase;
        }

        // Si la cámara ya llegó al punto de spawn, sacamos cactus
        if (camRight >= nextCactusSpawnX) {

            // Los cactus se generan un poco por delante para que entren en pantalla más tarde
            spawnCactusSingleOrDouble(camRight + 380f);

            // Marcamos cuándo tocará el siguiente cactus
            nextCactusSpawnX = camRight + nextGapCactus(phase);
        }

        // En la fase 1 solo hay cactus
        if (phase == LevelConfig.Phase.F1_CACTUS) return;

        // Miramos si hay algún enemigo vivo actualmente
        boolean anyAlive = enemyManager.hasAnyAliveSoldier() || enemyManager.hasAnyAliveTank();

        // Si antes había enemigos y ahora no, armamos un cooldown extra
        if (enemyWasAliveLastFrame && !anyAlive) extraCooldownArmed = true;

        // Guardamos el estado para el siguiente frame
        enemyWasAliveLastFrame = anyAlive;

        // Si toca cooldown extra y sigue sin haber enemigos, retrasamos el próximo spawn
        if (extraCooldownArmed && !anyAlive) {
            nextEnemySpawnX = camRight + extraEnemyCooldownGap(phase);
            extraCooldownArmed = false;
            return;
        }

        // Si aún no llegamos al punto de spawn, no hacemos nada
        if (camRight < nextEnemySpawnX) return;

        // En fase mixta alternamos soldado y tanque cuando ya no queda ninguno vivo
        if (phase == LevelConfig.Phase.F4_CACTUS_SOLDIERS_TANKS) {
            if (!enemyManager.hasAnyAliveSoldier() && !enemyManager.hasAnyAliveTank()) {
                f4WaveIsTank = !f4WaveIsTank;
            }
        }

        // Elegimos qué tipo toca según la fase
        SpawnType type = chooseEnemyType(phase);

        // Evitamos mezclar tanque y soldado vivos al mismo tiempo
        if (type == SpawnType.SOLDIER && enemyManager.hasAnyAliveTank()) return;
        if (type == SpawnType.TANK && enemyManager.hasAnyAliveSoldier()) return;

        // Limitamos la cantidad máxima de enemigos visibles
        if (type == SpawnType.SOLDIER &&
            enemyManager.countActiveSoldiers(camLeft, camRight) >= MAX_SOLDIERS_ON_SCREEN) return;

        if (type == SpawnType.TANK &&
            enemyManager.countActiveTanks(camLeft, camRight) >= MAX_TANKS_ON_SCREEN) return;

        // Generamos el enemigo un poco por delante de la cámara
        spawnEnemy(type, camRight + baseAheadEnemy(phase));

        // Marcamos cuándo tocará el siguiente enemigo
        nextEnemySpawnX = camRight + nextGapEnemy(phase);
    }

    // Reserva una posición X para cactus respetando separación mínima
    private float reserveCactusX(float x, float sep) {

        // Nos aseguramos de usar siempre una separación mínima segura
        float s = Math.max(sep, cactusMinSep);

        // Si se solapa con el último cactus, lo empujamos hacia delante
        if (x < lastCactusX + s) x = lastCactusX + s;

        // Guardamos esta posición como la última usada
        lastCactusX = x;

        return x;
    }

    // Reserva una posición X para enemigos respetando separación mínima
    private float reserveEnemyX(float x) {

        // Si se solapa con el último enemigo, lo empujamos hacia delante
        if (x < lastEnemyX + enemyMinSep) x = lastEnemyX + enemyMinSep;

        // Guardamos esta posición como la última usada
        lastEnemyX = x;

        return x;
    }

    // Genera uno o dos cactus con probabilidad de cactus doble
    private void spawnCactusSingleOrDouble(float baseX) {

        // Primer cactus
        float x1 = reserveCactusX(baseX, cactusMinSep);

        // Alternamos el color para dar variedad visual
        cactusColorFlip = !cactusColorFlip;
        cactusManager.spawnCactusAt(x1, cactusColorFlip);

        // Con una probabilidad pequeña, generamos un segundo cactus
        if (MathUtils.randomBoolean(DOUBLE_CACTUS_CHANCE)) {

            // Separación aleatoria para el segundo cactus
            float x2 = reserveCactusX(
                x1 + MathUtils.random(DOUBLE_MIN_OFFSET, DOUBLE_MAX_OFFSET),
                DOUBLE_MIN_OFFSET
            );

            // Volvemos a alternar el color para que sean diferentes
            cactusColorFlip = !cactusColorFlip;
            cactusManager.spawnCactusAt(x2, cactusColorFlip);
        }
    }

    // Genera un enemigo del tipo indicado en la posición indicada
    private void spawnEnemy(SpawnType type, float x) {

        // Ajustamos la posición para no solaparlo con spawns anteriores
        x = reserveEnemyX(x);

        // Spawn según tipo
        if (type == SpawnType.SOLDIER) enemyManager.spawnSoldierAt(x);
        else if (type == SpawnType.TANK) enemyManager.spawnTankAt(x);
    }

    // Elige el tipo de enemigo según la fase
    private SpawnType chooseEnemyType(LevelConfig.Phase phase) {
        switch (phase) {
            case F2_CACTUS_SOLDIERS: return SpawnType.SOLDIER;
            case F3_CACTUS_TANKS: return SpawnType.TANK;

            // En fase mixta alternamos según la bandera
            case F4_CACTUS_SOLDIERS_TANKS:
                return f4WaveIsTank ? SpawnType.TANK : SpawnType.SOLDIER;

            // Por defecto soldado
            default: return SpawnType.SOLDIER;
        }
    }

    // Distancia base a la que aparece el enemigo por delante de la cámara
    private float baseAheadEnemy(LevelConfig.Phase phase) {
        switch (phase) {
            case F2_CACTUS_SOLDIERS: return 850f;
            case F3_CACTUS_TANKS:
            case F4_CACTUS_SOLDIERS_TANKS: return 900f;
            default: return 850f;
        }
    }

    // Distancia aleatoria hasta el siguiente cactus según la fase
    private float nextGapCactus(LevelConfig.Phase phase) {
        switch (phase) {
            case F1_CACTUS: return MathUtils.random(950f, 1700f);
            case F2_CACTUS_SOLDIERS:
            case F3_CACTUS_TANKS: return MathUtils.random(1100f, 2000f);
            case F4_CACTUS_SOLDIERS_TANKS: return MathUtils.random(1200f, 2200f);
            default: return MathUtils.random(1100f, 2000f);
        }
    }

    // Distancia aleatoria hasta el siguiente enemigo según la fase
    private float nextGapEnemy(LevelConfig.Phase phase) {
        switch (phase) {
            case F2_CACTUS_SOLDIERS: return MathUtils.random(850f, 1400f);
            case F3_CACTUS_TANKS: return MathUtils.random(1000f, 1600f);
            case F4_CACTUS_SOLDIERS_TANKS: return MathUtils.random(900f, 1500f);
            default: return MathUtils.random(900f, 1500f);
        }
    }

    // Distancia extra tras eliminar enemigos antes de permitir otro spawn
    private float extraEnemyCooldownGap(LevelConfig.Phase phase) {
        switch (phase) {
            case F2_CACTUS_SOLDIERS: return MathUtils.random(900f, 1600f);
            case F3_CACTUS_TANKS: return MathUtils.random(1100f, 1900f);
            case F4_CACTUS_SOLDIERS_TANKS: return MathUtils.random(1000f, 1800f);
            default: return MathUtils.random(1000f, 1700f);
        }
    }
}
