package com.carmen.mijuego.projectiles;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import com.carmen.mijuego.enemies.Tank;

/**
 * Sistema de balas del tanque:
 * - decide cuándo disparan los tanques
 * - crea balas
 * - actualiza y elimina fuera/kill
 * - dibuja
 */
public class TankBulletSystem {

    // --- BALAS TANK (grandes) ---
    private static final float TANK_BULLET_W = 90f;
    private static final float TANK_BULLET_H = 45f;
    private static final float TANK_BULLET_SPEED = 700f;

    private final Texture bulletTexture;
    private final Array<Bullet> bullets = new Array<>();

    public TankBulletSystem(Texture bulletTexture) {
        this.bulletTexture = bulletTexture;
    }

    public void update(float delta, float camLeft, float camRight, Array<Tank> tanks) {

        // ================= DISPARO =================
        for (int i = 0; i < tanks.size; i++) {
            Tank t = tanks.get(i);

            // seguridad por si el tank ya está muerto/gone
            if (t.isDead() || t.isDestroying() || t.isGone()) continue;

            // ✅ ahora puede disparar tanto en MOVE como en IDLE (lo decide Tank.canShoot)
            if (!t.canShoot(delta)) continue;

            boolean right = t.isFacingRight();

            // ✅ usar muzzle real del Tank (más abajo y bien colocado)
            float muzzleX = t.getMuzzleX();
            float muzzleY = t.getMuzzleY();

            // centramos un poco la bala respecto al cañón
            // (si la quieres aún más baja, baja MUZZLE_Y_RATIO en Tank)
            float spawnX = right ? muzzleX : (muzzleX - TANK_BULLET_W);
            float spawnY = muzzleY - (TANK_BULLET_H * 0.35f);

            float velX = right ? TANK_BULLET_SPEED : -TANK_BULLET_SPEED;

            bullets.add(new Bullet(
                bulletTexture,
                spawnX,
                spawnY,
                velX,
                TANK_BULLET_W,
                TANK_BULLET_H
            ));
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

    public void clear() {
        bullets.clear();
    }
}
