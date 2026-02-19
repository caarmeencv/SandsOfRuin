package com.carmen.mijuego.projectiles;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.carmen.mijuego.enemies.Tank;

/**
 * Sistema de balas del tanque:
 * - decide cuándo disparan los tanques (t.canShoot(delta))
 * - crea balas
 * - actualiza y elimina fuera/kill
 * - dibuja
 */
public class TankBulletSystem {

    // --- BALAS TANK (grandes) ---
    private static final float TANK_BULLET_W = 90f;
    private static final float TANK_BULLET_H = 45f;
    private static final float TANK_BULLET_SPEED = 700f;

    // altura del cañón (ajusta si lo ves alto/bajo)
    private static final float TANK_MUZZLE_Y = 200f;

    private final Texture bulletTexture;
    private final Array<Bullet> bullets = new Array<>();

    public TankBulletSystem(Texture bulletTexture) {
        this.bulletTexture = bulletTexture;
    }

    public void update(float delta, float camLeft, float camRight, Array<Tank> tanks) {

        // ================= DISPARO =================
        for (int i = 0; i < tanks.size; i++) {
            Tank t = tanks.get(i);

            if (t.canShoot(delta)) {

                boolean right = t.isFacingRight();

                // ✅ Spawn desde el "morro" del tanque según dirección
                float muzzleX = right
                    ? (t.getX() + t.getWidth())
                    : (t.getX() - TANK_BULLET_W);

                float muzzleY = t.getY() + TANK_MUZZLE_Y;

                // ✅ Velocidad correcta: derecha +, izquierda -
                float velX = right ? TANK_BULLET_SPEED : -TANK_BULLET_SPEED;

                bullets.add(new Bullet(
                    bulletTexture,
                    muzzleX,
                    muzzleY,
                    velX,
                    TANK_BULLET_W,
                    TANK_BULLET_H
                ));
            }
        }

        // ================= UPDATE + LIMPIEZA =================
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(delta);

            if (!b.isAlive()
                || b.getX() < camLeft - 400f
                || b.getX() > camRight + 400f) {
                bullets.removeIndex(i);
            }
        }
    }

    public void draw(SpriteBatch batch) {
        for (int i = 0; i < bullets.size; i++) {
            bullets.get(i).draw(batch);
        }
    }

    public Array<Bullet> getBullets() {
        return bullets;
    }

    // ✅ AÑADIDO: limpiar todas las balas (para fase 5 / cutscene)
    public void clear() {
        bullets.clear();
    }
}
