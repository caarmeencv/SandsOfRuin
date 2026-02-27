package com.carmen.mijuego.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ParallaxPyramid {

    // Textura de la pared interior de la pirámide
    private Texture wallTex;

    // Textura del suelo interior
    private Texture groundTex;

    // Factor de movimiento de la pared
    // Cuanto más pequeño, más sensación de profundidad
    private float wallFactor;

    // Factor de movimiento del suelo
    private float groundFactor;

    // Constructor que recibe las texturas y sus factores de parallax
    public ParallaxPyramid(Texture wallTex, Texture groundTex, float wallFactor, float groundFactor) {
        this.wallTex = wallTex;
        this.groundTex = groundTex;
        this.wallFactor = wallFactor;
        this.groundFactor = groundFactor;
    }

    // Método que dibuja el parallax dentro de la pirámide
    public void render(SpriteBatch batch, OrthographicCamera camera, float viewportW, float viewportH) {

        // Calculamos el borde izquierdo visible según la cámara
        float camLeft = camera.position.x - viewportW * 0.5f;

        // Dibujamos la pared con su factor
        drawTiled(batch, wallTex, camLeft, viewportW, viewportH, wallFactor);

        // Dibujamos el suelo con su factor
        drawTiled(batch, groundTex, camLeft, viewportW, viewportH, groundFactor);
    }

    // Dibuja una textura repetida horizontalmente
    // factor controla cuánto se mueve respecto a la cámara
    private void drawTiled(SpriteBatch batch,
                           Texture tex,
                           float camLeft,
                           float viewportW,
                           float viewportH,
                           float factor) {

        // Ancho real de la textura
        float texW = tex.getWidth();

        // Calculamos cuánto debe desplazarse esta capa
        float layerOffset = camLeft * factor;

        // Ajustamos el desplazamiento para que repita correctamente
        float mod = layerOffset % texW;

        // Punto inicial desde donde empezamos a dibujar
        float startX = camLeft - mod;

        // Dibujamos la textura repetida hasta cubrir toda la pantalla
        for (float x = startX; x < camLeft + viewportW + texW; x += texW) {
            batch.draw(tex, x, 0f, texW, viewportH);
        }
    }
}
