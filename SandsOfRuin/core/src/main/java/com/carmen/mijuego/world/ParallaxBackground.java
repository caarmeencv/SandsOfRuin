package com.carmen.mijuego.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ParallaxBackground {

    private static class Layer {
        final Texture tex;
        final float factor;
        final float y;
        final float height;
        final boolean repeat;
        final boolean ignoreSpeedMul;

        Layer(Texture tex, float factor, float y, float height, boolean repeat, boolean ignoreSpeedMul) {
            this.tex = tex;
            this.factor = factor;
            this.y = y;
            this.height = height;
            this.repeat = repeat;
            this.ignoreSpeedMul = ignoreSpeedMul;
        }
    }

    private final OrthographicCamera camera;
    private final float worldW;
    private final float worldH;

    private final Texture sky;
    private final Layer[] layers;

    // ✅ speedMul ahora escala el MOVIMIENTO, no la posición absoluta
    private float speedMul = 1f;

    // ✅ parallaxScroll acumula cuánto “ha avanzado” el parallax (estable, sin saltos)
    private float parallaxScroll = 0f;
    private float prevCamLeft = Float.NaN;

    public ParallaxBackground(OrthographicCamera camera,
                              Viewport viewport,
                              Texture sky,
                              Texture[] textures,
                              float[] factors,
                              float[] ys,
                              float[] heights,
                              boolean[] repeat,
                              boolean[] ignoreSpeedMul) {

        if (textures.length != factors.length ||
            textures.length != ys.length ||
            textures.length != heights.length ||
            textures.length != repeat.length ||
            textures.length != ignoreSpeedMul.length) {
            throw new IllegalArgumentException("arrays con distinta longitud");
        }

        this.camera = camera;
        this.worldW = viewport.getWorldWidth();
        this.worldH = viewport.getWorldHeight();
        this.sky = sky;

        layers = new Layer[textures.length];
        for (int i = 0; i < textures.length; i++) {
            layers[i] = new Layer(textures[i], factors[i], ys[i], heights[i], repeat[i], ignoreSpeedMul[i]);
        }
    }

    public void setSpeedMul(float speedMul) {
        this.speedMul = speedMul;
    }

    /** Útil si cambias de pantalla o reinicias nivel */
    public void reset() {
        parallaxScroll = 0f;
        prevCamLeft = Float.NaN;
    }

    public void render(SpriteBatch batch) {
        float camLeft  = camera.position.x - worldW * 0.5f;
        float camRight = camLeft + worldW;

        // Inicialización del primer frame
        if (Float.isNaN(prevCamLeft)) {
            prevCamLeft = camLeft;
        }

        // ✅ delta real de cámara (lo que se movió este frame)
        float camDelta = camLeft - prevCamLeft;
        prevCamLeft = camLeft;

        // ✅ acumulamos scroll del parallax usando speedMul
        parallaxScroll += camDelta * speedMul;

        // cielo
        batch.draw(sky, camLeft, 0f, worldW, worldH);

        for (Layer layer : layers) {
            // Si ignora speedMul (por ejemplo para que el suelo siga), usamos movimiento real de cámara:
            // - opción A: que ignoreSpeedMul sea “no se congela”
            // - opción B: si quieres que se congele TODO en la cinemática, pon ignoreSpeedMul=false en todas.
            float baseScroll = layer.ignoreSpeedMul ? camLeft : parallaxScroll;

            float layerOffset = baseScroll * layer.factor;

            if (layer.repeat) {
                drawTiledInfinite(batch, layer.tex, camLeft, camRight, layerOffset, layer.y, layer.height);
            } else {
                drawNonRepeating(batch, layer.tex, camLeft, camRight, layerOffset, layer.y, layer.height);
            }
        }
    }

    private void drawTiledInfinite(SpriteBatch batch,
                                   Texture tex,
                                   float camLeft,
                                   float camRight,
                                   float layerOffset,
                                   float y,
                                   float height) {

        float offset = layerOffset % worldW;
        if (offset < 0) offset += worldW;

        float x = camLeft - offset;

        while (x > camLeft) x -= worldW;

        while (x < camRight) {
            batch.draw(tex, x, y, worldW, height);
            x += worldW;
        }

        batch.draw(tex, x, y, worldW, height);
    }

    private void drawNonRepeating(SpriteBatch batch,
                                  Texture tex,
                                  float camLeft,
                                  float camRight,
                                  float layerOffset,
                                  float y,
                                  float height) {

        float x = camLeft - layerOffset;

        while (x > camLeft) x -= worldW;

        while (x < camRight) {
            batch.draw(tex, x, y, worldW, height);
            x += worldW;
        }

        batch.draw(tex, x, y, worldW, height);
    }
}
