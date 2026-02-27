package com.carmen.mijuego.projectiles;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import com.carmen.mijuego.audio.AudioManager;
import com.carmen.mijuego.enemies.Mummy;

public class MummyBulletSystem {

    // Tamaño de la bala, velocidad y altura desde donde sale el disparo
    private static final float W = 70f;
    private static final float H = 34f;
    private static final float SPEED = 820f;
    private static final float MUZZLE_Y = 210f;

    // Se mantiene por compatibilidad aunque aquí no se use directamente
    private final AudioManager audio;

    // Textura de la bala
    private final Texture tex;

    // Lista dinámica de balas activas
    private final Array<Bullet> bullets = new Array<>();

    // Constructor del sistema de disparo de la momia
    public MummyBulletSystem(AudioManager audio, Texture bulletTexture) {
        this.audio = audio;
        this.tex = bulletTexture;
    }

    // Se llama cada frame para actualizar disparos
    public void update(float delta, float camLeft, float camRight, Mummy mummy) {

        // Si no existe momia o está muerta no se hace nada
        if (mummy == null || mummy.isDead()) return;

        // La momia decide internamente si puede disparar
        // El sonido ya se reproduce dentro de mummy.canShoot()
        if (mummy.canShoot(delta)) {

            boolean right = mummy.isFacingRight();

            // Posición base X de la momia
            float x = mummy.getX();

            // Si mira a la derecha el disparo sale por el lado derecho
            if (right) x += mummy.getWidth();
            else x -= W;

            // Velocidad horizontal según dirección
            float velX = -SPEED;
            if (right) velX = SPEED;

            // Se crea y se añade una nueva bala
            bullets.add(new Bullet(
                tex,
                x,
                mummy.getY() + MUZZLE_Y,
                velX,
                W,
                H
            ));
        }

        // Actualización de todas las balas activas
        for (int i = bullets.size - 1; i >= 0; i--) {

            Bullet b = bullets.get(i);
            b.update(delta);

            float x = b.getX();

            // Si la bala está muerta o demasiado lejos de cámara se elimina
            if (!b.isAlive() || x < camLeft - 500f || x > camRight + 500f) {
                bullets.removeIndex(i);
            }
        }
    }

    // Dibuja todas las balas
    public void draw(SpriteBatch batch) {
        for (int i = 0; i < bullets.size; i++) {
            bullets.get(i).draw(batch);
        }
    }

    // Devuelve la lista de balas para colisiones
    public Array<Bullet> getBullets() {
        return bullets;
    }

    // Borra todas las balas activas
    public void clear() {
        bullets.clear();
    }
}
