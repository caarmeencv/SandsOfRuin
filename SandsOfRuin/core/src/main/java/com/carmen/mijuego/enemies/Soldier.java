package com.carmen.mijuego.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.audio.AudioManager;
import com.carmen.mijuego.projectiles.Bullet;

public class Soldier {

    private final AudioManager audio;

    private enum State { IDLE, RUN, HURT, DEAD, GONE }

    private static final int FRAME_W = 403;
    private static final int FRAME_H = 457;

    private static final float SCALE = 0.55f;
    private final float width  = FRAME_W * SCALE;
    private final float height = FRAME_H * SCALE;

    private static final float FOOT_OFFSET = 14f;

    private static final float RUN_SPEED = 260f;
    private static final float STOP_DISTANCE = 800f;

    private static final float SHOOT_COOLDOWN = 3f;
    private static final float BULLET_SPEED = 900f;

    private static final float BULLET_W = 34f;
    private static final float BULLET_H = 18f;

    private static final float BULLET_OFFSET_X = 55f;
    private static final float BULLET_OFFSET_Y = 145f;

    private static final float HURT_DURATION = 0.35f;

    private static final int BLINK_TIMES = 3;
    private static final float BLINK_INTERVAL = 0.10f;

    private static final float HIT_PAD_L = 120f * SCALE;
    private static final float HIT_PAD_R = 120f * SCALE;
    private static final float HIT_PAD_BOTTOM = 50f * SCALE;
    private static final float HIT_PAD_TOP = 70f * SCALE;

    private float x, y;
    private boolean facingRight = false;
    private State state = State.IDLE;

    private final Texture idleTex;
    private final Texture deadTex;

    private final Animation<TextureRegion> runAnim;
    private final Animation<TextureRegion> hurtAnim;

    private float stateTime = 0f;
    private float shootTimer = 0f;
    private float hurtTimer = 0f;

    private boolean moving = false;

    // blink
    private float blinkTimer = 0f;
    private int blinkToggles = 0;
    private boolean visible = true;

    private int hitsTaken = 0;

    private final Texture bulletTex;
    private final Array<Bullet> bullets = new Array<>();

    private final Rectangle bounds = new Rectangle();

    private long runLoopId = -1;

    public Soldier(AudioManager audio,
                   Texture idleTex,
                   Texture runSheet,
                   Texture hurtSheet,
                   Texture deadTex,
                   Texture bulletTex,
                   float startX,
                   float startY) {

        this.audio = audio;

        this.idleTex = idleTex;
        this.deadTex = deadTex;
        this.bulletTex = bulletTex;

        this.x = startX;
        this.y = startY;

        runAnim  = buildAnim(runSheet, 0.10f);
        hurtAnim = buildAnim(hurtSheet, 0.08f);

        updateBounds();
    }

    private Animation<TextureRegion> buildAnim(Texture sheet, float frameDuration) {
        int cols = sheet.getWidth() / FRAME_W;
        if (cols <= 0) throw new IllegalArgumentException("Spritesheet soldier inválido");

        TextureRegion[][] split = TextureRegion.split(sheet, FRAME_W, FRAME_H);
        TextureRegion[] frames = new TextureRegion[cols];
        for (int i = 0; i < cols; i++) frames[i] = split[0][i];

        return new Animation<>(frameDuration, frames);
    }

    public void update(float delta, float aylaX, float camLeft, float camRight) {
        stateTime += delta;

        if (state == State.GONE) return;

        if (state == State.IDLE) {
            if (x < camRight + 10f) {
                state = State.RUN;
                stateTime = 0f;
                moving = false;
            }
        }

        if (state == State.RUN) {
            facingRight = aylaX > x;

            float dx = aylaX - x;
            float absDx = Math.abs(dx);
            boolean stopped = absDx <= STOP_DISTANCE;

            if (!stopped) {
                float dir = dx > 0 ? 1f : -1f;
                x += dir * RUN_SPEED * delta;
                moving = true;
            } else {
                moving = false;
            }

            handleRunSfx();

            if (stopped) {
                shootTimer -= delta;
                if (shootTimer <= 0f) {
                    shootTimer = SHOOT_COOLDOWN;
                    shoot();
                }
            }
        }

        // HURT -> DEAD
        if (state == State.HURT) {
            hurtTimer -= delta;
            if (hurtTimer <= 0f) {
                enterDeadBlink();
            }
        }

        if (state == State.DEAD) {
            stopRunLoop();

            blinkTimer += delta;
            if (blinkTimer >= BLINK_INTERVAL) {
                blinkTimer = 0f;
                visible = !visible;
                blinkToggles++;

                if (blinkToggles >= BLINK_TIMES * 2) {
                    state = State.GONE;
                }
            }
        }

        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(delta);

            if (b.getX() < camLeft - 400f || b.getX() > camRight + 400f) {
                b.kill();
            }
            if (!b.isAlive()) bullets.removeIndex(i);
        }

        updateBounds();
    }

    private void handleRunSfx() {
        boolean shouldRun = (state == State.RUN) && moving;
        if (shouldRun) {
            if (runLoopId == -1) {
                runLoopId = audio.loopSfx(Assets.SFX_CHARACTER_RUN, 0.30f);
            }
        } else {
            stopRunLoop();
        }
    }

    private void stopRunLoop() {
        if (runLoopId != -1) {
            audio.stopLoop(Assets.SFX_CHARACTER_RUN, runLoopId);
            runLoopId = -1;
        }
    }

    private void shoot() {
        if (state != State.RUN) return;

        audio.playSfx(Assets.SFX_SHOT_GUN_2);

        float dir = facingRight ? 1f : -1f;

        float spawnX = facingRight
            ? (x + width - BULLET_OFFSET_X)
            : (x + BULLET_OFFSET_X - BULLET_W);

        float drawY = y - FOOT_OFFSET;
        float spawnY = drawY + BULLET_OFFSET_Y;

        bullets.add(new Bullet(
            bulletTex,
            spawnX,
            spawnY,
            BULLET_SPEED * dir,
            BULLET_W,
            BULLET_H
        ));
    }

    public void hitByAylaBullet() {
        if (state == State.HURT || state == State.DEAD || state == State.GONE) return;

        hitsTaken++;

        if (hitsTaken == 1) {
            audio.playSfx(Assets.SFX_SOLDIER_DAMAGE);
        }

        if (hitsTaken >= 2) {
            state = State.HURT;
            stateTime = 0f;
            hurtTimer = HURT_DURATION;

            bounds.set(0, 0, 0, 0);
        }
    }

    private void enterDeadBlink() {
        state = State.DEAD;
        stateTime = 0f;

        audio.playSfx(Assets.SFX_SOLDIER_DEAD);

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

        } else if (state == State.HURT) {
            TextureRegion f = hurtAnim.getKeyFrame(stateTime, false);
            drawRegion(batch, f, drawY);

        } else if (state == State.RUN) {
            if (moving) {
                TextureRegion f = runAnim.getKeyFrame(stateTime, true);
                drawRegion(batch, f, drawY);
            } else {
                drawTexture(batch, idleTex, drawY);
            }

        } else {
            drawTexture(batch, idleTex, drawY);
        }

        for (Bullet b : bullets) b.draw(batch);
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
        if (state == State.HURT || state == State.DEAD || state == State.GONE) {
            bounds.set(0, 0, 0, 0);
            return;
        }

        float hitX = x + HIT_PAD_L;
        float hitY = y + HIT_PAD_BOTTOM;
        float hitW = width - (HIT_PAD_L + HIT_PAD_R);
        float hitH = height - (HIT_PAD_BOTTOM + HIT_PAD_TOP);

        if (hitW < 10f) hitW = 10f;
        if (hitH < 10f) hitH = 10f;

        bounds.set(hitX, hitY, hitW, hitH);
    }

    public Rectangle getBounds() { return bounds; }
    public Array<Bullet> getBullets() { return bullets; }

    public boolean isDead() { return state == State.DEAD || state == State.GONE; }
    public boolean isGone() { return state == State.GONE; }

    public boolean isOffScreenLeft(float camLeft) {
        return x + width < camLeft - 500f;
    }
}
