package com.carmen.mijuego.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.carmen.mijuego.enemies.Cactus;

public class CactusManager {

    private static final float MIN_DIST = 900f;
    private static final float MAX_DIST = 1700f;
    private static final float SPAWN_MARGIN = 250f;
    private static final float CACTUS_HEIGHT = 90f;

    private final Texture cactusPink;
    private final Texture cactusYellow;

    private final Array<Cactus> cactuses = new Array<>();
    private float nextSpawnX;

    public CactusManager(Texture pink, Texture yellow, float startX) {
        this.cactusPink = pink;
        this.cactusYellow = yellow;
        this.nextSpawnX = startX;
    }

    public void update(float camLeft, float camRight) {
        if (camRight >= nextSpawnX) {
            Texture tex = MathUtils.randomBoolean() ? cactusPink : cactusYellow;
            float x = camRight + SPAWN_MARGIN;
            cactuses.add(new Cactus(tex, x, 180f, CACTUS_HEIGHT));

            nextSpawnX = camRight + MathUtils.random(MIN_DIST, MAX_DIST);
        }

        for (int i = cactuses.size - 1; i >= 0; i--) {
            if (cactuses.get(i).isOffScreenLeft(camLeft)) {
                cactuses.removeIndex(i);
            }
        }
    }

    public Array<Cactus> getCactuses() {
        return cactuses;
    }
}
