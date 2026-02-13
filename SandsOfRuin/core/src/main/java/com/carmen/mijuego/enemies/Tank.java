package com.carmen.mijuego.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * Tank:
 * - tank_move: spritesheet horizontal 4 frames (loop)
 * - tank_idle: sprite único
 * - 3 disparos -> tank_destroy: spritesheet 15 frames (play once) -> tank_dead (sprite único)
 * - Hitbox: 3/4 ancho y ~40% alto
 */
public class Tank {

    private enum State { MOVE, IDLE, DESTROY, DEAD }

    private static final int MOVE_FRAMES = 4;
    private static final int DESTROY_FRAMES = 15;

    // ✅ MÁS GRANDE
    private static final float SCALE = 1.60f;

    // IA
    private static final float MOVE_SPEED = 230f;
    private static final float STOP_DISTANCE = 520f; // se para cerca de Ayla

    // Vida
    private static final int HP = 3;
    private int hitsTaken = 0;

    // Dibujo
    private static final float FOOT_OFFSET = 10f;

    // Animación
    private static final float MOVE_FRAME_TIME = 0.10f;
    private static final float DESTROY_FRAME_TIME = 0.06f; // 15 frames -> ~0.9s

    // --- DISPARO ---
    private static final float SHOOT_COOLDOWN = 2.2f; // cada 2.2s
    private float shootTimer = 0f;

    private float x, y;
    private boolean facingRight = false;

    private State state = State.MOVE;
    private float stateTime = 0f;

    // Texturas
    private final Texture idleTex;
    private final Texture deadTex;

    // Animaciones
    private final Animation<TextureRegion> moveAnim;
    private final Animation<TextureRegion> destroyAnim;

    // Tamaño final en mundo (según frame del move sheet)
    private final float width;
    private final float height;

    // Hitbox
    private final Rectangle bounds = new Rectangle();

    public Tank(Texture tankIdle,
                Texture tankMoveSheet,
                Texture tankDestroySheet,
                Texture tankDead,
                float startX,
                float startY) {

        this.idleTex = tankIdle;
        this.deadTex = tankDead;

        this.x = startX;
        this.y = startY;

        // ✅ frame size calculado
        int moveFrameW = tankMoveSheet.getWidth() / MOVE_FRAMES;
        int moveFrameH = tankMoveSheet.getHeight();

        this.width = moveFrameW * SCALE;
        this.height = moveFrameH * SCALE;

        this.moveAnim = buildAnimHorizontal(tankMoveSheet, MOVE_FRAMES, MOVE_FRAME_TIME, true);
        this.destroyAnim = buildAnimHorizontal(tankDestroySheet, DESTROY_FRAMES, DESTROY_FRAME_TIME, false);

        updateBounds();
    }

    /** Construye animación desde spritesheet horizontal (1 fila). */
    private Animation<TextureRegion> buildAnimHorizontal(Texture sheet, int frames, float frameTime, boolean loop) {
        int frameW = sheet.getWidth() / frames;
        int frameH = sheet.getHeight();

        TextureRegion[][] split = TextureRegion.split(sheet, frameW, frameH);

        Array<TextureRegion> regions = new Array<>(frames);
        for (int i = 0; i < frames; i++) {
            regions.add(split[0][i]);
        }

        Animation<TextureRegion> anim = new Animation<>(frameTime, regions);
        anim.setPlayMode(loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
        return anim;
    }

    public void update(float delta, float aylaX) {
        stateTime += delta;

        // si está muerto, no hace nada
        if (state == State.DEAD) {
            updateBounds();
            return;
        }

        // si está destruyéndose: cuando acaba la animación -> DEAD
        if (state == State.DESTROY) {
            if (destroyAnim.isAnimationFinished(stateTime)) {
                state = State.DEAD;
                stateTime = 0f;
            }
            updateBounds();
            return;
        }

        // mirar hacia Ayla
        facingRight = aylaX > x;

        float dx = aylaX - x;
        float absDx = Math.abs(dx);

        if (absDx <= STOP_DISTANCE) {
            state = State.IDLE;
        } else {
            state = State.MOVE;
            float dir = dx > 0 ? 1f : -1f;
            x += dir * MOVE_SPEED * delta;
        }

        updateBounds();
    }

    /** Cooldown de disparo. Llama a esto UNA vez por frame. */
    public boolean canShoot(float delta) {
        if (state == State.DEAD || state == State.DESTROY) return false;

        shootTimer += delta;
        if (shootTimer >= SHOOT_COOLDOWN) {
            shootTimer = 0f;
            return true;
        }
        return false;
    }

    /** Llamar cuando una bala de Ayla golpea al tanque. */
    public void hitByAylaBullet() {
        if (state == State.DEAD || state == State.DESTROY) return;

        hitsTaken++;
        if (hitsTaken >= HP) {
            state = State.DESTROY;
            stateTime = 0f; // reiniciar para reproducir destroy desde frame 0
        }
    }

    public void draw(SpriteBatch batch) {
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

        // MOVE
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
        // Hitbox: 3/4 ancho y ~40% alto
        float hitW = width * 0.75f;
        float hitH = height * 0.40f;

        float hitX = x + (width - hitW) / 2f;
        float hitY = y; // desde el suelo

        if (hitW < 10f) hitW = 10f;
        if (hitH < 10f) hitH = 10f;

        bounds.set(hitX, hitY, hitW, hitH);
    }

    public Rectangle getBounds() { return bounds; }

    public boolean isDead() { return state == State.DEAD; }
    public boolean isDestroying() { return state == State.DESTROY; }

    public boolean isOffScreenLeft(float camLeft) {
        return x + width < camLeft - 700f;
    }

    // --- GETTERS necesarios para disparar desde DesertScreen ---
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public boolean isFacingRight() { return facingRight; }
}
