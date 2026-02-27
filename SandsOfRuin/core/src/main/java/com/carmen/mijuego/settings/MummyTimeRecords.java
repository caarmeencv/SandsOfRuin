package com.carmen.mijuego.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MummyTimeRecords {

    // Nombre del archivo donde se guardan los récords
    private static final String PREF_NAME = "sands_of_ruin_records";

    // Clave interna donde se guardan los tiempos en formato texto
    private static final String KEY_TIMES = "mummy_times_seconds";

    // Máximo número de récords guardados
    private static final int MAX_RECORDS = 10;

    // Referencia estática a las preferencias
    private static Preferences PREFS;

    // Constructor privado para que no se pueda instanciar esta clase
    // Es una clase utilitaria solo con métodos estáticos
    private MummyTimeRecords() {
    }

    // Devuelve el objeto Preferences
    // Si todavía no existe, lo crea
    private static Preferences prefs() {
        if (PREFS == null) {
            PREFS = Gdx.app.getPreferences(PREF_NAME);
        }
        return PREFS;
    }

    // Añade un nuevo tiempo en segundos
    public static void addTimeSeconds(int seconds) {

        // Evita guardar valores inválidos
        if (seconds < 1) seconds = 1;

        // Carga lista actual
        List<Integer> times = getTimesSecondsInternal();

        // Añade el nuevo tiempo
        times.add(seconds);

        // Ordena de menor a mayor
        // Esto hace que el mejor tiempo quede arriba
        Collections.sort(times);

        // Si hay más de 10, elimina los peores
        while (times.size() > MAX_RECORDS) {
            times.remove(times.size() - 1);
        }

        // Guarda la lista actualizada
        saveList(times);
    }

    // Devuelve la lista pública de tiempos
    public static List<Integer> getTimesSeconds() {
        return getTimesSecondsInternal();
    }

    // Borra todos los récords guardados
    public static void clear() {
        prefs().remove(KEY_TIMES);
        prefs().flush();
    }

    // Lee el texto guardado y lo convierte en lista de enteros
    private static List<Integer> getTimesSecondsInternal() {

        // Lee la cadena guardada
        String raw = prefs().getString(KEY_TIMES, "");

        ArrayList<Integer> out = new ArrayList<Integer>();

        // Si está vacío, devuelve lista vacía
        if (raw == null) return out;
        if (raw.trim().isEmpty()) return out;

        // Separa los valores usando coma
        String[] parts = raw.split(",");

        for (int i = 0; i < parts.length; i++) {
            try {
                int v = Integer.parseInt(parts[i].trim());

                // Solo acepta valores válidos
                if (v >= 1) out.add(v);

            } catch (Exception e) {
                // Si algo falla al convertir, simplemente lo ignora
            }
        }

        // Ordena antes de devolver
        Collections.sort(out);

        return out;
    }

    // Convierte la lista de tiempos en una cadena y la guarda
    private static void saveList(List<Integer> times) {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < times.size(); i++) {

            if (i > 0) sb.append(",");

            sb.append(times.get(i));
        }

        prefs().putString(KEY_TIMES, sb.toString());
        prefs().flush();
    }

    // Convierte segundos a formato minutos y segundos
    public static String formatMMSS(int seconds) {

        if (seconds < 0) seconds = 0;

        int min = seconds / 60;
        int sec = seconds % 60;

        return String.format("%02d:%02d", min, sec);
    }
}
