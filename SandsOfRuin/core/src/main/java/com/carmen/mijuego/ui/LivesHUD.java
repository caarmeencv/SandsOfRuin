package com.carmen.mijuego.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class LivesHUD {

    private final Texture heart;

    // Siempre 5 (por ahora)
    private static final int HEARTS = 5;

    // Tamaño y separación
    private final float heartSize;
    private final float spacing;

    // Posición (márgenes desde la esquina superior izquierda)
    private final float offsetX;
    private final float offsetY;

    public LivesHUD(Texture heart) {
        this(heart, 42f, 10f, 18f, 18f);
    }

    public LivesHUD(Texture heart, float heartSize, float spacing, float offsetX, float offsetY) {
        this.heart = heart;
        this.heartSize = heartSize;
        this.spacing = spacing;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    /**
     * Dibuja 5 corazones pegados a la pantalla, usando la cámara actual.
     */
    public void draw(SpriteBatch batch, float camLeft, float camTop) {
        float y = camTop - offsetY - heartSize;

        for (int i = 0; i < HEARTS; i++) {
            float x = camLeft + offsetX + i * (heartSize + spacing);
            batch.draw(heart, x, y, heartSize, heartSize);
        }
    }
}
