package br.com.beraguasstudio.engine.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import br.com.beraguasstudio.engine.input.GameInputSystem;
import br.com.beraguasstudio.engine.ui.components.UIButton;

import org.junit.jupiter.api.Test;

class UIInputSystemTest {

    private final UICanvas canvas = new UICanvas(800, 600);
    private final GameInputSystem input = new GameInputSystem();
    private final UIInputSystem ui = new UIInputSystem();
    private int clicks;

    private UIButton button(Anchor anchor) {
        UIButton b = new UIButton();
        b.size(100, 50).anchor(anchor);
        b.onClick(() -> clicks++);
        canvas.add(b);
        canvas.update();
        return b;
    }

    private void frame(double mx, double my, Integer action) {
        input.beginFrame();
        input.onCursorPos(mx, my);
        if (action != null) {
            input.onMouseButton(GLFW_MOUSE_BUTTON_LEFT, action);
        }
        ui.update(input, canvas, 800, 600);
    }

    @Test
    void clickOnButtonFiresOnReleaseAndIsConsumed() {
        button(Anchor.TOP_LEFT);

        frame(10, 10, GLFW_PRESS);
        assertTrue(input.isClickConsumed());
        assertFalse(input.isMouseJustPressed(GLFW_MOUSE_BUTTON_LEFT));
        assertEquals(0, clicks);

        frame(10, 10, GLFW_RELEASE);
        assertEquals(1, clicks);
    }

    @Test
    void clickOutsideReachesGameplay() {
        button(Anchor.TOP_LEFT);
        frame(500, 500, GLFW_PRESS);
        assertFalse(input.isClickConsumed());
        assertTrue(input.isMouseJustPressed(GLFW_MOUSE_BUTTON_LEFT));
    }

    @Test
    void releasingOutsideDoesNotClick() {
        button(Anchor.TOP_LEFT);
        frame(10, 10, GLFW_PRESS);
        frame(500, 500, GLFW_RELEASE);
        assertEquals(0, clicks);
    }

    @Test
    void hoverUpdatesState() {
        UIButton b = button(Anchor.TOP_LEFT);
        frame(10, 10, null);
        assertTrue(b.hovered());
        frame(500, 500, null);
        assertFalse(b.hovered());
    }

    @Test
    void topmostZIndexWins() {
        UIButton low = button(Anchor.TOP_LEFT);
        UIButton high = button(Anchor.TOP_LEFT);
        high.zIndex(3);
        low.zIndex(1);
        canvas.update();
        frame(10, 10, null);
        assertTrue(high.hovered());
        assertFalse(low.hovered());
    }

    @Test
    void mouseIsMappedThroughHidpiAndScale() {
        // framebuffer com o dobro da janela e da altura de referência => escala 2
        canvas.setScreenSize(1600, 1200);
        UIButton b = button(Anchor.TOP_LEFT); // 0..100 x 0..50 unidades de UI = 0..100 x 0..50 px de janela
        frame(99, 49, null);
        assertTrue(b.hovered());
        frame(101, 49, null);
        assertFalse(b.hovered());
    }
}
