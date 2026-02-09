package com.carmen.mijuego.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Cactus {

    private final float worldX;
    private final float y;
    private final Texture texture;

    private final float width;
    private final float height;

    public Cactus(Texture texture, float worldX, float groundY, float heightWorld) {
        this.texture = texture;
        this.worldX = worldX;

        // 🌵 usa el mismo suelo que Ayla
        this.y = groundY;

        this.height = heightWorld;
        float ratio = (float) texture.getWidth() / (float) texture.getHeight();
        this.width = this.height * ratio;
    }

    public void draw(SpriteBatch batch, float midScroll) {
        float xDraw = worldX - midScroll;
        batch.draw(texture, xDraw, y, width, height);
    }

    public boolean isOffScreenLeft(float camLeft, float midScroll) {
        float xDraw = worldX - midScroll;
        return xDraw < camLeft - width - 150f;
    }
}
