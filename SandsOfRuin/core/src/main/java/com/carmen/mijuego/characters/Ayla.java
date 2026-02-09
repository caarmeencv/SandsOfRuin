package com.carmen.mijuego.characters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.carmen.mijuego.projectiles.Bullet;

public class Ayla {

    private float x, y, velY;
    private boolean facingRight = true;
    private boolean onGround = false;

    private static final float GRAVITY = -1800f;
    private static final float JUMP = 800f;

    // 🔧 Offset visual (solo dibujo)
    private static final float FOOT_OFFSET = 14f;

    // ✅ HITBOX AJUSTADA
    private static final float HIT_PAD_L = 75f;
    private static final float HIT_PAD_R = 75f;
    private static final float HIT_PAD_BOTTOM = 22f;
    private static final float HIT_PAD_TOP = 55f;

    private final Texture idleTex;
    private final Texture jumpTex;

    private final Animation<TextureRegion> runAnim;
    private float stateTime;

    private final float scale = 0.60f;
    private final float width, height;

    private static final int FRAME_W = 336;
    private static final int FRAME_H = 411;

    private final Rectangle bounds = new Rectangle();

    // =========================
    // ✅ DISPARO
    // =========================
    private final Texture bulletTex;
    private final Array<Bullet> bullets = new Array<>();

    private static final float BULLET_SPEED = 1200f;
    private static final float SHOOT_COOLDOWN = 0.20f; // 5 disparos/seg
    private float shootTimer = 0f;

    private static final int MAX_BULLETS_ON_SCREEN = 30;

    // Tamaño bala (ajustable)
    private static final float BULLET_W = 36f;
    private static final float BULLET_H = 18f;

    private static final float BULLET_OFFSET_X = 190f; // más cerca -> baja más (ej: 175f)
    private static final float BULLET_OFFSET_Y = 190f; // más arriba -> sube más (ej: 220f)


    public Ayla(Texture runSheet, Texture idle, Texture jump, Texture bulletTex, float startX, float startY) {
        this.idleTex = idle;
        this.jumpTex = jump;
        this.bulletTex = bulletTex;

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

    /**
     * Update de Ayla con disparo.
     * IMPORTANTE: usamos camLeft/camRight para borrar balas en scroll infinito.
     */
    public void update(float delta,
                       boolean left,
                       boolean right,
                       boolean jump,
                       boolean shoot,
                       float groundY,
                       float camLeft,
                       float camRight) {

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
        else stateTime = 0f;

        // ✅ disparo
        shootTimer -= delta;
        if (shoot) tryShoot();

        // ✅ actualizar balas + borrar si salen de la pantalla REAL (según cámara)
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

    private void tryShoot() {
        if (shootTimer > 0f) return;
        shootTimer = SHOOT_COOLDOWN;

        // ✅ límite para evitar acumulación infinita
        if (bullets.size >= MAX_BULLETS_ON_SCREEN) return;

        float dir = facingRight ? 1f : -1f;

        float spawnX = facingRight
            ? (x + BULLET_OFFSET_X)
            : (x + (width - BULLET_OFFSET_X) - BULLET_W);

        float spawnY = (y - FOOT_OFFSET) + BULLET_OFFSET_Y;

        bullets.add(new Bullet(
            bulletTex,
            spawnX,
            spawnY,
            BULLET_SPEED * dir,
            BULLET_W,
            BULLET_H
        ));
    }

    public void draw(SpriteBatch batch, boolean moving) {
        float drawY = y - FOOT_OFFSET;

        if (!onGround) {
            drawTexture(batch, jumpTex, drawY);
        } else if (!moving) {
            drawTexture(batch, idleTex, drawY);
        } else {
            TextureRegion frame = runAnim.getKeyFrame(stateTime, true);
            if (facingRight) batch.draw(frame, x, drawY, width, height);
            else batch.draw(frame, x + width, drawY, -width, height);
        }

        // ✅ balas por encima del fondo
        for (Bullet b : bullets) b.draw(batch);
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

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }

    public com.badlogic.gdx.utils.Array<com.carmen.mijuego.projectiles.Bullet> getBullets() {
        return bullets;
    }

}
