package br.com.beraguasstudio.engine.ui;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

import br.com.beraguasstudio.engine.input.Input;
import org.joml.Vector2f;

import java.util.List;

/**
 * Hover, clique e consumo de eventos da UI. Roda antes do passo fixo do jogo: um clique sobre um
 * elemento interativo é consumido ({@link Input#consumeClick()}) e o gameplay não o enxerga.
 * Sem dependência de GL.
 */
public final class UIInputSystem {

    private UIElement hovered;
    private UIElement pressed;

    /**
     * @param windowWidth  largura da janela em coordenadas de tela (mesma unidade do cursor)
     * @param windowHeight altura da janela em coordenadas de tela
     */
    public void update(Input input, UICanvas canvas, int windowWidth, int windowHeight) {
        Vector2f mouse = input.getMousePosition();
        // janela -> framebuffer (HiDPI) -> unidades de UI
        float x = mouse.x * canvas.screenWidth() / Math.max(1, windowWidth) / canvas.scale();
        float y = mouse.y * canvas.screenHeight() / Math.max(1, windowHeight) / canvas.scale();

        UIElement hit = hitTest(canvas, x, y);
        if (hit != hovered) {
            if (hovered != null) {
                hovered.onInteraction(false, false);
            }
            hovered = hit;
        }

        if (hit != null && input.isMouseJustPressed(GLFW_MOUSE_BUTTON_LEFT)) {
            pressed = hit;
            input.consumeClick();
        }
        if (pressed != null && !input.isMouseDown(GLFW_MOUSE_BUTTON_LEFT)) {
            if (pressed == hit) {
                pressed.onClick();
            }
            pressed.onInteraction(pressed == hovered, false);
            pressed = null;
        }
        if (hovered != null) {
            hovered.onInteraction(true, pressed == hovered);
        }
    }

    /** Elemento interativo visível mais ao topo sob o ponto, ou {@code null}. */
    static UIElement hitTest(UIElement element, float x, float y) {
        if (!element.visible()) {
            return null;
        }
        List<UIElement> order = element.drawOrder();
        for (int i = order.size() - 1; i >= 0; i--) {
            UIElement hit = hitTest(order.get(i), x, y);
            if (hit != null) {
                return hit;
            }
        }
        return element.interactive() && element.contains(x, y) ? element : null;
    }

    public UIElement hovered() {
        return hovered;
    }
}
