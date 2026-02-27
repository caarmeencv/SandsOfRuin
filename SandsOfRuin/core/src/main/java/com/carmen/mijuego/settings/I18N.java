package com.carmen.mijuego.settings;

import java.util.HashMap;
import java.util.Map;

public final class I18N {

    private final GameSettings settings;

    private static final Map<String, String> ES = new HashMap<String, String>();
    private static final Map<String, String> EN = new HashMap<String, String>();

    static {
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

        ES.put("options.title", "OPCIONES");
        EN.put("options.title", "OPTIONS");

        ES.put("options.lang", "IDIOMA");
        EN.put("options.lang", "LANGUAGE");

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

        ES.put("pause.continue", "CONTINUAR");
        EN.put("pause.continue", "CONTINUE");

        ES.put("pause.restart", "REINICIAR");
        EN.put("pause.restart", "RESTART");

        ES.put("pause.reset", "REINICIAR");
        EN.put("pause.reset", "RESTART");

        ES.put("pause.menu", "MENÚ");
        EN.put("pause.menu", "MENU");

        ES.put("intro.tap", "TOCA PARA CONTINUAR");
        EN.put("intro.tap", "TAP TO CONTINUE");

        ES.put("ui.back_to_menu_hint", "TOCA PARA VOLVER AL MENÚ\nPULSA ESC PARA VOLVER AL MENÚ");
        EN.put("ui.back_to_menu_hint", "TAP TO GO BACK TO MENU\nPRESS ESC TO GO BACK TO MENU");

        ES.put("credits.title", "CRÉDITOS");
        EN.put("credits.title", "CREDITS");

        ES.put("credits.hint", "TOCA PARA VOLVER");
        EN.put("credits.hint", "TAP TO GO BACK");

        ES.put("credits.text",
            "Desarrollo: Carmen\n" +
                "Proyecto: Sands of Ruin - Ayla Sahar\n\n" +
                "Arte:\n- ChatGPT\n- Grok\n" +
                "Música y SFX:\n- Pixabay\n" +
                "Estilo de texto:\n- Google Fonts\n"
        );

        EN.put("credits.text",
            "Developer: Carmen\n" +
                "Project: Sands of Ruin\n\n" +
                "Art:\n- ChatGPT\n" +
                "Music & SFX:\n- Pixabay\n" +
                "Text style:\n- Google Fonts\n"
        );

        ES.put("records.title", "RECORDS");
        EN.put("records.title", "RECORDS");

        ES.put("records.hint", "TOCA PARA VOLVER");
        EN.put("records.hint", "TAP TO GO BACK");

        ES.put("records.empty", "TODAVÍA NO HAS DERROTADO\nA LA MOMIA");
        EN.put("records.empty", "YOU HAVEN'T DEFEATED\nTHE MUMMY YET");

        ES.put("howto.title", "CÓMO JUGAR");
        EN.put("howto.title", "HOW TO PLAY");

        ES.put("howto.page1.left",
            "CONTROLES (MÓVIL)\n" +
                "• Moverse: flechas\n" +
                "• Saltar: flecha ↑ o acelerómetro\n" +
                "• Disparo: pistola\n" +
                "• Especial: granada\n\n" +
                "CONTROLES (PC)\n" +
                "• Moverse: A / D\n" +
                "• Saltar: W\n" +
                "• Disparo: K\n" +
                "• Especial: L"
        );

        EN.put("howto.page1.left",
            "CONTROLS (MOBILE)\n" +
                "• Move: arrows\n" +
                "• Jump: ↑ or accelerometer\n" +
                "• Shoot: pistol\n" +
                "• Special: grenade\n\n" +
                "CONTROLS (PC)\n" +
                "• Move: A / D\n" +
                "• Jump: W\n" +
                "• Shoot: K\n" +
                "• Special: L"
        );

        ES.put("howto.page1.right",
            "OBJETIVO\n" +
                "Cruza el desierto y llega\n" +
                "a la pirámide para conseguir\n" +
                "el tesoro.\n\n" +
                "VIDAS\n" +
                "• Empiezas con 5\n\n" +
                "POWER-UP\n" +
                "• En la pirámide: +2 vidas"
        );

        EN.put("howto.page1.right",
            "GOAL\n" +
                "Cross the desert and reach\n" +
                "the pyramid to claim\n" +
                "the treasure.\n\n" +
                "LIVES\n" +
                "• You start with 5\n\n" +
                "POWER-UP\n" +
                "• At the pyramid: +2 lives"
        );

        ES.put("howto.page2.left",
            "ENEMIGOS\n" +
                "• Cactus: no se destruyen\n" +
                "• Soldados: 2 impactos\n" +
                "• Tanques: 3 impactos\n" +
                "• Momia: 5 impactos"
        );

        EN.put("howto.page2.left",
            "ENEMIES\n" +
                "• Cactus: indestructible\n" +
                "• Soldiers: 2 hits\n" +
                "• Tanks: 3 hits\n" +
                "• Mummy: 5 hits"
        );

        ES.put("howto.page2.right",
            "ARMAS Y COOLDOWNS\n" +
                "• Especial (granada): 15 s\n    2 impactos\n\n" +
                "• Pistola: 2 balas\n" +
                "  Recarga automática: 3 s"
        );

        EN.put("howto.page2.right",
            "WEAPONS & COOLDOWNS\n" +
                "• Special (grenade): 15 s\n    2 impacts\n\n" +
                "• Pistol: 2 bullets\n" +
                "  Auto-reload: 3 s"
        );

        ES.put("howto.page", "PÁGINA");
        EN.put("howto.page", "PAGE");

        ES.put("howto.prev", "Anterior");
        EN.put("howto.prev", "Prev");

        ES.put("howto.next", "Siguiente");
        EN.put("howto.next", "Next");
    }

    public I18N(GameSettings settings) {
        this.settings = settings;
    }

    public String t(String key) {
        if (key == null) return "";

        boolean es = settings.isLangSpanish();

        String v;
        if (es) v = ES.get(key);
        else v = EN.get(key);

        if (v == null) {
            if (es) v = EN.get(key);
            else v = ES.get(key);
        }

        if (v == null) return key;
        return v;
    }
}
