package com.carmen.mijuego.combat;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.carmen.mijuego.characters.Ayla;
import com.carmen.mijuego.enemies.Cactus;
import com.carmen.mijuego.enemies.Soldier;
import com.carmen.mijuego.enemies.Tank;
import com.carmen.mijuego.projectiles.Bullet;

/**
 * Sistema central de colisiones del nivel.
 * Gestiona:
 *  - Ayla vs cactus
 *  - Ayla vs tank (cuerpo)
 *  - Balas de Soldier -> Ayla
 *  - Balas de Tank -> Ayla
 *  - Balas de Ayla -> Soldier
 *  - Balas de Ayla -> Tank
 *
 * El efecto del golpe (knockback, perder vida, sonido...)
 * se ejecuta mediante Runnable onHit.
 */
public class CollisionSystem {

    private static final float HIT_DELAY = 0.55f;

    private float cooldown = 0f;

    public void update(
        float delta,
        Ayla ayla,
        Array<Cactus> cactuses,
        Array<Soldier> soldiers,
        Array<Tank> tanks,
        Array<Bullet> tankBullets,
        Runnable onHit
    ) {

        cooldown -= delta;
        if (cooldown < 0f) cooldown = 0f;

        Rectangle aylaBounds = ayla.getBounds();

        /* =======================================
           1) AYLA VS CACTUS
        ======================================== */
        if (cooldown <= 0f) {
            for (Cactus c : cactuses) {
                if (aylaBounds.overlaps(c.getBounds())) {
                    triggerHit(onHit);
                    return;
                }
            }
        }

        /* =======================================
           2) AYLA VS TANK (CUERPO)
        ======================================== */
        if (cooldown <= 0f) {
            for (Tank t : tanks) {
                if (!t.isDead() && aylaBounds.overlaps(t.getBounds())) {
                    triggerHit(onHit);
                    return;
                }
            }
        }

        /* =======================================
           3) BALAS SOLDIER -> AYLA
        ======================================== */
        if (cooldown <= 0f) {
            for (Soldier s : soldiers) {
                Array<Bullet> bullets = s.getBullets();

                for (int i = bullets.size - 1; i >= 0; i--) {
                    Bullet b = bullets.get(i);

                    if (b.isAlive() && aylaBounds.overlaps(b.getBounds())) {
                        b.kill();
                        triggerHit(onHit);
                        return;
                    }
                }
            }
        }

        /* =======================================
           4) BALAS TANK -> AYLA
        ======================================== */
        if (cooldown <= 0f) {
            for (int i = tankBullets.size - 1; i >= 0; i--) {
                Bullet b = tankBullets.get(i);

                if (b.isAlive() && aylaBounds.overlaps(b.getBounds())) {
                    b.kill();
                    triggerHit(onHit);
                    return;
                }
            }
        }

        /* =======================================
           5) BALAS AYLA -> SOLDIERS
        ======================================== */
        Array<Bullet> aylaBullets = ayla.getBullets();

        for (int i = aylaBullets.size - 1; i >= 0; i--) {
            Bullet ab = aylaBullets.get(i);
            if (!ab.isAlive()) continue;

            for (Soldier s : soldiers) {
                if (!s.isDead() && ab.getBounds().overlaps(s.getBounds())) {
                    ab.kill();
                    s.hitByAylaBullet();
                    break;
                }
            }
        }

        /* =======================================
           6) BALAS AYLA -> TANKS
        ======================================== */
        for (int i = aylaBullets.size - 1; i >= 0; i--) {
            Bullet ab = aylaBullets.get(i);
            if (!ab.isAlive()) continue;

            for (Tank t : tanks) {
                if (t.isDead() || t.isDestroying()) continue;

                if (ab.getBounds().overlaps(t.getBounds())) {
                    ab.kill();
                    t.hitByAylaBullet();
                    break;
                }
            }
        }
    }

    private void triggerHit(Runnable onHit) {
        cooldown = HIT_DELAY;
        if (onHit != null) {
            onHit.run();
        }
    }
}
