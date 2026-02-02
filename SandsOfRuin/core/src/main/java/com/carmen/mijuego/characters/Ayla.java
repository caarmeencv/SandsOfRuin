package com.carmen.mijuego.characters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

public class Ayla {

    private float x, y;
    private float velY = 0f;

    private float speed = 260f;
    private boolean facingRight = true;
    private boolean onGround = false;

    private static final float GRAVITY = -1800f;
    private static final float JUMP_VEL = 800f;

    private Texture runSheet;
    private Animation<TextureRegion> runAnim;
    private float stateTime = 0f;

    private float scale = 0.4f;
    private float width, height;

    private float lastDx = 0f;
    public float getLastDx() { return lastDx; }

    public Ayla(float startX, float startY) {
        x = startX;
        y = startY;

        runSheet = new Texture("characters/ayla/ayla_run.png");
        runSheet.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        int FRAME_WIDTH = 464;
        int FRAME_HEIGHT = 688;

        TextureRegion[][] tmp = TextureRegion.split(runSheet, FRAME_WIDTH, FRAME_HEIGHT);
        TextureRegion[] frames = tmp[0];

        runAnim = new Animation<>(0.10f, frames);

        width  = FRAME_WIDTH * scale;
        height = FRAME_HEIGHT * scale;
    }

    public void update(float delta, boolean moveLeft, boolean moveRight, boolean jump, float groundY) {
        float oldX = x;
        boolean moving = false;

        // horizontal
        if (moveRight) {
            x += speed * delta;
            facingRight = true;
            moving = true;
        }
        if (moveLeft) {
            x -= speed * delta;
            facingRight = false;
            moving = true;
        }

        x = MathUtils.clamp(x, -999999f, 999999f);

        lastDx = x - oldX;

        if (jump && onGround) {
            velY = JUMP_VEL;
            onGround = false;
        }

        velY += GRAVITY * delta;
        y += velY * delta;

        if (y <= groundY) {
            y = groundY;
            velY = 0f;
            onGround = true;
        } else {
            onGround = false;
        }

        if (moving && onGround) stateTime += delta;
        else stateTime = 0f;
    }

    public void draw(SpriteBatch batch) {
        TextureRegion frame = runAnim.getKeyFrame(stateTime, true);

        if (facingRight) {
            batch.draw(frame, x, y, width, height);
        } else {
            batch.draw(frame, x + width, y, -width, height);
        }
    }

    public float getX() { return x; }
    public float getWidth() { return width; }

    public void dispose() {
        runSheet.dispose();
    }
}
