package com.carmen.mijuego.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class AccelerometerJumpDetector {

    // Este número es la fuerza mínima que tiene que detectar para considerar que se hizo el gesto de salto
    private static final float THRESHOLD = 12.5f;

    // Esto es un tiempo de espera para que no detecte muchos saltos seguidos con un solo movimiento
    // Evita que el acelerómetro haga doble salto sin querer
    private static final float COOLDOWN  = 0.35f;

    // Esto sirve para suavizar la señal del acelerómetro y evitar que el juego salte por vibraciones pequeñas
    // Cuanto más alto sea este número, más suave va, pero tarda más en reaccionar
    private static final float SMOOTH_ALPHA = 0.85f;

    // Aquí guardo el tipo de aplicación Android para comparar rápido
    // Esto se usa porque el acelerómetro normalmente solo existe en móviles
    private static final com.badlogic.gdx.Application.ApplicationType ANDROID =
        com.badlogic.gdx.Application.ApplicationType.Android;

    // Contador del cooldown para bloquear saltos durante un rato
    private float cooldownTimer = 0f;

    // Valor suavizado de la señal para que no sea brusca
    private float smooth = 0f;

    // Esto reinicia el detector, por ejemplo al empezar un nivel o al volver de pausa
    public void reset() {
        cooldownTimer = 0f;
        smooth = 0f;
    }

    public boolean updateAndConsume(float delta) {

        // Bajo el cooldown con el tiempo del frame
        cooldownTimer -= delta;
        if (cooldownTimer < 0f) cooldownTimer = 0f;

        // Si no estoy en Android, no uso acelerómetro y devuelvo false
        if (Gdx.app.getType() != ANDROID) return false;

        // Si el dispositivo no tiene acelerómetro, tampoco hago nada
        if (!Gdx.input.isPeripheralAvailable(Input.Peripheral.Accelerometer)) return false;

        // Leo dos ejes del acelerómetro
        // Y y Z suelen cambiar bastante cuando inclinas el móvil hacia delante o lo mueves
        float ay = Gdx.input.getAccelerometerY();
        float az = Gdx.input.getAccelerometerZ();

        // Creo una señal combinada con valores absolutos para medir fuerza sin importar el signo
        // Le doy un poco menos de peso al eje Z para que no sea tan sensible
        float forwardSignal = Math.abs(ay) + Math.abs(az) * 0.65f;

        // Suavizo la señal mezclando el valor anterior con el nuevo
        smooth = (SMOOTH_ALPHA * smooth) + ((1f - SMOOTH_ALPHA) * forwardSignal);

        // Si ya pasó el cooldown y la señal supera el umbral, digo que hay salto
        if (cooldownTimer <= 0f && smooth >= THRESHOLD) {
            cooldownTimer = COOLDOWN;
            return true;
        }

        // Si no se cumple lo anterior, no hay salto
        return false;
    }
}
