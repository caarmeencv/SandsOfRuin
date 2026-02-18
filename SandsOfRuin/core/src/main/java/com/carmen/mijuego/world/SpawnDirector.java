package com.carmen.mijuego.world;

import com.badlogic.gdx.math.MathUtils;

public class SpawnDirector {

    public enum SpawnType { CACTUS, SOLDIER, TANK }

    private final CactusManager cactusManager;
    private final EnemyManager enemyManager;

    private final float minSeparation; // viewportWidth + margin
    private final float globalGap;

    private float nextSpawnX = 0f;
    private float lastSpawnX = -999999f;

    private boolean cactusColorFlip = false;

    // F3: 1 solo tanque en toda la fase
    private boolean f3TankDone = false;

    public SpawnDirector(CactusManager cactusManager,
                         EnemyManager enemyManager,
                         float viewportWidth,
                         float margin,
                         float globalGap) {
        this.cactusManager = cactusManager;
        this.enemyManager = enemyManager;
        this.minSeparation = viewportWidth + margin;
        this.globalGap = globalGap;
    }

    public void reset(float firstSpawnX) {
        nextSpawnX = firstSpawnX;
        lastSpawnX = -999999f;
        cactusColorFlip = false;
        f3TankDone = false;
    }

    public boolean hasAnyActive(float camLeft, float camRight) {
        if (cactusManager.hasActive(camLeft, camRight)) return true;
        if (enemyManager.hasActiveSoldier(camLeft, camRight)) return true;
        if (enemyManager.hasActiveTank(camLeft, camRight)) return true;
        return false;
    }

    private float reserveX(float proposedX) {
        float sep = Math.max(minSeparation, globalGap);
        if (proposedX < lastSpawnX + sep) proposedX = lastSpawnX + sep;
        lastSpawnX = proposedX;
        return proposedX;
    }

    private void spawn(SpawnType type, float x) {
        x = reserveX(x);

        switch (type) {
            case CACTUS:
                cactusColorFlip = !cactusColorFlip;
                cactusManager.spawnCactusAt(x, cactusColorFlip);
                break;
            case SOLDIER:
                enemyManager.spawnSoldierAt(x);
                break;
            case TANK:
                enemyManager.spawnTankAt(x);
                break;
        }
    }

    public void update(LevelConfig.Phase phase, float camLeft, float camRight) {

        // F5: absolutamente nada
        if (phase == LevelConfig.Phase.F5_DECOR_NO_ENEMIES) return;

        if (hasAnyActive(camLeft, camRight)) return;
        if (camRight < nextSpawnX) return;

        SpawnType type = chooseSpawn(phase);

        if (phase == LevelConfig.Phase.F3_CACTUS_SOLDIERS_ONE_TANK && f3TankDone && type == SpawnType.TANK) {
            type = MathUtils.randomBoolean(0.55f) ? SpawnType.CACTUS : SpawnType.SOLDIER;
        }

        float spawnX = camRight + baseAhead(phase);
        spawn(type, spawnX);

        if (phase == LevelConfig.Phase.F3_CACTUS_SOLDIERS_ONE_TANK && type == SpawnType.TANK) {
            f3TankDone = true;
        }

        nextSpawnX = camRight + nextGap(phase);
    }

    private float baseAhead(LevelConfig.Phase phase) {
        switch (phase) {
            case F1_CACTUS:
                // ✅ MUY pronto
                return 80f;
            case F2_CACTUS_SOLDIERS:
                return 650f;
            case F3_CACTUS_SOLDIERS_ONE_TANK:
                return 720f;
            case F4_SOLDIERS_TANKS:
                return 720f;
            case F5_DECOR_NO_ENEMIES:
                return 999999f;
        }
        return 650f;
    }

    private float nextGap(LevelConfig.Phase phase) {
        switch (phase) {
            case F1_CACTUS:
                // intentos frecuentes (la separación real manda)
                return MathUtils.random(450f, 650f);
            case F2_CACTUS_SOLDIERS:
                return MathUtils.random(650f, 950f);
            case F3_CACTUS_SOLDIERS_ONE_TANK:
                return MathUtils.random(650f, 950f);
            case F4_SOLDIERS_TANKS:
                return MathUtils.random(600f, 900f);
            case F5_DECOR_NO_ENEMIES:
                return 999999f;
        }
        return 900f;
    }

    private SpawnType chooseSpawn(LevelConfig.Phase phase) {
        switch (phase) {
            case F1_CACTUS:
                return SpawnType.CACTUS;

            case F2_CACTUS_SOLDIERS:
                return MathUtils.randomBoolean(0.70f) ? SpawnType.CACTUS : SpawnType.SOLDIER;

            case F3_CACTUS_SOLDIERS_ONE_TANK:
                if (!f3TankDone && MathUtils.randomBoolean(0.18f)) return SpawnType.TANK;
                return MathUtils.randomBoolean(0.55f) ? SpawnType.CACTUS : SpawnType.SOLDIER;

            case F4_SOLDIERS_TANKS:
                return MathUtils.randomBoolean(0.55f) ? SpawnType.SOLDIER : SpawnType.TANK;

            case F5_DECOR_NO_ENEMIES:
                return SpawnType.CACTUS;
        }
        return SpawnType.CACTUS;
    }
}
