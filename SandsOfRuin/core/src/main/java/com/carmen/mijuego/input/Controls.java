package com.carmen.mijuego.input;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Controls implements InputProcessor {

    private final Viewport viewport;

    private final Texture btnLeft, btnRight, btnGrenade, btnShoot, btnJump, btnPause;

    private final Rectangle rLeft = new Rectangle();
    private final Rectangle rRight = new Rectangle();
    private final Rectangle rGrenade = new Rectangle();
    private final Rectangle rShoot = new Rectangle();
    private final Rectangle rJump = new Rectangle();
    private final Rectangle rPause = new Rectangle();

    public boolean leftPressed, rightPressed;
    public boolean jumpPressed, shootPressed, grenadePressed, pausePressed;

    private int leftPointer = -1;
    private int rightPointer = -1;

    private final Vector2 touchWorld = new Vector2();

    private float uiSize = 120f;
    private float uiMargin = 25f;
    private float uiGap = 18f;
    private float pauseSize = 90f;

    private static final float PRESSED = 0.7f;

    public Controls(Viewport viewport) {
        this.viewport = viewport;

        btnLeft    = new Texture("ui/controls/btn_move_left.png");
        btnRight   = new Texture("ui/controls/btn_move_right.png");
        btnGrenade = new Texture("ui/controls/btn_grenade.png");
        btnShoot   = new Texture("ui/controls/btn_shoot.png");
        btnJump    = new Texture("ui/controls/btn_jump.png");
        btnPause   = new Texture("ui/controls/btn_pause.png");

        setLinear(btnLeft);
        setLinear(btnRight);
        setLinear(btnGrenade);
        setLinear(btnShoot);
        setLinear(btnJump);
        setLinear(btnPause);
    }

    private void setLinear(Texture t) {
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    public void updateLayout(float worldW, float worldH) {
        float leftX = uiMargin;
        float leftY = uiMargin;

        float grenadeX = leftX;
        float grenadeY = leftY + uiSize + uiGap;

        float rightX = worldW - uiMargin - uiSize;
        float rightY = uiMargin;

        float jumpX = rightX;
        float jumpY = rightY + uiSize + uiGap;

        float shootX = rightX - uiGap - uiSize;
        float shootY = rightY;

        float pauseX = worldW - uiMargin - pauseSize;
        float pauseY = worldH - uiMargin - pauseSize;

        rLeft.set(leftX, leftY, uiSize, uiSize);
        rGrenade.set(grenadeX, grenadeY, uiSize, uiSize);

        rRight.set(rightX, rightY, uiSize, uiSize);
        rJump.set(jumpX, jumpY, uiSize, uiSize);
        rShoot.set(shootX, shootY, uiSize, uiSize);

        rPause.set(pauseX, pauseY, pauseSize, pauseSize);
    }

    public void draw(SpriteBatch batch) {
        if (leftPressed) batch.setColor(PRESSED, PRESSED, PRESSED, 1f);
        batch.draw(btnLeft, rLeft.x, rLeft.y, rLeft.width, rLeft.height);
        batch.setColor(1f, 1f, 1f, 1f);

        if (grenadePressed) batch.setColor(PRESSED, PRESSED, PRESSED, 1f);
        batch.draw(btnGrenade, rGrenade.x, rGrenade.y, rGrenade.width, rGrenade.height);
        batch.setColor(1f, 1f, 1f, 1f);

        if (rightPressed) batch.setColor(PRESSED, PRESSED, PRESSED, 1f);
        batch.draw(btnRight, rRight.x, rRight.y, rRight.width, rRight.height);
        batch.setColor(1f, 1f, 1f, 1f);

        if (jumpPressed) batch.setColor(PRESSED, PRESSED, PRESSED, 1f);
        batch.draw(btnJump, rJump.x, rJump.y, rJump.width, rJump.height);
        batch.setColor(1f, 1f, 1f, 1f);

        if (shootPressed) batch.setColor(PRESSED, PRESSED, PRESSED, 1f);
        batch.draw(btnShoot, rShoot.x, rShoot.y, rShoot.width, rShoot.height);
        batch.setColor(1f, 1f, 1f, 1f);

        if (pausePressed) batch.setColor(PRESSED, PRESSED, PRESSED, 1f);
        batch.draw(btnPause, rPause.x, rPause.y, rPause.width, rPause.height);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void unproject(int screenX, int screenY) {
        touchWorld.set(screenX, screenY);
        viewport.unproject(touchWorld);
    }

    private void clearActionButtons() {
        jumpPressed = false;
        shootPressed = false;
        grenadePressed = false;
        pausePressed = false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        unproject(screenX, screenY);

        if (rLeft.contains(touchWorld) && leftPointer == -1) {
            leftPressed = true;
            leftPointer = pointer;
            return true;
        }
        if (rRight.contains(touchWorld) && rightPointer == -1) {
            rightPressed = true;
            rightPointer = pointer;
            return true;
        }

        if (rJump.contains(touchWorld))    { jumpPressed = true; return true; }
        if (rShoot.contains(touchWorld))   { shootPressed = true; return true; }
        if (rGrenade.contains(touchWorld)) { grenadePressed = true; return true; }
        if (rPause.contains(touchWorld))   { pausePressed = true; return true; }

        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        unproject(screenX, screenY);

        if (pointer == leftPointer && !rLeft.contains(touchWorld)) {
            leftPressed = false;
            leftPointer = -1;
        }
        if (pointer == rightPointer && !rRight.contains(touchWorld)) {
            rightPressed = false;
            rightPointer = -1;
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer == leftPointer) {
            leftPressed = false;
            leftPointer = -1;
        }
        if (pointer == rightPointer) {
            rightPressed = false;
            rightPointer = -1;
        }

        clearActionButtons();
        return true;
    }


    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        clearActionButtons();
        leftPressed = false; rightPressed = false;
        leftPointer = -1; rightPointer = -1;
        return true;
    }

    @Override public boolean keyDown(int keycode) { return false; }
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }

    public void dispose() {
        btnLeft.dispose();
        btnRight.dispose();
        btnGrenade.dispose();
        btnShoot.dispose();
        btnJump.dispose();
        btnPause.dispose();
    }
}
