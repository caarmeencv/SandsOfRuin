package com.carmen.mijuego.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ParallaxBackground {

    private static class Layer {
        final Texture tex;
        final float factor;   // 0..1.. (1 = misma velocidad que el mundo)
        final float y;
        final float height;
        final boolean ignoreSpeedMul; // ✅ para que el suelo no se ralentice

        Layer(Texture tex, float factor, float y, float height, boolean ignoreSpeedMul) {
            this.tex = tex;
            this.factor = factor;
            this.y = y;
            this.height = height;
            this.ignoreSpeedMul = ignoreSpeedMul;
        }
    }

    private final OrthographicCamera camera;
    private final float worldW;
    private final float worldH;

    private final Texture sky;
    private final Layer[] layers;

    private float speedMul = 1f;

    public ParallaxBackground(OrthographicCamera camera,
                              Viewport viewport,
                              Texture sky,
                              Texture[] textures,
                              float[] factors,
                              float[] ys,
                              float[] heights,
                              boolean[] ignoreSpeedMul) {

        if (textures.length != factors.length ||
            textures.length != ys.length ||
            textures.length != heights.length ||
            textures.length != ignoreSpeedMul.length) {
            throw new IllegalArgumentException("arrays con distinta longitud");
        }

        this.camera = camera;
        this.worldW = viewport.getWorldWidth();
        this.worldH = viewport.getWorldHeight();
        this.sky = sky;

        layers = new Layer[textures.length];
        for (int i = 0; i < textures.length; i++) {
            layers[i] = new Layer(textures[i], factors[i], ys[i], heights[i], ignoreSpeedMul[i]);
        }
    }

    public void setSpeedMul(float speedMul) {
        this.speedMul = speedMul;
    }

    public void render(SpriteBatch batch, float scrollX) {
        float camLeft = camera.position.x - worldW * 0.5f;

        batch.draw(sky, camLeft, 0f, worldW, worldH);

        for (Layer layer : layers) {
            float mul = layer.ignoreSpeedMul ? 1f : speedMul;
            float layerOffset = scrollX * layer.factor * mul;
            drawTiled(batch, layer.tex, camLeft, layerOffset, layer.y, layer.height);
        }
    }

    private void drawTiled(SpriteBatch batch,
                           Texture tex,
                           float camLeft,
                           float layerOffset,
                           float y,
                           float height) {

        float offset = layerOffset % worldW;
        if (offset < 0) offset += worldW;

        float x0 = camLeft - offset;

        batch.draw(tex, x0,               y, worldW, height);
        batch.draw(tex, x0 + worldW,      y, worldW, height);
        batch.draw(tex, x0 + 2f * worldW, y, worldW, height);
    }
}
