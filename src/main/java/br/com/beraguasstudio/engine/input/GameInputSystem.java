package br.com.beraguasstudio.engine.input;

import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector2f;

/**
 * Estado de entrada alimentado pelos callbacks do GLFW. Chame {@link #beginFrame()}
 * antes de {@code glfwPollEvents()} a cada frame para que {@code isKeyJustPressed}
 * funcione.
 */
public final class GameInputSystem implements Input {

    private final boolean[] current = new boolean[GLFW_KEY_LAST + 1];
    private final boolean[] previous = new boolean[GLFW_KEY_LAST + 1];
    private final Vector2f mouse = new Vector2f();

    /** Conecta os callbacks à janela. Eles são liberados junto com a janela. */
    public void attach(long windowHandle) {
        glfwSetKeyCallback(windowHandle, (w, key, scancode, action, mods) -> onKey(key, action));
        glfwSetCursorPosCallback(windowHandle, (w, x, y) -> onCursorPos(x, y));
    }

    public void beginFrame() {
        System.arraycopy(current, 0, previous, 0, current.length);
    }

    /** Também usado para simular entrada em testes headless. */
    public void onKey(int key, int action) {
        if (!inRange(key)) {
            return;
        }
        current[key] = action != GLFW_RELEASE; // PRESS ou REPEAT
    }

    public void onCursorPos(double x, double y) {
        mouse.set((float) x, (float) y);
    }

    @Override
    public boolean isKeyDown(int keyCode) {
        return inRange(keyCode) && current[keyCode];
    }

    @Override
    public boolean isKeyJustPressed(int keyCode) {
        return inRange(keyCode) && current[keyCode] && !previous[keyCode];
    }

    @Override
    public Vector2f getMousePosition() {
        return new Vector2f(mouse);
    }

    @Override
    public boolean up() {
        return isKeyDown(GLFW_KEY_W) || isKeyDown(GLFW_KEY_UP);
    }

    @Override
    public boolean down() {
        return isKeyDown(GLFW_KEY_S) || isKeyDown(GLFW_KEY_DOWN);
    }

    @Override
    public boolean left() {
        return isKeyDown(GLFW_KEY_A) || isKeyDown(GLFW_KEY_LEFT);
    }

    @Override
    public boolean right() {
        return isKeyDown(GLFW_KEY_D) || isKeyDown(GLFW_KEY_RIGHT);
    }

    private static boolean inRange(int keyCode) {
        return keyCode >= 0 && keyCode <= GLFW_KEY_LAST;
    }
}
