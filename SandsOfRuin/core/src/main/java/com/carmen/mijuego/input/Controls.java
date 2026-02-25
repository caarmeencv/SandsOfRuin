package com.carmen.mijuego.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.audio.AudioManager;

public class Controls implements InputProcessor {

    public boolean leftPressed;
    public boolean rightPressed;
    public boolean jumpPressed;
    public boolean shootPressed;
    public boolean grenadePressed;
    public boolean pausePressed;

    private final Texture left;
    private final Texture right;
    private final Texture jump;
    private final Texture shoot;
    private final Texture grenade;
    private final Texture pause;

    private final Rectangle rLeft = new Rectangle();
    private final Rectangle rRight = new Rectangle();
    private final Rectangle rJump = new Rectangle();
    private final Rectangle rShoot = new Rectangle();
    private final Rectangle rGrenade = new Rectangle();
    private final Rectangle rPause = new Rectangle();

    private final Vector2 touch = new Vector2();
    private final Viewport viewport;

    private final AudioManager audio;

    // Multitouch: un dedo por botón
    private int leftPointer = -1;
    private int rightPointer = -1;
    private int jumpPointer = -1;
    private int shootPointer = -1;
    private int grenadePointer = -1;
    private int pausePointer = -1;

    private static final float PRESSED_TINT = 0.75f;

    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();
    private String counterText = "00:00";

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    public Controls(AudioManager audio,
                    Viewport viewport,
                    Texture left, Texture right, Texture jump,
                    Texture shoot, Texture grenade, Texture pause) {
        this.audio = audio;
        this.viewport = viewport;
        this.left = left;
        this.right = right;
        this.jump = jump;
        this.shoot = shoot;
        this.grenade = grenade;
        this.pause = pause;

        font = new BitmapFont();
        font.getData().setScale(3f);
    }

    public void setCounterText(String text) {
        this.counterText = text;
    }

    public void updateLayout(OrthographicCamera cam, Viewport vp) {
        float worldW = vp.getWorldWidth();
        float worldH = vp.getWorldHeight();

        float camLeft   = cam.position.x - worldW / 2f;
        float camRight  = cam.position.x + worldW / 2f;
        float camBottom = cam.position.y - worldH / 2f;
        float camTop    = cam.position.y + worldH / 2f;

        float size = worldH * 0.125f;

        float gap  = worldH * 0.020f;

        float safePadX = worldW * 0.03f;
        float safePadY = worldH * 0.035f;

        float pausePadX = worldW * 0.010f;
        float pausePadY = worldH * 0.015f;

        float bottomRaise = worldH * 0.015f;
        float bottomY = camBottom + safePadY + bottomRaise;

        rLeft.set(camLeft + safePadX, bottomY, size, size);

        rRight.set(rLeft.x + size + gap, bottomY, size, size);

        rJump.set(camRight - safePadX - size, bottomY, size, size);

        rShoot.set(rJump.x - gap - size, bottomY, size, size);

        rGrenade.set(rJump.x, bottomY + size + gap, size, size);

        float pauseSize = size * 0.82f;
        float pauseX = camRight - pausePadX - pauseSize;
        float pauseY = camTop - pausePadY - pauseSize;
        rPause.set(pauseX, pauseY, pauseSize, pauseSize);
    }

    public void draw(SpriteBatch batch) {
        draw(batch, 0f, 0f);
    }

    public void draw(SpriteBatch batch, float shootCooldownPercent, float grenadeCooldownPercent) {

        boolean anyRing = (shootCooldownPercent > 0f) || (grenadeCooldownPercent > 0f);
        if (anyRing) {
            batch.end();

            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(1f, 1f, 1f, 1f);

            if (shootCooldownPercent > 0f) {
                drawCooldownRing(rShoot, shootCooldownPercent);
            }
            if (grenadeCooldownPercent > 0f) {
                drawCooldownRing(rGrenade, grenadeCooldownPercent);
            }

            shapeRenderer.end();
            batch.begin();
        }

        drawButton(batch, left, rLeft, leftPressed);
        drawButton(batch, right, rRight, rightPressed);
        drawButton(batch, jump, rJump, jumpPressed);
        drawButton(batch, shoot, rShoot, shootPressed);
        drawButton(batch, grenade, rGrenade, grenadePressed);
        drawButton(batch, pause, rPause, pausePressed);

        layout.setText(font, counterText);

        float spacing = 22f;
        float textX = rPause.x - spacing - layout.width;
        float textY = rPause.y + (rPause.height * 0.63f) + (layout.height * 0.35f);

        font.draw(batch, layout, textX, textY);
    }

    private void drawCooldownRing(Rectangle r, float percent) {
        float angle = 360f * percent;

        float cx = r.x + r.width / 2f;
        float cy = r.y + r.height / 2f;

        float radius = Math.min(r.width, r.height) * 0.70f;

        int segments = 48;
        shapeRenderer.arc(cx, cy, radius, 90f, -angle, segments);
    }

    private void drawButton(SpriteBatch batch, Texture tex, Rectangle r, boolean pressed) {
        if (pressed) batch.setColor(PRESSED_TINT, PRESSED_TINT, PRESSED_TINT, 1f);
        batch.draw(tex, r.x, r.y, r.width, r.height);
        if (pressed) batch.setColor(1f, 1f, 1f, 1f);
    }

    private void unproject(int screenX, int screenY) {
        touch.set(screenX, screenY);
        viewport.unproject(touch);
    }

    private void releasePointer(int pointer) {
        if (pointer == leftPointer)    { leftPointer = -1; leftPressed = false; }
        if (pointer == rightPointer)   { rightPointer = -1; rightPressed = false; }
        if (pointer == jumpPointer)    { jumpPointer = -1; jumpPressed = false; }
        if (pointer == shootPointer)   { shootPointer = -1; shootPressed = false; }
        if (pointer == grenadePointer) { grenadePointer = -1; grenadePressed = false; }
        if (pointer == pausePointer)   { pausePointer = -1; pausePressed = false; }
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        unproject(screenX, screenY);

        if (leftPointer == -1 && rLeft.contains(touch)) {
            leftPointer = pointer;
            leftPressed = true;
            return true;
        }
        if (rightPointer == -1 && rRight.contains(touch)) {
            rightPointer = pointer;
            rightPressed = true;
            return true;
        }
        if (jumpPointer == -1 && rJump.contains(touch)) {
            jumpPointer = pointer;
            jumpPressed = true;
            return true;
        }
        if (shootPointer == -1 && rShoot.contains(touch)) {
            shootPointer = pointer;
            shootPressed = true;
            return true;
        }
        if (grenadePointer == -1 && rGrenade.contains(touch)) {
            grenadePointer = pointer;
            grenadePressed = true;
            return true;
        }
        if (pausePointer == -1 && rPause.contains(touch)) {
            pausePointer = pointer;
            pausePressed = true;

            if (audio != null) audio.playSfx(Assets.SFX_BUTTON_CLICKED);

            return true;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        releasePointer(pointer);
        return true;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        releasePointer(pointer);
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.A) leftPressed = true;
        if (keycode == Input.Keys.D) rightPressed = true;

        if (keycode == Input.Keys.W || keycode == Input.Keys.SPACE) jumpPressed = true;

        if (keycode == Input.Keys.K) shootPressed = true;
        if (keycode == Input.Keys.L) grenadePressed = true;

        if (keycode == Input.Keys.ESCAPE) {
            pausePressed = true;

            if (audio != null) audio.playSfx(Assets.SFX_BUTTON_CLICKED);
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.A) leftPressed = false;
        if (keycode == Input.Keys.D) rightPressed = false;

        if (keycode == Input.Keys.W || keycode == Input.Keys.SPACE) jumpPressed = false;

        if (keycode == Input.Keys.K) shootPressed = false;
        if (keycode == Input.Keys.L) grenadePressed = false;

        if (keycode == Input.Keys.ESCAPE) pausePressed = false;

        return false;
    }

    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }

    public void resetAll() {
        leftPressed = false;
        rightPressed = false;
        jumpPressed = false;
        shootPressed = false;
        grenadePressed = false;
        pausePressed = false;

        leftPointer = -1;
        rightPointer = -1;
        jumpPointer = -1;
        shootPointer = -1;
        grenadePointer = -1;
        pausePointer = -1;
    }

    public void dispose() {
        font.dispose();
        shapeRenderer.dispose();
    }
}
