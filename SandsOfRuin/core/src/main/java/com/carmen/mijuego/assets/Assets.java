package com.carmen.mijuego.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

public class Assets {

    // Aquí guardo el AssetManager, donde meto todas las imágenes y sonidos del juego
    // Así no tengo que cargar cosas una por una en cada pantalla
    private final AssetManager manager;

    // Estas constantes son rutas a archivos dentro de la carpeta assets
    // Son los fondos del desierto en capas, para el efecto de profundidad
    public static final String BG_DESERT_SKY_PATH    = "backgrounds/desert/sky.png";
    public static final String BG_DESERT_CLOUDS_PATH = "backgrounds/desert/clouds.png";
    public static final String BG_DESERT_RUINS_PATH  = "backgrounds/desert/ruins.png";
    public static final String BG_DESERT_MID_PATH    = "backgrounds/desert/mid.png";
    public static final String BG_DESERT_NEAR_PATH   = "backgrounds/desert/near.png";

    // Dos decoraciones que indican la llegada a la pirámide
    public static final String SPHINX_PYRAMID_PATH   = "backgrounds/desert/sphinx_pyramid.png";
    public static final String ENTRANCE_PYRAMID_PATH = "backgrounds/desert/entrance_pyramid.png";

    // Fondos de pantallas como créditos, opciones, logros y ayuda
    public static final String BG_CREDITS_PATH       = "screens/credits/credits_background.png";
    public static final String BG_OPTIONS_PATH       = "screens/options/options_background.png";
    public static final String BG_ACHIEVEMENTS_PATH  = "screens/achievements/achievements_background.png";
    public static final String BG_HOWTOPLAY_PATH     = "screens/howtoplay/howtoplay_background.png";

    // Rutas de las animaciones de Ayla
    public static final String AYLA_RUN_PATH  = "characters/ayla/ayla_run.png";
    public static final String AYLA_IDLE_PATH = "characters/ayla/ayla_idle.png";
    public static final String AYLA_JUMP_PATH = "characters/ayla/ayla_jump.png";

    // Rutas de proyectiles
    public static final String BULLET_PATH         = "projectiles/bullet.png";
    public static final String BULLET_SPECIAL_PATH = "projectiles/bullet_special.png";
    public static final String BULLET_MUMMY_PATH   = "projectiles/bullet_mummy.png";

    // Botones de control en pantalla
    public static final String UI_LEFT_PATH    = "ui/controls/btn_move_left.png";
    public static final String UI_RIGHT_PATH   = "ui/controls/btn_move_right.png";
    public static final String UI_JUMP_PATH    = "ui/controls/btn_jump.png";
    public static final String UI_SHOOT_PATH   = "ui/controls/btn_shoot.png";
    public static final String UI_GRENADE_PATH = "ui/controls/btn_grenade.png";
    public static final String UI_PAUSE_PATH   = "ui/controls/btn_pause.png";

    // Corazones del HUD para las vidas
    public static final String HUD_HEART_FULL_PATH  = "ui/hud/heart_full.png";
    public static final String HUD_HEART_EMPTY_PATH = "ui/hud/heart_empty.png";

    // Pantallas sueltas y botón de volver
    public static final String SCREEN_INTRO_PATH = "screens/intro/IntroScreen.png";
    public static final String BUTTON_BACK_PATH  = "screens/button_back.png";

    // Menú principal y sus botones
    public static final String SCREEN_MENU_BG_PATH               = "screens/menu/MenuScreen.png";
    public static final String SCREEN_MENU_BTN_GAME_PATH         = "screens/menu/GameButton.png";
    public static final String SCREEN_MENU_BTN_OPTIONS_PATH      = "screens/menu/OptionsButton.png";
    public static final String SCREEN_MENU_BTN_CREDITS_PATH      = "screens/menu/CreditsButton.png";
    public static final String SCREEN_MENU_BTN_ACHIEVEMENTS_PATH = "screens/menu/AchievementsButton.png";

    // Enemigos cactus
    public static final String CACTUS_PINK_PATH   = "enemies/cactus/Pink_Cactus.png";
    public static final String CACTUS_YELLOW_PATH = "enemies/cactus/Yellow_Cactus.png";

    // Animaciones del soldado
    public static final String SOLDIER_IDLE_PATH = "enemies/soldier/soldier_idle.png";
    public static final String SOLDIER_RUN_PATH  = "enemies/soldier/soldier_run.png";
    public static final String SOLDIER_HURT_PATH = "enemies/soldier/soldier_hurt.png";
    public static final String SOLDIER_DEAD_PATH = "enemies/soldier/soldier_dead.png";

    // Animaciones del tanque
    public static final String TANK_IDLE_PATH    = "enemies/tank/tank_idle.png";
    public static final String TANK_MOVE_PATH    = "enemies/tank/tank_move.png";
    public static final String TANK_DESTROY_PATH = "enemies/tank/tank_destroy.png";
    public static final String TANK_DEAD_PATH    = "enemies/tank/tank_dead.png";

    // Tesoro dentro de la pirámide
    public static final String TREASURE_PATH = "backgrounds/pyramid/pyramid_treasure.png";

    // Fondos de victoria y game over
    public static final String SCREEN_GAMEOVER_BG_PATH = "screens/GameOver/gameover_background.png";
    public static final String SCREEN_VICTORY_BG_PATH  = "screens/Victory/victory_background.png";

    // Fondo interior de la pirámide
    public static final String BG_PYRAMID_WALL_PATH   = "backgrounds/pyramid/pyramid_wall.png";
    public static final String BG_PYRAMID_GROUND_PATH = "backgrounds/pyramid/pyramid_ground.png";

    // Animaciones de la momia
    public static final String MUMMY_IDLE_PATH = "enemies/mummy/mummy_idle.png";
    public static final String MUMMY_WALK_PATH = "enemies/mummy/mummy_walk.png";
    public static final String MUMMY_HURT_PATH = "enemies/mummy/mummy_hurt.png";
    public static final String MUMMY_DEAD_PATH = "enemies/mummy/mummy_dead.png";

    // Fondos y botones de la pantalla de pausa
    public static final String PAUSE_BG_DESERT_PATH  = "screens/pause/pausedesert_background.png";
    public static final String PAUSE_BG_PYRAMID_PATH = "screens/pause/pausepyramid_background.png";

    public static final String PAUSE_BTN_CONTINUE_PATH = "screens/pause/ContinueButton.png";
    public static final String PAUSE_BTN_MENU_PATH     = "screens/pause/MenuButton.png";
    public static final String PAUSE_BTN_RESET_PATH    = "screens/pause/ResetButton.png";

    // Interruptores de opciones (on off y cambio de idioma)
    public static final String SWITCH_ON_PATH  = "screens/options/switch_ON.png";
    public static final String SWITCH_OFF_PATH = "screens/options/switch_OFF.png";
    public static final String SWITCH_EN_PATH  = "screens/options/switch_EN.png";
    public static final String SWITCH_ES_PATH  = "screens/options/switch_ES.png";

    // Aquí van los sonidos cortos, tipo efectos
    public static final String SFX_AYLA_DAMAGE_PATH       = "audio/effects/AylaDamage.mp3";
    public static final String SFX_EXPLOSION_GRENADE_PATH = "audio/effects/ExplosionGrenade.mp3";
    public static final String SFX_EXPLOSION_TANK_PATH    = "audio/effects/ExplosionTank.mp3";
    public static final String SFX_GAME_OVER_PATH         = "audio/effects/GameOver.mp3";
    public static final String SFX_MUMMY_DEAD_PATH        = "audio/effects/MummyDead.mp3";
    public static final String SFX_MUMMY_GRUNTS_PATH      = "audio/effects/MummyGrunts.mp3";
    public static final String SFX_MUMMY_SHOTS_PATH       = "audio/effects/MummyShots.mp3";
    public static final String SFX_SHOT_GUN_1_PATH        = "audio/effects/ShotGun.mp3";
    public static final String SFX_SHOT_GUN_2_PATH        = "audio/effects/ShotGun2.mp3";
    public static final String SFX_SOLDIER_DAMAGE_PATH    = "audio/effects/SoldierDamage.mp3";
    public static final String SFX_TANK_MOVE_PATH         = "audio/effects/TankMove.mp3";
    public static final String SFX_VICTORY_PATH           = "audio/effects/Victory.mp3";
    public static final String SFX_AYLA_JUMP_PATH      = "audio/effects/AylaJump.mp3";
    public static final String SFX_BUTTON_CLICKED_PATH = "audio/effects/ButtonClicked.mp3";
    public static final String SFX_CHARACTER_RUN_PATH  = "audio/effects/CharacterRun.mp3";
    public static final String SFX_LIVE_PATH           = "audio/effects/Live.mp3";
    public static final String SFX_SOLDIER_DEAD_SFXPATH = "audio/effects/SoldierDead.mp3";
    public static final String SFX_GUN_RELOAD_PATH      = "audio/effects/ReloadGun.mp3";

    // Música larga, tipo fondo, que normalmente se pone en bucle
    public static final String MUS_ACHIEVEMENTS_THEME_PATH  = "audio/themes/AchievementsTheme.mp3";
    public static final String MUS_ACHIEVEMENTS_THEME2_PATH = "audio/themes/AchievementsTheme2.mp3";
    public static final String MUS_CONFIG_THEME_PATH        = "audio/themes/ConfigurationsTheme.mp3";
    public static final String MUS_CREDITS_THEME_PATH       = "audio/themes/CreditsTheme.mp3";
    public static final String MUS_DESERT_THEME_PATH        = "audio/themes/DesertTheme.mp3";
    public static final String MUS_GAME_OVER_THEME_PATH     = "audio/themes/GameOverTheme.mp3";
    public static final String MUS_INTRO_THEME_PATH         = "audio/themes/IntroTheme.mp3";
    public static final String MUS_PAUSE_THEME_PATH         = "audio/themes/PauseTheme.mp3";
    public static final String MUS_PYRAMID_THEME_PATH       = "audio/themes/PyramidTheme.mp3";
    public static final String MUS_VICTORY_THEME_PATH       = "audio/themes/VictoryTheme.mp3";

    // La fuente principal del juego, que es una BitmapFont generada con su fnt y su png
    public static final String FONT_MAIN_FNT_PATH = "fonts/fonts.fnt";
    public static final String FONT_MAIN_PNG_PATH = "fonts/fonts.png";

    // Ahora vienen los AssetDescriptor
    // Gracias a esto, cuando cargo y pido assets, no me equivoco con rutas o tipos

    public static final AssetDescriptor<Texture> BUTTON_BACK =
        new AssetDescriptor<>(BUTTON_BACK_PATH, Texture.class);

    public static final AssetDescriptor<Texture> SKY =
        new AssetDescriptor<>(BG_DESERT_SKY_PATH, Texture.class);
    public static final AssetDescriptor<Texture> CLOUDS =
        new AssetDescriptor<>(BG_DESERT_CLOUDS_PATH, Texture.class);
    public static final AssetDescriptor<Texture> RUINS =
        new AssetDescriptor<>(BG_DESERT_RUINS_PATH, Texture.class);
    public static final AssetDescriptor<Texture> MID =
        new AssetDescriptor<>(BG_DESERT_MID_PATH, Texture.class);
    public static final AssetDescriptor<Texture> NEAR =
        new AssetDescriptor<>(BG_DESERT_NEAR_PATH, Texture.class);

    public static final AssetDescriptor<Texture> SCREEN_PAUSE_BG_DESERT =
        new AssetDescriptor<>(PAUSE_BG_DESERT_PATH, Texture.class);
    public static final AssetDescriptor<Texture> SCREEN_PAUSE_BG_PYRAMID =
        new AssetDescriptor<>(PAUSE_BG_PYRAMID_PATH, Texture.class);
    public static final AssetDescriptor<Texture> SCREEN_PAUSE_BTN_CONTINUE =
        new AssetDescriptor<>(PAUSE_BTN_CONTINUE_PATH, Texture.class);
    public static final AssetDescriptor<Texture> SCREEN_PAUSE_BTN_MENU =
        new AssetDescriptor<>(PAUSE_BTN_MENU_PATH, Texture.class);
    public static final AssetDescriptor<Texture> SCREEN_PAUSE_BTN_RESET =
        new AssetDescriptor<>(PAUSE_BTN_RESET_PATH, Texture.class);

    public static final AssetDescriptor<Texture> SPHINX_PYRAMID =
        new AssetDescriptor<>(SPHINX_PYRAMID_PATH, Texture.class);
    public static final AssetDescriptor<Texture> ENTRANCE_PYRAMID =
        new AssetDescriptor<>(ENTRANCE_PYRAMID_PATH, Texture.class);

    public static final AssetDescriptor<Texture> SCREEN_CREDITS_BG =
        new AssetDescriptor<>(BG_CREDITS_PATH, Texture.class);
    public static final AssetDescriptor<Texture> SCREEN_OPTIONS_BG =
        new AssetDescriptor<>(BG_OPTIONS_PATH, Texture.class);
    public static final AssetDescriptor<Texture> SCREEN_ACHIEVEMENTS_BG =
        new AssetDescriptor<>(BG_ACHIEVEMENTS_PATH, Texture.class);
    public static final AssetDescriptor<Texture> SCREEN_HOWTOPLAY_BG =
        new AssetDescriptor<>(BG_HOWTOPLAY_PATH, Texture.class);

    public static final AssetDescriptor<Texture> TREASURE =
        new AssetDescriptor<>(TREASURE_PATH, Texture.class);

    public static final AssetDescriptor<Texture> AYLA_RUN =
        new AssetDescriptor<>(AYLA_RUN_PATH, Texture.class);
    public static final AssetDescriptor<Texture> AYLA_IDLE =
        new AssetDescriptor<>(AYLA_IDLE_PATH, Texture.class);
    public static final AssetDescriptor<Texture> AYLA_JUMP =
        new AssetDescriptor<>(AYLA_JUMP_PATH, Texture.class);

    public static final AssetDescriptor<Texture> BULLET =
        new AssetDescriptor<>(BULLET_PATH, Texture.class);
    public static final AssetDescriptor<Texture> BULLET_SPECIAL =
        new AssetDescriptor<>(BULLET_SPECIAL_PATH, Texture.class);
    public static final AssetDescriptor<Texture> BULLET_MUMMY =
        new AssetDescriptor<>(BULLET_MUMMY_PATH, Texture.class);

    public static final AssetDescriptor<Texture> UI_LEFT =
        new AssetDescriptor<>(UI_LEFT_PATH, Texture.class);
    public static final AssetDescriptor<Texture> UI_RIGHT =
        new AssetDescriptor<>(UI_RIGHT_PATH, Texture.class);
    public static final AssetDescriptor<Texture> UI_JUMP =
        new AssetDescriptor<>(UI_JUMP_PATH, Texture.class);
    public static final AssetDescriptor<Texture> UI_SHOOT =
        new AssetDescriptor<>(UI_SHOOT_PATH, Texture.class);
    public static final AssetDescriptor<Texture> UI_GRENADE =
        new AssetDescriptor<>(UI_GRENADE_PATH, Texture.class);
    public static final AssetDescriptor<Texture> UI_PAUSE =
        new AssetDescriptor<>(UI_PAUSE_PATH, Texture.class);

    public static final AssetDescriptor<Texture> HUD_HEART_FULL =
        new AssetDescriptor<>(HUD_HEART_FULL_PATH, Texture.class);
    public static final AssetDescriptor<Texture> HUD_HEART_EMPTY =
        new AssetDescriptor<>(HUD_HEART_EMPTY_PATH, Texture.class);

    public static final AssetDescriptor<Texture> SCREEN_INTRO =
        new AssetDescriptor<>(SCREEN_INTRO_PATH, Texture.class);

    public static final AssetDescriptor<Texture> SCREEN_MENU_BG =
        new AssetDescriptor<>(SCREEN_MENU_BG_PATH, Texture.class);
    public static final AssetDescriptor<Texture> SCREEN_MENU_BTN_GAME =
        new AssetDescriptor<>(SCREEN_MENU_BTN_GAME_PATH, Texture.class);
    public static final AssetDescriptor<Texture> SCREEN_MENU_BTN_OPTIONS =
        new AssetDescriptor<>(SCREEN_MENU_BTN_OPTIONS_PATH, Texture.class);
    public static final AssetDescriptor<Texture> SCREEN_MENU_BTN_CREDITS =
        new AssetDescriptor<>(SCREEN_MENU_BTN_CREDITS_PATH, Texture.class);
    public static final AssetDescriptor<Texture> SCREEN_MENU_BTN_ACHIEVEMENTS =
        new AssetDescriptor<>(SCREEN_MENU_BTN_ACHIEVEMENTS_PATH, Texture.class);

    public static final AssetDescriptor<Texture> SWITCH_ON =
        new AssetDescriptor<>(SWITCH_ON_PATH, Texture.class);
    public static final AssetDescriptor<Texture> SWITCH_OFF =
        new AssetDescriptor<>(SWITCH_OFF_PATH, Texture.class);
    public static final AssetDescriptor<Texture> SWITCH_EN =
        new AssetDescriptor<>(SWITCH_EN_PATH, Texture.class);
    public static final AssetDescriptor<Texture> SWITCH_ES =
        new AssetDescriptor<>(SWITCH_ES_PATH, Texture.class);

    public static final AssetDescriptor<Texture> CACTUS_PINK =
        new AssetDescriptor<>(CACTUS_PINK_PATH, Texture.class);
    public static final AssetDescriptor<Texture> CACTUS_YELLOW =
        new AssetDescriptor<>(CACTUS_YELLOW_PATH, Texture.class);

    public static final AssetDescriptor<Texture> SOLDIER_IDLE =
        new AssetDescriptor<>(SOLDIER_IDLE_PATH, Texture.class);
    public static final AssetDescriptor<Texture> SOLDIER_RUN =
        new AssetDescriptor<>(SOLDIER_RUN_PATH, Texture.class);
    public static final AssetDescriptor<Texture> SOLDIER_HURT =
        new AssetDescriptor<>(SOLDIER_HURT_PATH, Texture.class);
    public static final AssetDescriptor<Texture> SOLDIER_DEAD =
        new AssetDescriptor<>(SOLDIER_DEAD_PATH, Texture.class);

    public static final AssetDescriptor<Texture> SCREEN_GAMEOVER_BG =
        new AssetDescriptor<>(SCREEN_GAMEOVER_BG_PATH, Texture.class);
    public static final AssetDescriptor<Texture> SCREEN_VICTORY_BG =
        new AssetDescriptor<>(SCREEN_VICTORY_BG_PATH, Texture.class);

    public static final AssetDescriptor<Texture> TANK_IDLE =
        new AssetDescriptor<>(TANK_IDLE_PATH, Texture.class);
    public static final AssetDescriptor<Texture> TANK_MOVE =
        new AssetDescriptor<>(TANK_MOVE_PATH, Texture.class);
    public static final AssetDescriptor<Texture> TANK_DESTROY =
        new AssetDescriptor<>(TANK_DESTROY_PATH, Texture.class);
    public static final AssetDescriptor<Texture> TANK_DEAD =
        new AssetDescriptor<>(TANK_DEAD_PATH, Texture.class);

    public static final AssetDescriptor<Texture> PYR_WALL =
        new AssetDescriptor<>(BG_PYRAMID_WALL_PATH, Texture.class);
    public static final AssetDescriptor<Texture> PYR_GROUND =
        new AssetDescriptor<>(BG_PYRAMID_GROUND_PATH, Texture.class);

    public static final AssetDescriptor<Texture> MUMMY_IDLE =
        new AssetDescriptor<>(MUMMY_IDLE_PATH, Texture.class);
    public static final AssetDescriptor<Texture> MUMMY_WALK =
        new AssetDescriptor<>(MUMMY_WALK_PATH, Texture.class);
    public static final AssetDescriptor<Texture> MUMMY_HURT =
        new AssetDescriptor<>(MUMMY_HURT_PATH, Texture.class);
    public static final AssetDescriptor<Texture> MUMMY_DEAD =
        new AssetDescriptor<>(MUMMY_DEAD_PATH, Texture.class);

    public static final AssetDescriptor<Sound> SFX_AYLA_JUMP =
        new AssetDescriptor<>(SFX_AYLA_JUMP_PATH, Sound.class);
    public static final AssetDescriptor<Sound> SFX_BUTTON_CLICKED =
        new AssetDescriptor<>(SFX_BUTTON_CLICKED_PATH, Sound.class);
    public static final AssetDescriptor<Sound> SFX_CHARACTER_RUN =
        new AssetDescriptor<>(SFX_CHARACTER_RUN_PATH, Sound.class);
    public static final AssetDescriptor<Sound> SFX_LIVE =
        new AssetDescriptor<>(SFX_LIVE_PATH, Sound.class);
    public static final AssetDescriptor<Sound> SFX_SOLDIER_DEAD =
        new AssetDescriptor<>(SFX_SOLDIER_DEAD_SFXPATH, Sound.class);

    public static final AssetDescriptor<Sound> SFX_AYLA_DAMAGE =
        new AssetDescriptor<>(SFX_AYLA_DAMAGE_PATH, Sound.class);
    public static final AssetDescriptor<Sound> SFX_GUN_RELOAD =
        new AssetDescriptor<>(SFX_GUN_RELOAD_PATH, Sound.class);
    public static final AssetDescriptor<Sound> SFX_EXPLOSION_GRENADE =
        new AssetDescriptor<>(SFX_EXPLOSION_GRENADE_PATH, Sound.class);
    public static final AssetDescriptor<Sound> SFX_EXPLOSION_TANK =
        new AssetDescriptor<>(SFX_EXPLOSION_TANK_PATH, Sound.class);
    public static final AssetDescriptor<Sound> SFX_GAME_OVER =
        new AssetDescriptor<>(SFX_GAME_OVER_PATH, Sound.class);
    public static final AssetDescriptor<Sound> SFX_MUMMY_DEAD =
        new AssetDescriptor<>(SFX_MUMMY_DEAD_PATH, Sound.class);
    public static final AssetDescriptor<Sound> SFX_MUMMY_GRUNTS =
        new AssetDescriptor<>(SFX_MUMMY_GRUNTS_PATH, Sound.class);
    public static final AssetDescriptor<Sound> SFX_MUMMY_SHOTS =
        new AssetDescriptor<>(SFX_MUMMY_SHOTS_PATH, Sound.class);
    public static final AssetDescriptor<Sound> SFX_SHOT_GUN_1 =
        new AssetDescriptor<>(SFX_SHOT_GUN_1_PATH, Sound.class);
    public static final AssetDescriptor<Sound> SFX_SHOT_GUN_2 =
        new AssetDescriptor<>(SFX_SHOT_GUN_2_PATH, Sound.class);
    public static final AssetDescriptor<Sound> SFX_SOLDIER_DAMAGE =
        new AssetDescriptor<>(SFX_SOLDIER_DAMAGE_PATH, Sound.class);
    public static final AssetDescriptor<Sound> SFX_TANK_MOVE =
        new AssetDescriptor<>(SFX_TANK_MOVE_PATH, Sound.class);
    public static final AssetDescriptor<Sound> SFX_VICTORY =
        new AssetDescriptor<>(SFX_VICTORY_PATH, Sound.class);

    public static final AssetDescriptor<Music> MUS_ACHIEVEMENTS_THEME =
        new AssetDescriptor<>(MUS_ACHIEVEMENTS_THEME_PATH, Music.class);
    public static final AssetDescriptor<Music> MUS_ACHIEVEMENTS_THEME2 =
        new AssetDescriptor<>(MUS_ACHIEVEMENTS_THEME2_PATH, Music.class);
    public static final AssetDescriptor<Music> MUS_CONFIG_THEME =
        new AssetDescriptor<>(MUS_CONFIG_THEME_PATH, Music.class);
    public static final AssetDescriptor<Music> MUS_CREDITS_THEME =
        new AssetDescriptor<>(MUS_CREDITS_THEME_PATH, Music.class);
    public static final AssetDescriptor<Music> MUS_DESERT_THEME =
        new AssetDescriptor<>(MUS_DESERT_THEME_PATH, Music.class);
    public static final AssetDescriptor<Music> MUS_GAME_OVER_THEME =
        new AssetDescriptor<>(MUS_GAME_OVER_THEME_PATH, Music.class);
    public static final AssetDescriptor<Music> MUS_INTRO_THEME =
        new AssetDescriptor<>(MUS_INTRO_THEME_PATH, Music.class);
    public static final AssetDescriptor<Music> MUS_PAUSE_THEME =
        new AssetDescriptor<>(MUS_PAUSE_THEME_PATH, Music.class);
    public static final AssetDescriptor<Music> MUS_PYRAMID_THEME =
        new AssetDescriptor<>(MUS_PYRAMID_THEME_PATH, Music.class);
    public static final AssetDescriptor<Music> MUS_VICTORY_THEME =
        new AssetDescriptor<>(MUS_VICTORY_THEME_PATH, Music.class);

    // Aquí creo el AssetManager
    public Assets() {
        this.manager = new AssetManager();
    }

    // Este método solo mete cosas en la cola de carga
    // Aquí no se cargan todavía, solo se dice lo que se va a cargar
    public void queueLoadAll() {

        //Fondos del desierto
        manager.load(SKY);
        manager.load(CLOUDS);
        manager.load(RUINS);
        manager.load(MID);
        manager.load(NEAR);

        // Decoraciones del desierto
        manager.load(SPHINX_PYRAMID);
        manager.load(ENTRANCE_PYRAMID);

        // Pantallas de fin de partida
        manager.load(SCREEN_GAMEOVER_BG);
        manager.load(SCREEN_VICTORY_BG);

        // Botón para volver atrás
        manager.load(BUTTON_BACK);

        // Pantalla de pausa y sus botones
        manager.load(SCREEN_PAUSE_BG_DESERT);
        manager.load(SCREEN_PAUSE_BG_PYRAMID);
        manager.load(SCREEN_PAUSE_BTN_CONTINUE);
        manager.load(SCREEN_PAUSE_BTN_MENU);
        manager.load(SCREEN_PAUSE_BTN_RESET);

        // Interruptores de opciones
        manager.load(SWITCH_ON);
        manager.load(SWITCH_OFF);
        manager.load(SWITCH_EN);
        manager.load(SWITCH_ES);

        // Fondos de pantallas de menú extra
        manager.load(SCREEN_CREDITS_BG);
        manager.load(SCREEN_OPTIONS_BG);
        manager.load(SCREEN_ACHIEVEMENTS_BG);
        manager.load(SCREEN_HOWTOPLAY_BG);

        // Tesoro
        manager.load(TREASURE);

        // Ayla
        manager.load(AYLA_RUN);
        manager.load(AYLA_IDLE);
        manager.load(AYLA_JUMP);

        // Balas
        manager.load(BULLET);
        manager.load(BULLET_SPECIAL);
        manager.load(BULLET_MUMMY);

        // Botones en pantalla
        manager.load(UI_LEFT);
        manager.load(UI_RIGHT);
        manager.load(UI_JUMP);
        manager.load(UI_SHOOT);
        manager.load(UI_GRENADE);
        manager.load(UI_PAUSE);

        // Corazones de vida
        manager.load(HUD_HEART_FULL);
        manager.load(HUD_HEART_EMPTY);

        // Pantallas del menú
        manager.load(SCREEN_INTRO);
        manager.load(SCREEN_MENU_BG);
        manager.load(SCREEN_MENU_BTN_GAME);
        manager.load(SCREEN_MENU_BTN_OPTIONS);
        manager.load(SCREEN_MENU_BTN_CREDITS);
        manager.load(SCREEN_MENU_BTN_ACHIEVEMENTS);

        // Enemigos
        manager.load(CACTUS_PINK);
        manager.load(CACTUS_YELLOW);

        manager.load(SOLDIER_IDLE);
        manager.load(SOLDIER_RUN);
        manager.load(SOLDIER_HURT);
        manager.load(SOLDIER_DEAD);

        // Pirámide
        manager.load(PYR_WALL);
        manager.load(PYR_GROUND);

        // Momia
        manager.load(MUMMY_IDLE);
        manager.load(MUMMY_WALK);
        manager.load(MUMMY_HURT);
        manager.load(MUMMY_DEAD);

        // Tanque
        manager.load(TANK_IDLE);
        manager.load(TANK_MOVE);
        manager.load(TANK_DESTROY);
        manager.load(TANK_DEAD);

        // Efectos de sonido
        manager.load(SFX_AYLA_JUMP);
        manager.load(SFX_BUTTON_CLICKED);
        manager.load(SFX_CHARACTER_RUN);
        manager.load(SFX_LIVE);
        manager.load(SFX_SOLDIER_DEAD);

        manager.load(SFX_GUN_RELOAD);
        manager.load(SFX_AYLA_DAMAGE);
        manager.load(SFX_EXPLOSION_GRENADE);
        manager.load(SFX_EXPLOSION_TANK);
        manager.load(SFX_GAME_OVER);
        manager.load(SFX_MUMMY_DEAD);
        manager.load(SFX_MUMMY_GRUNTS);
        manager.load(SFX_MUMMY_SHOTS);
        manager.load(SFX_SHOT_GUN_1);
        manager.load(SFX_SHOT_GUN_2);
        manager.load(SFX_SOLDIER_DAMAGE);
        manager.load(SFX_TANK_MOVE);
        manager.load(SFX_VICTORY);

        // Música de fondo
        manager.load(MUS_ACHIEVEMENTS_THEME);
        manager.load(MUS_ACHIEVEMENTS_THEME2);
        manager.load(MUS_CONFIG_THEME);
        manager.load(MUS_CREDITS_THEME);
        manager.load(MUS_DESERT_THEME);
        manager.load(MUS_GAME_OVER_THEME);
        manager.load(MUS_INTRO_THEME);
        manager.load(MUS_PAUSE_THEME);
        manager.load(MUS_PYRAMID_THEME);
        manager.load(MUS_VICTORY_THEME);
    }

    // Este método sí que se espera a que TODO esté cargado
    // O sea, aquí el juego se queda bloqueado hasta que termine
    // Después, aplico filtros para que las texturas se vean más suaves al escalar
    public void finishLoading() {
        manager.finishLoading();
        applyDefaultFilters();
    }

    // Principalmente mi idea principal era hacer una pantalla de carga
    // Este método va cargando poco a poco y devuelve true cuando ya está todo listo
    public boolean update() {
        boolean done = manager.update();
        if (done) applyDefaultFilters();
        return done;
    }

    // Esto devuelve el porcentaje de carga, de 0 a 1
    public float getProgress() {
        return manager.getProgress();
    }

    // Este método es para pedir un asset ya cargado usando su descriptor
    // Así no tengo que escribir la ruta a mano cada vez
    public <T> T get(AssetDescriptor<T> desc) {
        return manager.get(desc);
    }

    // Esto libera todo lo que cargó el AssetManager
    // Esto se llama cuando cierro el juego
    public void dispose() {
        manager.dispose();
    }

    // Aquí aplico el filtro Linear a las texturas
    // Esto hace que cuando la imagen se escala no se vea como pixelado
    private void applyDefaultFilters() {

        // Fondos desierto
        setLinear(SKY);
        setLinear(CLOUDS);
        setLinear(RUINS);
        setLinear(MID);
        setLinear(NEAR);

        setLinear(SPHINX_PYRAMID);
        setLinear(ENTRANCE_PYRAMID);

        // Botón volver
        setLinear(BUTTON_BACK);

        // Game over y victoria
        setLinear(SCREEN_GAMEOVER_BG);
        setLinear(SCREEN_VICTORY_BG);

        // Pausa
        setLinear(SCREEN_PAUSE_BG_DESERT);
        setLinear(SCREEN_PAUSE_BG_PYRAMID);
        setLinear(SCREEN_PAUSE_BTN_CONTINUE);
        setLinear(SCREEN_PAUSE_BTN_MENU);
        setLinear(SCREEN_PAUSE_BTN_RESET);

        // Switches
        setLinear(SWITCH_ON);
        setLinear(SWITCH_OFF);
        setLinear(SWITCH_EN);
        setLinear(SWITCH_ES);

        // Pantallas extra
        setLinear(SCREEN_CREDITS_BG);
        setLinear(SCREEN_OPTIONS_BG);
        setLinear(SCREEN_ACHIEVEMENTS_BG);
        setLinear(SCREEN_HOWTOPLAY_BG);

        // Tesoro
        setLinear(TREASURE);

        // Ayla
        setLinear(AYLA_RUN);
        setLinear(AYLA_IDLE);
        setLinear(AYLA_JUMP);

        // Balas
        setLinear(BULLET);
        setLinear(BULLET_SPECIAL);
        setLinear(BULLET_MUMMY);

        // Botones
        setLinear(UI_LEFT);
        setLinear(UI_RIGHT);
        setLinear(UI_JUMP);
        setLinear(UI_SHOOT);
        setLinear(UI_GRENADE);
        setLinear(UI_PAUSE);

        // Vidas
        setLinear(HUD_HEART_FULL);
        setLinear(HUD_HEART_EMPTY);

        // Intro
        setLinear(SCREEN_INTRO);

        // Menú
        setLinear(SCREEN_MENU_BG);
        setLinear(SCREEN_MENU_BTN_GAME);
        setLinear(SCREEN_MENU_BTN_OPTIONS);
        setLinear(SCREEN_MENU_BTN_CREDITS);
        setLinear(SCREEN_MENU_BTN_ACHIEVEMENTS);

        // Cactus
        setLinear(CACTUS_PINK);
        setLinear(CACTUS_YELLOW);

        // Soldado
        setLinear(SOLDIER_IDLE);
        setLinear(SOLDIER_RUN);
        setLinear(SOLDIER_HURT);
        setLinear(SOLDIER_DEAD);

        // Tanque
        setLinear(TANK_IDLE);
        setLinear(TANK_MOVE);
        setLinear(TANK_DESTROY);
        setLinear(TANK_DEAD);

        // Pirámide
        setLinear(PYR_WALL);
        setLinear(PYR_GROUND);

        // Momia
        setLinear(MUMMY_IDLE);
        setLinear(MUMMY_WALK);
        setLinear(MUMMY_HURT);
        setLinear(MUMMY_DEAD);
    }

    // Esto es una función para no repetir código
    // Le paso un descriptor de textura, y si está cargado, le pongo el filtro Linear
    private void setLinear(AssetDescriptor<Texture> desc) {
        if (!manager.isLoaded(desc.fileName)) return;
        Texture t = manager.get(desc);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }
}
