package com.carmen.mijuego.assets;

public final class AssetPaths {

    private AssetPaths() {}

    // Imagenes del desierto
    public static final String BG_DESERT_SKY    = "backgrounds/desert/05cielo.png";
    public static final String BG_DESERT_CLOUDS = "backgrounds/desert/04nubes.png";
    public static final String BG_DESERT_RUINS  = "backgrounds/desert/03ruinas.png";
    public static final String BG_DESERT_MID    = "backgrounds/desert/02medio.png";
    public static final String BG_DESERT_NEAR   = "backgrounds/desert/01cerca.png";

    // Imagenes de Ayla
    public static final String AYLA_RUN  = "characters/ayla/ayla_run.png";
    public static final String AYLA_IDLE = "characters/ayla/ayla_idle.png";
    public static final String AYLA_JUMP = "characters/ayla/ayla_jump.png";

    // ✅ Proyectiles
    public static final String BULLET = "projectiles/bullet.png";

    // Imagenes de los controles
    public static final String UI_LEFT    = "ui/controls/btn_move_left.png";
    public static final String UI_RIGHT   = "ui/controls/btn_move_right.png";
    public static final String UI_JUMP    = "ui/controls/btn_jump.png";
    public static final String UI_SHOOT   = "ui/controls/btn_shoot.png";
    public static final String UI_GRENADE = "ui/controls/btn_grenade.png";
    public static final String UI_PAUSE   = "ui/controls/btn_pause.png";

    // HUD
    public static final String HUD_HEART_FULL = "ui/hud/heart_full.png";

    // Imagen de la pantalla de introduccion
    public static final String SCREEN_INTRO = "screens/intro/IntroScreen.png";

    // Imagen de la pantalla de menu y los botones del menu
    public static final String SCREEN_MENU_BG = "screens/menu/MenuScreen.png";
    public static final String SCREEN_MENU_BTN_GAME = "screens/menu/GameButton.png";
    public static final String SCREEN_MENU_BTN_OPTIONS = "screens/menu/OptionsButton.png";
    public static final String SCREEN_MENU_BTN_CREDITS = "screens/menu/CreditsButton.png";
    public static final String SCREEN_MENU_BTN_ACHIEVEMENTS = "screens/menu/AchievementsButton.png";

    // Enemies - Cactus
    public static final String CACTUS_PINK   = "enemies/cactus/Pink_Cactus.png";
    public static final String CACTUS_YELLOW = "enemies/cactus/Yellow_Cactus.png";

    // Soldier
    public static final String SOLDIER_IDLE = "enemies/soldier/soldier_idle.png";
    public static final String SOLDIER_RUN  = "enemies/soldier/soldier_run.png";   // spritesheet 403x457
    public static final String SOLDIER_HURT = "enemies/soldier/soldier_hurt.png";  // spritesheet 403x457
    public static final String SOLDIER_DEAD = "enemies/soldier/soldier_dead.png";

    // Tank
    public static final String TANK_IDLE    = "enemies/tank/tank_idle.png";     // 385x431 (o similar)
    public static final String TANK_MOVE    = "enemies/tank/tank_move.png";     // spritesheet o frame único
    public static final String TANK_DESTROY = "enemies/tank/tank_destroy.png";  // 1 vez
    public static final String TANK_DEAD    = "enemies/tank/tank_dead.png";     // se queda
}
