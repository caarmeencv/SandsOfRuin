package com.carmen.mijuego.settings;

/**
 * I18N sencillo por claves (ES/EN) basado en GameSettings.
 * Si falta una key, devuelve la key tal cual para que lo detectes rápido.
 */
public class I18N {

    private final GameSettings settings;

    public I18N(GameSettings settings) {
        this.settings = settings;
    }

    public String t(String key) {
        boolean es = settings.isLangSpanish();

        switch (key) {

            /* =========================
               MENÚ PRINCIPAL
            ========================== */
            case "menu.play":          return es ? "JUGAR" : "PLAY";
            case "menu.options":       return es ? "OPCIONES" : "OPTIONS";
            case "menu.credits":       return es ? "CRÉDITOS" : "CREDITS";
            case "menu.achievements":  return es ? "LOGROS" : "ACHIEVEMENTS";
            case "menu.howto":         return es ? "¿CÓMO JUGAR?" : "HOW TO PLAY?";

            /* =========================
               OPCIONES
            ========================== */
            case "options.title":      return es ? "OPCIONES" : "OPTIONS";
            case "options.lang":       return es ? "IDIOMA" : "LANGUAGE";

            // ✅ solo el salto por acelerómetro
            case "options.accelJump":  return es ? "SALTO ACELERÓMETRO" : "ACCELEROMETER JUMP";

            case "options.vibration":  return es ? "VIBRACIÓN" : "VIBRATION";
            case "options.music":      return es ? "MÚSICA" : "MUSIC";
            case "options.sfx":        return es ? "EFECTOS DE SONIDO" : "SOUND EFFECTS";
            case "options.back":       return es ? "VOLVER AL MENÚ" : "BACK TO MENU";

            /* =========================
               PAUSA
            ========================== */
            case "pause.continue":     return es ? "CONTINUAR" : "CONTINUE";
            case "pause.restart":      return es ? "REINICIAR" : "RESTART";
            case "pause.menu":         return es ? "VOLVER AL MENÚ" : "BACK TO MENU";

            /* =========================
               INTRO
            ========================== */
            case "intro.tap":          return es ? "TOCA PARA CONTINUAR" : "TAP TO CONTINUE";

            /* =========================
               GAME OVER / VICTORY (HINT)
            ========================== */
            case "ui.back_to_menu_hint":
                return es
                    ? "TOCA PARA VOLVER AL MENÚ\nPULSA ESC PARA VOLVER AL MENÚ"
                    : "TAP TO GO BACK TO MENU\nPRESS ESC TO GO BACK TO MENU";

            /* =========================
               CRÉDITOS
            ========================== */
            case "credits.title":      return es ? "CRÉDITOS" : "CREDITS";
            case "credits.hint":       return es ? "TOCA PARA VOLVER" : "TAP TO GO BACK";
            case "credits.text":
                return es
                    ? "Desarrollo: Carmen\n"
                    + "Proyecto: Sands of Ruin\n\n"
                    + "Arte:\n- ChatGPT\n\n"
                    + "Música y SFX:\n- Pixabay\n"
                    : "Developer: Carmen\n"
                    + "Project: Sands of Ruin\n\n"
                    + "Art:\n- ChatGPT\n\n"
                    + "Music & SFX:\n- Pixabay\n";

            /* =========================
               LOGROS / RECORDS
            ========================== */
            case "records.title":      return es ? "LOGROS" : "ACHIEVEMENTS";
            case "records.hint":       return es ? "TOCA PARA VOLVER" : "TAP TO GO BACK";
            case "records.text":
                return es
                    ? "Logros desbloqueados:\n\n"
                    + "- Primer enemigo derrotado\n"
                    + "- 1 minuto sobrevivido\n"
                    + "- Llegar a la pirámide\n"
                    + "- Derrotar a la momia\n"
                    : "Unlocked achievements:\n\n"
                    + "- First enemy defeated\n"
                    + "- Survive 1 minute\n"
                    + "- Reach the pyramid\n"
                    + "- Defeat the mummy\n";

            /* =========================
               CÓMO JUGAR
            ========================== */
            case "howto.title":        return es ? "CÓMO JUGAR" : "HOW TO PLAY";

            case "howto.left":
                return es
                    ? "CONTROLES MÓVIL:\n"
                    + " Flechas: moverse\n"
                    + " Flecha arriba: saltar (DOBLE SALTO)\n"
                    + " Agitar móvil: saltar (activar en Ajustes)\n"
                    + " Pistola: disparo normal\n"
                    + " Bomba: especial (2 disparos, cooldown)\n\n"
                    + "CONTROLES ORDENADOR:\n"
                    + " A / D: moverse\n"
                    + " ESPACIO o W: saltar\n"
                    + " K: disparar\n"
                    + " L: especial\n\n"
                    + "COMBATE:\n"
                    + " Soldado: 2 disparos\n"
                    + " Tanque: 3 disparos\n"
                    + " Momia: 5 disparos\n"
                    + " Cactus: no se destruyen"
                    : "MOBILE CONTROLS:\n"
                    + " Arrows: move\n"
                    + " Up arrow: jump (DOUBLE JUMP)\n"
                    + " Shake phone: jump (enable in Options)\n"
                    + " Pistol: normal shot\n"
                    + " Bomb: special (2 shots, cooldown)\n\n"
                    + "PC CONTROLS:\n"
                    + " A / D: move\n"
                    + " SPACE or W: jump\n"
                    + " K: shoot\n"
                    + " L: special\n\n"
                    + "COMBAT:\n"
                    + " Soldier: 2 shots\n"
                    + " Tank: 3 shots\n"
                    + " Mummy: 5 shots\n"
                    + " Cactus: can't be destroyed";

            case "howto.right":
                return es
                    ? "OBJETIVO:\n\n"
                    + "Avanza por el desierto\n"
                    + "superando enemigos y\n"
                    + "obstáculos.\n\n"
                    + "Llega a la pirámide\n"
                    + "y derrota a la momia\n"
                    + "para conseguir\n"
                    + "el tesoro."
                    : "GOAL:\n\n"
                    + "Advance through the desert\n"
                    + "beating enemies and\n"
                    + "avoiding obstacles.\n\n"
                    + "Reach the pyramid\n"
                    + "and defeat the mummy\n"
                    + "to obtain\n"
                    + "the treasure.";

            case "howto.tap.normal":   return es ? "Toca aquí para volver" : "Tap here to go back";
            case "howto.tap.hover":    return es ? "TOCA AQUÍ PARA VOLVER" : "TAP HERE TO GO BACK";
            case "howto.key.normal":   return es ? "Pulsa ESC / Atrás para volver" : "Press ESC / Back to go back";
            case "howto.key.hover":    return es ? "PULSA ESC / ATRÁS PARA VOLVER" : "PRESS ESC / BACK TO GO BACK";

            default:
                return key;
        }
    }
}
