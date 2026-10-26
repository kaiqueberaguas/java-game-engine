package br.com.beraguasstudio.engine.ui;

/** Resolução de âncoras em coordenadas de UI (origem no topo esquerdo, Y para baixo). Sem GL. */
final class UILayout {

    private UILayout() { }

    /** Calcula {@code computedX/Y} de {@code element} e, recursivamente, de todos os filhos. */
    static void layout(UIElement element, float parentX, float parentY, float parentW, float parentH) {
        element.beforeLayout();
        Anchor a = element.anchor();
        // o offset sempre aponta para dentro do pai: na borda direita/base ele subtrai
        float dirX = a.fx == 1f ? -1f : 1f;
        float dirY = a.fy == 1f ? -1f : 1f;
        float x = parentX + (parentW - element.width()) * a.fx + element.offsetX() * dirX;
        float y = parentY + (parentH - element.height()) * a.fy + element.offsetY() * dirY;
        element.setComputed(x, y);
        for (UIElement child : element.children()) {
            layout(child, x, y, element.width(), element.height());
        }
    }
}
