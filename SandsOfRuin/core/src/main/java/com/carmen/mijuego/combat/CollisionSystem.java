package com.carmen.mijuego.combat;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.carmen.mijuego.characters.Ayla;
import com.carmen.mijuego.enemies.Cactus;

public class CollisionSystem {

    private static final float HIT_DELAY = 0.55f;

    private float cooldown = 0f;

    public void update(float delta, Ayla ayla, Array<Cactus> cactuses, Runnable onHit) {
        cooldown -= delta;
        if (cooldown > 0f) return;

        Rectangle ab = ayla.getBounds();

        for (Cactus c : cactuses) {
            if (ab.overlaps(c.getBounds())) {
                cooldown = HIT_DELAY;
                onHit.run();   // knockback, vida, sonido…
                break;
            }
        }
    }
}
