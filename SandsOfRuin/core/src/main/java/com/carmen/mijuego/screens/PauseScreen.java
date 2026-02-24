package com.carmen.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.carmen.mijuego.Main;
import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.audio.AudioManager;

public class PauseScreen implements Screen {

    public enum Context {
        DESERT,
        PYRAMID
    }

    private final Main game;
    private final Screen returnScreen;
    private final Context context;

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture bg;
    private Texture btnContinue, btnReset, btnMenu;

    private final Rectangle rContinue = new Rectangle();
    private final Rectangle rReset = new Rectangle();
    private final Rectangle rMenu = new Rectangle();

    private final Vector2 pointerWorld = new Vector2();

    public PauseScreen(Main game, Screen returnScreen, Context context) {
        this.game = game;
        this.returnScreen = returnScreen;
        this.context = context;

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0f);
        camera.update();

        if (context == Context.DESERT)
            bg = game.assets.get(Assets.SCREEN_PAUSE_BG_DESERT);
        else
            bg = game.assets.get(Assets.SCREEN_PAUSE_BG_PYRAMID);

        btnContinue = game.assets.get(Assets.SCREEN_PAUSE_BTN_CONTINUE);
        btnReset    = game.assets.get(Assets.SCREEN_PAUSE_BTN_RESET);
        btnMenu     = game.assets.get(Assets.SCREEN_PAUSE_BTN_MENU);

        updateLayout();
    }

    private void updateLayout() {
        float btnW = 420f;
        float btnH = btnW * ((float) btnContinue.getHeight() / btnContinue.getWidth());
        float gap = 35f;
        float totalH = btnH * 3f + gap * 2f;

        float x = (WORLD_W - btnW) / 2f;
        float startY = (WORLD_H - totalH) / 2f;

        rMenu.set(x, startY, btnW, btnH);
        rReset.set(x, startY + btnH + gap, btnW, btnH);
        rContinue.set(x, startY + (btnH + gap) * 2f, btnW, btnH);
    }

    @Override
    public void show() {
        game.audio.playMusic(Assets.MUS_PAUSE_THEME, true);

        // 🔥 MUY IMPORTANTE: cortar input del juego
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {

        pointerWorld.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(pointerWorld);

        if (Gdx.input.justTouched()) {

            if (rContinue.contains(pointerWorld)) {
                game.setScreen(returnScreen);
                return;
            }

            if (rReset.contains(pointerWorld)) {
                game.resetRun();
                if (context == Context.DESERT)
                    game.setScreen(new DesertScreen(game));
                else
                    game.setScreen(new PyramidScreen(game));
                return;
            }

            if (rMenu.contains(pointerWorld)) {
                game.setScreen(new MenuScreen(game));
                return;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
        {
            game.setScreen(returnScreen);
            return;
        }

        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.draw(bg,0,0,WORLD_W,WORLD_H);
        game.batch.draw(btnContinue,rContinue.x,rContinue.y,rContinue.width,rContinue.height);
        game.batch.draw(btnReset,rReset.x,rReset.y,rReset.width,rReset.height);
        game.batch.draw(btnMenu,rMenu.x,rMenu.y,rMenu.width,rMenu.height);
        game.batch.end();
    }

    @Override public void resize(int width,int height){viewport.update(width,height,true);}
    @Override public void pause(){}
    @Override public void resume(){}
    @Override public void hide(){}
    @Override public void dispose(){}
}
