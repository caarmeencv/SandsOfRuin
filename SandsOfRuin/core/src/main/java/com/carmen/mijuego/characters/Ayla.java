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

    // -----------------------
    // DOBLE SALTO (robusto)
    // -----------------------
    private static final int MAX_JUMPS = 2;
    private int jumpsLeft = MAX_JUMPS;

    // Segundo salto un pelín más suave (opcional)
    private static final float DOUBLE_JUMP_MUL = 0.90f;

    // Para detectar "just pressed" aunque jump llegue como estado (mantener pulsado)
    private boolean jumpWasDown = false;

    // Offset visual (solo dibujo)
    private static final float FOOT_OFFSET = 14f;

    // HITBOX AJUSTADA
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

    // DISPARO
    private final Texture bulletTex;
    private final Array<Bullet> bullets = new Array<>();

    private static final float BULLET_SPEED = 1200f;
    private static final float SHOOT_COOLDOWN = 0.20f; // 5 disparos/seg
    private float shootTimer = 0f;

    private static final int MAX_BULLETS_ON_SCREEN = 30;

    // Tamaño bala (ajustable)
    private static final float BULLET_W = 36f;
    private static final float BULLET_H = 18f;

    private static final float BULLET_OFFSET_X = 190f;
    private static final float BULLET_OFFSET_Y = 190f;

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

        // -----------------------
        // DOBLE SALTO con "just pressed" interno
        // -----------------------
        boolean jumpJustPressed = jump && !jumpWasDown;
        jumpWasDown = jump;

        if (jumpJustPressed && jumpsLeft > 0) {
            boolean isDoubleJump = !onGround;

            velY = JUMP * (isDoubleJump ? DOUBLE_JUMP_MUL : 1f);

            onGround = false;
            jumpsLeft--;
        }

        // física
        velY += GRAVITY * delta;
        y += velY * delta;

        // suelo
        if (y <= groundY) {
            y = groundY;
            velY = 0;
            onGround = true;
            jumpsLeft = MAX_JUMPS; // recarga
        }

        if (moving && onGround) stateTime += delta;
        else stateTime = 0f;

        // disparo
        shootTimer -= delta;
        if (shoot) tryShoot();

        // actualizar balas + borrar si salen de pantalla según cámara
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

    public Array<Bullet> getBullets() {
        return bullets;
    }
}
