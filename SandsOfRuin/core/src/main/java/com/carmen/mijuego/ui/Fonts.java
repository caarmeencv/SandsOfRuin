package com.carmen.mijuego.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.carmen.mijuego.assets.Assets;

public final class Fonts {

    // Fuente principal del juego
    // Se guarda en una variable estática para reutilizarla
    private static BitmapFont mainFont;

    // Constructor privado
    // No quiero que nadie cree objetos de esta clase
    // Solo se usan métodos estáticos
    private Fonts() {
    }

    // Devuelve la fuente principal del juego
    // Si todavía no existe, la carga desde archivo
    public static BitmapFont main() {

        // Si no está creada todavía, la crea
        if (mainFont == null) {
            mainFont = new BitmapFont(
                Gdx.files.internal(Assets.FONT_MAIN_FNT_PATH)
            );
        }

        return mainFont;
    }

    // Resetea el color de la fuente a blanco
    // Esto es importante porque muchas pantallas cambian el color
    public static void resetColor(BitmapFont font) {
        if (font != null) {
            font.setColor(1f, 1f, 1f, 1f);
        }
    }

    // Libera la memoria de la fuente
    // Se debería llamar cuando se cierre el juego
    public static void dispose() {

        if (mainFont != null) {
            mainFont.dispose();
            mainFont = null;
        }
    }
}
