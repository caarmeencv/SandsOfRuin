package com.carmen.mijuego.input;

import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.carmen.mijuego.assets.Assets;
import com.carmen.mijuego.audio.AudioManager;

public class Controls implements InputProcessor {

    // Estos booleanos son el estado final que usará el juego
    // Si leftPressed es true, Ayla se mueve a la izquierda, y así con todos
    public boolean leftPressed, rightPressed, jumpPressed, shootPressed, grenadePressed, pausePressed;

    // Texturas de los botones de la interfaz
    private final Texture left, right, jump, shoot, grenade, pause;

    // Rectángulos que marcan dónde está cada botón en pantalla y su tamaño
    private final Rectangle rLeft = new Rectangle(), rRight = new Rectangle(), rJump = new Rectangle(),
        rShoot = new Rectangle(), rGrenade = new Rectangle(), rPause = new Rectangle();

    // Vector donde guardo la posición del toque ya convertida a coordenadas de la interfaz
    private final Vector2 touch = new Vector2();

    // Audio para el click del botón de pausa
    private final AudioManager audio;

    // Cada dedo que toca la pantalla tiene un número llamado pointer
    // Aquí guardo qué dedo está usando cada botón para permitir multitouch bien
    private int leftPointer = -1, rightPointer = -1, jumpPointer = -1, shootPointer = -1, grenadePointer = -1, pausePointer = -1;

    // Cuando un botón está pulsado lo dibujo un poco más oscuro
    private static final float PRESSED_TINT = 0.75f;

    // Fuente y layout para dibujar el contador de tiempo
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    // Texto del contador que se dibuja al lado del botón de pausa
    private String counterText = "00:00";

    // Esto se usa para dibujar los anillos de cooldown de disparo y granada
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    // Esto dice si el salto por acelerómetro está activado
    // Si está activado, el botón de salto desaparece y se recoloca la interfaz
    private boolean accelJumpEnabled = false;

    // Resolución virtual fija de la interfaz
    // Así los botones quedan siempre en el mismo sitio aunque cambie el tamaño real de la pantalla
    private static final float UI_W = 1280f, UI_H = 720f;

    // Cámara y viewport para dibujar la interfaz y para convertir toques a coordenadas de UI
    private final OrthographicCamera uiCam = new OrthographicCamera();
    private final Viewport uiViewport = new FitViewport(UI_W, UI_H, uiCam);

    public Controls(AudioManager audio, Assets assets,
                    Texture left, Texture right, Texture jump,
                    Texture shoot, Texture grenade, Texture pause) {

        // Guardo audio y texturas
        this.audio = audio;
        this.left = left; this.right = right; this.jump = jump;
        this.shoot = shoot; this.grenade = grenade; this.pause = pause;

        // Aplico el viewport para que la cámara quede lista
        uiViewport.apply(true);
        uiCam.position.set(UI_W / 2f, UI_H / 2f, 0f);
        uiCam.update();

        // Cargo la fuente desde archivo y la escalo para que se vea grande
        font = new BitmapFont(Gdx.files.internal(Assets.FONT_MAIN_FNT_PATH));
        font.getData().setScale(2.6f);

        // Calculo posiciones y tamaños de los botones
        updateLayout();
    }

    public void resize(int width, int height) {

        // Esto se llama cuando cambia el tamaño de la pantalla
        // Por ejemplo si cambias orientación o si hay diferentes resoluciones
        uiViewport.update(width, height, true);
        uiCam.position.set(UI_W / 2f, UI_H / 2f, 0f);
        uiCam.update();

        // Recalculo el layout de botones
        updateLayout();
    }

    public void beginUI(SpriteBatch batch) {

        // Esto prepara el batch para dibujar en coordenadas de interfaz
        uiViewport.apply();
        batch.setProjectionMatrix(uiCam.combined);
    }

    public void setAccelJumpEnabled(boolean enabled) {

        // Si no cambia nada, no hago nada
        if (accelJumpEnabled == enabled) return;

        // Cambio el modo
        accelJumpEnabled = enabled;

        // Si activo acelerómetro, quito el salto táctil por si estaba pulsado
        if (enabled) { jumpPressed = false; jumpPointer = -1; }

        // Recoloco botones porque cambia si existe botón de salto o no
        updateLayout();
    }

    public boolean isAccelJumpEnabled() { return accelJumpEnabled; }

    // Esto cambia el texto del contador, normalmente lo pone la pantalla del juego
    public void setCounterText(String text) { counterText = text; }

    public void updateLayout() {

        // Aquí calculo tamaño base del botón y separaciones en función del alto de la UI
        // Así todo escala bien con la resolución virtual
        float size = UI_H * 0.125f, gap = UI_H * 0.020f;
        float safeX = UI_W * 0.03f, safeY = UI_H * 0.035f;
        float bottomY = safeY + UI_H * 0.015f;

        // Botones de movimiento en la parte izquierda abajo
        rLeft.set(safeX, bottomY, size, size);
        rRight.set(rLeft.x + size + gap, bottomY, size, size);

        // En el lado derecho van disparo, salto y granada
        float rightSlotX = UI_W - safeX - size;

        // Disparo se coloca a la izquierda del slot derecho
        rShoot.set(rightSlotX - gap - size, bottomY, size, size);

        // Si no está el acelerómetro, aparece botón de salto y granada encima
        if (!accelJumpEnabled) {
            rJump.set(rightSlotX, bottomY, size, size);
            rGrenade.set(rightSlotX, bottomY + size + gap, size, size);
        } else {
            // Si el acelerómetro está activado, no dibujo el botón de salto
            // Y la granada baja para ocupar ese sitio
            rJump.set(0, 0, 0, 0);
            rGrenade.set(rightSlotX, bottomY, size, size);
        }

        // Botón de pausa en la parte superior derecha, un poco más pequeño
        float pauseSize = size * 0.95f;
        rPause.set(UI_W - UI_W * 0.010f - pauseSize, UI_H - UI_H * 0.015f - pauseSize, pauseSize, pauseSize);
    }

    public void draw(SpriteBatch batch) {
        draw(batch, 0f, 0f);
    }

    public void draw(SpriteBatch batch, float shootCd, float grenadeCd) {

        // Si hay cooldown, dibujo un aro alrededor del botón correspondiente
        // Para dibujar líneas con ShapeRenderer tengo que cerrar el batch un momento
        if (shootCd > 0f || grenadeCd > 0f) {
            batch.end();
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
            if (shootCd > 0f) drawCooldownRing(rShoot, shootCd);
            if (grenadeCd > 0f) drawCooldownRing(rGrenade, grenadeCd);
            shapeRenderer.end();
            batch.begin();
        }

        // Dibujo los botones y oscurezco el que esté pulsado
        drawButton(batch, left, rLeft, leftPressed);
        drawButton(batch, right, rRight, rightPressed);
        if (!accelJumpEnabled) drawButton(batch, jump, rJump, jumpPressed);
        drawButton(batch, shoot, rShoot, shootPressed);
        drawButton(batch, grenade, rGrenade, grenadePressed);
        drawButton(batch, pause, rPause, pausePressed);

        // Dibujo el contador cerca del botón de pausa
        // Primero mido el texto para colocarlo bien alineado
        layout.setText(font, counterText);
        font.draw(batch, layout, rPause.x - 22f - layout.width,
            rPause.y + (rPause.height * 0.63f) + (layout.height * 0.35f));
    }

    private void drawCooldownRing(Rectangle r, float p) {

        // Dibujo un arco alrededor del botón
        // p es un porcentaje de 0 a 1 que dice cuánto queda de cooldown
        float cx = r.x + r.width / 2f, cy = r.y + r.height / 2f;
        shapeRenderer.arc(cx, cy, Math.min(r.width, r.height) * 0.70f, 90f, -360f * p, 48);
    }

    private void drawButton(SpriteBatch batch, Texture tex, Rectangle r, boolean pressed) {

        // Si el rectángulo es cero, significa que ese botón está desactivado
        if (r.width <= 0f || r.height <= 0f) return;

        // Si está pulsado lo dibujo más oscuro
        if (pressed) batch.setColor(PRESSED_TINT, PRESSED_TINT, PRESSED_TINT, 1f);

        // Dibujo el botón
        batch.draw(tex, r.x, r.y, r.width, r.height);

        // Devuelvo el color normal para que no afecte a otros dibujos
        if (pressed) batch.setColor(1f, 1f, 1f, 1f);
    }

    private void unprojectUI(int sx, int sy) {

        // sx y sy vienen en píxeles de pantalla real
        // Aquí los convierto a coordenadas de la UI virtual 1280 por 720
        touch.set(sx, sy);
        uiViewport.unproject(touch);
    }

    private void releasePointer(int p) {

        // Cuando levanto un dedo, miro si era el dedo de algún botón
        // Si lo era, suelto ese botón y libero ese pointer
        if (p == leftPointer)    { leftPointer = -1; leftPressed = false; }
        if (p == rightPointer)   { rightPointer = -1; rightPressed = false; }
        if (p == jumpPointer)    { jumpPointer = -1; jumpPressed = false; }
        if (p == shootPointer)   { shootPointer = -1; shootPressed = false; }
        if (p == grenadePointer) { grenadePointer = -1; grenadePressed = false; }
        if (p == pausePointer)   { pausePointer = -1; pausePressed = false; }
    }

    @Override
    public boolean touchDown(int sx, int sy, int pointer, int button) {

        // Convierto la posición del toque a coordenadas de UI
        unprojectUI(sx, sy);

        // Compruebo botones uno por uno
        // También compruebo que ese botón no esté ya ocupado por otro dedo
        if (leftPointer == -1 && rLeft.contains(touch)) { leftPointer = pointer; leftPressed = true; return true; }
        if (rightPointer == -1 && rRight.contains(touch)) { rightPointer = pointer; rightPressed = true; return true; }

        // Si el salto por acelerómetro está activado, este botón no existe
        if (!accelJumpEnabled && jumpPointer == -1 && rJump.contains(touch)) { jumpPointer = pointer; jumpPressed = true; return true; }

        if (shootPointer == -1 && rShoot.contains(touch)) { shootPointer = pointer; shootPressed = true; return true; }
        if (grenadePointer == -1 && rGrenade.contains(touch)) { grenadePointer = pointer; grenadePressed = true; return true; }

        // El botón de pausa reproduce un sonido al pulsarlo
        if (pausePointer == -1 && rPause.contains(touch)) {
            pausePointer = pointer;
            pausePressed = true;
            if (audio != null) audio.playSfx(Assets.SFX_BUTTON_CLICKED);
            return true;
        }

        return false;
    }

    @Override
    public boolean touchUp(int sx, int sy, int pointer, int button) {

        // Al levantar el dedo, libero el botón que usaba ese dedo
        releasePointer(pointer);
        return true;
    }

    @Override
    public boolean touchCancelled(int sx, int sy, int pointer, int button) {

        // Esto es parecido a touchUp pero para casos especiales donde el toque se cancela
        releasePointer(pointer);
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {

        // Controles de teclado para poder probar el juego en ordenador
        if (keycode == Input.Keys.A) leftPressed = true;
        if (keycode == Input.Keys.D) rightPressed = true;

        // Si el salto por acelerómetro está activado, no permito salto con teclado aquí
        if (!accelJumpEnabled && (keycode == Input.Keys.W || keycode == Input.Keys.SPACE)) jumpPressed = true;

        if (keycode == Input.Keys.K) shootPressed = true;
        if (keycode == Input.Keys.L) grenadePressed = true;

        // Escape se usa como pausa en ordenador
        if (keycode == Input.Keys.ESCAPE) {
            pausePressed = true;
            if (audio != null) audio.playSfx(Assets.SFX_BUTTON_CLICKED);
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {

        // Cuando suelto tecla, suelto el botón
        if (keycode == Input.Keys.A) leftPressed = false;
        if (keycode == Input.Keys.D) rightPressed = false;

        if (!accelJumpEnabled && (keycode == Input.Keys.W || keycode == Input.Keys.SPACE)) jumpPressed = false;

        if (keycode == Input.Keys.K) shootPressed = false;
        if (keycode == Input.Keys.L) grenadePressed = false;

        if (keycode == Input.Keys.ESCAPE) pausePressed = false;
        return false;
    }

    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDragged(int sx, int sy, int pointer) { return false; }
    @Override public boolean mouseMoved(int sx, int sy) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }

    public void resetAll() {

        // Esto suelta todo de golpe, útil al entrar o salir de pantallas o al pausar
        leftPressed = rightPressed = jumpPressed = shootPressed = grenadePressed = pausePressed = false;
        leftPointer = rightPointer = jumpPointer = shootPointer = grenadePointer = pausePointer = -1;
    }

    public void dispose() {

        // Libero recursos que he creado manualmente
        // La fuente y el shapeRenderer no los gestiona el AssetManager aquí
        font.dispose();
        shapeRenderer.dispose();
    }
}
