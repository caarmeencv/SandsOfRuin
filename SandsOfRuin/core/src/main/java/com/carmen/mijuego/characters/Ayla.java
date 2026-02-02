package com.carmen.mijuego.characters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

public class Ayla {

    private float x, y, velY;
    private boolean facingRight = true;
    private boolean onGround = false;

    private static final float SPEED = 260f;
    private static final float GRAVITY = -1800f;
    private static final float JUMP = 800f;

    private Animation<TextureRegion> runAnim;
    private float stateTime;

    private float scale = 0.4f;
    private float width, height;

    private float lastDx;

    public Ayla(Texture runSheet, float startX, float startY) {
        x = startX;
        y = startY;

        int FRAME_W = 464;
        int FRAME_H = 688;

        TextureRegion[][] split = TextureRegion.split(runSheet, FRAME_W, FRAME_H);
        runAnim = new Animation<>(0.1f, split[0]);

        width = FRAME_W * scale;
        height = FRAME_H * scale;
    }

    public void update(float delta, boolean left, boolean right, boolean jump, float groundY) {
        float oldX = x;
        boolean moving = false;

        if (right) { x += SPEED * delta; facingRight = true; moving = true; }
        if (left)  { x -= SPEED * delta; facingRight = false; moving = true; }

        x = MathUtils.clamp(x, -99999, 99999);
        lastDx = x - oldX;

        if (jump && onGround) {
            velY = JUMP;
            onGround = false;
        }

        velY += GRAVITY * delta;
        y += velY * delta;

        if (y <= groundY) {
            y = groundY;
            velY = 0;
            onGround = true;
        }

        if (moving && onGround) stateTime += delta;
        else stateTime = 0;
    }

    public void draw(SpriteBatch batch) {
        TextureRegion frame = runAnim.getKeyFrame(stateTime, true);
        if (facingRight)
            batch.draw(frame, x, y, width, height);
        else
            batch.draw(frame, x + width, y, -width, height);
    }

    public float getLastDx() { return lastDx; }
    public float getX() { return x; }
}
