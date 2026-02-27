package com.carmen.mijuego.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.Viewport;

public class WorldController {

    // Velocidad de avance hacia la derecha
    private static final float SCROLL_FWD = 320f;

    // Velocidad de retroceso hacia la izquierda
    private static final float SCROLL_BACK = 260f;

    // Distancia total que retrocede al recibir knockback
    private static final float KNOCK_DIST = 140f;

    // Velocidad a la que se aplica el knockback
    private static final float KNOCK_SPEED = 900f;

    // Posición horizontal acumulada del mundo
    private float scrollX;

    // Cantidad de distancia que queda por retroceder en knockback
    private float knockRemaining;

    // Indica si el mundo está congelado (no se puede mover más)
    private boolean frozen;

    // Actualiza el desplazamiento del mundo según input y delta time
    public void update(float delta, boolean left, boolean right, float viewportW) {

        // Si está congelado no permitimos movimiento
        if (frozen) return;

        // Si hay knockback pendiente, se aplica primero
        if (knockRemaining > 0f) {

            // Calculamos cuánto se retrocede en este frame
            float step = Math.min(KNOCK_SPEED * delta, knockRemaining);

            // Movemos el scroll hacia atrás
            scrollX = Math.max(0f, scrollX - step);

            // Restamos lo ya aplicado
            knockRemaining -= step;

        } else {

            // Movimiento normal hacia la derecha
            if (right) scrollX += SCROLL_FWD * delta;

            // Movimiento hacia la izquierda más lento
            if (left) scrollX -= SCROLL_BACK * delta;

            // Evitamos que el scroll sea negativo
            if (scrollX < 0f) scrollX = 0f;
        }

        // Comprobamos si hay que congelar el mundo
        clampAndFreezeIfNeeded(viewportW);
    }

    // Limita el scroll y congela el mundo si se llega al límite final
    private void clampAndFreezeIfNeeded(float viewportW) {

        // Límite máximo permitido antes del freeze
        float limitScroll = LevelConfig.FREEZE_CAM_RIGHT - viewportW;

        // Si la cámara alcanza el punto de congelación
        if (scrollX + viewportW >= LevelConfig.FREEZE_CAM_RIGHT) {

            // Ajustamos exactamente al límite permitido
            scrollX = Math.max(0f, limitScroll);

            // Activamos congelación
            frozen = true;

            // Cancelamos cualquier knockback pendiente
            knockRemaining = 0f;
        }
    }

    // Aplica efecto de retroceso cuando el jugador recibe daño
    public void applyKnockback() {

        // Solo si el mundo no está congelado
        if (!frozen) knockRemaining = KNOCK_DIST;
    }

    // Actualiza la posición de la cámara según el scroll actual
    public void updateCamera(OrthographicCamera cam, Viewport vp, float delta, boolean moving) {

        // Si no está congelado volvemos a comprobar límites
        if (!frozen) clampAndFreezeIfNeeded(vp.getWorldWidth());

        // La cámara siempre debe centrarse respecto al scroll
        float targetX = scrollX + vp.getWorldWidth() * 0.5f;

        // Si el mundo se está moviendo, aplicamos suavizado
        if (!frozen && (moving || knockRemaining > 0f)) {

            // Interpolación suave hacia la posición objetivo
            cam.position.x += (targetX - cam.position.x) * 10f * delta;

        } else {

            // Si está congelado o quieto, colocamos directamente
            cam.position.x = targetX;
        }

        // Actualizamos la cámara
        cam.update();
    }

    // Devuelve el desplazamiento actual del mundo
    public float getScrollX() {
        return scrollX;
    }

    // Indica si el mundo está congelado
    public boolean isFrozen() {
        return frozen;
    }

    // Reinicia el estado del controlador
    public void reset() {

        scrollX = 0f;
        knockRemaining = 0f;
        frozen = false;
    }
}
