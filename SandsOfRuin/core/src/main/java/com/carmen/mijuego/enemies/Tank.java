package com.carmen.mijuego.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.audio.AudioManager;

public class Tank {

    private final AudioManager audio;

    private enum State { MOVE, IDLE, DESTROY, DEAD, GONE }

    private static final int MOVE_FRAMES = 4;
    private static final int DESTROY_FRAMES = 15;

    private static final float SCALE = 1.60f;

    // IA
    private static final float MOVE_SPEED = 230f;
    private static final float STOP_DISTANCE = 520f;

    private static final int HP = 3;
    private int hitsTaken = 0;

    private static final float FOOT_OFFSET = 10f;

    private static final float MOVE_FRAME_TIME = 0.10f;
    private static final float DESTROY_FRAME_TIME = 0.06f;

    private static final float SHOOT_COOLDOWN = 2.2f;
    private float shootTimer = 0f;

    private static final int BLINK_TIMES = 3;
    private static final float BLINK_INTERVAL = 0.10f;
    private float blinkTimer = 0f;
    private int blinkToggles = 0;
    private boolean visible = true;

    private float x, y;
    private boolean facingRight = false;

    private State state = State.MOVE;
    private float stateTime = 0f;

    private final Texture idleTex;
    private final Texture deadTex;

    private final Animation<TextureRegion> moveAnim;
    private final Animation<TextureRegion> destroyAnim;

    private final float width;
    private final float height;

    private final Rectangle bounds = new Rectangle();

    private long moveLoopId = -1;

    public Tank(AudioManager audio,
                Texture tankIdle,
                Texture tankMoveSheet,
                Texture tankDestroySheet,
                Texture tankDead,
                float startX,
                float startY) {

        this.audio = audio;

        this.idleTex = tankIdle;
        this.deadTex = tankDead;

        this.x = startX;
        this.y = startY;

        int moveFrameW = tankMoveSheet.getWidth() / MOVE_FRAMES;
        int moveFrameH = tankMoveSheet.getHeight();

        this.width = moveFrameW * SCALE;
        this.height = moveFrameH * SCALE;

        this.moveAnim = buildAnimHorizontal(tankMoveSheet, MOVE_FRAMES, MOVE_FRAME_TIME, true);
        this.destroyAnim = buildAnimHorizontal(tankDestroySheet, DESTROY_FRAMES, DESTROY_FRAME_TIME, false);

        updateBounds();
    }

    private Animation<TextureRegion> buildAnimHorizontal(Texture sheet, int frames, float frameTime, boolean loop) {
        int frameW = sheet.getWidth() / frames;
        int frameH = sheet.getHeight();

        TextureRegion[][] split = TextureRegion.split(sheet, frameW, frameH);

        Array<TextureRegion> regions = new Array<>(frames);
        for (int i = 0; i < frames; i++) regions.add(split[0][i]);

        Animation<TextureRegion> anim = new Animation<>(frameTime, regions);
        anim.setPlayMode(loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
        return anim;
    }

    public void update(float delta, float aylaX) {
        stateTime += delta;

        if (state == State.GONE) {
            stopMoveLoop();
            return;
        }

        if (state == State.DESTROY) {
            stopMoveLoop();
            if (destroyAnim.isAnimationFinished(stateTime)) {
                enterDeadBlink();
            }
            updateBounds();
            return;
        }

        if (state == State.DEAD) {
            stopMoveLoop();
            blinkTimer += delta;
            if (blinkTimer >= BLINK_INTERVAL) {
                blinkTimer = 0f;
                visible = !visible;
                blinkToggles++;

                if (blinkToggles >= BLINK_TIMES * 2) {
                    state = State.GONE;
                }
            }
            updateBounds();
            return;
        }

        facingRight = aylaX > x;

        float dx = aylaX - x;
        float absDx = Math.abs(dx);

        if (absDx <= STOP_DISTANCE) {
            state = State.IDLE;
            stopMoveLoop();
        } else {
            state = State.MOVE;
            float dir = dx > 0 ? 1f : -1f;
            x += dir * MOVE_SPEED * delta;

            startMoveLoop();
        }

        updateBounds();
    }

    private void startMoveLoop() {
        if (moveLoopId == -1) {
            moveLoopId = audio.loopSfx(Assets.SFX_TANK_MOVE, 0.35f);
        }
    }

    private void stopMoveLoop() {
        if (moveLoopId != -1) {
            audio.stopLoop(Assets.SFX_TANK_MOVE, moveLoopId);
            moveLoopId = -1;
        }
    }

    public boolean canShoot(float delta) {
        if (state == State.DEAD || state == State.DESTROY || state == State.GONE) return false;

        if (state != State.IDLE) {
            shootTimer = 0f;
            return false;
        }

        shootTimer += delta;
        if (shootTimer >= SHOOT_COOLDOWN) {
            shootTimer = 0f;

            audio.playSfx(Assets.SFX_EXPLOSION_GRENADE);

            return true;
        }
        return false;
    }

    public void hitByAylaBullet() {
        if (state == State.DESTROY || state == State.DEAD || state == State.GONE) return;

        hitsTaken++;
        if (hitsTaken >= HP) {
            state = State.DESTROY;
            stateTime = 0f;

            audio.playSfx(Assets.SFX_EXPLOSION_TANK);

            bounds.set(0, 0, 0, 0);
        }
    }

    private void enterDeadBlink() {
        state = State.DEAD;
        stateTime = 0f;

        blinkTimer = 0f;
        blinkToggles = 0;
        visible = true;

        bounds.set(0, 0, 0, 0);
    }

    public void draw(SpriteBatch batch) {
        if (state == State.GONE) return;
        if (state == State.DEAD && !visible) return;

        float drawY = y - FOOT_OFFSET;

        if (state == State.DEAD) {
            drawTexture(batch, deadTex, drawY);
            return;
        }

        if (state == State.DESTROY) {
            TextureRegion f = destroyAnim.getKeyFrame(stateTime, false);
            drawRegion(batch, f, drawY);
            return;
        }

        if (state == State.IDLE) {
            drawTexture(batch, idleTex, drawY);
            return;
        }

        TextureRegion frame = moveAnim.getKeyFrame(stateTime, true);
        drawRegion(batch, frame, drawY);
    }

    private void drawTexture(SpriteBatch batch, Texture tex, float drawY) {
        if (facingRight) batch.draw(tex, x, drawY, width, height);
        else batch.draw(tex, x + width, drawY, -width, height);
    }

    private void drawRegion(SpriteBatch batch, TextureRegion r, float drawY) {
        if (facingRight) batch.draw(r, x, drawY, width, height);
        else batch.draw(r, x + width, drawY, -width, height);
    }

    private void updateBounds() {
        if (state == State.DESTROY || state == State.DEAD || state == State.GONE) {
            bounds.set(0, 0, 0, 0);
            return;
        }

        float hitW = width * 0.75f;
        float hitH = height * 0.40f;

        float hitX = x + (width - hitW) / 2f;
        float hitY = y;

        if (hitW < 10f) hitW = 10f;
        if (hitH < 10f) hitH = 10f;

        bounds.set(hitX, hitY, hitW, hitH);
    }

    public Rectangle getBounds() { return bounds; }

    public boolean isDead() { return state == State.DEAD || state == State.GONE; }
    public boolean isDestroying() { return state == State.DESTROY; }
    public boolean isGone() { return state == State.GONE; }

    public boolean isOffScreenLeft(float camLeft) {
        return x + width < camLeft - 700f;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public boolean isFacingRight() { return facingRight; }
}
