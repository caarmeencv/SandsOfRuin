package com.carmen.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.carmen.mijuego.Main;
import com.carmen.mijuego.characters.Ayla;
import com.carmen.mijuego.input.Controls;

public class DesertScreen implements Screen {

    private final Main game;

    private static final float WORLD_WIDTH = 1280f;
    private static final float WORLD_HEIGHT = 720f;

    private static final float GROUND_Y = 120f;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture cielo, nubes, ruinas, medio, cerca;
    private Ayla ayla;

    // offsets parallax
    private float xNubes, xRuinas, xMedio, xCerca;

    private Controls controls;

    private boolean intro = true;
    private float introTargetX = 120f;
    private float introSpawnX  = -350f;

    public DesertScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);

        cielo  = new Texture("backgrounds/desert/05cielo.png");
        nubes  = new Texture("backgrounds/desert/04nubes.png");
        ruinas = new Texture("backgrounds/desert/03ruinas.png");
        medio  = new Texture("backgrounds/desert/02medio.png");
        cerca  = new Texture("backgrounds/desert/01cerca.png");

        setLinear(cielo);
        setLinear(nubes);
        setLinear(ruinas);
        setLinear(medio);
        setLinear(cerca);

        ayla = new Ayla(introSpawnX, GROUND_Y);

        controls = new Controls(viewport);
        Gdx.input.setInputProcessor(controls);
    }

    private void setLinear(Texture t) {
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        boolean moveRight = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || controls.rightPressed;
        boolean moveLeft  = Gdx.input.isKeyPressed(Input.Keys.LEFT)  || controls.leftPressed;
        boolean jump      = Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || controls.jumpPressed;

        if (intro) {
            moveLeft = false;
            moveRight = true;
            jump = false;

            if (ayla.getX() >= introTargetX) {
                intro = false;
                moveRight = false;
            }
        }

        ayla.update(delta, moveLeft, moveRight, jump, GROUND_Y);

        float dx = ayla.getLastDx();

        xNubes  -= dx * 0.15f;
        xRuinas -= dx * 0.30f;
        xMedio  -= dx * 0.60f;
        xCerca  -= dx * 0.90f;

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        float worldW = viewport.getWorldWidth();
        float worldH = viewport.getWorldHeight();

        controls.updateLayout(worldW, worldH);

        game.batch.begin();

        game.batch.draw(cielo, 0, 0, worldW, worldH);

        drawLayer(nubes,  xNubes);
        drawLayer(ruinas, xRuinas);
        drawLayer(medio,  xMedio);
        drawLayer(cerca,  xCerca);

        ayla.draw(game.batch);
        controls.draw(game.batch);

        game.batch.end();
    }

    private void drawLayer(Texture texture, float offsetX) {
        float worldW = viewport.getWorldWidth();
        float worldH = viewport.getWorldHeight();

        float scale = worldH / texture.getHeight();
        float drawW = texture.getWidth() * scale;

        float startX = offsetX % drawW;
        if (startX > 0) startX -= drawW;

        for (float xx = startX; xx < worldW; xx += drawW) {
            game.batch.draw(texture, xx, 0, drawW, worldH);
        }
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        cielo.dispose();
        nubes.dispose();
        ruinas.dispose();
        medio.dispose();
        cerca.dispose();
        ayla.dispose();
        controls.dispose();
    }
}
