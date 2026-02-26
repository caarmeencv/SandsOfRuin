package com.carmen.mijuego.settings;

import java.util.HashMap;
import java.util.Map;

/**
 * I18N por claves (ES/EN) basado en GameSettings.
 * - Guarda todas las traducciones en mapas.
 * - Devuelve el texto según el idioma actual.
 * - Si falta una key, devuelve la key (para detectar rápido errores).
 */
public final class I18N {

    private final GameSettings settings;

    // Mapas de traducciones
    private static final Map<String, String> ES = new HashMap<>();
    private static final Map<String, String> EN = new HashMap<>();

    // Se ejecuta una vez y rellena las traducciones
    static {

        /* =========================
           MENÚ PRINCIPAL
        ========================== */
        ES.put("menu.play", "JUGAR");
        EN.put("menu.play", "PLAY");

        ES.put("menu.options", "OPCIONES");
        EN.put("menu.options", "OPTIONS");

        ES.put("menu.credits", "CRÉDITOS");
        EN.put("menu.credits", "CREDITS");

        ES.put("menu.achievements", "RECORDS");
        EN.put("menu.achievements", "RECORDS");

        ES.put("menu.howto", "¿CÓMO JUGAR?");
        EN.put("menu.howto", "HOW TO PLAY?");


        /* =========================
           OPCIONES
        ========================== */
        ES.put("options.title", "OPCIONES");
        EN.put("options.title", "OPTIONS");

        ES.put("options.lang", "IDIOMA");
        EN.put("options.lang", "LANGUAGE");

        // ✅ solo el salto por acelerómetro
        ES.put("options.accelJump", "SALTO ACELERÓMETRO");
        EN.put("options.accelJump", "ACCELEROMETER JUMP");

        ES.put("options.vibration", "VIBRACIÓN");
        EN.put("options.vibration", "VIBRATION");

        ES.put("options.music", "MÚSICA");
        EN.put("options.music", "MUSIC");

        ES.put("options.sfx", "EFECTOS DE SONIDO");
        EN.put("options.sfx", "SOUND EFFECTS");

        ES.put("options.back", "VOLVER AL MENÚ");
        EN.put("options.back", "BACK TO MENU");


        /* =========================
           PAUSA
        ========================== */
        ES.put("pause.continue", "CONTINUAR");
        EN.put("pause.continue", "CONTINUE");

        ES.put("pause.restart", "REINICIAR");
        EN.put("pause.restart", "RESTART");

        ES.put("pause.menu", "VOLVER AL MENÚ");
        EN.put("pause.menu", "BACK TO MENU");


        /* =========================
           INTRO
        ========================== */
        ES.put("intro.tap", "TOCA PARA CONTINUAR");
        EN.put("intro.tap", "TAP TO CONTINUE");


        /* =========================
           GAME OVER / VICTORY (HINT)
        ========================== */
        ES.put("ui.back_to_menu_hint", "TOCA PARA VOLVER AL MENÚ\nPULSA ESC PARA VOLVER AL MENÚ");
        EN.put("ui.back_to_menu_hint", "TAP TO GO BACK TO MENU\nPRESS ESC TO GO BACK TO MENU");


        /* =========================
           CRÉDITOS
        ========================== */
        ES.put("credits.title", "CRÉDITOS");
        EN.put("credits.title", "CREDITS");

        ES.put("credits.hint", "TOCA PARA VOLVER");
        EN.put("credits.hint", "TAP TO GO BACK");

        ES.put("credits.text",
            "Desarrollo: Carmen\n" +
                "Proyecto: Sands of Ruin\n\n" +
                "Arte:\n- ChatGPT\n\n" +
                "Música y SFX:\n- Pixabay\n"
        );

        EN.put("credits.text",
            "Developer: Carmen\n" +
                "Project: Sands of Ruin\n\n" +
                "Art:\n- ChatGPT\n\n" +
                "Music & SFX:\n- Pixabay\n"
        );


        /* =========================
           LOGROS / RECORDS
        ========================== */
        ES.put("records.title", "LOGROS");
        EN.put("records.title", "ACHIEVEMENTS");

        ES.put("records.hint", "TOCA PARA VOLVER");
        EN.put("records.hint", "TAP TO GO BACK");

        ES.put("records.text",
            "Logros desbloqueados:\n\n" +
                "- Primer enemigo derrotado\n" +
                "- 1 minuto sobrevivido\n" +
                "- Llegar a la pirámide\n" +
                "- Derrotar a la momia\n"
        );

        EN.put("records.text",
            "Unlocked achievements:\n\n" +
                "- First enemy defeated\n" +
                "- Survive 1 minute\n" +
                "- Reach the pyramid\n" +
                "- Defeat the mummy\n"
        );


        /* =========================
           CÓMO JUGAR
        ========================== */
        ES.put("howto.title", "CÓMO JUGAR");
        EN.put("howto.title", "HOW TO PLAY");

        ES.put("howto.left",
            "CONTROLES MÓVIL:\n" +
                " Flechas: moverse\n" +
                " Flecha arriba: saltar (DOBLE SALTO)\n" +
                " Agitar móvil: saltar (activar en Ajustes)\n" +
                " Pistola: disparo normal\n" +
                " Bomba: especial (2 disparos, cooldown)\n\n" +
                "CONTROLES ORDENADOR:\n" +
                " A / D: moverse\n" +
                " ESPACIO o W: saltar\n" +
                " K: disparar\n" +
                " L: especial\n\n" +
                "COMBATE:\n" +
                " Soldado: 2 disparos\n" +
                " Tanque: 3 disparos\n" +
                " Momia: 5 disparos\n" +
                " Cactus: no se destruyen"
        );

        EN.put("howto.left",
            "MOBILE CONTROLS:\n" +
                " Arrows: move\n" +
                " Up arrow: jump (DOUBLE JUMP)\n" +
                " Shake phone: jump (enable in Options)\n" +
                " Pistol: normal shot\n" +
                " Bomb: special (2 shots, cooldown)\n\n" +
                "PC CONTROLS:\n" +
                " A / D: move\n" +
                " SPACE or W: jump\n" +
                " K: shoot\n" +
                " L: special\n\n" +
                "COMBAT:\n" +
                " Soldier: 2 shots\n" +
                " Tank: 3 shots\n" +
                " Mummy: 5 shots\n" +
                " Cactus: can't be destroyed"
        );

        ES.put("howto.right",
            "OBJETIVO:\n\n" +
                "Avanza por el desierto\n" +
                "superando enemigos y\n" +
                "obstáculos.\n\n" +
                "Llega a la pirámide\n" +
                "y derrota a la momia\n" +
                "para conseguir\n" +
                "el tesoro."
        );

        EN.put("howto.right",
            "GOAL:\n\n" +
                "Advance through the desert\n" +
                "beating enemies and\n" +
                "avoiding obstacles.\n\n" +
                "Reach the pyramid\n" +
                "and defeat the mummy\n" +
                "to obtain\n" +
                "the treasure."
        );

        ES.put("howto.tap.normal", "Toca aquí para volver");
        EN.put("howto.tap.normal", "Tap here to go back");

        ES.put("howto.tap.hover", "TOCA AQUÍ PARA VOLVER");
        EN.put("howto.tap.hover", "TAP HERE TO GO BACK");

        ES.put("howto.key.normal", "Pulsa ESC / Atrás para volver");
        EN.put("howto.key.normal", "Press ESC / Back to go back");

        ES.put("howto.key.hover", "PULSA ESC / ATRÁS PARA VOLVER");
        EN.put("howto.key.hover", "PRESS ESC / BACK TO GO BACK");
    }

    /**
     * Constructor: mantiene compatibilidad con tu estructura actual (GameSettings).
     */
    public I18N(GameSettings settings) {
        this.settings = settings;
    }

    /**
     * Devuelve el texto traducido para una key según el idioma de GameSettings.
     * - Si no existe en el idioma actual, intenta buscar en el otro idioma (fallback).
     * - Si tampoco existe, devuelve la key.
     */
    public String t(String key) {
        if (key == null) return "";

        boolean es = settings.isLangSpanish();

        String v = es ? ES.get(key) : EN.get(key);

        // fallback al otro idioma por si falta
        if (v == null) {
            v = es ? EN.get(key) : ES.get(key);
        }

        return (v == null) ? key : v;
    }
}
