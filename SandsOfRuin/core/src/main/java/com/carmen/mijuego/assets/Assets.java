package com.carmen.mijuego.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

public class Assets {

    private final AssetManager manager;

    // ===================== TEXTURES =====================

    // ===== DESERT =====
    public static final AssetDescriptor<Texture> SKY =
        new AssetDescriptor<>(AssetPaths.BG_DESERT_SKY, Texture.class);
    public static final AssetDescriptor<Texture> CLOUDS =
        new AssetDescriptor<>(AssetPaths.BG_DESERT_CLOUDS, Texture.class);
    public static final AssetDescriptor<Texture> RUINS =
        new AssetDescriptor<>(AssetPaths.BG_DESERT_RUINS, Texture.class);
    public static final AssetDescriptor<Texture> MID =
        new AssetDescriptor<>(AssetPaths.BG_DESERT_MID, Texture.class);
    public static final AssetDescriptor<Texture> NEAR =
        new AssetDescriptor<>(AssetPaths.BG_DESERT_NEAR, Texture.class);

    // ===== AYLA =====
    public static final AssetDescriptor<Texture> AYLA_RUN =
        new AssetDescriptor<>(AssetPaths.AYLA_RUN, Texture.class);
    public static final AssetDescriptor<Texture> AYLA_IDLE =
        new AssetDescriptor<>(AssetPaths.AYLA_IDLE, Texture.class);
    public static final AssetDescriptor<Texture> AYLA_JUMP =
        new AssetDescriptor<>(AssetPaths.AYLA_JUMP, Texture.class);

    // ===== PROJECTILES =====
    public static final AssetDescriptor<Texture> BULLET =
        new AssetDescriptor<>(AssetPaths.BULLET, Texture.class);

    // ===== UI =====
    public static final AssetDescriptor<Texture> UI_LEFT =
        new AssetDescriptor<>(AssetPaths.UI_LEFT, Texture.class);
    public static final AssetDescriptor<Texture> UI_RIGHT =
        new AssetDescriptor<>(AssetPaths.UI_RIGHT, Texture.class);
    public static final AssetDescriptor<Texture> UI_JUMP =
        new AssetDescriptor<>(AssetPaths.UI_JUMP, Texture.class);
    public static final AssetDescriptor<Texture> UI_SHOOT =
        new AssetDescriptor<>(AssetPaths.UI_SHOOT, Texture.class);
    public static final AssetDescriptor<Texture> UI_GRENADE =
        new AssetDescriptor<>(AssetPaths.UI_GRENADE, Texture.class);
    public static final AssetDescriptor<Texture> UI_PAUSE =
        new AssetDescriptor<>(AssetPaths.UI_PAUSE, Texture.class);

    // ===== HUD =====
    public static final AssetDescriptor<Texture> HUD_HEART_FULL =
        new AssetDescriptor<>(AssetPaths.HUD_HEART_FULL, Texture.class);

    // ===== SCREENS =====
    public static final AssetDescriptor<Texture> SCREEN_INTRO =
        new AssetDescriptor<>(AssetPaths.SCREEN_INTRO, Texture.class);

    public static final AssetDescriptor<Texture> SCREEN_MENU_BG =
        new AssetDescriptor<>(AssetPaths.SCREEN_MENU_BG, Texture.class);
    public static final AssetDescriptor<Texture> SCREEN_MENU_BTN_GAME =
        new AssetDescriptor<>(AssetPaths.SCREEN_MENU_BTN_GAME, Texture.class);
    public static final AssetDescriptor<Texture> SCREEN_MENU_BTN_OPTIONS =
        new AssetDescriptor<>(AssetPaths.SCREEN_MENU_BTN_OPTIONS, Texture.class);
    public static final AssetDescriptor<Texture> SCREEN_MENU_BTN_CREDITS =
        new AssetDescriptor<>(AssetPaths.SCREEN_MENU_BTN_CREDITS, Texture.class);
    public static final AssetDescriptor<Texture> SCREEN_MENU_BTN_ACHIEVEMENTS =
        new AssetDescriptor<>(AssetPaths.SCREEN_MENU_BTN_ACHIEVEMENTS, Texture.class);

    // ===== ENEMIES =====
    public static final AssetDescriptor<Texture> CACTUS_PINK =
        new AssetDescriptor<>(AssetPaths.CACTUS_PINK, Texture.class);
    public static final AssetDescriptor<Texture> CACTUS_YELLOW =
        new AssetDescriptor<>(AssetPaths.CACTUS_YELLOW, Texture.class);

    public static final AssetDescriptor<Texture> SOLDIER_IDLE =
        new AssetDescriptor<>(AssetPaths.SOLDIER_IDLE, Texture.class);
    public static final AssetDescriptor<Texture> SOLDIER_RUN =
        new AssetDescriptor<>(AssetPaths.SOLDIER_RUN, Texture.class);
    public static final AssetDescriptor<Texture> SOLDIER_HURT =
        new AssetDescriptor<>(AssetPaths.SOLDIER_HURT, Texture.class);
    public static final AssetDescriptor<Texture> SOLDIER_DEAD =
        new AssetDescriptor<>(AssetPaths.SOLDIER_DEAD, Texture.class);

    // ===== TANK =====
    public static final AssetDescriptor<Texture> TANK_IDLE =
        new AssetDescriptor<>(AssetPaths.TANK_IDLE, Texture.class);
    public static final AssetDescriptor<Texture> TANK_MOVE =
        new AssetDescriptor<>(AssetPaths.TANK_MOVE, Texture.class);
    public static final AssetDescriptor<Texture> TANK_DESTROY =
        new AssetDescriptor<>(AssetPaths.TANK_DESTROY, Texture.class);
    public static final AssetDescriptor<Texture> TANK_DEAD =
        new AssetDescriptor<>(AssetPaths.TANK_DEAD, Texture.class);

    // ===================== AUDIO =====================

    // ===== SFX (Sound) =====
    public static final AssetDescriptor<Sound> SFX_AYLA_DAMAGE =
        new AssetDescriptor<>(AssetPaths.SFX_AYLA_DAMAGE, Sound.class);
    public static final AssetDescriptor<Sound> SFX_EXPLOSION_GRENADE =
        new AssetDescriptor<>(AssetPaths.SFX_EXPLOSION_GRENADE, Sound.class);
    public static final AssetDescriptor<Sound> SFX_EXPLOSION_TANK =
        new AssetDescriptor<>(AssetPaths.SFX_EXPLOSION_TANK, Sound.class);
    public static final AssetDescriptor<Sound> SFX_GAME_OVER =
        new AssetDescriptor<>(AssetPaths.SFX_GAME_OVER, Sound.class);
    public static final AssetDescriptor<Sound> SFX_GRENADE_TAPE =
        new AssetDescriptor<>(AssetPaths.SFX_GRENADE_TAPE, Sound.class);
    public static final AssetDescriptor<Sound> SFX_MUMMY_DEAD =
        new AssetDescriptor<>(AssetPaths.SFX_MUMMY_DEAD, Sound.class);
    public static final AssetDescriptor<Sound> SFX_MUMMY_GRUNTS =
        new AssetDescriptor<>(AssetPaths.SFX_MUMMY_GRUNTS, Sound.class);
    public static final AssetDescriptor<Sound> SFX_MUMMY_SHOTS =
        new AssetDescriptor<>(AssetPaths.SFX_MUMMY_SHOTS, Sound.class);
    public static final AssetDescriptor<Sound> SFX_PYRAMID_STEPS =
        new AssetDescriptor<>(AssetPaths.SFX_PYRAMID_STEPS, Sound.class);
    public static final AssetDescriptor<Sound> SFX_SAND_WALK =
        new AssetDescriptor<>(AssetPaths.SFX_SAND_WALK, Sound.class);
    public static final AssetDescriptor<Sound> SFX_SHOT_GUN_1 =
        new AssetDescriptor<>(AssetPaths.SFX_SHOT_GUN_1, Sound.class);
    public static final AssetDescriptor<Sound> SFX_SHOT_GUN_2 =
        new AssetDescriptor<>(AssetPaths.SFX_SHOT_GUN_2, Sound.class);
    public static final AssetDescriptor<Sound> SFX_SHOT_TANK =
        new AssetDescriptor<>(AssetPaths.SFX_SHOT_TANK, Sound.class);
    public static final AssetDescriptor<Sound> SFX_SOLDIER_DAMAGE =
        new AssetDescriptor<>(AssetPaths.SFX_SOLDIER_DAMAGE, Sound.class);
    public static final AssetDescriptor<Sound> SFX_TANK_MOVE =
        new AssetDescriptor<>(AssetPaths.SFX_TANK_MOVE, Sound.class);
    public static final AssetDescriptor<Sound> SFX_VICTORY =
        new AssetDescriptor<>(AssetPaths.SFX_VICTORY, Sound.class);

    // ===== MUSIC (Music) =====
    public static final AssetDescriptor<Music> MUS_ACHIEVEMENTS_THEME =
        new AssetDescriptor<>(AssetPaths.MUS_ACHIEVEMENTS_THEME, Music.class);
    public static final AssetDescriptor<Music> MUS_ACHIEVEMENTS_THEME2 =
        new AssetDescriptor<>(AssetPaths.MUS_ACHIEVEMENTS_THEME2, Music.class);
    public static final AssetDescriptor<Music> MUS_CONFIG_THEME =
        new AssetDescriptor<>(AssetPaths.MUS_CONFIG_THEME, Music.class);
    public static final AssetDescriptor<Music> MUS_CREDITS_THEME =
        new AssetDescriptor<>(AssetPaths.MUS_CREDITS_THEME, Music.class);
    public static final AssetDescriptor<Music> MUS_DESERT_THEME =
        new AssetDescriptor<>(AssetPaths.MUS_DESERT_THEME, Music.class);
    public static final AssetDescriptor<Music> MUS_GAME_OVER_THEME =
        new AssetDescriptor<>(AssetPaths.MUS_GAME_OVER_THEME, Music.class);
    public static final AssetDescriptor<Music> MUS_INTRO_THEME =
        new AssetDescriptor<>(AssetPaths.MUS_INTRO_THEME, Music.class);
    public static final AssetDescriptor<Music> MUS_PAUSE_THEME =
        new AssetDescriptor<>(AssetPaths.MUS_PAUSE_THEME, Music.class);
    public static final AssetDescriptor<Music> MUS_PYRAMID_THEME =
        new AssetDescriptor<>(AssetPaths.MUS_PYRAMID_THEME, Music.class);
    public static final AssetDescriptor<Music> MUS_VICTORY_THEME =
        new AssetDescriptor<>(AssetPaths.MUS_VICTORY_THEME, Music.class);

    // ===================== LIFECYCLE =====================

    public Assets() {
        this.manager = new AssetManager();
    }

    /** Carga en cola todos los assets usados en el juego. */
    public void queueLoadAll() {
        // Desert
        manager.load(SKY);
        manager.load(CLOUDS);
        manager.load(RUINS);
        manager.load(MID);
        manager.load(NEAR);

        // Ayla
        manager.load(AYLA_RUN);
        manager.load(AYLA_IDLE);
        manager.load(AYLA_JUMP);

        // Projectiles
        manager.load(BULLET);

        // UI
        manager.load(UI_LEFT);
        manager.load(UI_RIGHT);
        manager.load(UI_JUMP);
        manager.load(UI_SHOOT);
        manager.load(UI_GRENADE);
        manager.load(UI_PAUSE);

        // HUD
        manager.load(HUD_HEART_FULL);

        // Screens
        manager.load(SCREEN_INTRO);
        manager.load(SCREEN_MENU_BG);
        manager.load(SCREEN_MENU_BTN_GAME);
        manager.load(SCREEN_MENU_BTN_OPTIONS);
        manager.load(SCREEN_MENU_BTN_CREDITS);
        manager.load(SCREEN_MENU_BTN_ACHIEVEMENTS);

        // Enemies
        manager.load(CACTUS_PINK);
        manager.load(CACTUS_YELLOW);

        manager.load(SOLDIER_IDLE);
        manager.load(SOLDIER_RUN);
        manager.load(SOLDIER_HURT);
        manager.load(SOLDIER_DEAD);

        // Tank
        manager.load(TANK_IDLE);
        manager.load(TANK_MOVE);
        manager.load(TANK_DESTROY);
        manager.load(TANK_DEAD);

        // SFX
        manager.load(SFX_AYLA_DAMAGE);
        manager.load(SFX_EXPLOSION_GRENADE);
        manager.load(SFX_EXPLOSION_TANK);
        manager.load(SFX_GAME_OVER);
        manager.load(SFX_GRENADE_TAPE);
        manager.load(SFX_MUMMY_DEAD);
        manager.load(SFX_MUMMY_GRUNTS);
        manager.load(SFX_MUMMY_SHOTS);
        manager.load(SFX_PYRAMID_STEPS);
        manager.load(SFX_SAND_WALK);
        manager.load(SFX_SHOT_GUN_1);
        manager.load(SFX_SHOT_GUN_2);
        manager.load(SFX_SHOT_TANK);
        manager.load(SFX_SOLDIER_DAMAGE);
        manager.load(SFX_TANK_MOVE);
        manager.load(SFX_VICTORY);

        // Music
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

    public void finishLoading() {
        manager.finishLoading();
        applyDefaultFilters();
    }

    public boolean update() {
        boolean done = manager.update();
        if (done) applyDefaultFilters();
        return done;
    }

    public float getProgress() {
        return manager.getProgress();
    }

    public <T> T get(AssetDescriptor<T> desc) {
        return manager.get(desc);
    }

    public void dispose() {
        manager.dispose();
    }

    private void applyDefaultFilters() {
        // Textures con filtro linear
        setLinear(SKY);
        setLinear(CLOUDS);
        setLinear(RUINS);
        setLinear(MID);
        setLinear(NEAR);

        setLinear(AYLA_RUN);
        setLinear(AYLA_IDLE);
        setLinear(AYLA_JUMP);

        setLinear(BULLET);

        setLinear(UI_LEFT);
        setLinear(UI_RIGHT);
        setLinear(UI_JUMP);
        setLinear(UI_SHOOT);
        setLinear(UI_GRENADE);
        setLinear(UI_PAUSE);

        setLinear(HUD_HEART_FULL);

        setLinear(SCREEN_INTRO);
        setLinear(SCREEN_MENU_BG);
        setLinear(SCREEN_MENU_BTN_GAME);
        setLinear(SCREEN_MENU_BTN_OPTIONS);
        setLinear(SCREEN_MENU_BTN_CREDITS);
        setLinear(SCREEN_MENU_BTN_ACHIEVEMENTS);

        setLinear(CACTUS_PINK);
        setLinear(CACTUS_YELLOW);

        setLinear(SOLDIER_IDLE);
        setLinear(SOLDIER_RUN);
        setLinear(SOLDIER_HURT);
        setLinear(SOLDIER_DEAD);

        setLinear(TANK_IDLE);
        setLinear(TANK_MOVE);
        setLinear(TANK_DESTROY);
        setLinear(TANK_DEAD);
    }

    private void setLinear(AssetDescriptor<Texture> desc) {
        if (!manager.isLoaded(desc.fileName)) return;
        Texture t = manager.get(desc);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }
}
