package com.carmen.mijuego.ui;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class CurseParticles {

    // Partícula individual (datos de estado)
    private static class P {
        float angle;     // ángulo alrededor del personaje (órbita)
        float radius;    // distancia al centro (órbita)
        float angVel;    // velocidad angular (grados/seg)
        float life;      // vida actual
        float maxLife;   // duración total
        float rise;      // desplazamiento vertical acumulado (sube)
        float size;      // tamaño del cuadradito
        float alpha;     // transparencia calculada
        float r, g, b;   // color
    }

    // Textura de 1x1 píxel (para dibujar cuadrados coloreados)
    private Texture pixel;

    // Lista de partículas vivas
    private Array<P> parts;

    // Acumulador y parámetros de emisión
    private float emitAcc;    // acumula "partículas pendientes" según delta
    private float emitRate;   // partículas por segundo (aprox)
    private float orbitBase;  // radio base de órbita
    private float orbitVar;   // variación aleatoria del radio

    public CurseParticles() {

        // Crea una textura mínima 1x1 blanca (usamos tint con setColor)
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(1f, 1f, 1f, 1f);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();

        parts = new Array<P>();

        // Ajustes iniciales
        emitAcc = 0f;

        // 90 partículas/segundo cuando está activo
        emitRate = 90f;

        // Órbita alrededor del personaje
        orbitBase = 55f;
        orbitVar = 35f;
    }

    /**
     * Actualiza el sistema de partículas.
     * @param delta    tiempo entre frames
     * @param centerX  centro donde orbitan (normalmente Ayla)
     * @param centerY  centro donde orbitan
     * @param active   si la maldición está activa (si no, no emite nuevas)
     */
    public void update(float delta, float centerX, float centerY, boolean active) {

        // 1) Emisión: solo si active == true
        // emitAcc va acumulando y spawnea 1 partícula cada vez que pasa de 1
        if (active) {
            emitAcc += delta * emitRate;
            while (emitAcc >= 1f) {
                emitAcc -= 1f;
                spawn();
            }
        }

        // 2) Simulación: recorre partículas y actualiza su vida/movimiento
        for (int i = parts.size - 1; i >= 0; i--) {
            P p = parts.get(i);

            // aumenta el tiempo de vida
            p.life += delta;

            // si se acabó su vida -> se elimina
            if (p.life >= p.maxLife) {
                parts.removeIndex(i);
                continue;
            }

            // gira alrededor del centro
            p.angle += p.angVel * delta;

            // sube poco a poco (efecto “fantasmal”)
            p.rise += 22f * delta;

            // alpha: se desvanece linealmente (1 -> 0)
            float t = 1f - (p.life / p.maxLife);
            p.alpha = MathUtils.clamp(t, 0f, 1f);
        }
    }

    /**
     * Crea una partícula nueva con valores aleatorios.
     * Hay 2 tipos: spark (chispas pequeñas y rápidas) y normal (más grandes y duraderas).
     */
    private void spawn() {
        P p = new P();

        // 25% de probabilidad de ser "spark"
        boolean spark = MathUtils.randomBoolean(0.25f);

        // posición inicial: ángulo random y radio random dentro de un rango
        p.angle = MathUtils.random(0f, 360f);
        p.radius = orbitBase + MathUtils.random(-orbitVar, orbitVar);

        // dirección aleatoria de giro
        float sign = MathUtils.randomBoolean() ? 1f : -1f;

        // velocidad angular random (más alta = más rápido)
        p.angVel = MathUtils.random(120f, 280f) * sign;

        p.life = 0f;

        // duración: sparks duran menos
        if (spark) p.maxLife = MathUtils.random(0.25f, 0.55f);
        else       p.maxLife = MathUtils.random(0.8f, 1.4f);

        p.rise = 0f;

        // tamaño: sparks son más pequeñas
        if (spark) p.size = MathUtils.random(3f, 6f);
        else       p.size = MathUtils.random(8f, 16f);

        // colores: sparks más claras, normales más moradas
        if (spark) {
            p.r = 0.95f; p.g = 0.80f; p.b = 1.00f;
        } else {
            p.r = 0.65f; p.g = 0.25f; p.b = 0.95f;
        }

        p.alpha = 1f;

        parts.add(p);
    }

    /**
     * Dibuja las partículas.
     * Se usa el pixel 1x1 y se escala al tamaño de cada partícula.
     * El color se aplica con batch.setColor().
     */
    public void draw(SpriteBatch batch, float centerX, float centerY) {

        // Guardamos el color actual del batch para restaurarlo después
        float prevR = batch.getColor().r;
        float prevG = batch.getColor().g;
        float prevB = batch.getColor().b;
        float prevA = batch.getColor().a;

        // Dibuja cada partícula en su posición orbitando el centro
        for (int i = 0; i < parts.size; i++) {
            P p = parts.get(i);

            // órbita: cos/sin del ángulo por el radio + subida vertical
            float x = centerX + MathUtils.cosDeg(p.angle) * p.radius;
            float y = centerY + MathUtils.sinDeg(p.angle) * p.radius + p.rise;

            // alpha final: se multiplica por 0.55 para que no “tape” demasiado
            batch.setColor(p.r, p.g, p.b, 0.55f * p.alpha);

            // dibuja el cuadrado centrado en (x,y)
            batch.draw(pixel,
                x - p.size * 0.5f,
                y - p.size * 0.5f,
                p.size,
                p.size
            );
        }

        // Restauramos el color original del batch
        batch.setColor(prevR, prevG, prevB, prevA);
    }

    // Libera la textura del píxel (llamar cuando se destruya la pantalla/juego)
    public void dispose() {
        pixel.dispose();
    }
}
