package com.carmen.mijuego.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Parallax 2D tile infinito en X, basado en cámara (SUAVE siempre).
 * - wallFactor  : 0..1 (más lento)
 * - groundFactor: 0..1 (casi igual al mundo)
 */
public class ParallaxPyramid {

    private final Texture wallTex;
    private final Texture groundTex;
    private final float wallFactor;
    private final float groundFactor;

    public ParallaxPyramid(Texture wallTex, Texture groundTex, float wallFactor, float groundFactor) {
        this.wallTex = wallTex;
        this.groundTex = groundTex;
        this.wallFactor = wallFactor;
        this.groundFactor = groundFactor;
    }

    public void render(SpriteBatch batch, OrthographicCamera camera, float viewportW, float viewportH) {
        float camLeft = camera.position.x - viewportW / 2f;

        drawTiled(batch, wallTex, camLeft, viewportW, viewportH, wallFactor);
        drawTiled(batch, groundTex, camLeft, viewportW, viewportH, groundFactor);
    }

    private void drawTiled(SpriteBatch batch,
                           Texture tex,
                           float camLeft,
                           float viewportW,
                           float viewportH,
                           float factor) {

        float texW = tex.getWidth();

        // Offset del layer usando la cámara (siempre suave)
        float layerOffset = camLeft * factor;

        // inicio alineado al tile
        float startX = camLeft - (layerOffset % texW);

        for (float x = startX; x < camLeft + viewportW + texW; x += texW) {
            batch.draw(tex, x, 0, texW, viewportH);
        }
    }
}
