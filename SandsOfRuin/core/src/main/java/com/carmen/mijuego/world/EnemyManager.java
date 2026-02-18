package com.carmen.mijuego.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.carmen.mijuego.enemies.Soldier;
import com.carmen.mijuego.enemies.Tank;

public class EnemyManager {

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

    public EnemyManager(Texture soldierIdle,
                        Texture soldierRun,
                        Texture soldierHurt,
                        Texture soldierDead,
                        Texture bulletTex,
                        Texture tankIdle,
                        Texture tankMove,
                        Texture tankDestroy,
                        Texture tankDead,
                        float groundY) {

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
            tankIdle,
            tankMove,
            tankDestroy,
            tankDead,
            x,
            groundY
        ));
    }

    public void update(float delta, float aylaX, float camLeft, float camRight) {

        for (int i = soldiers.size - 1; i >= 0; i--) {
            Soldier s = soldiers.get(i);

            s.update(delta, aylaX, camLeft, camRight);
            s.setAylaX(aylaX);

            if (s.isOffScreenLeft(camLeft)) {
                soldiers.removeIndex(i);
            }
        }

        for (int i = tanks.size - 1; i >= 0; i--) {
            Tank t = tanks.get(i);

            // tu Tank.update(delta, aylaX)
            t.update(delta, aylaX);

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
            if (s.isDead()) continue;

            float x = s.getBounds().x;
            if (x > camLeft - 200f && x < camRight + 200f) return true;
        }
        return false;
    }

    public boolean hasActiveTank(float camLeft, float camRight) {
        for (int i = 0; i < tanks.size; i++) {
            Tank t = tanks.get(i);
            if (t.isDead()) continue;

            float x = t.getBounds().x;
            if (x > camLeft - 200f && x < camRight + 200f) return true;
        }
        return false;
    }

    public void clear() {
        soldiers.clear();
        tanks.clear();
    }
}
