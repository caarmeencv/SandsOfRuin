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

    // Esta variable guarda la música que está sonando en ese momento
    private Music currentMusic;

    // Volumen de música y efectos
    // La música la pongo un poco más baja para que no moleste
    private float musicVol = 0.6f;
    private float sfxVol = 1.0f;

    // Estas variables son un respaldo por si todavía no existen las settings
    // Así el audio puede seguir funcionando igualmente
    private boolean musicEnabledFallback = true;
    private boolean sfxEnabledFallback = true;

    // Constructores

    // Constructor cuando todavía no tengo GameSettings
    public AudioManager(Assets assets) {
        this.assets = assets;
        this.settings = null;
    }

    // Constructor cuando ya tengo GameSettings
    public AudioManager(Assets assets, GameSettings settings) {
        this.assets = assets;
        this.settings = settings;
    }

    // Permite asignar las settings después de crear el AudioManager
    public void setSettings(GameSettings settings) {
        this.settings = settings;
    }

    // Vibración

    public void vibrate(int ms) {

        // Primero compruebo si el dispositivo tiene vibrador
        boolean hasVibrator = Gdx.input.isPeripheralAvailable(Input.Peripheral.Vibrator);

        // Si no tiene vibrador, no hago nada
        if (!hasVibrator) {
            return;
        }

        boolean enabled;

        // Si existen settings, miro si la vibración está activada
        if (settings != null) {
            enabled = settings.isVibrationEnabled();
        } else {
            // Si no hay settings todavía, permito vibrar por defecto
            enabled = true;
        }

        // Si está activado, hago vibrar el móvil
        if (enabled) {
            Gdx.input.vibrate(ms);
        }
    }

    // Música

    public void playMusic(AssetDescriptor<Music> musicDesc, boolean looping) {

        // Si la música está desactivada, no hago nada
        if (!isMusicEnabled()) {
            return;
        }

        try {
            // Obtengo la música ya cargada
            Music newMusic = assets.get(musicDesc);

            // Si ya hay música sonando y es la misma, no la reinicio
            if (currentMusic != null) {
                if (currentMusic == newMusic) {
                    return;
                }
            }

            // Paro la música anterior
            stopMusic();

            // Guardo la nueva música
            currentMusic = newMusic;

            // Le digo si debe repetirse en bucle
            currentMusic.setLooping(looping);

            // Aplico el volumen actual
            currentMusic.setVolume(musicVol);

            // Empiezo a reproducirla
            currentMusic.play();

        } catch (Exception e) {
            Gdx.app.error("AudioManager",
                "Error reproduciendo música: " + musicDesc.fileName, e);
        }
    }

    public void stopMusic() {

        try {
            // Si hay música sonando, la paro
            if (currentMusic != null) {
                currentMusic.stop();
                currentMusic = null;
            }
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "Error parando música", e);
        }
    }

    // Cambia el volumen de la música
    public void setMusicVolume(float volume) {

        // Me aseguro de que esté entre cero y uno
        musicVol = clamp01(volume);

        // Si hay música sonando, aplico el nuevo volumen
        if (currentMusic != null) {
            currentMusic.setVolume(musicVol);
        }
    }

    // Activa o desactiva la música
    public void setMusicEnabled(boolean enabled) {

        if (settings != null) {
            settings.setMusicEnabled(enabled);
            settings.save();
        } else {
            musicEnabledFallback = enabled;
        }

        // Si la desactivo, paro la música actual
        if (!enabled) {
            stopMusic();
        }
    }

    public boolean isMusicEnabled() {

        if (settings != null) {
            return settings.isMusicEnabled();
        }

        return musicEnabledFallback;
    }

    // Efectos de sonido

    public void playSfx(AssetDescriptor<Sound> sfxDesc) {

        // Si los efectos están desactivados, no hago nada
        if (!isSfxEnabled()) {
            return;
        }

        try {
            // Obtengo el sonido ya cargado
            Sound sound = assets.get(sfxDesc);

            // Lo reproduzco con el volumen actual
            sound.play(sfxVol);

        } catch (Exception e) {
            Gdx.app.error("AudioManager",
                "Error reproduciendo efecto: " + sfxDesc.fileName, e);
        }
    }

    // Reproduce un sonido en bucle y devuelve un identificador
    // Ese identificador sirve para poder detenerlo después
    public long loopSfx(AssetDescriptor<Sound> sfxDesc, float volumeMultiplier) {

        if (!isSfxEnabled()) {
            return -1;
        }

        try {
            Sound sound = assets.get(sfxDesc);

            float finalVolume = sfxVol * volumeMultiplier;
            finalVolume = clamp01(finalVolume);

            long loopId = sound.loop(finalVolume);
            return loopId;

        } catch (Exception e) {
            Gdx.app.error("AudioManager",
                "Error reproduciendo loop de efecto: " + sfxDesc.fileName, e);
        }

        return -1;
    }

    // Detiene un sonido en bucle usando su identificador
    public void stopLoop(AssetDescriptor<Sound> sfxDesc, long loopId) {

        if (loopId == -1) {
            return;
        }

        try {
            Sound sound = assets.get(sfxDesc);
            sound.stop(loopId);

        } catch (Exception e) {
            Gdx.app.error("AudioManager",
                "Error deteniendo loop de efecto: " + sfxDesc.fileName, e);
        }
    }

    public void setSfxVolume(float volume) {
        sfxVol = clamp01(volume);
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

        if (settings != null) {
            return settings.isSfxEnabled();
        }

        return sfxEnabledFallback;
    }

    // Método auxiliar para asegurar que un valor esté entre cero y uno
    private float clamp01(float value) {

        if (value < 0f) {
            return 0f;
        }

        if (value > 1f) {
            return 1f;
        }

        return value;
    }
}
