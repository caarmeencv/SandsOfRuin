package com.carmen.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.carmen.mijuego.Main;
import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.settings.MummyTimeRecords;
import com.carmen.mijuego.ui.Fonts;

import java.util.List;

public class RecordsScreen implements Screen {

    private final Main game;
    private List<Integer> times;
    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture bg;

    private BitmapFont font;
    private final GlyphLayout layoutTitle = new GlyphLayout();
    private final GlyphLayout layoutHint  = new GlyphLayout();

    private float blinkTime = 0f;

    // ✅ Bloqueo de input al entrar (para que no se “coma” el toque del menú)
    private float inputBlockTime = 0f;

    public RecordsScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0f);
        camera.update();

        bg = game.assets.get(Assets.SCREEN_ACHIEVEMENTS_BG);

        font = Fonts.main(game);
        font.setColor(Color.WHITE);
    }

    @Override
    public void show() {
        game.audio.playMusic(Assets.MUS_ACHIEVEMENTS_THEME, true);

        inputBlockTime = 0.20f;

        // ✅ LEER SOLO UNA VEZ
        times = MummyTimeRecords.getTimesSeconds();
    }

    private void goBack() {
        game.setScreen(new MenuScreen(game));
    }

    @Override
    public void render(float delta) {

        // ✅ actualizar bloqueo
        if (inputBlockTime > 0f) {
            inputBlockTime -= delta;
            if (inputBlockTime < 0f) inputBlockTime = 0f;
        }

        // ✅ Back/Escape siempre funcionan
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            goBack();
            return;
        }

        // ✅ Touch solo si ya pasó el bloqueo
        if (inputBlockTime == 0f) {
            if (Gdx.input.justTouched()) {
                goBack();
                return;
            }
        }

        blinkTime += delta;

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);

        String title = game.i18n.t("records.title");
        String hint  = game.i18n.t("records.hint");


        font.setColor(Color.WHITE);
        font.getData().setScale(3.6f);
        layoutTitle.setText(font, title);

        font.getData().setScale(1.6f);
        layoutHint.setText(font, hint);

        game.batch.begin();

        game.batch.draw(bg, 0, 0, WORLD_W, WORLD_H);

        // Título centrado
        font.getData().setScale(3.6f);
        font.setColor(Color.WHITE);
        font.draw(game.batch, layoutTitle, (WORLD_W - layoutTitle.width) / 2f, WORLD_H - 110f);

        // Lista de tiempos
        float startX = 220f;
        float startY = WORLD_H - 220f;
        float gap = 60f;

        font.getData().setScale(2.0f);
        font.setColor(Color.WHITE);

        if (times.size() == 0) {

            String empty;
            if (game.settings.isLangSpanish()) {
                empty = "Aún no has derrotado a la momia";
            } else {
                empty = "You haven't defeated the mummy yet";
            }

            GlyphLayout tmp = new GlyphLayout();
            tmp.setText(font, empty, Color.WHITE, WORLD_W - 200f, Align.center, true);
            font.draw(game.batch, tmp, 100f, WORLD_H / 2f + 30f);

        } else {

            for (int i = 0; i < times.size(); i++) {
                int secs = times.get(i);
                String line = (i + 1) + ". " + MummyTimeRecords.formatMMSS(secs);
                font.draw(game.batch, line, startX, startY);
                startY -= gap;
                if (startY < 150f) break;
            }
        }

        // Hint parpadeo
        font.getData().setScale(1.6f);
        float alpha = 0.4f + 0.6f * (0.5f + 0.5f * (float)Math.sin(blinkTime * 4f));
        font.setColor(1f, 1f, 1f, alpha);
        font.draw(game.batch, layoutHint, (WORLD_W - layoutHint.width) / 2f, 60f);

        Fonts.resetColor(font);
        font.getData().setScale(1f);

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        // NO dispose (AssetManager)
    }
}
