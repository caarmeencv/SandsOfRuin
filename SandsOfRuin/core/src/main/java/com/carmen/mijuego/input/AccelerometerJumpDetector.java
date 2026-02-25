package com.carmen.mijuego.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Detecta un "salto" agitando el móvil hacia delante usando el acelerómetro.
 * Devuelve true SOLO 1 frame (como un "just pressed").
 */
public class AccelerometerJumpDetector {

    // Ajusta sensibilidad
    private static final float THRESHOLD = 12.5f;
    private static final float COOLDOWN  = 0.35f;

    private float cooldownTimer = 0f;

    // suavizado anti-ruido
    private float smooth = 0f;
    private static final float SMOOTH_ALPHA = 0.85f;

    public void reset() {
        cooldownTimer = 0f;
        smooth = 0f;
    }

    public boolean updateAndConsume(float delta) {

        cooldownTimer -= delta;
        if (cooldownTimer < 0f) cooldownTimer = 0f;

        // Solo Android + acelerómetro
        if (Gdx.app.getType() != com.badlogic.gdx.Application.ApplicationType.Android) return false;
        if (!Gdx.input.isPeripheralAvailable(Input.Peripheral.Accelerometer)) return false;

        float ay = Gdx.input.getAccelerometerY();
        float az = Gdx.input.getAccelerometerZ();

        // señal fuerte (según orientación landscape varía, por eso combinamos)
        float forwardSignal = Math.abs(ay) + Math.abs(az) * 0.65f;

        smooth = (SMOOTH_ALPHA * smooth) + ((1f - SMOOTH_ALPHA) * forwardSignal);

        if (cooldownTimer <= 0f && smooth >= THRESHOLD) {
            cooldownTimer = COOLDOWN;
            return true;
        }

        return false;
    }
}
