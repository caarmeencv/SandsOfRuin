package com.carmen.mijuego;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.audio.AudioManager;
import com.carmen.mijuego.screens.IntroScreen;
import com.carmen.mijuego.settings.GameSettings;
import com.carmen.mijuego.settings.I18N;
import com.carmen.mijuego.settings.MummyTimeRecords;
import com.carmen.mijuego.ui.Fonts;

public class Main extends Game {

    // SpriteBatch principal usado para dibujar todo el juego
    public SpriteBatch batch;

    // Gestor de recursos gráficos y sonidos
    public Assets assets;

    // Controlador de música y efectos
    public AudioManager audio;

    // Configuración del juego (idioma, vibración, música, etc)
    public GameSettings settings;

    // Sistema de textos traducidos
    public I18N i18n;

    // Número de vidas actuales del jugador
    public int vidas = 5;

    // Contador global de tiempo desde que empieza la partida
    public float runTimeSeconds = 0f;

    @Override
    public void create() {

        // Creamos el SpriteBatch principal
        batch = new SpriteBatch();

        // Inicializamos el gestor de assets
        assets = new Assets();

        // Encolamos todos los recursos que se van a cargar
        assets.queueLoadAll();

        // Esperamos hasta que termine de cargar todo
        assets.finishLoading();

        // Cargamos las opciones guardadas
        settings = new GameSettings();

        // Inicializamos el sistema de textos según el idioma
        i18n = new I18N(settings);

        // Creamos el gestor de audio con assets y configuración
        audio = new AudioManager(assets, settings);

        // Reiniciamos variables de partida
        resetRun();

        // Establecemos la primera pantalla del juego
        setScreen(new IntroScreen(this));
    }

    // Reinicia las variables principales al empezar una nueva partida
    public void resetRun() {

        // Restauramos las vidas iniciales
        vidas = 5;

        // Reiniciamos el contador global de tiempo
        runTimeSeconds = 0f;
    }

    // Se llama justo cuando la momia muere
    // Guarda el tiempo total de la partida en el sistema de récords
    public void onMummyDefeated() {

        // Convertimos el tiempo acumulado a segundos enteros hacia arriba
        int secs = (int) Math.ceil(runTimeSeconds);

        // Nos aseguramos de que el mínimo sea 1 segundo
        if (secs < 1) secs = 1;

        // Mensaje de log para comprobar que se guarda correctamente
        Gdx.app.log("Main", "MUMMY DEFEATED -> saving time=" + secs + "s (runTimeSeconds=" + runTimeSeconds + ")");

        // Guardamos el tiempo en la lista de récords
        MummyTimeRecords.addTimeSeconds(secs);

        // Línea opcional de debug si quieres imprimir los valores guardados
        // MummyTimeRecords.debugRaw();
    }

    // Aplica vibración solo si está activada en las opciones
    public void vibrateHit(int ms) {

        // Comprobamos que settings exista y que la vibración esté activada
        if (settings != null && settings.isVibrationEnabled()) {

            // Activamos vibración del dispositivo
            Gdx.input.vibrate(ms);
        }
    }

    @Override
    public void dispose() {

        // Llamamos al dispose de la clase padre
        super.dispose();

        // Detenemos la música si está sonando
        if (audio != null) audio.stopMusic();

        // Liberamos las fuentes
        Fonts.dispose();

        // Liberamos el SpriteBatch
        if (batch != null) batch.dispose();

        // Liberamos todos los assets cargados
        if (assets != null) assets.dispose();
    }
}
