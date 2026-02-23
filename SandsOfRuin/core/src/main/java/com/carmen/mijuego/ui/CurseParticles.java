package com.carmen.mijuego.ui;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class CurseParticles {

    private static class P {
        float angle;
        float radius;
        float angVel;
        float life, maxLife;
        float rise;
        float size;
        float alpha;
        float r, g, b;
    }

    private final Texture pixel;
    private final Array<P> parts = new Array<>();

    // Ajustes
    private float emitAcc = 0f;
    private float emitRate = 90f; // partículas/seg
    private float orbitBase = 55f;
    private float orbitVar  = 35f;

    public CurseParticles() {
        // ✅ textura 1x1 blanca
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(1, 1, 1, 1);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();
    }

    /**
     * @param active si está maldita -> emite; si no, solo deja morir
     */
    public void update(float delta, float centerX, float centerY, boolean active) {

        // emitir
        if (active) {
            emitAcc += delta * emitRate;
            while (emitAcc >= 1f) {
                emitAcc -= 1f;
                spawn(centerX, centerY);
            }
        }

        // update + limpiar
        for (int i = parts.size - 1; i >= 0; i--) {
            P p = parts.get(i);

            p.life += delta;
            if (p.life >= p.maxLife) {
                parts.removeIndex(i);
                continue;
            }

            // órbita + subida suave
            p.angle += p.angVel * delta;
            p.rise += 22f * delta;

            // fade out
            float t = 1f - (p.life / p.maxLife);
            p.alpha = MathUtils.clamp(t, 0f, 1f);
        }
    }

    private void spawn(float cx, float cy) {
        P p = new P();

        // humo morado + chispas
        boolean spark = MathUtils.randomBoolean(0.25f);

        p.angle = MathUtils.random(0f, 360f);
        p.radius = orbitBase + MathUtils.random(-orbitVar, orbitVar);
        p.angVel = MathUtils.random(120f, 280f) * (MathUtils.randomBoolean() ? 1f : -1f);

        p.life = 0f;
        p.maxLife = spark ? MathUtils.random(0.25f, 0.55f) : MathUtils.random(0.8f, 1.4f);

        p.rise = 0f;

        p.size = spark ? MathUtils.random(3f, 6f) : MathUtils.random(8f, 16f);

        // morado (humo) o chispa (más clara)
        if (spark) {
            p.r = 0.95f; p.g = 0.80f; p.b = 1.00f;
        } else {
            p.r = 0.65f; p.g = 0.25f; p.b = 0.95f;
        }

        parts.add(p);
    }

    public void draw(SpriteBatch batch, float centerX, float centerY) {

        float prevR = batch.getColor().r;
        float prevG = batch.getColor().g;
        float prevB = batch.getColor().b;
        float prevA = batch.getColor().a;

        for (int i = 0; i < parts.size; i++) {
            P p = parts.get(i);

            float rad = p.radius;
            float x = centerX + MathUtils.cosDeg(p.angle) * rad;
            float y = centerY + MathUtils.sinDeg(p.angle) * rad + p.rise;

            batch.setColor(p.r, p.g, p.b, 0.55f * p.alpha);
            batch.draw(pixel, x - p.size / 2f, y - p.size / 2f, p.size, p.size);
        }

        batch.setColor(prevR, prevG, prevB, prevA);
    }

    public void dispose() {
        pixel.dispose();
    }
}
