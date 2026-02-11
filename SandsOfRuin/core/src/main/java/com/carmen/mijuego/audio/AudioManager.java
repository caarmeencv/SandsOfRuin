package com.carmen.mijuego.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.carmen.mijuego.assets.Assets;

public class AudioManager {

    private final Assets assets;

    private Music currentMusic;

    private float musicVol = 0.6f;
    private float sfxVol = 1.0f;

    private boolean musicEnabled = true;
    private boolean sfxEnabled = true;

    public AudioManager(Assets assets) {
        this.assets = assets;
    }

    // ===================== MUSIC =====================
    public void playMusic(AssetDescriptor<Music> musicDesc, boolean looping) {
        if (!musicEnabled) return;

        try {
            Music newMusic = assets.get(musicDesc);

            // si ya está sonando esa misma música, no la reinicies
            if (currentMusic == newMusic) return;

            stopMusic();

            currentMusic = newMusic;
            currentMusic.setLooping(looping);
            currentMusic.setVolume(musicVol);
            currentMusic.play();

        } catch (Exception e) {
            // ✅ en vez de petar "sin saber", lo verás en Logcat
            Gdx.app.error("AudioManager", "ERROR reproduciendo music: " + musicDesc.fileName, e);
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
        if (currentMusic != null) currentMusic.setVolume(musicVol);
    }

    public void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        if (!musicEnabled) stopMusic();
    }

    // ===================== SFX =====================
    public void playSfx(AssetDescriptor<Sound> sfxDesc) {
        if (!sfxEnabled) return;

        try {
            Sound s = assets.get(sfxDesc);
            s.play(sfxVol);
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "ERROR reproduciendo sfx: " + sfxDesc.fileName, e);
        }
    }

    public void setSfxVolume(float v) {
        sfxVol = clamp01(v);
    }

    public void setSfxEnabled(boolean enabled) {
        sfxEnabled = enabled;
    }

    private float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }
}
