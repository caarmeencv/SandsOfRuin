package com.carmen.mijuego.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.audio.AudioManager;
import com.carmen.mijuego.projectiles.Bullet;

public class Ayla {

    private final AudioManager audio;

    private float x, y, velY;
    private boolean facingRight = true;
    private boolean onGround = false;

    private static final float GRAVITY = -1800f;
    private static final float JUMP = 800f;

    private static final int MAX_JUMPS = 2;
    private int jumpsLeft = MAX_JUMPS;

    private static final float DOUBLE_JUMP_MUL = 0.90f;

    private boolean jumpWasDown = false;

    private static final float FOOT_OFFSET = 14f;

    // HITBOX
    private static final float HIT_PAD_L = 75f;
    private static final float HIT_PAD_R = 75f;
    private static final float HIT_PAD_BOTTOM = 22f;
    private static final float HIT_PAD_TOP = 20f;

    private final Texture idleTex;
    private final Texture jumpTex;

    private final Animation<TextureRegion> runAnim;
    private float stateTime;

    private final float scale = 0.60f;
    private final float width, height;

    private static final int FRAME_W = 336;
    private static final int FRAME_H = 411;

    private final Rectangle bounds = new Rectangle();

    private final Texture bulletTex;
    private final Texture specialBulletTex;

    private final Array<Bullet> bullets = new Array<>();

    private static final float BULLET_SPEED = 1200f;

    private static final int MAX_BULLETS_ON_SCREEN = 30;

    private static final float BULLET_W = 36f;
    private static final float BULLET_H = 18f;

    private static final float BULLET_OFFSET_X = 190f;
    private static final float BULLET_OFFSET_Y = 190f;

    private static final int NORMAL_MAX_SHOTS = 2;
    private static final float NORMAL_COOLDOWN_TIME = 3f;

    private int normalShots = 0;
    private boolean normalOnCooldown = false;
    private float normalCooldownTimer = 0f;

    private boolean shootWasDown = false;

    private static final float SPECIAL_COOLDOWN_TIME = 15f;
    private float specialTimer = 0f;

    private boolean specialWasDown = false;

    private float cursedTimer = 0f;
    private static final float CURSE_DEFAULT_TIME = 4.0f;

    private int maxLives = 5;
    private int lives = 5;

    private float invulnTimer = 0f;
    private static final float INVULN_TIME = 3.0f;

    private float blinkTimer = 0f;

    private long runLoopId = -1;

    public Ayla(AudioManager audio,
                Texture runSheet,
                Texture idle,
                Texture jump,
                Texture bulletTex,
                Texture specialBulletTex,
                float startX,
                float startY) {

        this.audio = audio;

        this.idleTex = idle;
        this.jumpTex = jump;
        this.bulletTex = bulletTex;
        this.specialBulletTex = specialBulletTex;

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

        updateBounds();
    }

    public void update(float delta,
                       boolean left,
                       boolean right,
                       boolean jump,
                       boolean shoot,
                       boolean grenade,
                       float groundY,
                       float camLeft,
                       float camRight) {
        update(delta, left, right, jump, shoot, grenade, groundY, camLeft, camRight, false);
    }

    public void update(float delta,
                       boolean left,
                       boolean right,
                       boolean jump,
                       boolean shoot,
                       boolean grenade,
                       float groundY,
                       float camLeft,
                       float camRight,
                       boolean forceRun) {

        boolean movingByInput = (left ^ right);
        boolean moving = movingByInput || forceRun;

        if (right) facingRight = true;
        if (left)  facingRight = false;

        if (forceRun && !movingByInput) facingRight = true;

        if (invulnTimer > 0f) invulnTimer -= delta;
        if (invulnTimer < 0f) invulnTimer = 0f;

        if (blinkTimer > 0f) blinkTimer -= delta;
        if (blinkTimer < 0f) blinkTimer = 0f;

        boolean jumpJustPressed = jump && !jumpWasDown;
        jumpWasDown = jump;

        if (jumpJustPressed && jumpsLeft > 0) {
            boolean isDoubleJump = !onGround;
            velY = JUMP * (isDoubleJump ? DOUBLE_JUMP_MUL : 1f);

            if (audio != null) audio.playSfx(Assets.SFX_AYLA_JUMP);

            onGround = false;
            jumpsLeft--;
        }

        velY += GRAVITY * delta;
        y += velY * delta;

        if (y <= groundY) {
            y = groundY;
            velY = 0;
            onGround = true;
            jumpsLeft = MAX_JUMPS;
        }

        if (moving && onGround) stateTime += delta;
        else stateTime = 0f;

        handleRunSfx(moving, onGround);

        if (cursedTimer > 0f) {
            cursedTimer -= delta;
            if (cursedTimer < 0f) cursedTimer = 0f;
        }

        if (normalOnCooldown) {
            normalCooldownTimer += delta;
            if (normalCooldownTimer >= NORMAL_COOLDOWN_TIME) {
                normalOnCooldown = false;
                normalCooldownTimer = 0f;
                normalShots = 0;
            }
        }

        if (specialTimer > 0f) {
            specialTimer -= delta;
            if (specialTimer < 0f) specialTimer = 0f;
        }

        boolean shootJustPressed = shoot && !shootWasDown;
        shootWasDown = shoot;

        boolean specialJustPressed = grenade && !specialWasDown;
        specialWasDown = grenade;

        if (shootJustPressed) tryNormalShoot();
        if (specialJustPressed) trySpecialShoot();

        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(delta);

            if (b.getX() < camLeft - 300f || b.getX() > camRight + 300f) {
                b.kill();
            }
            if (!b.isAlive()) bullets.removeIndex(i);
        }

        updateBounds();
    }

    private void handleRunSfx(boolean moving, boolean onGroundNow) {

        // Si está muerta -> fuera
        if (isDead()) {
            stopRunLoop();
            return;
        }

        boolean shouldRun = moving && onGroundNow;


        if (shouldRun) {
            if (runLoopId == -1) {
                runLoopId = audio.loopSfx(Assets.SFX_CHARACTER_RUN, 0.35f);
            }
        } else {
            stopRunLoop();
        }
    }

    private void stopRunLoop() {
        if (runLoopId != -1) {
            if (audio != null) audio.stopLoop(Assets.SFX_CHARACTER_RUN, runLoopId);
            runLoopId = -1;
        }
    }

    private void tryNormalShoot() {
        if (normalOnCooldown) return;

        if (normalShots >= NORMAL_MAX_SHOTS) {
            normalOnCooldown = true;
            normalCooldownTimer = 0f;

            if (audio != null) audio.playSfx(Assets.SFX_GUN_RELOAD);
            return;
        }

        if (bullets.size >= MAX_BULLETS_ON_SCREEN) return;

        spawnBullet(bulletTex, 1);
        normalShots++;

        if (audio != null) audio.playSfx(Assets.SFX_SHOT_GUN_2);
    }

    private void trySpecialShoot() {
        if (specialTimer > 0f) return;
        if (bullets.size >= MAX_BULLETS_ON_SCREEN) return;

        spawnBullet(specialBulletTex, 2);
        specialTimer = SPECIAL_COOLDOWN_TIME;

        if (audio != null) audio.playSfx(Assets.SFX_SHOT_GUN_1);
    }

    private void spawnBullet(Texture tex, int damage) {
        float dir = facingRight ? 1f : -1f;

        float spawnX = facingRight
            ? (x + BULLET_OFFSET_X)
            : (x + (width - BULLET_OFFSET_X) - BULLET_W);

        float spawnY = (y - FOOT_OFFSET) + BULLET_OFFSET_Y;

        bullets.add(new Bullet(
            tex,
            spawnX,
            spawnY,
            BULLET_SPEED * dir,
            BULLET_W,
            BULLET_H,
            damage
        ));
    }

    public void draw(SpriteBatch batch, boolean moving) {

        float drawY = y - FOOT_OFFSET;

        float pr = batch.getColor().r;
        float pg = batch.getColor().g;
        float pb = batch.getColor().b;
        float pa = batch.getColor().a;

        boolean visible = true;

        if (invulnTimer > 0f) {
            float blinkSpeed = 15f;
            visible = ((int)(invulnTimer * blinkSpeed)) % 2 == 0;
            batch.setColor(1f, 1f, 1f, 0.45f);
        }

        if (visible) {

            if (!onGround) {
                drawTexture(batch, jumpTex, drawY);

            } else if (!moving) {
                drawTexture(batch, idleTex, drawY);

            } else {
                TextureRegion frame = runAnim.getKeyFrame(stateTime, true);
                if (facingRight)
                    batch.draw(frame, x, drawY, width, height);
                else
                    batch.draw(frame, x + width, drawY, -width, height);
            }

            for (Bullet b : bullets) b.draw(batch);
        }

        batch.setColor(pr, pg, pb, pa);
    }

    private void drawTexture(SpriteBatch batch, Texture tex, float drawY) {
        if (facingRight) batch.draw(tex, x, drawY, width, height);
        else batch.draw(tex, x + width, drawY, -width, height);
    }

    private void updateBounds() {
        float hitX = x + HIT_PAD_L;
        float hitY = y + HIT_PAD_BOTTOM;
        float hitW = width - (HIT_PAD_L + HIT_PAD_R);
        float hitH = height - (HIT_PAD_BOTTOM + HIT_PAD_TOP);

        if (hitW < 10f) hitW = 10f;
        if (hitH < 10f) hitH = 10f;

        bounds.set(hitX, hitY, hitW, hitH);
    }

    public Rectangle getBounds() { return bounds; }

    public void setX(float x) {
        this.x = x;
        updateBounds();
    }

    public int getLives() { return lives; }
    public void setLives(int lives) {
        this.lives = lives;
        if (this.lives < 0) this.lives = 0;
    }
    public int getMaxLives() { return maxLives; }
    public boolean isDead() { return lives <= 0; }

    public boolean canTakeDamage() {
        return invulnTimer <= 0f && !isDead();
    }

    public boolean takeDamage() {
        if (!canTakeDamage()) return false;

        lives--;
        if (lives < 0) lives = 0;

        invulnTimer = INVULN_TIME;

        if (audio != null) {
            audio.playSfx(Assets.SFX_AYLA_DAMAGE);
        }

        if (Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Android) {
            try {
                if (Gdx.input.isPeripheralAvailable(Input.Peripheral.Vibrator)) {
                    audio.vibrate(150);
                }
            } catch (Exception ignored) {}
        }

        if (isDead()) stopRunLoop();

        return true;
    }

    public void resetLives() {
        lives = maxLives;
        invulnTimer = 0f;
        blinkTimer = 0f;
        stopRunLoop();
    }

    public void stopAllLoops() {
        stopRunLoop();
    }

    public void applyCurse() {
        applyCurse(CURSE_DEFAULT_TIME);
    }

    public void applyCurse(float seconds) {
        cursedTimer = Math.max(cursedTimer, seconds);
    }

    public void clearCurse() {
        cursedTimer = 0f;
    }

    public boolean isCursed() {
        return cursedTimer > 0f;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }

    public Array<Bullet> getBullets() { return bullets; }

    public float getNormalCooldownPercent() {
        if (!normalOnCooldown) return 0f;
        float p = normalCooldownTimer / NORMAL_COOLDOWN_TIME;
        if (p < 0f) p = 0f;
        if (p > 1f) p = 1f;
        return p;
    }

    public float getSpecialCooldownPercent() {
        if (specialTimer <= 0f) return 0f;
        float p = specialTimer / SPECIAL_COOLDOWN_TIME;
        if (p < 0f) p = 0f;
        if (p > 1f) p = 1f;
        return p;
    }
}
