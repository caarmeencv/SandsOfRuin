package com.carmen.mijuego.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.carmen.mijuego.audio.AudioManager;
import com.carmen.mijuego.enemies.Soldier;
import com.carmen.mijuego.enemies.Tank;

public class EnemyManager {

    // Referencia al sistema de audio para pasar a los enemigos
    private AudioManager audio;

    // Texturas del soldado
    private Texture soldierIdle;
    private Texture soldierRun;
    private Texture soldierHurt;
    private Texture soldierDead;
    private Texture bulletTex;

    // Texturas del tanque
    private Texture tankIdle;
    private Texture tankMove;
    private Texture tankDestroy;
    private Texture tankDead;

    // Altura del suelo donde se colocan
    private float groundY;

    // Listas dinámicas de enemigos activos
    private Array<Soldier> soldiers;
    private Array<Tank> tanks;

    // Constructor que recibe todas las texturas necesarias
    public EnemyManager(AudioManager audio,
                        Texture soldierIdle,
                        Texture soldierRun,
                        Texture soldierHurt,
                        Texture soldierDead,
                        Texture bulletTex,
                        Texture tankIdle,
                        Texture tankMove,
                        Texture tankDestroy,
                        Texture tankDead,
                        float groundY) {

        this.audio = audio;

        this.soldierIdle = soldierIdle;
        this.soldierRun = soldierRun;
        this.soldierHurt = soldierHurt;
        this.soldierDead = soldierDead;
        this.bulletTex = bulletTex;

        this.tankIdle = tankIdle;
        this.tankMove = tankMove;
        this.tankDestroy = tankDestroy;
        this.tankDead = tankDead;

        this.groundY = groundY;

        soldiers = new Array<Soldier>();
        tanks = new Array<Tank>();
    }

    // Crea un soldado en la posición indicada
    public void spawnSoldierAt(float x) {
        soldiers.add(
            new Soldier(audio, soldierIdle, soldierRun, soldierHurt, soldierDead, bulletTex, x, groundY)
        );
    }

    // Crea un tanque en la posición indicada
    public void spawnTankAt(float x) {
        tanks.add(
            new Tank(audio, tankIdle, tankMove, tankDestroy, tankDead, x, groundY)
        );
    }

    // Se llama cada frame para actualizar todos los enemigos
    public void update(float delta, float aylaX, float camLeft, float camRight) {

        // Actualiza soldados
        for (int i = soldiers.size - 1; i >= 0; i--) {

            Soldier s = soldiers.get(i);

            s.update(delta, aylaX, camLeft, camRight);

            // Si ya desapareció o salió de pantalla, se elimina
            if (s.isGone() || s.isOffScreenLeft(camLeft)) {
                soldiers.removeIndex(i);
            }
        }

        // Actualiza tanques
        for (int i = tanks.size - 1; i >= 0; i--) {

            Tank t = tanks.get(i);

            t.update(delta, aylaX);

            if (t.isGone() || t.isOffScreenLeft(camLeft)) {
                tanks.removeIndex(i);
            }
        }
    }

    // Devuelve la lista de soldados
    public Array<Soldier> getSoldiers() {
        return soldiers;
    }

    // Devuelve la lista de tanques
    public Array<Tank> getTanks() {
        return tanks;
    }

    // Comprueba si hay algún soldado activo cerca de la cámara
    public boolean hasActiveSoldier(float camLeft, float camRight) {

        for (int i = 0; i < soldiers.size; i++) {

            Soldier s = soldiers.get(i);

            if (s.isDead() || s.isGone()) continue;

            float x = s.getBounds().x;

            if (x > camLeft - 200f) {
                if (x < camRight + 200f) return true;
            }
        }

        return false;
    }

    // Comprueba si hay algún tanque activo cerca de la cámara
    public boolean hasActiveTank(float camLeft, float camRight) {

        for (int i = 0; i < tanks.size; i++) {

            Tank t = tanks.get(i);

            if (t.isDead() || t.isGone()) continue;

            float x = t.getBounds().x;

            if (x > camLeft - 200f) {
                if (x < camRight + 200f) return true;
            }
        }

        return false;
    }

    // Cuenta soldados activos cerca de la cámara
    public int countActiveSoldiers(float camLeft, float camRight) {

        int c = 0;

        for (int i = 0; i < soldiers.size; i++) {

            Soldier s = soldiers.get(i);

            if (s.isDead() || s.isGone()) continue;

            float x = s.getBounds().x;

            if (x > camLeft - 200f) {
                if (x < camRight + 200f) c++;
            }
        }

        return c;
    }

    // Cuenta tanques activos cerca de la cámara
    public int countActiveTanks(float camLeft, float camRight) {

        int c = 0;

        for (int i = 0; i < tanks.size; i++) {

            Tank t = tanks.get(i);

            if (t.isDead() || t.isGone()) continue;

            float x = t.getBounds().x;

            if (x > camLeft - 200f) {
                if (x < camRight + 200f) c++;
            }
        }

        return c;
    }

    // Comprueba si queda algún soldado vivo en todo el nivel
    public boolean hasAnyAliveSoldier() {

        for (int i = 0; i < soldiers.size; i++) {

            Soldier s = soldiers.get(i);

            if (!s.isDead() && !s.isGone()) return true;
        }

        return false;
    }

    // Comprueba si queda algún tanque vivo en todo el nivel
    public boolean hasAnyAliveTank() {

        for (int i = 0; i < tanks.size; i++) {

            Tank t = tanks.get(i);

            if (!t.isDead() && !t.isGone()) return true;
        }

        return false;
    }

    // Elimina todos los enemigos
    public void clear() {
        soldiers.clear();
        tanks.clear();
    }
}
