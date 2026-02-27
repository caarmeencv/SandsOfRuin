package com.carmen.mijuego.projectiles;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import com.carmen.mijuego.enemies.Tank;

public class TankBulletSystem {

    // Tamaño de la bala y velocidad horizontal
    private static final float W = 90f;
    private static final float H = 45f;
    private static final float SPEED = 700f;

    // Textura que usan todas las balas del tanque
    private final Texture tex;

    // Lista dinámica con todas las balas activas
    private final Array<Bullet> bullets = new Array<>();

    // Constructor que recibe la textura de la bala
    public TankBulletSystem(Texture bulletTexture) {
        tex = bulletTexture;
    }

    // Se llama cada frame para actualizar disparos
    public void update(float delta, float camLeft, float camRight, Array<Tank> tanks) {

        // Recorre todos los tanques activos en el nivel
        for (int i = 0; i < tanks.size; i++) {

            Tank t = tanks.get(i);

            // Si el tanque está muerto, destruyéndose o ya desaparecido, no dispara
            if (t.isDead() || t.isDestroying() || t.isGone()) continue;

            // Si todavía no puede disparar según su cooldown, tampoco
            if (!t.canShoot(delta)) continue;

            boolean right = t.isFacingRight();

            // Posición del cañón del tanque
            float muzzleX = t.getMuzzleX();

            float x = muzzleX;

            // Si dispara hacia la izquierda, ajusto la posición
            if (!right) x -= W;

            // Velocidad según dirección
            float velX = -SPEED;
            if (right) velX = SPEED;

            // Ajusto un poco la altura para que salga bien visualmente
            float y = t.getMuzzleY() - (H * 0.35f);

            // Creo la bala y la añado a la lista
            bullets.add(new Bullet(tex, x, y, velX, W, H));
        }

        // Actualización de todas las balas activas
        for (int i = bullets.size - 1; i >= 0; i--) {

            Bullet b = bullets.get(i);
            b.update(delta);

            float x = b.getX();

            // Si la bala está muerta o demasiado lejos de la cámara se elimina
            if (!b.isAlive() || x < camLeft - 400f || x > camRight + 400f) {
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

    // Devuelve la lista de balas para comprobar colisiones
    public Array<Bullet> getBullets() {
        return bullets;
    }

    // Elimina todas las balas activas
    public void clear() {
        bullets.clear();
    }
}
