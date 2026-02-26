package com.carmen.mijuego.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.settings.GameSettings;

public class AudioManager {

    private final Assets assets;
    private GameSettings settings;

    private Music currentMusic;

    private float musicVol = 0.6f;
    private float sfxVol = 1.0f;

    private boolean musicEnabledFallback = true;
    private boolean sfxEnabledFallback = true;

    public AudioManager(Assets assets) {
        this.assets = assets;
        this.settings = null;
    }

    public AudioManager(Assets assets, GameSettings settings) {
        this.assets = assets;
        this.settings = settings;
    }

    public void setSettings(GameSettings settings) {
        this.settings = settings;
    }

    // ==========================
    //VIBRACION (CONTROLADA POR SETTINGS)
    // ==========================
    public void vibrate(int ms) {

        // si no hay vibrator, salimos
        if (!Gdx.input.isPeripheralAvailable(Input.Peripheral.Vibrator)) return;

        boolean enabled = false;

        if (settings != null) {
            enabled = settings.isVibrationEnabled();
        }

        // fallback por si settings fuera null
        if (settings == null) {
            enabled = true;
        }

        if (enabled) {
            Gdx.input.vibrate(ms);
        }
    }

    // ==========================
    // MUSIC
    // ==========================

    public void playMusic(AssetDescriptor<Music> musicDesc, boolean looping) {

        if (!isMusicEnabled()) return;

        try {
            Music newMusic = assets.get(musicDesc);

            if (currentMusic == newMusic) return;

            stopMusic();

            currentMusic = newMusic;
            currentMusic.setLooping(looping);
            currentMusic.setVolume(musicVol);
            currentMusic.play();

        } catch (Exception e) {
            Gdx.app.error("AudioManager",
                "ERROR reproduciendo music: " + musicDesc.fileName, e);
        }
    }

    public void stopMusic() {
        try {
            if (currentMusic != null) {
                currentMusic.stop();
                currentMusic = null;
            }
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "ERROR parando music", e);
        }
    }

    public void setMusicVolume(float v) {
        musicVol = clamp01(v);
        if (currentMusic != null) {
            currentMusic.setVolume(musicVol);
        }
    }

    public void setMusicEnabled(boolean enabled) {

        if (settings != null) {
            settings.setMusicEnabled(enabled);
            settings.save();
        } else {
            musicEnabledFallback = enabled;
        }

        if (!enabled) stopMusic();
    }

    public boolean isMusicEnabled() {
        if (settings != null) return settings.isMusicEnabled();
        return musicEnabledFallback;
    }

    // ==========================
    // SFX
    // ==========================

    public void playSfx(AssetDescriptor<Sound> sfxDesc) {

        if (!isSfxEnabled()) return;

        try {
            Sound s = assets.get(sfxDesc);
            s.play(sfxVol);

        } catch (Exception e) {
            Gdx.app.error("AudioManager",
                "ERROR reproduciendo sfx: " + sfxDesc.fileName, e);
        }
    }

    public long loopSfx(AssetDescriptor<Sound> sfxDesc, float volumeMultiplier) {

        if (!isSfxEnabled()) return -1;

        try {
            Sound s = assets.get(sfxDesc);
            float vol = clamp01(sfxVol * volumeMultiplier);
            return s.loop(vol);

        } catch (Exception e) {
            Gdx.app.error("AudioManager",
                "ERROR loop sfx: " + sfxDesc.fileName, e);
            return -1;
        }
    }

    public void stopLoop(AssetDescriptor<Sound> sfxDesc, long loopId) {

        if (loopId == -1) return;

        try {
            Sound s = assets.get(sfxDesc);
            s.stop(loopId);

        } catch (Exception e) {
            Gdx.app.error("AudioManager",
                "ERROR stop loop sfx: " + sfxDesc.fileName, e);
        }
    }

    public void setSfxVolume(float v) {
        sfxVol = clamp01(v);
    }

    public void setSfxEnabled(boolean enabled) {

        if (settings != null) {
            settings.setSfxEnabled(enabled);
            settings.save();
        } else {
            sfxEnabledFallback = enabled;
        }
    }

    public boolean isSfxEnabled() {
        if (settings != null) return settings.isSfxEnabled();
        return sfxEnabledFallback;
    }

    private float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }
}
