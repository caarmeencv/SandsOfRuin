package com.carmen.mijuego.projectiles;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/** Proyectil simple (bala) que se mueve en horizontal y tiene hitbox. */
public class Bullet {

    private float x, y;
    private final float velX;

    private final float width;
    private final float height;

    private final Texture texture;
    private final Rectangle bounds = new Rectangle();

    private boolean alive = true;

    public Bullet(Texture texture, float startX, float startY, float velX, float width, float height) {
        this.texture = texture;
        this.x = startX;
        this.y = startY;
        this.velX = velX;
        this.width = width;
        this.height = height;
        updateBounds();
    }

    public void update(float delta) {
        x += velX * delta;
        updateBounds();
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, width, height);
    }

    private void updateBounds() {
        bounds.set(x, y, width, height);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isAlive() {
        return alive;
    }

    public void kill() {
        alive = false;
    }

    public float getX() {
        return x;
    }
}
