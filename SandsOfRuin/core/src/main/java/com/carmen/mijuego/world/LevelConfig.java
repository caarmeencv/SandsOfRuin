package com.carmen.mijuego.world;

public final class LevelConfig {

    private LevelConfig() {}

    /**
     * ~3 minutos manteniendo derecha (320 px/s):
     * 320 * 180 = 57600 -> redondeo a 58000
     */
    public static final float DESERT_LENGTH = 58000f;

    // 5 fases
    public static final float F1_END = 12000f;
    public static final float F2_END = 24000f;
    public static final float F3_END = 36000f;
    public static final float F4_END = 50000f;
    public static final float F5_END = DESERT_LENGTH;

    public enum Phase {
        F1_CACTUS,
        F2_CACTUS_SOLDIERS,
        F3_CACTUS_SOLDIERS_ONE_TANK,
        F4_SOLDIERS_TANKS,
        F5_DECOR_NO_ENEMIES
    }

    public static Phase phaseFor(float camRight) {
        if (camRight < F1_END) return Phase.F1_CACTUS;
        if (camRight < F2_END) return Phase.F2_CACTUS_SOLDIERS;
        if (camRight < F3_END) return Phase.F3_CACTUS_SOLDIERS_ONE_TANK;
        if (camRight < F4_END) return Phase.F4_SOLDIERS_TANKS;
        return Phase.F5_DECOR_NO_ENEMIES;
    }
}
