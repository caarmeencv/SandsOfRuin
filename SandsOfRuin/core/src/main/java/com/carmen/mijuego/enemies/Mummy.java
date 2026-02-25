package com.carmen.mijuego.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.audio.AudioManager;

public class Mummy {

    private final AudioManager audio;

    public enum State { IDLE, WALK, DYING_HURT, DEAD }

    private static final int FRAME_W = 410;
    private static final int FRAME_H = 608;

    private static final float VISUAL_SCALE = 0.68f;
    private final float width  = FRAME_W * VISUAL_SCALE;
    private final float height = FRAME_H * VISUAL_SCALE;

    private static final float FOLLOW_Y_OFFSET = -45f;
    private static final float DRAW_Y_OFFSET = -10f;

    private static final float WALK_FRAME = 0.12f;
    private static final float HURT_FRAME = 0.08f;

    private static final int HP_MAX = 5;
    private int hp = HP_MAX;

    private static final float SPEED = 45f;
    private static final float STOP_DISTANCE = 650f;

    private static final float SHOOT_COOLDOWN = 2.20f;
    private float shootTimer = 0f;

    private static final float HIT_SCALE = 0.40f;
    private static final float HIT_PAD_L = 120f * HIT_SCALE;
    private static final float HIT_PAD_R = 120f * HIT_SCALE;
    private static final float HIT_PAD_B = 70f  * HIT_SCALE;
    private static final float HIT_PAD_T = 90f  * HIT_SCALE;

    private final Texture idleTex;
    private final Texture deadTex;
    private final Texture walkSheet;
    private final Texture hurtSheet;

    private final TextureRegion idleFrame;
    private final TextureRegion deadFrame;

    private final Animation<TextureRegion> walkAnim;
    private final Animation<TextureRegion> hurtAnim;

    private State state = State.IDLE;
    private float stateTime = 0f;

    private float x, y;
    private boolean facingRight = false;

    private final Rectangle bounds = new Rectangle();

    // SFX continuo gruñidos
    private long gruntsLoopId = -1;

    public Mummy(AudioManager audio, Texture idle, Texture walk, Texture hurt, Texture dead, float startX, float groundY) {
        this.audio = audio;

        this.idleTex = idle;
        this.walkSheet = walk;
        this.hurtSheet = hurt;
        this.deadTex = dead;

        this.x = startX;
        this.y = groundY + FOLLOW_Y_OFFSET;

        this.idleFrame = new TextureRegion(idleTex, 0, 0, FRAME_W, FRAME_H);
        this.deadFrame = new TextureRegion(deadTex, 0, 0, FRAME_W, FRAME_H);

        this.walkAnim = buildAnimLoop(walkSheet, WALK_FRAME);
        this.hurtAnim = buildAnimNormal(hurtSheet, HURT_FRAME);

        updateBounds();
    }

    public void startFight() {
        if (state == State.IDLE) {
            state = State.WALK;
            stateTime = 0f;
        }
    }

    public void update(float delta, float aylaX, float groundY) {

        if (state == State.DEAD) {
            stopGrunts();
            return;
        }

        this.y = groundY + FOLLOW_Y_OFFSET;
        stateTime += delta;

        shootTimer -= delta;
        if (shootTimer < 0f) shootTimer = 0f;

        if (state == State.DYING_HURT) {
            stopGrunts();
            if (hurtAnim.isAnimationFinished(stateTime)) {
                enterDead();
            }
            return;
        }

        float dx = aylaX - x;
        facingRight = dx > 0;
        float dist = Math.abs(dx);

        if (dist <= STOP_DISTANCE) {
            // cerca -> IDLE
            if (state != State.IDLE) {
                state = State.IDLE;
                stateTime = 0f;
            }
            stopGrunts();
            updateBounds();
            return;
        }

        // lejos -> WALK
        if (state != State.WALK) {
            state = State.WALK;
            stateTime = 0f;
        }

        startGrunts();

        x += Math.signum(dx) * SPEED * delta;
        updateBounds();
    }

    private void startGrunts() {
        if (gruntsLoopId == -1) {
            gruntsLoopId = audio.loopSfx(Assets.SFX_MUMMY_GRUNTS, 0.35f);
        }
    }

    private void stopGrunts() {
        if (gruntsLoopId != -1) {
            audio.stopLoop(Assets.SFX_MUMMY_GRUNTS, gruntsLoopId);
            gruntsLoopId = -1;
        }
    }

    public boolean canShoot(float delta) {
        if (state == State.DEAD || state == State.DYING_HURT) return false;

        if (state != State.IDLE) return false;
        if (shootTimer > 0f) return false;

        shootTimer = SHOOT_COOLDOWN;

        audio.playSfx(Assets.SFX_MUMMY_SHOTS);

        return true;
    }

    public void draw(SpriteBatch batch) {

        TextureRegion frame;

        switch (state) {
            case IDLE: frame = idleFrame; break;
            case WALK: frame = walkAnim.getKeyFrame(stateTime, true); break;
            case DYING_HURT: frame = hurtAnim.getKeyFrame(stateTime, false); break;
            case DEAD:
            default: frame = deadFrame; break;
        }

        float drawY = y + DRAW_Y_OFFSET;

        if (facingRight)
            batch.draw(frame, x, drawY, width, height);
        else
            batch.draw(frame, x + width, drawY, -width, height);
    }

    public void hitByAylaBullet() {
        if (state == State.DEAD || state == State.DYING_HURT) return;

        hp--;
        if (hp <= 0) {
            state = State.DYING_HURT;
            stateTime = 0f;
            bounds.set(0, 0, 0, 0);
            stopGrunts();
        }
    }

    private void enterDead() {
        state = State.DEAD;
        stateTime = 0f;
        bounds.set(0, 0, 0, 0);

        stopGrunts();
        audio.playSfx(Assets.SFX_MUMMY_DEAD);
    }

    public void applyWorldScroll(float dx) {
        if (state == State.DEAD) {
            x += dx;
        }
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public boolean isFacingRight() { return facingRight; }

    public Rectangle getBounds() { return bounds; }
    public boolean isDead() { return state == State.DEAD; }
    public int getHp() { return hp; }

    private Animation<TextureRegion> buildAnimLoop(Texture sheet, float frameDuration) {
        TextureRegion[] frames = splitAllFrames(sheet);
        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(Animation.PlayMode.LOOP);
        return anim;
    }

    private Animation<TextureRegion> buildAnimNormal(Texture sheet, float frameDuration) {
        TextureRegion[] frames = splitAllFrames(sheet);
        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(Animation.PlayMode.NORMAL);
        return anim;
    }

    private TextureRegion[] splitAllFrames(Texture sheet) {
        int w = sheet.getWidth();
        int h = sheet.getHeight();

        if (w % FRAME_W != 0 || h % FRAME_H != 0) {
            return new TextureRegion[]{
                new TextureRegion(sheet, 0, 0, Math.min(FRAME_W, w), Math.min(FRAME_H, h))
            };
        }

        TextureRegion[][] grid = TextureRegion.split(sheet, FRAME_W, FRAME_H);
        int rows = grid.length;
        int cols = grid[0].length;

        TextureRegion[] out = new TextureRegion[rows * cols];
        int k = 0;
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                out[k++] = grid[r][c];

        return out;
    }

    private void updateBounds() {
        if (state == State.DYING_HURT || state == State.DEAD) {
            bounds.set(0, 0, 0, 0);
            return;
        }

        float hitX = x + HIT_PAD_L;
        float hitY = y + HIT_PAD_B;
        float hitW = width - (HIT_PAD_L + HIT_PAD_R);
        float hitH = height - (HIT_PAD_B + HIT_PAD_T);

        if (hitW < 10f) hitW = 10f;
        if (hitH < 10f) hitH = 10f;

        bounds.set(hitX, hitY, hitW, hitH);
    }
}
