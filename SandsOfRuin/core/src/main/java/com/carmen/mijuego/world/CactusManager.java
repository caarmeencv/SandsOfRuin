package com.carmen.mijuego.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.carmen.mijuego.enemies.Cactus;

public class CactusManager {

    // Altura visual del cactus
    private static final float CACTUS_HEIGHT = 90f;

    // Posición fija en el eje Y donde se colocan
    private static final float CACTUS_Y = 180f;

    // Texturas disponibles
    private Texture cactusPink;
    private Texture cactusYellow;

    // Lista dinámica de cactus activos en el nivel
    private Array<Cactus> cactuses;

    // Constructor que recibe las dos texturas posibles
    public CactusManager(Texture pink, Texture yellow) {
        cactusPink = pink;
        cactusYellow = yellow;
        cactuses = new Array<Cactus>();
    }

    // Crea un cactus en una posición concreta
    // El boolean indica si es rosa o amarillo
    public void spawnCactusAt(float x, boolean pink) {

        Texture tex;

        if (pink) tex = cactusPink;
        else tex = cactusYellow;

        // Se crea el cactus en la posición X indicada
        // La Y es fija para que esté en el suelo
        cactuses.add(new Cactus(tex, x, CACTUS_Y, CACTUS_HEIGHT));
    }

    // Se llama cada frame
    // Elimina los cactus que ya han salido completamente por la izquierda
    public void update(float camLeft) {

        for (int i = cactuses.size - 1; i >= 0; i--) {

            if (cactuses.get(i).isOffScreenLeft(camLeft)) {
                cactuses.removeIndex(i);
            }
        }
    }

    // Devuelve la lista de cactus
    // Se usa para colisiones y para dibujarlos
    public Array<Cactus> getCactuses() {
        return cactuses;
    }

    // Comprueba si hay algún cactus activo cerca de la cámara
    // Esto sirve para evitar generar demasiados obstáculos juntos
    public boolean hasActive(float camLeft, float camRight) {

        for (int i = 0; i < cactuses.size; i++) {

            Cactus c = cactuses.get(i);
            float x = c.getBounds().x;

            // Comprueba si está dentro de un rango ampliado de la cámara
            if (x > camLeft - 200f) {
                if (x < camRight + 200f) {
                    return true;
                }
            }
        }

        return false;
    }

    // Elimina todos los cactus activos
    // Se usa por ejemplo al cambiar de nivel o reiniciar
    public void clear() {
        cactuses.clear();
    }
}
