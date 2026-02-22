package com.carmen.mijuego.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.Viewport;

public class WorldController {

    private static final float SCROLL_FWD = 320f;
    private static final float SCROLL_BACK = 260f;

    private static final float KNOCK_DIST = 140f;
    private static final float KNOCK_SPEED = 900f;

    private float scrollX = 0f;
    private float knockRemaining = 0f;

    private boolean frozen = false;

    /**
     * Lógica de scroll (mundo) en función del input.
     * Ahora incluye “freeze” cuando camRight alcanza FREEZE_CAM_RIGHT.
     */
    public void update(float delta, boolean left, boolean right, float viewportWidth) {

        // Si ya está congelado, no se mueve nada.
        if (frozen) return;

        // Knockback (si aplica)
        if (knockRemaining > 0f) {
            float step = KNOCK_SPEED * delta;
            if (step > knockRemaining) step = knockRemaining;
            scrollX -= step;
            knockRemaining -= step;
            if (scrollX < 0f) scrollX = 0f;
        } else {
            if (right) scrollX += SCROLL_FWD * delta;
            if (left)  scrollX -= SCROLL_BACK * delta;
            if (scrollX < 0f) scrollX = 0f;
        }

        // Tras mover, comprobamos si toca congelar
        clampAndMaybeFreeze(viewportWidth);
    }

    /**
     * Congela el mundo cuando el borde derecho de la cámara llega a FREEZE_CAM_RIGHT.
     * camRight = scrollX + viewportWidth
     */
    private void clampAndMaybeFreeze(float viewportWidth) {
        float camRight = scrollX + viewportWidth;

        if (camRight >= LevelConfig.FREEZE_CAM_RIGHT) {
            // Ajustamos scrollX para que camRight quede EXACTAMENTE en FREEZE_CAM_RIGHT
            scrollX = LevelConfig.FREEZE_CAM_RIGHT - viewportWidth;
            if (scrollX < 0f) scrollX = 0f;

            frozen = true;
            knockRemaining = 0f; // importante: no seguir empujando hacia atrás
        }
    }

    public void applyKnockback() {
        // Si está congelado, no permitimos knockback (si no, “se movería”)
        if (frozen) return;
        knockRemaining = KNOCK_DIST;
    }

    /**
     * Cámara sigue al scrollX, pero si está congelado, se queda fija.
     */
    public void updateCamera(OrthographicCamera cam, Viewport vp, float delta, boolean moving) {

        // Por si alguien llama updateCamera sin llamar update()
        // garantizamos que el freeze se aplique también aquí.
        if (!frozen) {
            clampAndMaybeFreeze(vp.getWorldWidth());
        }

        float target = scrollX + vp.getWorldWidth() / 2f;

        if (!frozen && (moving || knockRemaining > 0f)) {
            cam.position.x += (target - cam.position.x) * 10f * delta;
        } else {
            cam.position.x = target;
        }

        cam.update();
    }

    public float getScrollX() { return scrollX; }

    public boolean isFrozen() { return frozen; }

    public void reset() {
        scrollX = 0f;
        knockRemaining = 0f;
        frozen = false;
    }
}
