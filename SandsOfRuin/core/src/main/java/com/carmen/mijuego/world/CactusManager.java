package com.carmen.mijuego.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.carmen.mijuego.enemies.Cactus;

public class CactusManager {

    private static final float CACTUS_HEIGHT = 90f;
    private static final float CACTUS_Y = 180f;

    private final Texture cactusPink;
    private final Texture cactusYellow;

    private final Array<Cactus> cactuses = new Array<>();

    public CactusManager(Texture pink, Texture yellow) {
        this.cactusPink = pink;
        this.cactusYellow = yellow;
    }

    public void spawnCactusAt(float x, boolean pink) {
        Texture tex = pink ? cactusPink : cactusYellow;
        cactuses.add(new Cactus(tex, x, CACTUS_Y, CACTUS_HEIGHT));
    }

    public void update(float camLeft) {
        for (int i = cactuses.size - 1; i >= 0; i--) {
            if (cactuses.get(i).isOffScreenLeft(camLeft)) {
                cactuses.removeIndex(i);
            }
        }
    }

    public Array<Cactus> getCactuses() {
        return cactuses;
    }

    /** True si hay algún cactus visible/activo en cámara (con margen). */
    public boolean hasActive(float camLeft, float camRight) {
        for (int i = 0; i < cactuses.size; i++) {
            Cactus c = cactuses.get(i);
            float x = c.getBounds().x;
            if (x > camLeft - 200f && x < camRight + 200f) return true;
        }
        return false;
    }

    /** Borra todos los cactus (para entrar en fase 5 sin nada). */
    public void clear() {
        cactuses.clear();
    }
}
