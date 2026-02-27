package com.carmen.mijuego.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class LivesHUD {

    // Texturas del corazón lleno y vacío
    private Texture heartFull;
    private Texture heartEmpty;

    // Número máximo de corazones que se dibujan
    private int maxHearts;

    // Tamaño de cada corazón
    private float heartSize;

    // Espacio entre corazones
    private float spacing;

    // Desplazamiento respecto a la esquina superior izquierda de cámara
    private float offsetX;
    private float offsetY;

    // Constructor simple con valores por defecto
    // 5 corazones, tamaño 56, separación 12 y offsets ajustados
    public LivesHUD(Texture heartFull, Texture heartEmpty) {
        this(heartFull, heartEmpty, 5, 56f, 12f, 18f, 22f);
    }

    // Constructor completo donde puedo personalizar todo
    public LivesHUD(Texture heartFull, Texture heartEmpty,
                    int maxHearts, float heartSize, float spacing,
                    float offsetX, float offsetY) {

        this.heartFull = heartFull;
        this.heartEmpty = heartEmpty;
        this.maxHearts = maxHearts;

        this.heartSize = heartSize;
        this.spacing = spacing;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    // Dibuja el HUD de vidas
    // camLeft y camTop se usan para que siempre quede fijo en pantalla
    public void draw(SpriteBatch batch, float camLeft, float camTop, int currentLives, boolean visible) {

        // Si no debe verse, no hace nada
        if (!visible) return;

        // Calcula la posición Y desde la parte superior de la cámara
        float y = camTop - offsetY - heartSize;

        // Guarda el color actual del batch para restaurarlo después
        float pr = batch.getColor().r;
        float pg = batch.getColor().g;
        float pb = batch.getColor().b;
        float pa = batch.getColor().a;

        // Recorre todos los corazones posibles
        for (int i = 0; i < maxHearts; i++) {

            // Calcula la posición X de cada corazón
            float x = camLeft + offsetX + i * (heartSize + spacing);

            // Si este índice es menor que las vidas actuales, dibuja corazón lleno
            if (i < currentLives) {
                batch.setColor(1f, 1f, 1f, 1f);
                batch.draw(heartFull, x, y, heartSize, heartSize);
            }
            // Si no, dibuja corazón vacío con transparencia
            else {
                batch.setColor(1f, 1f, 1f, 0.25f);
                batch.draw(heartEmpty, x, y, heartSize, heartSize);
            }
        }

        // Restaura el color original del batch
        batch.setColor(pr, pg, pb, pa);
    }

    // Versión simplificada que siempre lo dibuja visible
    public void draw(SpriteBatch batch, float camLeft, float camTop, int currentLives) {
        draw(batch, camLeft, camTop, currentLives, true);
    }
}
