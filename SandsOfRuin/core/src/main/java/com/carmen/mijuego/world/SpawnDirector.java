package com.carmen.mijuego.world;

import com.badlogic.gdx.math.MathUtils;

public class SpawnDirector {

    public enum SpawnType { CACTUS, SOLDIER, TANK }

    private final CactusManager cactusManager;
    private final EnemyManager enemyManager;

    // Separación mínima dentro de cada “stream”
    private final float cactusMinSep;
    private final float enemyMinSep;

    // colas independientes
    private float nextCactusSpawnX = 0f;
    private float nextEnemySpawnX  = 0f;

    // últimas X por tipo (no global)
    private float lastCactusX = -999999f;
    private float lastEnemyX  = -999999f;

    private boolean cactusColorFlip = false;

    // doble cactus (más raro)
    private static final float DOUBLE_CACTUS_CHANCE = 0.08f; // 8%
    private static final float DOUBLE_MIN_OFFSET = 180f;
    private static final float DOUBLE_MAX_OFFSET = 260f;

    // F4: oleadas alternas
    private boolean f4WaveIsTank = false;
    private LevelConfig.Phase lastPhase = null;

    // límites cerca de cámara
    private static final int MAX_SOLDIERS_ON_SCREEN = 3;
    private static final int MAX_TANKS_ON_SCREEN    = 2;

    // ✅ cooldown extra tras limpiar enemigos (evita “sale otro instantáneo”)
    private boolean enemyWasAliveLastFrame = false;
    private boolean extraCooldownArmed = false;

    public SpawnDirector(CactusManager cactusManager,
                         EnemyManager enemyManager,
                         float viewportWidth,
                         float margin,
                         float globalGap) {
        this.cactusManager = cactusManager;
        this.enemyManager = enemyManager;

        // ✅ CLAVE para que la distancia NO parezca siempre igual:
        // min sep pequeño, y que la aleatoriedad la marque el gap
        this.cactusMinSep = 140f;

        // enemigos separados pero sin depender de cactus
        this.enemyMinSep  = Math.max(viewportWidth * 0.60f, globalGap);
    }

    public void reset(float firstSpawnX) {
        // ✅ Cactus “antes” para que al empezar ya haya que hacer algo
        // (si firstSpawnX es camRight+algo, lo adelantamos)
        nextCactusSpawnX = firstSpawnX - 600f;

        nextEnemySpawnX  = firstSpawnX;

        lastCactusX = -999999f;
        lastEnemyX  = -999999f;

        cactusColorFlip = false;

        f4WaveIsTank = false;
        lastPhase = null;

        enemyWasAliveLastFrame = false;
        extraCooldownArmed = false;
    }

    // ===================== Helpers =====================

    private void onPhaseChanged(LevelConfig.Phase phase) {
        if (phase == lastPhase) return;

        if (phase == LevelConfig.Phase.F4_CACTUS_SOLDIERS_TANKS) {
            f4WaveIsTank = false; // empieza con soldados
        }
        lastPhase = phase;
    }

    private int countSoldiersNearCam(float camLeft, float camRight) {
        return enemyManager.countActiveSoldiers(camLeft, camRight);
    }

    private int countTanksNearCam(float camLeft, float camRight) {
        return enemyManager.countActiveTanks(camLeft, camRight);
    }

    // ===================== Reservas por tipo =====================

    private float reserveCactusX(float proposedX, float sep) {
        float separation = Math.max(sep, cactusMinSep);
        if (proposedX < lastCactusX + separation) proposedX = lastCactusX + separation;
        lastCactusX = proposedX;
        return proposedX;
    }

    private float reserveEnemyX(float proposedX) {
        float separation = enemyMinSep;
        if (proposedX < lastEnemyX + separation) proposedX = lastEnemyX + separation;
        lastEnemyX = proposedX;
        return proposedX;
    }

    // ===================== Spawn =====================

    private void spawnCactusSingleOrDouble(float baseX) {
        // cactus 1
        float x1 = reserveCactusX(baseX, cactusMinSep);
        cactusColorFlip = !cactusColorFlip;
        cactusManager.spawnCactusAt(x1, cactusColorFlip);

        // cactus 2 (a veces) cerca para doble salto
        if (MathUtils.randomBoolean(DOUBLE_CACTUS_CHANCE)) {
            float offset = MathUtils.random(DOUBLE_MIN_OFFSET, DOUBLE_MAX_OFFSET);
            float x2 = reserveCactusX(x1 + offset, DOUBLE_MIN_OFFSET);
            cactusColorFlip = !cactusColorFlip;
            cactusManager.spawnCactusAt(x2, cactusColorFlip);
        }
    }

    private void spawnEnemy(SpawnType type, float x) {
        x = reserveEnemyX(x);
        if (type == SpawnType.SOLDIER) enemyManager.spawnSoldierAt(x);
        else if (type == SpawnType.TANK) enemyManager.spawnTankAt(x);
    }

    // ===================== Update =====================

    public void update(LevelConfig.Phase phase, float camLeft, float camRight) {

        onPhaseChanged(phase);

        // F5: nada
        if (phase == LevelConfig.Phase.F5_PYRAMID_FREEZE) return;

        // ✅ CACTUS SIEMPRE (F1-F4)
        if (camRight >= nextCactusSpawnX) {
            float cactusX = camRight + baseAheadCactus(phase);
            spawnCactusSingleOrDouble(cactusX);
            nextCactusSpawnX = camRight + nextGapCactus(phase); // ✅ mucho más aleatorio y menos denso
        }

        // F1: solo cactus
        if (phase == LevelConfig.Phase.F1_CACTUS) return;

        // ===== lógica de “cooldown extra” cuando limpias enemigos =====
        boolean anyEnemyAliveNow = enemyManager.hasAnyAliveSoldier() || enemyManager.hasAnyAliveTank();

        // Si pasamos de “había enemigos” a “ya no hay ninguno”, armamos cooldown extra
        if (enemyWasAliveLastFrame && !anyEnemyAliveNow) {
            extraCooldownArmed = true;
        }
        enemyWasAliveLastFrame = anyEnemyAliveNow;

        // Si hay cooldown armado, ponemos nextEnemySpawnX más adelante y salimos
        if (extraCooldownArmed && !anyEnemyAliveNow) {
            // ✅ espera extra aleatoria antes del siguiente enemigo
            nextEnemySpawnX = camRight + extraEnemyCooldownGap(phase);
            extraCooldownArmed = false;
            return;
        }

        // si aún no toca enemigo
        if (camRight < nextEnemySpawnX) return;

        // ✅ F4: alterna oleadas SOLO cuando no queda nadie vivo (global)
        if (phase == LevelConfig.Phase.F4_CACTUS_SOLDIERS_TANKS) {
            if (!enemyManager.hasAnyAliveSoldier() && !enemyManager.hasAnyAliveTank()) {
                f4WaveIsTank = !f4WaveIsTank;
            }
        }

        SpawnType enemyType = chooseEnemyType(phase);

        // ✅ CLAVE: nunca soldado y tanque juntos (global)
        if (enemyType == SpawnType.SOLDIER && enemyManager.hasAnyAliveTank()) return;
        if (enemyType == SpawnType.TANK && enemyManager.hasAnyAliveSoldier()) return;

        // ✅ límites cerca de cámara
        if (enemyType == SpawnType.SOLDIER) {
            if (countSoldiersNearCam(camLeft, camRight) >= MAX_SOLDIERS_ON_SCREEN) return;
        } else if (enemyType == SpawnType.TANK) {
            if (countTanksNearCam(camLeft, camRight) >= MAX_TANKS_ON_SCREEN) return;
        }

        float enemyX = camRight + baseAheadEnemy(phase);
        spawnEnemy(enemyType, enemyX);

        // ✅ siguiente enemigo más lento/aleatorio
        nextEnemySpawnX = camRight + nextGapEnemy(phase);
    }

    private SpawnType chooseEnemyType(LevelConfig.Phase phase) {
        switch (phase) {
            case F2_CACTUS_SOLDIERS:
                return SpawnType.SOLDIER;

            case F3_CACTUS_TANKS:
                return SpawnType.TANK;

            case F4_CACTUS_SOLDIERS_TANKS:
                return f4WaveIsTank ? SpawnType.TANK : SpawnType.SOLDIER;

            default:
                return SpawnType.SOLDIER;
        }
    }

    // ===================== Distancias =====================

    private float baseAheadCactus(LevelConfig.Phase phase) {
        // ✅ más cerca para que al empezar “pase algo” pronto
        return 380f;
    }

    private float nextGapCactus(LevelConfig.Phase phase) {
        // ✅ Menos cactus + distancia realmente aleatoria entre ellos
        // (grandes rangos => no parecen “a la misma distancia”)
        switch (phase) {
            case F1_CACTUS:
                return MathUtils.random(950f, 1700f);
            case F2_CACTUS_SOLDIERS:
            case F3_CACTUS_TANKS:
                return MathUtils.random(1100f, 2000f);
            case F4_CACTUS_SOLDIERS_TANKS:
                return MathUtils.random(1200f, 2200f);
            default:
                return MathUtils.random(1100f, 2000f);
        }
    }

    private float baseAheadEnemy(LevelConfig.Phase phase) {
        // enemigos un poquito más lejos para reacción
        switch (phase) {
            case F2_CACTUS_SOLDIERS:        return 850f;
            case F3_CACTUS_TANKS:           return 900f;
            case F4_CACTUS_SOLDIERS_TANKS:  return 900f;
            default:                        return 850f;
        }
    }

    private float nextGapEnemy(LevelConfig.Phase phase) {
        // ✅ más aleatorio y menos “metralleta”
        switch (phase) {
            case F2_CACTUS_SOLDIERS:
                return MathUtils.random(850f, 1400f);
            case F3_CACTUS_TANKS:
                return MathUtils.random(1000f, 1600f);
            case F4_CACTUS_SOLDIERS_TANKS:
                return MathUtils.random(900f, 1500f);
            default:
                return MathUtils.random(900f, 1500f);
        }
    }

    private float extraEnemyCooldownGap(LevelConfig.Phase phase) {
        // ✅ espera extra tras limpiar un enemigo para que no aparezca “automático”
        switch (phase) {
            case F2_CACTUS_SOLDIERS:
                return MathUtils.random(900f, 1600f);
            case F3_CACTUS_TANKS:
                return MathUtils.random(1100f, 1900f);
            case F4_CACTUS_SOLDIERS_TANKS:
                return MathUtils.random(1000f, 1800f);
            default:
                return MathUtils.random(1000f, 1700f);
        }
    }
}
