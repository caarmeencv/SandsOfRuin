package com.carmen.mijuego.projectiles;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.audio.AudioManager;
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

    // Ajustes del "muzzle"
    private static final float TANK_MUZZLE_Y = 200f;      // altura del cañón
    private static final float TANK_MUZZLE_INSET_X = 70f; // spawn dentro del sprite

    private final AudioManager audio;          // ✅ NUEVO
    private final Texture bulletTexture;
    private final Array<Bullet> bullets = new Array<>();

    public TankBulletSystem(AudioManager audio, Texture bulletTexture) {
        this.audio = audio;
        this.bulletTexture = bulletTexture;
    }

    public void update(float delta, float camLeft, float camRight, Array<Tank> tanks) {

        // ================= DISPARO =================
        for (int i = 0; i < tanks.size; i++) {
            Tank t = tanks.get(i);

            // seguridad por si el tank ya está muerto/gone
            if (t.isDead() || t.isDestroying() || t.isGone()) continue;

            // SOLO dispara si está parado (idle)
            if (!t.canShoot(delta)) continue;

            // ✅ SONIDO disparo tanque
            if (audio != null) audio.playSfx(Assets.SFX_EXPLOSION_GRENADE);

            boolean right = t.isFacingRight();

            float muzzleX;
            if (right) {
                muzzleX = (t.getX() + t.getWidth()) - TANK_MUZZLE_INSET_X;
            } else {
                muzzleX = t.getX() + TANK_MUZZLE_INSET_X - TANK_BULLET_W;
            }

            float muzzleY = t.getY() + TANK_MUZZLE_Y;
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
