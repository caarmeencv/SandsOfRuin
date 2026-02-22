package com.carmen.mijuego.world;

public final class LevelConfig {

    private LevelConfig() {}

    /**
     * ~2 minutos manteniendo derecha (320 px/s):
     * 320 * 120 = 38400
     */
    public static final float DESERT_LENGTH = 38400f;

    // 5 fases (la 5 será “llegada a pirámide y congelación”)
    public static final float F1_END = 8000f;
    public static final float F2_END = 16000f;
    public static final float F3_END = 24000f;
    public static final float F4_END = 32000f;

    /**
     * En cuanto el borde derecho de la cámara (camRight) llega aquí,
     * se congela el scroll y ya no se avanza más.
     *
     * Lo normal es que coincida con el inicio de F5 (justo al terminar F4).
     */
    public static final float FREEZE_CAM_RIGHT = F4_END;

    // “fin” lógico (aunque realmente no se alcanzará porque congelamos antes)
    public static final float F5_END = DESERT_LENGTH;

    public enum Phase {
        F1_CACTUS,
        F2_CACTUS_SOLDIERS,
        F3_CACTUS_SOLDIERS_ONE_TANK,
        F4_SOLDIERS_TANKS,
        F5_PYRAMID_FREEZE
    }

    public static Phase phaseFor(float camRight) {
        if (camRight < F1_END) return Phase.F1_CACTUS;
        if (camRight < F2_END) return Phase.F2_CACTUS_SOLDIERS;
        if (camRight < F3_END) return Phase.F3_CACTUS_SOLDIERS_ONE_TANK;
        if (camRight < F4_END) return Phase.F4_SOLDIERS_TANKS;
        return Phase.F5_PYRAMID_FREEZE;
    }
}
