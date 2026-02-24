package com.carmen.mijuego.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class LivesHUD {

    private final Texture heartFull;
    private final Texture heartEmpty;

    private final int maxHearts;

    private final float heartSize;
    private final float spacing;

    private final float offsetX;
    private final float offsetY;

    public LivesHUD(Texture heartFull, Texture heartEmpty) {
        this(heartFull, heartEmpty, 5, 42f, 10f, 18f, 18f);
    }

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

    // ✅ NUEVO: draw con flag
    public void draw(SpriteBatch batch, float camLeft, float camTop, int currentLives, boolean visible) {
        if (!visible) return;

        float y = camTop - offsetY - heartSize;

        float pr = batch.getColor().r;
        float pg = batch.getColor().g;
        float pb = batch.getColor().b;
        float pa = batch.getColor().a;

        for (int i = 0; i < maxHearts; i++) {
            float x = camLeft + offsetX + i * (heartSize + spacing);

            if (i < currentLives) {
                batch.setColor(1, 1, 1, 1f);
                batch.draw(heartFull, x, y, heartSize, heartSize);
            } else {
                batch.setColor(1, 1, 1, 0.25f);
                batch.draw(heartEmpty, x, y, heartSize, heartSize);
            }
        }

        batch.setColor(pr, pg, pb, pa);
    }

    // ✅ Mantengo tu método antiguo para no romper código
    public void draw(SpriteBatch batch, float camLeft, float camTop, int currentLives) {
        draw(batch, camLeft, camTop, currentLives, true);
    }
}
