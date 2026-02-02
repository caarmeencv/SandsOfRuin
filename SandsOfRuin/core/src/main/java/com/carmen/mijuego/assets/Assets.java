package com.carmen.mijuego.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

/**
 * Gestor central de recursos.
 * Carga y reutiliza texturas desde AssetManager.
 */
public class Assets {

    private final AssetManager manager;

    // ===== DESCRIPTORS (los nombres coinciden con DesertScreen) =====
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

    public static final AssetDescriptor<Texture> AYLA_RUN =
        new AssetDescriptor<>(AssetPaths.AYLA_RUN, Texture.class);

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

    public Assets() {
        this.manager = new AssetManager();
    }

    public void queueLoadAll() {
        manager.load(SKY);
        manager.load(CLOUDS);
        manager.load(RUINS);
        manager.load(MID);
        manager.load(NEAR);

        manager.load(AYLA_RUN);

        manager.load(UI_LEFT);
        manager.load(UI_RIGHT);
        manager.load(UI_JUMP);
        manager.load(UI_SHOOT);
        manager.load(UI_GRENADE);
        manager.load(UI_PAUSE);

        manager.load(SCREEN_INTRO);

        manager.load(SCREEN_MENU_BG);
        manager.load(SCREEN_MENU_BTN_GAME);
        manager.load(SCREEN_MENU_BTN_OPTIONS);
        manager.load(SCREEN_MENU_BTN_CREDITS);
        manager.load(SCREEN_MENU_BTN_ACHIEVEMENTS);
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

    public Texture get(AssetDescriptor<Texture> desc) {
        return manager.get(desc);
    }

    public void dispose() {
        manager.dispose();
    }

    private void applyDefaultFilters() {
        setLinear(SKY);
        setLinear(CLOUDS);
        setLinear(RUINS);
        setLinear(MID);
        setLinear(NEAR);

        setLinear(AYLA_RUN);

        setLinear(UI_LEFT);
        setLinear(UI_RIGHT);
        setLinear(UI_JUMP);
        setLinear(UI_SHOOT);
        setLinear(UI_GRENADE);
        setLinear(UI_PAUSE);

        setLinear(SCREEN_INTRO);

        setLinear(SCREEN_MENU_BG);
        setLinear(SCREEN_MENU_BTN_GAME);
        setLinear(SCREEN_MENU_BTN_OPTIONS);
        setLinear(SCREEN_MENU_BTN_CREDITS);
        setLinear(SCREEN_MENU_BTN_ACHIEVEMENTS);
    }

    private void setLinear(AssetDescriptor<Texture> desc) {
        if (!manager.isLoaded(desc.fileName)) return;
        Texture t = manager.get(desc);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }
}
