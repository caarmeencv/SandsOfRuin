package com.carmen.mijuego.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

import com.carmen.mijuego.audio.AudioManager;
import com.carmen.mijuego.enemies.Soldier;
import com.carmen.mijuego.enemies.Tank;

public class EnemyManager {

    private final AudioManager audio; // ✅ NUEVO

    private final Texture soldierIdle;
    private final Texture soldierRun;
    private final Texture soldierHurt;
    private final Texture soldierDead;
    private final Texture bulletTex;

    private final Texture tankIdle;
    private final Texture tankMove;
    private final Texture tankDestroy;
    private final Texture tankDead;

    private final float groundY;

    private final Array<Soldier> soldiers = new Array<>();
    private final Array<Tank> tanks = new Array<>();

    public EnemyManager(AudioManager audio,              // ✅ NUEVO
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
    }

    public void spawnSoldierAt(float x) {
        soldiers.add(new Soldier(
            audio, // ✅ PASAR AUDIO
            soldierIdle,
            soldierRun,
            soldierHurt,
            soldierDead,
            bulletTex,
            x,
            groundY
        ));
    }

    public void spawnTankAt(float x) {
        tanks.add(new Tank(
            audio, // ✅ PASAR AUDIO
            tankIdle,
            tankMove,
            tankDestroy,
            tankDead,
            x,
            groundY
        ));
    }

    public void update(float delta, float aylaX, float camLeft, float camRight) {

        // ===== SOLDIERS =====
        for (int i = soldiers.size - 1; i >= 0; i--) {
            Soldier s = soldiers.get(i);

            s.update(delta, aylaX, camLeft, camRight);

            if (s.isGone()) {
                soldiers.removeIndex(i);
                continue;
            }

            if (s.isOffScreenLeft(camLeft)) {
                soldiers.removeIndex(i);
            }
        }

        // ===== TANKS =====
        for (int i = tanks.size - 1; i >= 0; i--) {
            Tank t = tanks.get(i);

            t.update(delta, aylaX);

            if (t.isGone()) {
                tanks.removeIndex(i);
                continue;
            }

            if (t.isOffScreenLeft(camLeft)) {
                tanks.removeIndex(i);
            }
        }
    }

    public Array<Soldier> getSoldiers() { return soldiers; }
    public Array<Tank> getTanks() { return tanks; }

    public boolean hasActiveSoldier(float camLeft, float camRight) {
        for (int i = 0; i < soldiers.size; i++) {
            Soldier s = soldiers.get(i);
            if (s.isDead() || s.isGone()) continue;

            float x = s.getBounds().x;
            if (x > camLeft - 200f && x < camRight + 200f) return true;
        }
        return false;
    }

    public boolean hasActiveTank(float camLeft, float camRight) {
        for (int i = 0; i < tanks.size; i++) {
            Tank t = tanks.get(i);
            if (t.isDead() || t.isGone()) continue;

            float x = t.getBounds().x;
            if (x > camLeft - 200f && x < camRight + 200f) return true;
        }
        return false;
    }

    public int countActiveSoldiers(float camLeft, float camRight) {
        int c = 0;
        for (int i = 0; i < soldiers.size; i++) {
            Soldier s = soldiers.get(i);
            if (s.isDead() || s.isGone()) continue;

            float x = s.getBounds().x;
            if (x > camLeft - 200f && x < camRight + 200f) c++;
        }
        return c;
    }

    public int countActiveTanks(float camLeft, float camRight) {
        int c = 0;
        for (int i = 0; i < tanks.size; i++) {
            Tank t = tanks.get(i);
            if (t.isDead() || t.isGone()) continue;

            float x = t.getBounds().x;
            if (x > camLeft - 200f && x < camRight + 200f) c++;
        }
        return c;
    }

    public boolean hasAnyAliveSoldier() {
        for (int i = 0; i < soldiers.size; i++) {
            Soldier s = soldiers.get(i);
            if (!s.isDead() && !s.isGone()) return true;
        }
        return false;
    }

    public boolean hasAnyAliveTank() {
        for (int i = 0; i < tanks.size; i++) {
            Tank t = tanks.get(i);
            if (!t.isDead() && !t.isGone()) return true;
        }
        return false;
    }

    public void clear() {
        soldiers.clear();
        tanks.clear();
    }
}
