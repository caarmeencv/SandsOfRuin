package com.carmen.mijuego.world;

public final class LevelConfig {

    private LevelConfig() {}

    // Mantengo tu pirámide donde estaba
    public static final float DESERT_LENGTH = 38400f; // puedes dejarlo, no afecta si congelas antes

    public static final float F1_END = 8000f;
    public static final float F2_END = 16000f;
    public static final float F3_END = 24000f;
    public static final float F4_END = 32000f;

    public static final float FREEZE_CAM_RIGHT = F4_END;
    public static final float F5_END = DESERT_LENGTH;

    public enum Phase {
        F1_CACTUS,
        F2_CACTUS_SOLDIERS,
        F3_CACTUS_TANKS,
        F4_CACTUS_SOLDIERS_TANKS,
        F5_PYRAMID_FREEZE
    }

    public static Phase phaseFor(float camRight) {
        if (camRight < F1_END) return Phase.F1_CACTUS;
        if (camRight < F2_END) return Phase.F2_CACTUS_SOLDIERS;
        if (camRight < F3_END) return Phase.F3_CACTUS_TANKS;
        if (camRight < F4_END) return Phase.F4_CACTUS_SOLDIERS_TANKS;
        return Phase.F5_PYRAMID_FREEZE;
    }
}
