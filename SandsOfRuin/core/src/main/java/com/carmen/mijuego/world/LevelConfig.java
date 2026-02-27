package com.carmen.mijuego.world;

public final class LevelConfig {

    // Longitud total del desierto
    public static final float DESERT_LENGTH = 38400f;

    // Distancias donde termina cada fase del nivel
    public static final float F1_END = 8000f;
    public static final float F2_END = 16000f;
    public static final float F3_END = 24000f;
    public static final float F4_END = 32000f;

    // Punto donde se congela la cámara antes de la pirámide
    public static final float FREEZE_CAM_RIGHT = F4_END;

    // Última fase hasta el final real del desierto
    public static final float F5_END = DESERT_LENGTH;

    // Fases del nivel
    public enum Phase {
        F1_CACTUS,
        F2_CACTUS_SOLDIERS,
        F3_CACTUS_TANKS,
        F4_CACTUS_SOLDIERS_TANKS,
        F5_PYRAMID_FREEZE
    }

    // Constructor privado para evitar que se cree un objeto
    private LevelConfig() {
    }

    // Devuelve en qué fase está el nivel según la posición de la cámara
    public static Phase phaseFor(float camRight) {

        if (camRight < F1_END) return Phase.F1_CACTUS;

        if (camRight < F2_END) return Phase.F2_CACTUS_SOLDIERS;

        if (camRight < F3_END) return Phase.F3_CACTUS_TANKS;

        if (camRight < F4_END) return Phase.F4_CACTUS_SOLDIERS_TANKS;

        return Phase.F5_PYRAMID_FREEZE;
    }
}
