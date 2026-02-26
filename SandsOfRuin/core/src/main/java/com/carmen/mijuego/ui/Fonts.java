package com.carmen.mijuego.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.carmen.mijuego.Main;
import com.carmen.mijuego.assets.Assets;

/**
 * Fuente única del juego (BitmapFont) cargada por AssetManager.
 * Importante: NO se debe hacer dispose() en las pantallas.
 * La libera AssetManager cuando se cierra el juego.
 */
public final class Fonts {

    private Fonts() {}

    /** Devuelve la fuente principal del juego (fonts/fonts.fnt). */
    public static BitmapFont main(Main game) {
        return game.assets.get(Assets.FONT_MAIN);
    }

    /** Reseteo rápido por si alguna pantalla cambia color. */
    public static void resetColor(BitmapFont font) {
        font.setColor(1f, 1f, 1f, 1f);
    }
}
