package com.carmen.mijuego.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Cactus {

    private final float x;
    private final float y;
    private final Texture texture;

    private final float width;
    private final float height;

    // Offset visual (solo dibujo)
    private static final float FOOT_OFFSET = 14f;

    // HITBOX AJUSTADA (MENOS ALTA)
    private static final float HIT_PAD_L = 18f;
    private static final float HIT_PAD_R = 18f;

    // base menos peligrosa
    private static final float HIT_PAD_BOTTOM = 28f;

    // CLAVE: recortamos la parte superior
    private static final float HIT_PAD_TOP = 25f;

    private final Rectangle bounds = new Rectangle();

    public Cactus(Texture texture, float xWorld, float groundY, float heightWorld) {
        this.texture = texture;
        this.x = xWorld;
        this.y = groundY;

        this.height = heightWorld;
        float ratio = (float) texture.getWidth() / (float) texture.getHeight();
        this.width = this.height * ratio;

        updateBounds();
    }

    private void updateBounds() {
        float hitX = x + HIT_PAD_L;
        float hitY = y + HIT_PAD_BOTTOM;
        float hitW = width - (HIT_PAD_L + HIT_PAD_R);
        float hitH = height - (HIT_PAD_BOTTOM + HIT_PAD_TOP);

        // seguridad
        if (hitW < 10f) hitW = 10f;
        if (hitH < 10f) hitH = 10f;

        bounds.set(hitX, hitY, hitW, hitH);
    }

    public void draw(SpriteBatch batch) {
        float drawY = y - FOOT_OFFSET;
        batch.draw(texture, x, drawY, width, height);
    }

    public boolean isOffScreenLeft(float camLeft) {
        return x + width < camLeft - 150f;
    }

    public Rectangle getBounds() { return bounds; }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
}
