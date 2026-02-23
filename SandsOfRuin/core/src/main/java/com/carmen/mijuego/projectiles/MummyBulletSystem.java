package com.carmen.mijuego.projectiles;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.carmen.mijuego.enemies.Mummy;

/**
 * Sistema de balas de la momia (tipo TankBulletSystem):
 * - decide cuándo dispara la momia (m.canShoot(delta))
 * - crea balas con BULLET_MUMMY
 * - actualiza, elimina fuera
 * - dibuja
 */
public class MummyBulletSystem {

    private static final float MUMMY_BULLET_W = 70f;
    private static final float MUMMY_BULLET_H = 34f;
    private static final float MUMMY_BULLET_SPEED = 820f;

    // altura aproximada desde el suelo hasta "la mano/boca" (ajústalo si lo ves alto/bajo)
    private static final float MUMMY_MUZZLE_Y = 210f;

    private final Texture bulletTexture;
    private final Array<Bullet> bullets = new Array<>();

    public MummyBulletSystem(Texture bulletTexture) {
        this.bulletTexture = bulletTexture;
    }

    public void update(float delta, float camLeft, float camRight, Mummy mummy) {
        if (mummy == null || mummy.isDead()) return;

        // ================= DISPARO =================
        if (mummy.canShoot(delta)) {

            boolean right = mummy.isFacingRight();

            float muzzleX = right
                ? (mummy.getX() + mummy.getWidth())
                : (mummy.getX() - MUMMY_BULLET_W);

            float muzzleY = mummy.getY() + MUMMY_MUZZLE_Y;

            float velX = right ? MUMMY_BULLET_SPEED : -MUMMY_BULLET_SPEED;

            bullets.add(new Bullet(
                bulletTexture,
                muzzleX,
                muzzleY,
                velX,
                MUMMY_BULLET_W,
                MUMMY_BULLET_H
            ));
        }

        // ================= UPDATE + LIMPIEZA =================
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(delta);

            if (!b.isAlive()
                || b.getX() < camLeft - 500f
                || b.getX() > camRight + 500f) {
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
