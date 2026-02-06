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

    private final Texture idleTex;
    private final Texture jumpTex;

    private Animation<TextureRegion> runAnim;
    private float stateTime;

    // Más pequeña:
    private float scale = 0.60f;
    private float width, height;

    private float lastDx;

    // Frames reales del run sheet
    private static final int FRAME_W = 336;
    private static final int FRAME_H = 411;

    public Ayla(Texture runSheet, Texture idle, Texture jump, float startX, float startY) {
        this.idleTex = idle;
        this.jumpTex = jump;

        x = startX;
        y = startY;

        int cols = runSheet.getWidth() / FRAME_W;
        int rows = runSheet.getHeight() / FRAME_H;

        if (cols <= 0 || rows <= 0) {
            throw new IllegalArgumentException(
                "Spritesheet Ayla demasiado pequeño. Sheet=" +
                    runSheet.getWidth() + "x" + runSheet.getHeight() +
                    " frame=" + FRAME_W + "x" + FRAME_H
            );
        }

        TextureRegion[][] split = TextureRegion.split(runSheet, FRAME_W, FRAME_H);

        TextureRegion[] frames = new TextureRegion[cols];
        for (int i = 0; i < cols; i++) frames[i] = split[0][i];

        runAnim = new Animation<>(0.10f, frames);

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

    // El nivel empuja a Ayla: no puede estar por detrás de minX
    public void pushMinX(float minX) {
        if (x < minX) {
            x = minX;
        }
    }

    public void draw(SpriteBatch batch, boolean moving) {
        if (!onGround) {
            drawTexture(batch, jumpTex);
            return;
        }

        if (!moving) {
            drawTexture(batch, idleTex);
            return;
        }

        TextureRegion frame = runAnim.getKeyFrame(stateTime, true);
        if (facingRight) batch.draw(frame, x, y, width, height);
        else batch.draw(frame, x + width, y, -width, height);
    }

    private void drawTexture(SpriteBatch batch, Texture tex) {
        if (facingRight) batch.draw(tex, x, y, width, height);
        else batch.draw(tex, x + width, y, -width, height);
    }

    public float getLastDx() { return lastDx; }
    public float getX() { return x; }
    public boolean isOnGround() { return onGround; }
}
