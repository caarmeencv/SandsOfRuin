package com.carmen.mijuego.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ParallaxBackground {

    private static class Layer {
        final Texture tex;
        final float factor; // 0..1 (más pequeño = más lejos)

        Layer(Texture tex, float factor) {
            this.tex = tex;
            this.factor = factor;
        }
    }

    private final OrthographicCamera camera;
    private final float worldW;
    private final float worldH;

    private final Texture sky;      // fondo fijo
    private final Layer[] layers;   // capas con parallax

    // ✅ Multiplicador global para hacer el fondo más lento/rápido sin tocar factores
    private float speedMul = 1f;

    public ParallaxBackground(OrthographicCamera camera, Viewport viewport,
                              Texture sky,
                              Texture[] textures, float[] factors) {

        if (textures.length != factors.length) {
            throw new IllegalArgumentException("textures y factors deben tener la misma longitud");
        }

        this.camera = camera;
        this.worldW = viewport.getWorldWidth();
        this.worldH = viewport.getWorldHeight();

        this.sky = sky;

        layers = new Layer[textures.length];
        for (int i = 0; i < textures.length; i++) {
            layers[i] = new Layer(textures[i], factors[i]);
        }
    }

    public void setSpeedMul(float speedMul) {
        this.speedMul = speedMul;
    }

    public void render(SpriteBatch batch, float scrollX) {
        float camLeft = camera.position.x - worldW * 0.5f;

        // Cielo fijo (sigue cámara)
        batch.draw(sky, camLeft, 0f, worldW, worldH);

        // Capas con parallax tileadas
        for (Layer layer : layers) {
            float layerOffset = scrollX * layer.factor * speedMul;
            drawTiled(batch, layer.tex, camLeft, layerOffset);
        }
    }

    private void drawTiled(SpriteBatch batch, Texture tex, float camLeft, float layerOffset) {
        // offset dentro de [0, worldW)
        float offset = layerOffset % worldW;
        if (offset < 0) offset += worldW;

        float x0 = camLeft - offset;

        // ✅ 3 tiles para evitar huecos/cortes
        batch.draw(tex, x0,             0f, worldW, worldH);
        batch.draw(tex, x0 + worldW,    0f, worldW, worldH);
        batch.draw(tex, x0 + 2f*worldW, 0f, worldW, worldH);
    }
}
