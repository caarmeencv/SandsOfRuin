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

    public void playMusic(AssetDescriptor<Music> musicDesc, boolean looping) {
        if (!musicEnabled) return;

        try {
            Music newMusic = assets.get(musicDesc);

            if (currentMusic == newMusic) return;

            stopMusic();

            currentMusic = newMusic;
            currentMusic.setLooping(looping);
            currentMusic.setVolume(musicVol);
            currentMusic.play();

        } catch (Exception e) {
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

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void playSfx(AssetDescriptor<Sound> sfxDesc) {
        if (!sfxEnabled) return;

        try {
            Sound s = assets.get(sfxDesc);
            s.play(sfxVol);
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "ERROR reproduciendo sfx: " + sfxDesc.fileName, e);
        }
    }

    public long loopSfx(AssetDescriptor<Sound> sfxDesc, float volumeMultiplier) {
        if (!sfxEnabled) return -1;

        try {
            Sound s = assets.get(sfxDesc);
            float vol = clamp01(sfxVol * volumeMultiplier);
            return s.loop(vol);
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "ERROR loop sfx: " + sfxDesc.fileName, e);
            return -1;
        }
    }

    public void stopLoop(AssetDescriptor<Sound> sfxDesc, long loopId) {
        if (loopId == -1) return;

        try {
            Sound s = assets.get(sfxDesc);
            s.stop(loopId);
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "ERROR stop loop sfx: " + sfxDesc.fileName, e);
        }
    }

    public void stopAllSfx() {
        // Esto para todos los sonidos reproducidos por TODOS los Sound cargados.
        // Úsalo solo si quieres un "mute hard".
        // En general, mejor parar loops por id (como haces con Ayla).
        // Aquí no podemos iterar todos los Sound fácilmente sin guardarlos, así que lo dejamos vacío a propósito.
    }

    public void setSfxVolume(float v) {
        sfxVol = clamp01(v);
    }

    public void setSfxEnabled(boolean enabled) {
        sfxEnabled = enabled;


    }

    public boolean isSfxEnabled() {
        return sfxEnabled;
    }

    private float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }
}
