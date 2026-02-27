package com.carmen.mijuego.projectiles;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Bullet {

    // Posición actual de la bala en el mundo
    private float x, y;

    // Velocidad horizontal, tamaño y textura
    private final float velX, width, height;
    private final Texture texture;

    // Rectángulo para colisiones
    private final Rectangle bounds = new Rectangle();

    // Si está viva o ya debe eliminarse
    private boolean alive = true;

    // Daño que hace la bala
    private final int damage;

    // Constructor normal que crea una bala con daño 1
    public Bullet(Texture texture, float startX, float startY, float velX, float width, float height) {
        this(texture, startX, startY, velX, width, height, 1);
    }

    // Constructor completo donde puedo indicar también el daño
    public Bullet(Texture texture, float startX, float startY, float velX, float width, float height, int damage) {

        // Guardo la textura
        this.texture = texture;

        // Posición inicial
        x = startX;
        y = startY;

        // Velocidad horizontal
        this.velX = velX;

        // Tamaño visual
        this.width = width;
        this.height = height;

        // Daño que hará al impactar
        this.damage = damage;

        // Inicializo el rectángulo de colisión en la posición inicial
        bounds.set(x, y, width, height);
    }

    // Se llama cada frame para mover la bala
    public void update(float delta) {

        // Movimiento horizontal en función del tiempo
        x += velX * delta;

        // Actualizo el rectángulo de colisión
        bounds.set(x, y, width, height);
    }

    // Dibuja la bala en pantalla
    public void draw(SpriteBatch batch) {

        // Si la velocidad es positiva, la bala va hacia la derecha
        if (velX >= 0f) {
            batch.draw(texture, x, y, width, height);
        }
        // Si la velocidad es negativa, la bala va hacia la izquierda
        // La dibujo invertida usando ancho negativo
        else {
            batch.draw(texture, x + width, y, -width, height);
        }
    }

    // Devuelve el rectángulo para comprobar colisiones
    public Rectangle getBounds() {
        return bounds;
    }

    // Dice si la bala sigue activa
    public boolean isAlive() {
        return alive;
    }

    // Marca la bala como muerta
    public void kill() {
        alive = false;
    }

    // Devuelve la posición X
    public float getX() {
        return x;
    }

    // Devuelve la velocidad horizontal
    public float getVelX() {
        return velX;
    }

    // Devuelve el daño que hace esta bala
    public int getDamage() {
        return damage;
    }
}
