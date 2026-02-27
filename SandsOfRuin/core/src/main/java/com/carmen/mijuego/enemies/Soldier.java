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

    // Esto sirve para reproducir sonidos del soldado como correr, recibir daño, disparar y morir
    private final AudioManager audio;

    // Estados del soldado
    // IDLE significa que está parado esperando entrar
    // RUN significa que corre y persigue o se coloca a distancia de disparo
    // HURT significa que está en animación de daño antes de morir
    // DEAD significa que ya está muerto pero todavía se dibuja parpadeando
    // GONE significa que ya no se dibuja ni colisiona, se considera eliminado
    private enum State { IDLE, RUN, HURT, DEAD, GONE }

    // Tamaño de cada frame dentro del spritesheet
    private static final int FRAME_W = 403, FRAME_H = 457;

    // Escala para dibujarlo más pequeño
    private static final float SCALE = 0.55f;

    // Tamaño final ya escalado
    private final float width = FRAME_W * SCALE, height = FRAME_H * SCALE;

    // Ajuste visual para dibujar un poco más abajo y que parezca que pisa el suelo bien
    private static final float FOOT_OFFSET = 14f;

    // Velocidad para correr y distancia a Ayla donde ya no se acerca más
    // Cuando está a esa distancia, se queda y empieza a disparar
    private static final float RUN_SPEED = 260f, STOP_DISTANCE = 800f;

    // Tiempo entre disparos del soldado y velocidad de sus balas
    private static final float SHOOT_COOLDOWN = 3f, BULLET_SPEED = 900f;

    // Tamaño de bala y ajustes para que salga desde la mano o arma
    private static final float BULLET_W = 34f, BULLET_H = 18f;
    private static final float BULLET_OFFSET_X = 55f, BULLET_OFFSET_Y = 145f;

    // Tiempo que dura el estado de daño antes de pasar a muerto
    private static final float HURT_DURATION = 0.35f;

    // Cuántas veces parpadea al morir y cada cuánto cambia visible o invisible
    private static final int BLINK_TIMES = 3;
    private static final float BLINK_INTERVAL = 0.10f;

    // Recortes del hitbox para que la colisión sea más justa
    private static final float HIT_PAD_L = 120f * SCALE, HIT_PAD_R = 120f * SCALE;
    private static final float HIT_PAD_BOTTOM = 50f * SCALE, HIT_PAD_TOP = 70f * SCALE;

    // Posición del soldado
    private float x, y;

    // Dirección a la que mira para dibujar y disparar
    private boolean facingRight = false;

    // Estado actual
    private State state = State.IDLE;

    // Texturas de idle y muerto
    // Las otras dos acciones son animaciones y salen de spritesheets
    private final Texture idleTex, deadTex;
    private final Animation<TextureRegion> runAnim, hurtAnim;

    // stateTime controla el avance de animación
    // shootTimer controla cuándo puede volver a disparar
    // hurtTimer controla cuánto queda en estado HURT
    private float stateTime = 0f, shootTimer = 0f, hurtTimer = 0f;

    // Esto dice si realmente se está moviendo en ese momento
    private boolean moving = false;

    // Variables del parpadeo al morir
    private float blinkTimer = 0f;
    private int blinkToggles = 0;
    private boolean visible = true;

    // Número de impactos recibidos
    // En este enemigo muere con dos impactos
    private int hitsTaken = 0;

    // Textura de bala y lista de balas disparadas
    private final Texture bulletTex;
    private final Array<Bullet> bullets = new Array<>();

    // Hitbox del soldado
    private final Rectangle bounds = new Rectangle();

    // Id del sonido en bucle de correr para poder pararlo
    private long runLoopId = -1;

    public Soldier(AudioManager audio,
                   Texture idleTex,
                   Texture runSheet,
                   Texture hurtSheet,
                   Texture deadTex,
                   Texture bulletTex,
                   float startX,
                   float startY) {

        // Guardo audio y texturas
        this.audio = audio;
        this.idleTex = idleTex;
        this.deadTex = deadTex;
        this.bulletTex = bulletTex;

        // Posición inicial
        x = startX;
        y = startY;

        // Creo animaciones de correr y de recibir daño
        runAnim = buildAnim(runSheet, 0.10f);
        hurtAnim = buildAnim(hurtSheet, 0.08f);

        // Creo el hitbox inicial
        updateBounds();
    }

    private Animation<TextureRegion> buildAnim(Texture sheet, float frameDuration) {

        // Calculo cuántos frames hay en horizontal
        int cols = sheet.getWidth() / FRAME_W;

        // Si no hay frames, algo está mal en el spritesheet
        if (cols <= 0) throw new IllegalArgumentException("Spritesheet soldier inválido");

        // Corto la imagen en frames del tamaño exacto
        TextureRegion[][] split = TextureRegion.split(sheet, FRAME_W, FRAME_H);

        // Me quedo con los frames de la primera fila
        TextureRegion[] frames = new TextureRegion[cols];
        for (int i = 0; i < cols; i++) frames[i] = split[0][i];

        // Devuelvo animación con esa duración por frame
        return new Animation<>(frameDuration, frames);
    }

    public void update(float delta, float aylaX, float camLeft, float camRight) {

        // Avanzo el tiempo de animación
        stateTime += delta;

        // Si ya está fuera, no hago nada
        if (state == State.GONE) return;

        // Cuando el soldado entra en pantalla por la derecha, pasa de idle a run
        if (state == State.IDLE && x < camRight + 10f) {
            state = State.RUN;
            stateTime = 0f;
            moving = false;
        }

        if (state == State.RUN) {

            // Mira hacia Ayla
            facingRight = aylaX > x;

            // Distancia a Ayla
            float dx = aylaX - x;
            float absDx = Math.abs(dx);

            // Si está cerca, se queda parado para disparar
            boolean stopped = absDx <= STOP_DISTANCE;

            if (!stopped) {

                // Si Ayla está a la derecha, dir es positivo, si está a la izquierda, dir es negativo
                float dir = -1f;
                if (dx > 0f) dir = 1f;

                // Muevo al soldado hacia Ayla
                x += dir * RUN_SPEED * delta;
                moving = true;

            } else {
                moving = false;
            }

            // Sonido de correr solo cuando está corriendo de verdad
            handleRunSfx();

            // Si está parado, baja el timer y cuando llega a cero dispara
            if (stopped) {
                shootTimer -= delta;
                if (shootTimer <= 0f) {
                    shootTimer = SHOOT_COOLDOWN;
                    shoot();
                }
            }
        }

        // Si está herido, espero el tiempo de hurt y luego paso a parpadeo de muerto
        if (state == State.HURT) {
            hurtTimer -= delta;
            if (hurtTimer <= 0f) enterDeadBlink();
        }

        // Si está muerto, hace parpadeo y cuando termina desaparece del todo
        if (state == State.DEAD) {
            stopRunLoop();

            blinkTimer += delta;
            if (blinkTimer >= BLINK_INTERVAL) {
                blinkTimer = 0f;
                visible = !visible;
                blinkToggles++;

                // Se multiplica por dos porque cada parpadeo cuenta visible e invisible
                if (blinkToggles >= BLINK_TIMES * 2) state = State.GONE;
            }
        }

        // Actualizo sus balas y elimino las que se salen de cámara o ya están muertas
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(delta);

            if (b.getX() < camLeft - 400f || b.getX() > camRight + 400f) b.kill();
            if (!b.isAlive()) bullets.removeIndex(i);
        }

        // Actualizo hitbox al final
        updateBounds();
    }

    private void handleRunSfx() {

        // Si está corriendo y se está moviendo, pongo sonido de correr en bucle
        // Si no, lo paro
        if (state == State.RUN && moving) {
            if (runLoopId == -1) runLoopId = audio.loopSfx(Assets.SFX_CHARACTER_RUN, 0.30f);
        } else {
            stopRunLoop();
        }
    }

    private void stopRunLoop() {

        // Si hay un loop activo, lo paro y reseteo el id
        if (runLoopId != -1) {
            audio.stopLoop(Assets.SFX_CHARACTER_RUN, runLoopId);
            runLoopId = -1;
        }
    }

    private void shoot() {

        // Solo puede disparar si está en estado run, que es el estado normal de combate
        if (state != State.RUN) return;

        // Sonido de disparo
        audio.playSfx(Assets.SFX_SHOT_GUN_2);

        // Dirección de la bala según hacia dónde mire
        float dir = -1f;
        if (facingRight) dir = 1f;

        // Posición donde aparece la bala dependiendo del lado
        float spawnX;
        if (facingRight) spawnX = x + width - BULLET_OFFSET_X;
        else spawnX = x + BULLET_OFFSET_X - BULLET_W;

        // Altura desde donde sale la bala
        float spawnY = (y - FOOT_OFFSET) + BULLET_OFFSET_Y;

        // Creo la bala y la guardo en la lista
        bullets.add(new Bullet(bulletTex, spawnX, spawnY, BULLET_SPEED * dir, BULLET_W, BULLET_H));
    }

    public void hitByAylaBullet() {

        // Si ya está en estados donde no debe recibir daño, no hago nada
        if (state == State.HURT || state == State.DEAD || state == State.GONE) return;

        // Sumo impactos
        hitsTaken++;

        // Solo reproduzco el sonido de daño en el primer impacto para que no suene raro dos veces seguidas
        if (hitsTaken == 1) audio.playSfx(Assets.SFX_SOLDIER_DAMAGE);

        // Con dos impactos pasa a estado HURT y luego muere
        if (hitsTaken >= 2) {
            state = State.HURT;
            stateTime = 0f;
            hurtTimer = HURT_DURATION;

            // Quito hitbox para que ya no choque mientras está muriendo
            bounds.set(0, 0, 0, 0);
        }
    }

    private void enterDeadBlink() {

        // Entro en estado muerto y activo el parpadeo
        state = State.DEAD;
        stateTime = 0f;

        // Sonido de morir
        audio.playSfx(Assets.SFX_SOLDIER_DEAD);

        // Reseteo el control de parpadeo
        blinkTimer = 0f;
        blinkToggles = 0;
        visible = true;

        // Quito hitbox para que ya no colisione
        bounds.set(0, 0, 0, 0);
    }

    public void draw(SpriteBatch batch) {

        // Si ya no existe, no dibujo nada
        if (state == State.GONE) return;

        // Si está muerto y justo toca estar invisible, no lo dibujo en este frame
        if (state == State.DEAD && !visible) return;

        float drawY = y - FOOT_OFFSET;

        // Según el estado, elijo qué se dibuja
        // Si está muerto, dibujo la textura de muerto
        // Si está herido, dibujo la animación de hurt
        // Si está corriendo y realmente se mueve, dibujo la animación de correr
        // Si no, dibujo el idle
        if (state == State.DEAD) drawThing(batch, deadTex, null, drawY);
        else if (state == State.HURT) drawThing(batch, null, hurtAnim.getKeyFrame(stateTime, false), drawY);
        else if (state == State.RUN && moving) drawThing(batch, null, runAnim.getKeyFrame(stateTime, true), drawY);
        else drawThing(batch, idleTex, null, drawY);

        // Dibujo sus balas
        for (Bullet b : bullets) b.draw(batch);
    }

    private void drawThing(SpriteBatch batch, Texture tex, TextureRegion r, float drawY) {

        // Si me pasaron un frame de animación, dibujo ese frame
        // Si no, dibujo la textura normal
        if (r != null) {
            if (facingRight) batch.draw(r, x, drawY, width, height);
            else batch.draw(r, x + width, drawY, -width, height);
        } else {
            if (facingRight) batch.draw(tex, x, drawY, width, height);
            else batch.draw(tex, x + width, drawY, -width, height);
        }
    }

    private void updateBounds() {

        // Si está en estados donde no debe colisionar, dejo hitbox vacío
        if (state == State.HURT || state == State.DEAD || state == State.GONE) {
            bounds.set(0, 0, 0, 0);
            return;
        }

        // Calculo hitbox recortado
        float hitX = x + HIT_PAD_L, hitY = y + HIT_PAD_BOTTOM;
        float hitW = width - (HIT_PAD_L + HIT_PAD_R);
        float hitH = height - (HIT_PAD_BOTTOM + HIT_PAD_TOP);

        // Tamaño mínimo por seguridad
        if (hitW < 10f) hitW = 10f;
        if (hitH < 10f) hitH = 10f;

        bounds.set(hitX, hitY, hitW, hitH);
    }

    public Rectangle getBounds() { return bounds; }
    public Array<Bullet> getBullets() { return bullets; }

    public boolean isDead() { return state == State.DEAD || state == State.GONE; }
    public boolean isGone() { return state == State.GONE; }

    public boolean isOffScreenLeft(float camLeft) { return x + width < camLeft - 500f; }
}
