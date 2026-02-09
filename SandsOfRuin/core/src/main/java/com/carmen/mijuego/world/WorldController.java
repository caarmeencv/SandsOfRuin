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

    public void update(float delta, boolean left, boolean right) {
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
    }

    public void applyKnockback() {
        knockRemaining = KNOCK_DIST;
    }

    public void updateCamera(OrthographicCamera cam, Viewport vp, float delta, boolean moving) {
        float target = scrollX + vp.getWorldWidth() / 2f;
        if (moving || knockRemaining > 0f) {
            cam.position.x += (target - cam.position.x) * 10f * delta;
        } else {
            cam.position.x = target;
        }
        cam.update();
    }

    public float getScrollX() { return scrollX; }
}
