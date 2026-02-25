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

    private final int damage;

    public Bullet(Texture texture, float startX, float startY, float velX, float width, float height) {
        this(texture, startX, startY, velX, width, height, 1);
    }

    public Bullet(Texture texture, float startX, float startY, float velX, float width, float height, int damage) {
        this.texture = texture;
        this.x = startX;
        this.y = startY;
        this.velX = velX;
        this.width = width;
        this.height = height;
        this.damage = damage;
        updateBounds();
    }

    public void update(float delta) {
        x += velX * delta;
        updateBounds();
    }

    public void draw(SpriteBatch batch) {
        if (velX >= 0) {
            batch.draw(texture, x, y, width, height);
        } else {
            batch.draw(texture, x + width, y, -width, height);
        }
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

    public float getVelX() {
        return velX;
    }

    public int getDamage() {
        return damage;
    }
}
