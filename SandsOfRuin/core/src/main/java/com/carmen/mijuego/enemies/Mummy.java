package com.carmen.mijuego.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.audio.AudioManager;

public class Mummy {

    // Esto sirve para reproducir sonidos de la momia, como gruñidos, disparos y muerte
    private final AudioManager audio;

    // Estados de la momia
    // IDLE significa quieta
    // WALK significa caminando hacia Ayla
    // DYING_HURT significa que está haciendo la animación de morir
    // DEAD significa que ya está muerta y ya no interactúa
    public enum State { IDLE, WALK, DYING_HURT, DEAD }

    // Tamaño de cada frame dentro del spritesheet de la momia
    private static final int FRAME_W = 410, FRAME_H = 608;

    // Escala visual para dibujarla más pequeña
    private static final float VISUAL_SCALE = 0.68f;

    // Ancho y alto finales ya escalados
    private final float width = FRAME_W * VISUAL_SCALE;
    private final float height = FRAME_H * VISUAL_SCALE;

    // Ajustes verticales
    // FOLLOW_Y_OFFSET baja o sube a la momia respecto al suelo para que encaje con el escenario
    // DRAW_Y_OFFSET es un pequeño ajuste extra solo para el dibujo
    private static final float FOLLOW_Y_OFFSET = -45f, DRAW_Y_OFFSET = -10f;

    // Velocidad de animación de caminar y de herida o muerte
    private static final float WALK_FRAME = 0.12f, HURT_FRAME = 0.08f;

    // Vida total de la momia
    private static final int HP_MAX = 5;
    private int hp = HP_MAX;

    // Velocidad de movimiento y distancia mínima a Ayla para dejar de caminar
    // Si Ayla está cerca, se queda quieta para poder disparar
    private static final float SPEED = 45f, STOP_DISTANCE = 650f;

    // Tiempo entre disparos
    private static final float SHOOT_COOLDOWN = 2.20f;

    // Temporizador que baja y cuando llega a cero puede volver a disparar
    private float shootTimer = 0f;

    // Ajustes del hitbox para que sea más pequeño que la imagen
    private static final float HIT_SCALE = 0.40f;
    private static final float HIT_PAD_L = 120f * HIT_SCALE, HIT_PAD_R = 120f * HIT_SCALE;
    private static final float HIT_PAD_B = 70f * HIT_SCALE,  HIT_PAD_T = 90f  * HIT_SCALE;

    // Frames y animaciones
    // idleFrame es la imagen quieta
    // deadFrame es la imagen muerta
    // walkAnim es la animación de caminar en bucle
    // hurtAnim es la animación de morir que solo se reproduce una vez
    private final TextureRegion idleFrame;
    private final TextureRegion deadFrame;
    private final Animation<TextureRegion> walkAnim;
    private final Animation<TextureRegion> hurtAnim;

    // Estado actual y tiempo para la animación
    private State state = State.IDLE;
    private float stateTime = 0f;

    // Posición de la momia en el mundo
    private float x, y;

    // Hacia dónde mira la momia para dibujarla girada
    private boolean facingRight = false;

    // Rectángulo de colisión de la momia
    private final Rectangle bounds = new Rectangle();

    // Id del sonido de gruñidos en bucle, para poder pararlo
    private long gruntsLoopId = -1;

    public Mummy(AudioManager audio, Texture idle, Texture walk, Texture hurt, Texture dead,
                 float startX, float groundY) {

        // Guardo el audio
        this.audio = audio;

        // Posición inicial
        x = startX;

        // Coloco la momia a la altura del suelo con el offset para que encaje bien
        y = groundY + FOLLOW_Y_OFFSET;

        // Creo los frames de idle y dead usando el tamaño del frame
        idleFrame = new TextureRegion(idle, 0, 0, FRAME_W, FRAME_H);
        deadFrame = new TextureRegion(dead, 0, 0, FRAME_W, FRAME_H);

        // Creo la animación de caminar en bucle
        walkAnim = buildAnimLoop(walk, WALK_FRAME);

        // Creo la animación de morir, que se reproduce una sola vez
        hurtAnim = buildAnimNormal(hurt, HURT_FRAME);

        // Creo el hitbox inicial
        updateBounds();
    }

    public void startFight() {

        // Esto se llama cuando empieza el combate
        // Si estaba en estado quieta, pasa a caminar
        if (state == State.IDLE) {
            state = State.WALK;
            stateTime = 0f;
        }
    }

    public void update(float delta, float aylaX, float groundY) {

        // Si ya está muerta, me aseguro de parar los sonidos y no hago nada más
        if (state == State.DEAD) {
            stopGrunts();
            return;
        }

        // Actualizo la altura por si el suelo cambia o por si hay scroll
        y = groundY + FOLLOW_Y_OFFSET;

        // Avanzo el tiempo de animación
        stateTime += delta;

        // Bajo el temporizador de disparo
        shootTimer -= delta;
        if (shootTimer < 0f) shootTimer = 0f;

        // Si está en animación de morir, espero a que termine para pasar a muerta
        if (state == State.DYING_HURT) {
            stopGrunts();
            if (hurtAnim.isAnimationFinished(stateTime)) enterDead();
            return;
        }

        // Distancia horizontal entre Ayla y la momia
        float dx = aylaX - x;

        // La momia mira hacia la dirección donde esté Ayla
        facingRight = dx > 0f;

        // Si Ayla está suficientemente cerca, la momia se para y se queda quieta
        // Esta distancia está pensada para que tenga sentido con el disparo
        if (Math.abs(dx) <= STOP_DISTANCE) {
            if (state != State.IDLE) { state = State.IDLE; stateTime = 0f; }
            stopGrunts();
            updateBounds();
            return;
        }

        // Si Ayla está lejos, la momia camina hacia ella
        if (state != State.WALK) { state = State.WALK; stateTime = 0f; }

        // Activo los gruñidos mientras camina
        startGrunts();

        // Muevo la momia hacia Ayla usando el signo de dx para saber la dirección
        x += Math.signum(dx) * SPEED * delta;

        // Actualizo el hitbox al mover
        updateBounds();
    }

    private void startGrunts() {

        // Si todavía no hay sonido en bucle, lo inicio
        // Esto evita que se inicie una y otra vez cada frame
        if (gruntsLoopId == -1) gruntsLoopId = audio.loopSfx(Assets.SFX_MUMMY_GRUNTS, 0.35f);
    }

    private void stopGrunts() {

        // Si el sonido está activo, lo paro y reseteo el id
        if (gruntsLoopId != -1) {
            audio.stopLoop(Assets.SFX_MUMMY_GRUNTS, gruntsLoopId);
            gruntsLoopId = -1;
        }
    }

    // Esto se usa para parar sonidos si se pausa el juego o se cambia de pantalla
    public void stopAllLoops() { stopGrunts(); }

    public boolean canShoot(float delta) {

        // Si está muerta o muriéndose, no puede disparar
        if (state == State.DEAD || state == State.DYING_HURT) return false;

        // Solo dispara cuando está quieta, que es cuando está a distancia correcta
        if (state != State.IDLE) return false;

        // Si todavía está en cooldown, no puede disparar
        if (shootTimer > 0f) return false;

        // Si puede disparar, activo el cooldown y reproduzco sonido
        shootTimer = SHOOT_COOLDOWN;
        audio.playSfx(Assets.SFX_MUMMY_SHOTS);
        return true;
    }

    public void draw(SpriteBatch batch) {

        // Aquí elijo qué imagen se debe dibujar dependiendo del estado
        TextureRegion frame;

        switch (state) {
            case WALK: frame = walkAnim.getKeyFrame(stateTime, true); break;
            case DYING_HURT: frame = hurtAnim.getKeyFrame(stateTime, false); break;
            case DEAD: frame = deadFrame; break;
            case IDLE:
            default: frame = idleFrame; break;
        }

        // Ajusto la y final del dibujo con un pequeño offset
        float drawY = y + DRAW_Y_OFFSET;

        // Dibujo normal si mira a la derecha
        // Si mira a la izquierda, dibujo con ancho negativo para que se vea girada
        if (facingRight) batch.draw(frame, x, drawY, width, height);
        else batch.draw(frame, x + width, drawY, -width, height);
    }

    public void hitByAylaBullet() {

        // Si está muerta o muriéndose, ignoro el impacto
        if (state == State.DEAD || state == State.DYING_HURT) return;

        // Bajo vida
        hp--;

        // Si llega a cero, paso al estado de morir y quito el hitbox
        // Quito el hitbox para que ya no se le pueda seguir pegando o chocando
        if (hp <= 0) {
            state = State.DYING_HURT;
            stateTime = 0f;
            bounds.set(0, 0, 0, 0);
            stopGrunts();
        }
    }

    private void enterDead() {

        // Aquí termino el proceso de morir y dejo la momia en estado muerta
        state = State.DEAD;
        stateTime = 0f;

        // Quito hitbox para que ya no exista en colisiones
        bounds.set(0, 0, 0, 0);

        // Paro sonidos y reproduzco sonido de muerte
        stopGrunts();
        audio.playSfx(Assets.SFX_MUMMY_DEAD);
    }

    public void applyWorldScroll(float dx) {

        // Esto se usa cuando el mundo se mueve con scroll
        // Si está muerta, también se mueve para que no se quede atrás
        if (state == State.DEAD) x += dx;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public boolean isFacingRight() { return facingRight; }

    public Rectangle getBounds() { return bounds; }
    public boolean isDead() { return state == State.DEAD; }
    public int getHp() { return hp; }

    private Animation<TextureRegion> buildAnimLoop(Texture sheet, float frameDuration) {

        // Esto crea una animación que se repite en bucle
        TextureRegion[] frames = splitAllFrames(sheet);
        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(Animation.PlayMode.LOOP);
        return anim;
    }

    private Animation<TextureRegion> buildAnimNormal(Texture sheet, float frameDuration) {

        // Esto crea una animación que se reproduce una sola vez y se detiene al final
        TextureRegion[] frames = splitAllFrames(sheet);
        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(Animation.PlayMode.NORMAL);
        return anim;
    }

    private TextureRegion[] splitAllFrames(Texture sheet) {

        // Aquí saco el tamaño total de la imagen
        int w = sheet.getWidth(), h = sheet.getHeight();

        // Si el tamaño no encaja con el tamaño de frame esperado, uso un solo frame
        // Esto evita errores si alguna imagen no es spritesheet, por ejemplo si es solo una pose
        if (w % FRAME_W != 0 || h % FRAME_H != 0) {
            return new TextureRegion[]{
                new TextureRegion(sheet, 0, 0, Math.min(FRAME_W, w), Math.min(FRAME_H, h))
            };
        }

        // Si encaja, corto todo el spritesheet en una cuadrícula
        TextureRegion[][] grid = TextureRegion.split(sheet, FRAME_W, FRAME_H);
        int rows = grid.length, cols = grid[0].length;

        // Creo un array plano con todos los frames uno detrás de otro
        TextureRegion[] out = new TextureRegion[rows * cols];
        int k = 0;

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                out[k++] = grid[r][c];

        return out;
    }

    private void updateBounds() {

        // Si está muriéndose o muerta, dejo el hitbox vacío para que no choque con nada
        if (state == State.DYING_HURT || state == State.DEAD) { bounds.set(0, 0, 0, 0); return; }

        // Calculo el hitbox con recortes para que sea más justo que la imagen completa
        float hitX = x + HIT_PAD_L, hitY = y + HIT_PAD_B;
        float hitW = width - (HIT_PAD_L + HIT_PAD_R);
        float hitH = height - (HIT_PAD_B + HIT_PAD_T);

        // Tamaño mínimo por seguridad
        if (hitW < 10f) hitW = 10f;
        if (hitH < 10f) hitH = 10f;

        bounds.set(hitX, hitY, hitW, hitH);
    }
}
