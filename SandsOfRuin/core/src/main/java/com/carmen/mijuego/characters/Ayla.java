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

    // Posición de Ayla en el mundo y velocidad vertical para el salto y la caída
    private float x, y, velY;

    // Esto guarda hacia dónde está mirando Ayla para que dispare hacia ese lado
    private boolean facingRight = true;

    // Esto dice si Ayla está tocando el suelo o está en el aire
    private boolean onGround = false;

    // Gravedad hacia abajo y fuerza del salto
    private static final float GRAVITY = -1800f;
    private static final float JUMP = 800f;

    // Número máximo de saltos seguidos, aquí es doble salto
    private static final int MAX_JUMPS = 2;
    private int jumpsLeft = MAX_JUMPS;

    // En el segundo salto se aplica un poquito menos de fuerza para que se sienta diferente
    private static final float DOUBLE_JUMP_MUL = 0.90f;

    // Esto sirve para detectar si el botón de salto se acaba de pulsar ahora o si se está dejando pulsado
    private boolean jumpWasDown = false;

    // Esto es un ajuste para dibujar la textura un poco más abajo, para que el pie coincida mejor con el suelo
    private static final float FOOT_OFFSET = 14f;

    // Estas constantes recortan el hitbox para que no sea tan grande como la imagen
    // Así las colisiones se sienten más justas y menos injustas
    private static final float HIT_PAD_L = 75f;
    private static final float HIT_PAD_R = 75f;
    private static final float HIT_PAD_BOTTOM = 22f;
    private static final float HIT_PAD_TOP = 20f;

    // Este rectángulo es la caja de colisión real de Ayla
    private final Rectangle bounds = new Rectangle();

    // Texturas para estar quieta y para saltar
    private final Texture idleTex;
    private final Texture jumpTex;

    // Animación para correr, que sale de un spritesheet
    private final Animation<TextureRegion> runAnim;

    // Tiempo acumulado para que la animación avance frame a frame
    private float stateTime;

    // Escala para dibujar a Ayla más pequeña que el tamaño real del spritesheet
    private final float scale = 0.60f;

    // Tamaño final que ocupa Ayla en pantalla ya escalada
    private final float width, height;

    // Tamaño de cada frame dentro del spritesheet de correr
    private static final int FRAME_W = 336;
    private static final int FRAME_H = 411;

    // Texturas de las balas normales y las especiales
    private final Texture bulletTex;
    private final Texture specialBulletTex;

    // Lista de balas activas en pantalla
    private final Array<Bullet> bullets = new Array<>();

    // Velocidad de la bala y límite de balas para no llenar la pantalla
    private static final float BULLET_SPEED = 1200f;
    private static final int MAX_BULLETS_ON_SCREEN = 30;

    // Tamaño de cada bala
    private static final float BULLET_W = 36f;
    private static final float BULLET_H = 18f;

    // Ajuste para que la bala salga más o menos desde la mano o el arma
    private static final float BULLET_OFFSET_X = 190f;
    private static final float BULLET_OFFSET_Y = 190f;

    // Número de tiros normales permitidos antes de obligar a recargar
    private static final int NORMAL_MAX_SHOTS = 2;

    // Tiempo de recarga del disparo normal
    private static final float NORMAL_COOLDOWN_TIME = 3f;

    // Cuántos tiros normales llevo desde la última recarga
    private int normalShots = 0;

    // Esto dice si el disparo normal está en recarga ahora mismo
    private boolean normalOnCooldown = false;

    // Contador para medir cuánto queda de recarga
    private float normalCooldownTimer = 0f;

    // Esto sirve para disparar solo una vez cuando se pulsa, y no disparar en automático si se deja pulsado
    private boolean shootWasDown = false;

    // Cooldown del disparo especial
    private static final float SPECIAL_COOLDOWN_TIME = 15f;

    // Temporizador del disparo especial
    private float specialTimer = 0f;

    // Igual que con el disparo normal, para detectar pulsación nueva
    private boolean specialWasDown = false;

    // Estado de maldición, que dura unos segundos
    private float cursedTimer = 0f;
    private static final float CURSE_DEFAULT_TIME = 4.0f;

    // Vidas máximas y vidas actuales
    private int maxLives = 5;
    private int lives = 5;

    // Tiempo de invulnerabilidad después de recibir daño
    private float invulnTimer = 0f;
    private static final float INVULN_TIME = 3.0f;

    // Este timer lo usas para el parpadeo, aunque en el dibujo realmente usas invulnTimer
    private float blinkTimer = 0f;

    // Id del sonido en bucle de correr, para poder pararlo después
    private long runLoopId = -1;

    public Ayla(AudioManager audio,
                Texture runSheet,
                Texture idle,
                Texture jump,
                Texture bulletTex,
                Texture specialBulletTex,
                float startX,
                float startY) {

        // Guardo el audio para poder usar efectos
        this.audio = audio;

        // Guardo las texturas principales
        this.idleTex = idle;
        this.jumpTex = jump;
        this.bulletTex = bulletTex;
        this.specialBulletTex = specialBulletTex;

        // Posición inicial de Ayla
        this.x = startX;
        this.y = startY;

        // Calculo cuántos frames tiene el spritesheet de correr
        int cols = runSheet.getWidth() / FRAME_W;

        // Si por algún motivo no hay frames, lanzo error porque ese spritesheet estaría mal
        if (cols <= 0) throw new IllegalArgumentException("Spritesheet Ayla inválido");

        // Aquí separo el spritesheet en cuadritos del tamaño de cada frame
        TextureRegion[][] split = TextureRegion.split(runSheet, FRAME_W, FRAME_H);

        // Me quedo con todos los frames de la primera fila
        TextureRegion[] frames = new TextureRegion[cols];
        for (int i = 0; i < cols; i++) frames[i] = split[0][i];

        // Creo la animación de correr con una velocidad de 0.10 segundos por frame
        runAnim = new Animation<>(0.10f, frames);

        // Calculo el tamaño final de Ayla según el scale
        width = FRAME_W * scale;
        height = FRAME_H * scale;

        // Actualizo el hitbox con la posición inicial
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
        // Este método llama al update completo con forceRun en false
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

        // Esto sirve para saber si se está moviendo a izquierda o derecha
        // El XOR hace que sea verdadero solo si está pulsado uno de los dos, no los dos a la vez
        boolean movingByInput = (left ^ right);

        // Si forceRun es true, Ayla se considera moviéndose aunque no pulses nada
        boolean moving = movingByInput || forceRun;

        // Actualizo hacia dónde mira Ayla para dibujar y disparar
        if (right) facingRight = true;
        if (left)  facingRight = false;

        // Si se fuerza correr y no hay input, la pongo mirando a la derecha
        if (forceRun && !movingByInput) facingRight = true;

        // Bajo los temporizadores de invulnerabilidad y parpadeo
        if (invulnTimer > 0f) { invulnTimer -= delta; if (invulnTimer < 0f) invulnTimer = 0f; }
        if (blinkTimer > 0f)  { blinkTimer -= delta;  if (blinkTimer < 0f)  blinkTimer = 0f; }

        // Detecto si el salto se acaba de pulsar ahora mismo
        boolean jumpJustPressed = jump && !jumpWasDown;
        jumpWasDown = jump;

        // Si se acaba de pulsar y todavía quedan saltos disponibles, salto
        if (jumpJustPressed && jumpsLeft > 0) {

            // Si no está en el suelo, es doble salto
            boolean isDoubleJump = !onGround;

            // Si es doble salto, aplico un multiplicador para que sea un poco más bajo
            float mul = 1f;
            if (isDoubleJump) mul = DOUBLE_JUMP_MUL;

            // Cambio velocidad vertical hacia arriba
            velY = JUMP * mul;

            // Sonido de salto
            if (audio != null) audio.playSfx(Assets.SFX_AYLA_JUMP);

            // Ahora está en el aire y gasto un salto
            onGround = false;
            jumpsLeft--;
        }

        // Aplico gravedad y muevo a Ayla en vertical
        velY += GRAVITY * delta;
        y += velY * delta;

        // Si baja por debajo del suelo, la coloco exactamente en el suelo y reseteo saltos
        if (y <= groundY) {
            y = groundY;
            velY = 0f;
            onGround = true;
            jumpsLeft = MAX_JUMPS;
        }

        // Si se mueve y está en el suelo, avanzo la animación de correr
        // Si no, reseteo el tiempo para que vuelva al inicio
        if (moving && onGround) stateTime += delta;
        else stateTime = 0f;

        // Gestiono el sonido de pasos en bucle
        handleRunSfx(moving, onGround);

        // Bajo el timer de maldición si está activo
        if (cursedTimer > 0f) { cursedTimer -= delta; if (cursedTimer < 0f) cursedTimer = 0f; }

        // Si el disparo normal está en cooldown, avanzo el contador y cuando termine reseteo tiros
        if (normalOnCooldown) {
            normalCooldownTimer += delta;
            if (normalCooldownTimer >= NORMAL_COOLDOWN_TIME) {
                normalOnCooldown = false;
                normalCooldownTimer = 0f;
                normalShots = 0;
            }
        }

        // Bajo el timer del disparo especial
        if (specialTimer > 0f) { specialTimer -= delta; if (specialTimer < 0f) specialTimer = 0f; }

        // Detecto si se ha pulsado disparo ahora mismo
        boolean shootJustPressed = shoot && !shootWasDown;
        shootWasDown = shoot;

        // Detecto si se ha pulsado granada ahora mismo
        boolean specialJustPressed = grenade && !specialWasDown;
        specialWasDown = grenade;

        // Si se pulsa disparo, intento disparo normal
        if (shootJustPressed) tryNormalShoot();

        // Si se pulsa granada, intento disparo especial
        if (specialJustPressed) trySpecialShoot();

        // Actualizo balas, y borro las que salen fuera de cámara o ya no están vivas
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(delta);

            if (b.getX() < camLeft - 300f || b.getX() > camRight + 300f) b.kill();
            if (!b.isAlive()) bullets.removeIndex(i);
        }

        // Actualizo el hitbox al final del update
        updateBounds();
    }

    private void handleRunSfx(boolean moving, boolean onGroundNow) {

        // Si está muerta, paro el sonido de correr
        if (isDead()) {
            stopRunLoop();
            return;
        }

        // Si se mueve y está en el suelo, pongo el sonido de correr en bucle
        if (moving && onGroundNow) {
            if (runLoopId == -1 && audio != null) {
                runLoopId = audio.loopSfx(Assets.SFX_CHARACTER_RUN, 0.35f);
            }
        } else {
            // Si no se mueve o está en el aire, paro el sonido de correr
            stopRunLoop();
        }
    }

    private void stopRunLoop() {
        // Si había un loop activo, lo paro usando su id
        if (runLoopId != -1) {
            if (audio != null) audio.stopLoop(Assets.SFX_CHARACTER_RUN, runLoopId);
            runLoopId = -1;
        }
    }

    private void tryNormalShoot() {

        // Si está en cooldown, no puede disparar
        if (normalOnCooldown) return;

        // Si ya disparó el máximo, activo recarga y reproduzco sonido de recargar
        if (normalShots >= NORMAL_MAX_SHOTS) {
            normalOnCooldown = true;
            normalCooldownTimer = 0f;
            if (audio != null) audio.playSfx(Assets.SFX_GUN_RELOAD);
            return;
        }

        // Si hay demasiadas balas en pantalla, no creo más
        if (bullets.size >= MAX_BULLETS_ON_SCREEN) return;

        // Creo la bala normal con daño 1
        spawnBullet(bulletTex, 1);
        normalShots++;

        // Sonido del disparo
        if (audio != null) audio.playSfx(Assets.SFX_SHOT_GUN_2);
    }

    private void trySpecialShoot() {

        // Si todavía está en cooldown, no puede disparar especial
        if (specialTimer > 0f) return;

        // Si hay demasiadas balas, tampoco creo más
        if (bullets.size >= MAX_BULLETS_ON_SCREEN) return;

        // Creo bala especial con daño 2 y activo su cooldown
        spawnBullet(specialBulletTex, 2);
        specialTimer = SPECIAL_COOLDOWN_TIME;

        // Sonido del disparo especial
        if (audio != null) audio.playSfx(Assets.SFX_SHOT_GUN_1);
    }

    private void spawnBullet(Texture tex, int damage) {

        // Dirección de disparo según hacia dónde mire Ayla
        float dir = 1f;
        if (!facingRight) dir = -1f;

        // Posición inicial en X según si mira a derecha o izquierda
        float spawnX;
        if (facingRight) spawnX = x + BULLET_OFFSET_X;
        else spawnX = x + (width - BULLET_OFFSET_X) - BULLET_W;

        // Posición inicial en Y, ajustada con el offset del pie
        float spawnY = (y - FOOT_OFFSET) + BULLET_OFFSET_Y;

        // Creo la bala y la meto en la lista de balas activas
        bullets.add(new Bullet(tex, spawnX, spawnY, BULLET_SPEED * dir, BULLET_W, BULLET_H, damage));
    }

    public void draw(SpriteBatch batch, boolean moving) {

        // Ajusto la Y para dibujar con el offset del pie
        float drawY = y - FOOT_OFFSET;

        // Guardo el color actual del batch para restaurarlo al final
        float pr = batch.getColor().r;
        float pg = batch.getColor().g;
        float pb = batch.getColor().b;
        float pa = batch.getColor().a;

        boolean visible = true;

        // Si está invulnerable, hago efecto de parpadeo y también la hago un poco transparente
        if (invulnTimer > 0f) {
            float blinkSpeed = 15f;
            visible = ((int) (invulnTimer * blinkSpeed)) % 2 == 0;
            batch.setColor(1f, 1f, 1f, 0.45f);
        }

        if (visible) {

            // Si está en el aire, dibujo la textura de salto
            if (!onGround) {
                drawTexture(batch, jumpTex, drawY);

                // Si está en el suelo y no se mueve, dibujo la textura de idle
            } else if (!moving) {
                drawTexture(batch, idleTex, drawY);

                // Si se mueve en el suelo, dibujo la animación de correr
            } else {
                TextureRegion frame = runAnim.getKeyFrame(stateTime, true);
                if (facingRight) batch.draw(frame, x, drawY, width, height);
                else batch.draw(frame, x + width, drawY, -width, height);
            }

            // Dibujo todas las balas
            for (Bullet b : bullets) b.draw(batch);
        }

        // Devuelvo el color original del batch para que no afecte al resto del juego
        batch.setColor(pr, pg, pb, pa);
    }

    private void drawTexture(SpriteBatch batch, Texture tex, float drawY) {
        // Dibujo la textura mirando a derecha o a izquierda usando ancho negativo
        if (facingRight) batch.draw(tex, x, drawY, width, height);
        else batch.draw(tex, x + width, drawY, -width, height);
    }

    private void updateBounds() {

        // Calculo la posición real del hitbox aplicando recortes
        float hitX = x + HIT_PAD_L;
        float hitY = y + HIT_PAD_BOTTOM;

        // Calculo tamaño del hitbox también recortado
        float hitW = width - (HIT_PAD_L + HIT_PAD_R);
        float hitH = height - (HIT_PAD_BOTTOM + HIT_PAD_TOP);

        // Aseguro un tamaño mínimo para evitar hitbox cero
        if (hitW < 10f) hitW = 10f;
        if (hitH < 10f) hitH = 10f;

        bounds.set(hitX, hitY, hitW, hitH);
    }

    // Devuelve el hitbox para colisiones
    public Rectangle getBounds() { return bounds; }

    // Cambia la X y actualiza hitbox
    public void setX(float x) { this.x = x; updateBounds(); }

    // Devuelve vidas actuales
    public int getLives() { return lives; }

    // Cambia vidas y evita que sean negativas
    public void setLives(int lives) {
        this.lives = lives;
        if (this.lives < 0) this.lives = 0;
    }

    // Devuelve vidas máximas
    public int getMaxLives() { return maxLives; }

    // Dice si está muerta
    public boolean isDead() { return lives <= 0; }

    // Dice si puede recibir daño ahora mismo
    public boolean canTakeDamage() { return invulnTimer <= 0f && !isDead(); }

    public boolean takeDamage() {

        // Si no puede recibir daño, no hago nada
        if (!canTakeDamage()) return false;

        // Bajo una vida
        lives--;
        if (lives < 0) lives = 0;

        // Activo invulnerabilidad
        invulnTimer = INVULN_TIME;

        // Sonido de daño
        if (audio != null) audio.playSfx(Assets.SFX_AYLA_DAMAGE);

        // Vibración solo si es Android y tiene vibrador disponible
        if (Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Android) {
            try {
                if (Gdx.input.isPeripheralAvailable(Input.Peripheral.Vibrator)) {
                    audio.vibrate(150);
                }
            } catch (Exception ignored) {}
        }

        // Si muere, paro el sonido de correr
        if (isDead()) stopRunLoop();

        return true;
    }

    // Resetea vidas y estados de invulnerabilidad
    public void resetLives() {
        lives = maxLives;
        invulnTimer = 0f;
        blinkTimer = 0f;
        stopRunLoop();
    }

    // Para sonidos en bucle si hubiera más en el futuro
    public void stopAllLoops() { stopRunLoop(); }

    // Activa maldición con el tiempo por defecto
    public void applyCurse() { applyCurse(CURSE_DEFAULT_TIME); }

    // Activa maldición por un tiempo específico
    public void applyCurse(float seconds) {
        cursedTimer = Math.max(cursedTimer, seconds);
    }

    // Quita la maldición
    public void clearCurse() { cursedTimer = 0f; }

    // Dice si está maldita ahora mismo
    public boolean isCursed() { return cursedTimer > 0f; }

    // Getters de posición y tamaño
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }

    // Devuelve la lista de balas para colisiones con enemigos
    public Array<Bullet> getBullets() { return bullets; }

    // Esto sirve para dibujar una barra de recarga del disparo normal
    public float getNormalCooldownPercent() {
        if (!normalOnCooldown) return 0f;
        float p = normalCooldownTimer / NORMAL_COOLDOWN_TIME;
        if (p < 0f) p = 0f;
        if (p > 1f) p = 1f;
        return p;
    }

    // Esto sirve para dibujar una barra de recarga del disparo especial
    public float getSpecialCooldownPercent() {
        if (specialTimer <= 0f) return 0f;
        float p = specialTimer / SPECIAL_COOLDOWN_TIME;
        if (p < 0f) p = 0f;
        if (p > 1f) p = 1f;
        return p;
    }
}
