package com.carmen.mijuego.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Cactus {

    // Posición del cactus en el mundo
    // x es la posición horizontal y y es la altura del suelo donde se apoya
    private final float x;
    private final float y;

    // Imagen del cactus
    private final Texture texture;

    // Tamaño real que va a ocupar el cactus en el juego
    // width se calcula usando la proporción de la imagen para que no se deforme
    private final float width;
    private final float height;

    // Este ajuste solo se usa para dibujar el cactus un poco más abajo
    // Es solo visual para que parezca bien apoyado en el suelo
    private static final float FOOT_OFFSET = 14f;

    // Estas variables sirven para hacer el hitbox más pequeño que la imagen
    // Así la colisión es más justa y no te quita vida cuando parece que no lo tocaste
    private static final float HIT_PAD_L = 18f;
    private static final float HIT_PAD_R = 18f;

    // Aquí recorto la base porque a veces la parte de abajo del cactus no debería ser tan peligrosa
    private static final float HIT_PAD_BOTTOM = 28f;

    // Aquí recorto la parte de arriba para que no sea tan fácil chocarse con la punta si apenas rozas
    private static final float HIT_PAD_TOP = 25f;

    // Este rectángulo es la caja de colisión que se usa para detectar si Ayla lo toca
    private final Rectangle bounds = new Rectangle();

    public Cactus(Texture texture, float xWorld, float groundY, float heightWorld) {

        // Guardo la textura y la posición
        this.texture = texture;
        this.x = xWorld;
        this.y = groundY;

        // La altura del cactus en el mundo la recibo como parámetro
        this.height = heightWorld;

        // Calculo el ancho usando la proporción original de la imagen
        // Esto evita que el cactus se vea estirado o aplastado
        float ratio = (float) texture.getWidth() / (float) texture.getHeight();
        this.width = this.height * ratio;

        // Creo el hitbox con los valores iniciales
        updateBounds();
    }

    private void updateBounds() {

        // Calculo la posición del hitbox con recortes para que sea más pequeño
        float hitX = x + HIT_PAD_L;
        float hitY = y + HIT_PAD_BOTTOM;

        // Calculo el tamaño del hitbox con recortes por izquierda, derecha, abajo y arriba
        float hitW = width - (HIT_PAD_L + HIT_PAD_R);
        float hitH = height - (HIT_PAD_BOTTOM + HIT_PAD_TOP);

        // Pongo un tamaño mínimo por seguridad para evitar hitbox demasiado pequeño o negativo
        if (hitW < 10f) {
            hitW = 10f;
        }

        if (hitH < 10f) {
            hitH = 10f;
        }

        // Actualizo el rectángulo de colisión
        bounds.set(hitX, hitY, hitW, hitH);
    }

    public void draw(SpriteBatch batch) {

        // Ajusto el dibujo para que el cactus quede bien apoyado visualmente
        float drawY = y - FOOT_OFFSET;

        // Dibujo la imagen en la pantalla
        batch.draw(texture, x, drawY, width, height);
    }

    public boolean isOffScreenLeft(float camLeft) {

        // Esto sirve para saber si el cactus ya quedó muy atrás de la cámara
        // Si ya está lejos por la izquierda, se puede borrar para ahorrar rendimiento
        return x + width < camLeft - 150f;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
