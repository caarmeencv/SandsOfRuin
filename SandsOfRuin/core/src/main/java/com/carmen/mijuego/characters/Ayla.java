package com.carmen.mijuego.characters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Ayla {

    private float x, y, velY;
    private boolean facingRight = true;
    private boolean onGround = false;

    private static final float GRAVITY = -1800f;
    private static final float JUMP = 800f;

    private final Texture idleTex;
    private final Texture jumpTex;

    private final Animation<TextureRegion> runAnim;
    private float stateTime;

    private final float scale = 0.60f;
    private final float width, height;

    private static final int FRAME_W = 336;
    private static final int FRAME_H = 411;

    public Ayla(Texture runSheet, Texture idle, Texture jump, float startX, float startY) {
        this.idleTex = idle;
        this.jumpTex = jump;

        this.x = startX;
        this.y = startY;

        int cols = runSheet.getWidth() / FRAME_W;
        if (cols <= 0) throw new IllegalArgumentException("Spritesheet Ayla inválido");

        TextureRegion[][] split = TextureRegion.split(runSheet, FRAME_W, FRAME_H);
        TextureRegion[] frames = new TextureRegion[cols];
        for (int i = 0; i < cols; i++) frames[i] = split[0][i];

        runAnim = new Animation<>(0.10f, frames);

        width = FRAME_W * scale;
        height = FRAME_H * scale;
    }

    // Ayla NO mueve X: solo salto + animación
    public void update(float delta, boolean left, boolean right, boolean jump, float groundY) {
        boolean moving = (left ^ right);

        if (right) facingRight = true;
        if (left)  facingRight = false;

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

    public void setX(float x) { this.x = x; }
    public float getX() { return x; }
}
