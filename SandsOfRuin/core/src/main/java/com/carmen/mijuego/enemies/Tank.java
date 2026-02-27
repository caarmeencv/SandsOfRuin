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

    // Esto sirve para reproducir sonidos del tanque como moverse y explotar
    private final AudioManager audio;

    // Estados del tanque
    // MOVE significa que se está moviendo
    // IDLE significa que se para cerca de Ayla
    // DESTROY significa que está haciendo la animación de destruirse
    // DEAD significa que ya está destruido y parpadea
    // GONE significa que ya desapareció del juego y no se dibuja ni colisiona
    private enum State { MOVE, IDLE, DESTROY, DEAD, GONE }

    // Número de frames del spritesheet de movimiento y de destrucción
    private static final int MOVE_FRAMES = 4, DESTROY_FRAMES = 15;

    // Escala visual del tanque
    private static final float SCALE = 1.35f;

    // Velocidad del tanque y distancia donde deja de avanzar para quedarse a rango
    private static final float MOVE_SPEED = 230f, STOP_DISTANCE = 520f;

    // Vida del tanque en impactos
    // Con 3 impactos entra en destrucción
    private static final int HP = 3;
    private int hitsTaken = 0;

    // Ajuste visual para dibujar el tanque un poco más abajo o mejor alineado con el suelo
    private static final float FOOT_OFFSET = 10f;

    // Tiempo entre disparos del tanque
    private static final float SHOOT_COOLDOWN = 2.2f;

    // Temporizador que acumula tiempo y cuando llega al cooldown permite disparar
    private float shootTimer = 0f;

    // Esto sirve para saber desde qué punto exacto sale el disparo del tanque
    // Se usa como porcentaje del tamaño del tanque
    private static final float MUZZLE_Y_RATIO = 0.30f;
    private static final float MUZZLE_X_RATIO = 0.88f;

    // Parpadeo cuando muere
    private static final int BLINK_TIMES = 3;
    private static final float BLINK_INTERVAL = 0.10f;

    private float blinkTimer = 0f;
    private int blinkToggles = 0;
    private boolean visible = true;

    // Posición del tanque
    private float x, y;

    // Dirección a la que mira para dibujar girado y para calcular el cañón
    private boolean facingRight = false;

    // Estado actual y tiempo de animación
    private State state = State.MOVE;
    private float stateTime = 0f;

    // Texturas para idle y muerto
    // Movimiento y destrucción son animaciones
    private final Texture idleTex, deadTex;
    private final Animation<TextureRegion> moveAnim, destroyAnim;

    // Tamaño real del tanque ya escalado
    private final float width, height;

    // Hitbox del tanque
    private final Rectangle bounds = new Rectangle();

    // Id del sonido en bucle mientras se mueve
    private long moveLoopId = -1;

    public Tank(AudioManager audio,
                Texture tankIdle,
                Texture tankMoveSheet,
                Texture tankDestroySheet,
                Texture tankDead,
                float startX,
                float startY) {

        // Guardo el audio
        this.audio = audio;

        // Guardo las texturas
        idleTex = tankIdle;
        deadTex = tankDead;

        // Posición inicial
        x = startX;
        y = startY;

        // Calculo el tamaño de un frame del spritesheet de movimiento
        int moveFrameW = tankMoveSheet.getWidth() / MOVE_FRAMES;
        int moveFrameH = tankMoveSheet.getHeight();

        // Calculo el tamaño final del tanque en pantalla
        width = moveFrameW * SCALE;
        height = moveFrameH * SCALE;

        // Creo animación de movimiento en bucle
        moveAnim = buildAnimHorizontal(tankMoveSheet, MOVE_FRAMES, 0.10f, true);

        // Creo animación de destrucción que se reproduce una sola vez
        destroyAnim = buildAnimHorizontal(tankDestroySheet, DESTROY_FRAMES, 0.06f, false);

        // Creo el hitbox inicial
        updateBounds();
    }

    private Animation<TextureRegion> buildAnimHorizontal(Texture sheet, int frames, float frameTime, boolean loop) {

        // Aquí calculo el tamaño de cada frame dividiendo el ancho total entre el número de frames
        int frameW = sheet.getWidth() / frames;
        int frameH = sheet.getHeight();

        // Corto el spritesheet en frames
        TextureRegion[][] split = TextureRegion.split(sheet, frameW, frameH);

        // Me guardo los frames de la primera fila
        Array<TextureRegion> regions = new Array<>(frames);
        for (int i = 0; i < frames; i++) regions.add(split[0][i]);

        // Creo la animación con el tiempo por frame
        Animation<TextureRegion> anim = new Animation<>(frameTime, regions);

        // Si loop es true se repite, si no se reproduce una sola vez
        if (loop) anim.setPlayMode(Animation.PlayMode.LOOP);
        else anim.setPlayMode(Animation.PlayMode.NORMAL);

        return anim;
    }

    public void update(float delta, float aylaX) {

        // Avanzo tiempo de animación
        stateTime += delta;

        // Si ya desapareció, solo me aseguro de parar el sonido
        if (state == State.GONE) { stopMoveLoop(); return; }

        // Si está en destrucción, espero a que termine la animación para pasar a muerto con parpadeo
        if (state == State.DESTROY) {
            stopMoveLoop();
            if (destroyAnim.isAnimationFinished(stateTime)) enterDeadBlink();
            updateBounds();
            return;
        }

        // Si está muerto, hace el parpadeo y cuando termina desaparece
        if (state == State.DEAD) {
            stopMoveLoop();

            blinkTimer += delta;
            if (blinkTimer >= BLINK_INTERVAL) {
                blinkTimer = 0f;
                visible = !visible;
                blinkToggles++;

                // Se multiplica por dos porque cuenta visible e invisible
                if (blinkToggles >= BLINK_TIMES * 2) state = State.GONE;
            }

            updateBounds();
            return;
        }

        // El tanque mira hacia Ayla
        facingRight = aylaX > x;

        // Distancia horizontal a Ayla
        float dx = aylaX - x;
        float absDx = Math.abs(dx);

        // Si está cerca, se queda quieto
        if (absDx <= STOP_DISTANCE) {
            state = State.IDLE;
            stopMoveLoop();
        } else {
            // Si está lejos, se mueve hacia ella
            state = State.MOVE;

            float dir = -1f;
            if (dx > 0f) dir = 1f;

            x += dir * MOVE_SPEED * delta;
            startMoveLoop();
        }

        updateBounds();
    }

    private void startMoveLoop() {

        // Si no está sonando el sonido en bucle del movimiento, lo inicio
        if (moveLoopId == -1) moveLoopId = audio.loopSfx(Assets.SFX_TANK_MOVE, 0.35f);
    }

    private void stopMoveLoop() {

        // Si está sonando, lo paro y reseteo el id
        if (moveLoopId != -1) {
            audio.stopLoop(Assets.SFX_TANK_MOVE, moveLoopId);
            moveLoopId = -1;
        }
    }

    public boolean canShoot(float delta) {

        // Si está muerto, destruyéndose o ya desapareció, no puede disparar
        if (state == State.DEAD || state == State.DESTROY || state == State.GONE) return false;

        // Sumo tiempo al temporizador
        shootTimer += delta;

        // Cuando llega al cooldown, permito disparo y reseteo el timer
        if (shootTimer >= SHOOT_COOLDOWN) {
            shootTimer = 0f;

            // Aquí reproduces un sonido como aviso o explosión de disparo
            audio.playSfx(Assets.SFX_EXPLOSION_GRENADE);

            return true;
        }

        return false;
    }

    public void hitByAylaBullet() {

        // Si está en estados donde no debe recibir impactos, no hago nada
        if (state == State.DESTROY || state == State.DEAD || state == State.GONE) return;

        // Sumo impactos recibidos
        hitsTaken++;

        // Si ya llegó a su vida máxima en impactos, empiezo destrucción
        if (hitsTaken >= HP) {
            state = State.DESTROY;
            stateTime = 0f;

            // Sonido de explosión del tanque
            audio.playSfx(Assets.SFX_EXPLOSION_TANK);

            // Quito hitbox para que no choque mientras se destruye
            bounds.set(0, 0, 0, 0);
        }
    }

    private void enterDeadBlink() {

        // Entro en estado muerto y preparo el parpadeo
        state = State.DEAD;
        stateTime = 0f;

        blinkTimer = 0f;
        blinkToggles = 0;
        visible = true;

        // Quito hitbox para que ya no colisione
        bounds.set(0, 0, 0, 0);
    }

    public void draw(SpriteBatch batch) {

        // Si ya desapareció no dibujo
        if (state == State.GONE) return;

        // Si está muerto y justo toca invisible, no dibujo este frame
        if (state == State.DEAD && !visible) return;

        float drawY = y - FOOT_OFFSET;

        // Si está muerto dibujo la textura de muerto y ya está
        if (state == State.DEAD) { drawThing(batch, deadTex, null, drawY); return; }

        // Si se está destruyendo dibujo la animación de destrucción
        if (state == State.DESTROY) {
            drawThing(batch, null, destroyAnim.getKeyFrame(stateTime, false), drawY);
            return;
        }

        // Si está quieto dibujo la textura idle
        if (state == State.IDLE) { drawThing(batch, idleTex, null, drawY); return; }

        // Si se está moviendo dibujo la animación de movimiento
        drawThing(batch, null, moveAnim.getKeyFrame(stateTime, true), drawY);
    }

    private void drawThing(SpriteBatch batch, Texture tex, TextureRegion r, float drawY) {

        // Si me pasan un frame de animación, dibujo ese frame
        // Si no, dibujo una textura normal
        if (r != null) {
            if (facingRight) batch.draw(r, x, drawY, width, height);
            else batch.draw(r, x + width, drawY, -width, height);
        } else {
            if (facingRight) batch.draw(tex, x, drawY, width, height);
            else batch.draw(tex, x + width, drawY, -width, height);
        }
    }

    private void updateBounds() {

        // Si está destruyéndose, muerto o desaparecido, el hitbox se borra
        if (state == State.DESTROY || state == State.DEAD || state == State.GONE) {
            bounds.set(0, 0, 0, 0);
            return;
        }

        // El hitbox del tanque es un rectángulo más pequeño que la imagen
        // Para que la colisión sea más justa, solo uso parte del ancho y parte del alto
        float hitW = width * 0.75f;
        float hitH = height * 0.40f;

        // Centro el hitbox dentro del tanque
        float hitX = x + (width - hitW) / 2f;
        float hitY = y;

        // Tamaño mínimo por seguridad
        if (hitW < 10f) hitW = 10f;
        if (hitH < 10f) hitH = 10f;

        bounds.set(hitX, hitY, hitW, hitH);
    }

    public Rectangle getBounds() { return bounds; }

    public boolean isDead() { return state == State.DEAD || state == State.GONE; }
    public boolean isDestroying() { return state == State.DESTROY; }
    public boolean isGone() { return state == State.GONE; }

    public boolean isOffScreenLeft(float camLeft) { return x + width < camLeft - 700f; }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public boolean isFacingRight() { return facingRight; }

    public float getMuzzleX() {

        // Esto devuelve la posición X del cañón para disparar desde ahí
        // Si mira a la derecha uso un porcentaje hacia la derecha
        // Si mira a la izquierda invierto el porcentaje para que salga del otro lado
        if (facingRight) return x + width * MUZZLE_X_RATIO;
        return x + width * (1f - MUZZLE_X_RATIO);
    }

    public float getMuzzleY() {

        // Esto devuelve la posición Y del cañón usando el porcentaje del alto
        // También tiene en cuenta el offset del dibujo para que coincida con el sprite
        return (y - FOOT_OFFSET) + height * MUZZLE_Y_RATIO;
    }
}
