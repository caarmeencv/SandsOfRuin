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
    private final Viewport viewport;
    private final float worldW;
    private final float worldH;

    private final Texture sky;      // fondo fijo (sin parallax)
    private final Layer[] layers;   // capas con parallax

    public ParallaxBackground(OrthographicCamera camera, Viewport viewport,
                              Texture sky,
                              Texture[] textures, float[] factors) {

        if (textures.length != factors.length) {
            throw new IllegalArgumentException("textures y factors deben tener la misma longitud");
        }

        this.camera = camera;
        this.viewport = viewport;
        this.worldW = viewport.getWorldWidth();
        this.worldH = viewport.getWorldHeight();

        this.sky = sky;

        layers = new Layer[textures.length];
        for (int i = 0; i < textures.length; i++) {
            layers[i] = new Layer(textures[i], factors[i]);
        }
    }

    public void render(SpriteBatch batch, float scrollX) {
        float camLeft = camera.position.x - worldW * 0.5f;

        // Cielo fijo (no hace parallax, solo “sigue” la cámara)
        batch.draw(sky, camLeft, 0f, worldW, worldH);

        // Capas con parallax tileadas
        for (Layer layer : layers) {
            drawTiled(batch, layer.tex, camLeft, scrollX * layer.factor);
        }
    }

    private void drawTiled(SpriteBatch batch, Texture tex, float camLeft, float layerX) {
        // layerX es el desplazamiento horizontal de ESA capa en coordenadas mundo
        float start = layerX % worldW;
        if (start > 0) start -= worldW;

        batch.draw(tex, camLeft - start, 0f, worldW, worldH);
        batch.draw(tex, camLeft - start + worldW, 0f, worldW, worldH);
    }
}
