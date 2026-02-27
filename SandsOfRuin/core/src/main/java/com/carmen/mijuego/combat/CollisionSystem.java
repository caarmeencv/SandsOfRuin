package com.carmen.mijuego.combat;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import com.carmen.mijuego.characters.Ayla;
import com.carmen.mijuego.enemies.Cactus;
import com.carmen.mijuego.enemies.Soldier;
import com.carmen.mijuego.enemies.Tank;
import com.carmen.mijuego.projectiles.Bullet;

public class CollisionSystem {

    // Esto es el tiempo de “protección” después de que Ayla reciba daño
    // Sirve para que no pierda 5 vidas seguidas en medio segundo por tocar algo
    private static final float HIT_DELAY = 0.55f;

    // Este contador baja con el tiempo y mientras esté activo no se puede volver a recibir daño
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

        // Bajo el cooldown con el delta del frame
        cooldown -= delta;

        // Si baja de cero lo dejo en cero para evitar valores raros
        if (cooldown < 0f) {
            cooldown = 0f;
        }

        // Cojo el rectángulo de colisión de Ayla una sola vez para reutilizarlo
        Rectangle aylaBounds = ayla.getBounds();

        // Aquí van las colisiones que dañan a Ayla
        // Solo se comprueban si el cooldown está a cero
        if (cooldown <= 0f) {

            // Primero miro si Ayla toca un cactus
            for (Cactus c : cactuses) {
                if (aylaBounds.overlaps(c.getBounds())) {
                    triggerHit(onHit);
                    return;
                }
            }

            // Luego miro si Ayla choca con un soldado por el cuerpo
            for (Soldier s : soldiers) {

                // Si el soldado ya está muerto o ya desapareció de la escena, lo ignoro
                if (s.isDead() || s.isGone()) {
                    continue;
                }

                if (aylaBounds.overlaps(s.getBounds())) {
                    triggerHit(onHit);
                    return;
                }
            }

            // Luego miro si Ayla choca con un tanque por el cuerpo
            for (Tank t : tanks) {

                // Si el tanque está muerto, explotando o ya desapareció, lo ignoro
                if (t.isDead() || t.isDestroying() || t.isGone()) {
                    continue;
                }

                if (aylaBounds.overlaps(t.getBounds())) {
                    triggerHit(onHit);
                    return;
                }
            }

            // Luego miro las balas de cada soldado y compruebo si alguna da a Ayla
            for (Soldier s : soldiers) {

                // Cojo la lista de balas del soldado
                Array<Bullet> bullets = s.getBullets();

                // Recorro desde el final porque si quito balas es más seguro así
                for (int i = bullets.size - 1; i >= 0; i--) {
                    Bullet b = bullets.get(i);

                    // Solo si la bala está viva y toca a Ayla
                    if (b.isAlive() && aylaBounds.overlaps(b.getBounds())) {
                        b.kill();
                        triggerHit(onHit);
                        return;
                    }
                }
            }

            // Luego miro las balas del tanque que vienen en la lista tankBullets
            for (int i = tankBullets.size - 1; i >= 0; i--) {
                Bullet b = tankBullets.get(i);

                if (b.isAlive() && aylaBounds.overlaps(b.getBounds())) {
                    b.kill();
                    triggerHit(onHit);
                    return;
                }
            }
        }

        // A partir de aquí son las colisiones donde Ayla hace daño con sus balas

        // Cojo las balas de Ayla
        Array<Bullet> aylaBullets = ayla.getBullets();

        // Primero Ayla contra soldados
        for (int i = aylaBullets.size - 1; i >= 0; i--) {
            Bullet ab = aylaBullets.get(i);

            // Si la bala ya está muerta no hago nada
            if (!ab.isAlive()) {
                continue;
            }

            // Cojo el rectángulo de la bala y su daño
            Rectangle abBounds = ab.getBounds();
            int dmg = ab.getDamage();

            // Recorro todos los soldados para ver si alguno recibe el impacto
            for (Soldier s : soldiers) {

                // Si el soldado ya no cuenta, lo salto
                if (s.isDead() || s.isGone()) {
                    continue;
                }

                // Si hay choque bala contra soldado
                if (abBounds.overlaps(s.getBounds())) {

                    // Mato la bala para que no atraviese y pegue a más
                    ab.kill();

                    // Aquí aplico daño según el valor dmg
                    // Si dmg es 2, llamo dos veces a hitByAylaBullet
                    for (int d = 0; d < dmg; d++) {
                        s.hitByAylaBullet();

                        // Si ya muere con un golpe, corto el bucle
                        if (s.isDead() || s.isGone()) {
                            break;
                        }
                    }

                    // Salgo porque esa bala ya impactó
                    break;
                }
            }
        }

        // Ahora Ayla contra tanques
        for (int i = aylaBullets.size - 1; i >= 0; i--) {
            Bullet ab = aylaBullets.get(i);

            if (!ab.isAlive()) {
                continue;
            }

            Rectangle abBounds = ab.getBounds();
            int dmg = ab.getDamage();

            for (Tank t : tanks) {

                // Si el tanque está fuera de juego no lo golpeo
                if (t.isDead() || t.isDestroying() || t.isGone()) {
                    continue;
                }

                if (abBounds.overlaps(t.getBounds())) {

                    ab.kill();

                    // Igual que con el soldado, aplico tantos impactos como diga dmg
                    for (int d = 0; d < dmg; d++) {
                        t.hitByAylaBullet();

                        // Si ya muere o empieza a destruirse, paro
                        if (t.isDead() || t.isDestroying() || t.isGone()) {
                            break;
                        }
                    }

                    break;
                }
            }
        }
    }

    private void triggerHit(Runnable onHit) {

        // Cuando Ayla recibe un golpe, activo el cooldown para que no reciba otro enseguida
        cooldown = HIT_DELAY;

        // Si me han pasado una acción para ejecutar cuando hay daño, la ejecuto
        // Normalmente esto será algo como quitar vida, poner invulnerabilidad, sonido y vibración
        if (onHit != null) {
            onHit.run();
        }
    }
}
