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
    private final boolean[] mouseCurrent = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];
    private final boolean[] mousePrevious = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];
    private final Vector2f mouse = new Vector2f();
    private boolean clickConsumed;

    /** Conecta os callbacks à janela. Eles são liberados junto com a janela. */
    public void attach(long windowHandle) {
        glfwSetKeyCallback(windowHandle, (w, key, scancode, action, mods) -> onKey(key, action));
        glfwSetCursorPosCallback(windowHandle, (w, x, y) -> onCursorPos(x, y));
        glfwSetMouseButtonCallback(windowHandle, (w, button, action, mods) -> onMouseButton(button, action));
    }

    public void beginFrame() {
        System.arraycopy(current, 0, previous, 0, current.length);
        System.arraycopy(mouseCurrent, 0, mousePrevious, 0, mouseCurrent.length);
        clickConsumed = false;
    }

    /** Também usado para simular entrada em testes headless. */
    public void onKey(int key, int action) {
        if (!inRange(key)) {
            return;
        }
        current[key] = action != GLFW_RELEASE; // PRESS ou REPEAT
    }

    public void onMouseButton(int button, int action) {
        if (button >= 0 && button < mouseCurrent.length) {
            mouseCurrent[button] = action != GLFW_RELEASE;
        }
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
    public boolean isMouseDown(int button) {
        return button >= 0 && button < mouseCurrent.length && mouseCurrent[button];
    }

    @Override
    public boolean isMouseJustPressed(int button) {
        return !clickConsumed && isMouseDown(button) && !mousePrevious[button];
    }

    @Override
    public void consumeClick() {
        clickConsumed = true;
    }

    @Override
    public boolean isClickConsumed() {
        return clickConsumed;
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
