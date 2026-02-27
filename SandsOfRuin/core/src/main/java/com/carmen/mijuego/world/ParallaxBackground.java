package com.carmen.mijuego.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ParallaxBackground {

    /**
     * Una capa del parallax.
     * - tex: textura de la capa
     * - factor: cuánto se mueve respecto al movimiento de la cámara
     *   (0 = casi quieta, 1 = se mueve igual que la cámara)
     * - y: posición vertical donde se dibuja
     * - height: altura a la que se escala la textura en el mundo
     * - repeat: si la capa se repite en horizontal
     * - ignoreSpeedMul: si ignora el speedMul (útil para capa "near" que va 1:1 con la cámara)
     */
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

    // Cámara del mundo (de donde sacamos camLeft)
    private final OrthographicCamera camera;

    // Tamaño del viewport en coordenadas del mundo
    private final float worldW;
    private final float worldH;

    // Cielo (fondo) que siempre llena toda la pantalla
    private final Texture sky;

    // Capas del parallax
    private final Layer[] layers;

    /**
     * Multiplicador de velocidad global del parallax.
     * Ej: 0.70f para que el fondo se mueva un 70% del movimiento real.
     */
    private float speedMul = 1f;

    /**
     * Scroll acumulado que usamos para las capas que NO ignoran speedMul.
     * Esto hace que el parallax sea suave incluso si la cámara se mueve con interpolación.
     */
    private float parallaxScroll = 0f;

    /**
     * Guardamos el camLeft del frame anterior para calcular cuánto se movió la cámara.
     * Empieza como NaN para detectar "primer frame".
     */
    private float prevCamLeft = Float.NaN;

    /**
     * Constructor:
     * Recibe:
     * - cámara y viewport (para worldW/worldH)
     * - sky (fondo)
     * - arrays paralelos con las capas (textura, factor, y, height, repeat, ignoreSpeedMul)
     */
    public ParallaxBackground(
        OrthographicCamera camera,
        Viewport viewport,
        Texture sky,
        Texture[] textures,
        float[] factors,
        float[] ys,
        float[] heights,
        boolean[] repeat,
        boolean[] ignoreSpeedMul) {

        // Seguridad: todos los arrays deben tener misma longitud
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

        // Construimos el array de capas
        layers = new Layer[textures.length];
        for (int i = 0; i < textures.length; i++) {
            layers[i] = new Layer(textures[i], factors[i], ys[i], heights[i], repeat[i], ignoreSpeedMul[i]);
        }
    }

    public void setSpeedMul(float speedMul) {
        this.speedMul = speedMul;
    }

    /**
     * Reset por si reinicias el nivel y quieres que el fondo vuelva a 0.
     */
    public void reset() {
        parallaxScroll = 0f;
        prevCamLeft = Float.NaN;
    }

    /**
     * Dibuja el parallax completo:
     * 1) Calcula camLeft/camRight
     * 2) Calcula cuánto se movió la cámara desde el frame anterior
     * 3) Acumula parallaxScroll
     * 4) Dibuja sky
     * 5) Dibuja cada capa con su factor
     */
    public void render(SpriteBatch batch) {
        float camLeft = camera.position.x - worldW * 0.5f;
        float camRight = camLeft + worldW;

        // Primer frame: inicializamos prevCamLeft
        if (Float.isNaN(prevCamLeft)) prevCamLeft = camLeft;

        // Movimiento real de la cámara este frame
        float camDelta = camLeft - prevCamLeft;
        prevCamLeft = camLeft;

        // Acumulamos scroll (con speedMul)
        parallaxScroll += camDelta * speedMul;

        // Fondo base: el cielo siempre cubre todo
        batch.draw(sky, camLeft, 0f, worldW, worldH);

        // Capas extra
        for (int i = 0; i < layers.length; i++) {
            Layer layer = layers[i];

            // baseScroll:
            // - si ignoreSpeedMul: usamos camLeft (se mueve "real" con la cámara)
            // - si no: usamos parallaxScroll (afectado por speedMul)
            float baseScroll = layer.ignoreSpeedMul ? camLeft : parallaxScroll;

            // Offset final de la capa según factor (parallax)
            float layerOffset = baseScroll * layer.factor;

            // Si se repite o no, en este código se usa la MISMA función
            // porque drawSpan ya repite rellenando la pantalla.
            drawSpan(batch, layer.tex, camLeft, camRight, layerOffset, layer.y, layer.height);
        }
    }

    /**
     * Dibuja una textura cubriendo desde camLeft hasta camRight, repitiéndola
     * horizontalmente a trozos de worldW (ancho del viewport).
     *
     * layerOffset define el desplazamiento (scroll) de esa capa.
     */
    private void drawSpan(SpriteBatch batch, Texture tex,
                          float camLeft, float camRight,
                          float layerOffset, float y, float height) {

        // Normalizamos el offset para que esté en [0..worldW)
        float offset = layerOffset % worldW;
        if (offset < 0f) offset += worldW;

        // Empezamos dibujando lo más a la izquierda posible para cubrir pantalla
        float x = camLeft - offset;
        while (x > camLeft) x -= worldW;

        // Dibujamos bloques hasta pasar el borde derecho
        while (x < camRight) {
            batch.draw(tex, x, y, worldW, height);
            x += worldW;
        }

        // Uno extra por seguridad para cubrir huecos por redondeo
        batch.draw(tex, x, y, worldW, height);
    }
}
