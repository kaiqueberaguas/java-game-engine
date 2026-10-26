package br.com.beraguasstudio.engine.ui.components;

import br.com.beraguasstudio.engine.render.Texture;
import br.com.beraguasstudio.engine.ui.UIElement;
import br.com.beraguasstudio.engine.ui.UIMeshBuilder;

/** Retângulo de cor sólida ou textura, opcionalmente em 9-slice (cantos fixos, bordas e centro esticados). */
public class UIPanel extends UIElement {

    private float border;
    private float uvBorderX;
    private float uvBorderY;

    /**
     * Ativa 9-slice: {@code borderPx} é a espessura da moldura em pixels da textura
     * ({@code textureWidth x textureHeight}); na tela a moldura mantém esse tamanho em unidades de UI.
     */
    public UIPanel nineSlice(float borderPx, int textureWidth, int textureHeight) {
        border = borderPx;
        uvBorderX = borderPx / textureWidth;
        uvBorderY = borderPx / textureHeight;
        markDirty();
        return this;
    }

    public UIPanel nineSlice(Texture texture, float borderPx) {
        texture(texture);
        return nineSlice(borderPx, texture.width(), texture.height());
    }

    @Override
    protected void emit(UIMeshBuilder mesh) {
        if (border <= 0f) {
            super.emit(mesh);
            return;
        }
        float b = Math.min(border, Math.min(width(), height()) / 2f);
        float[] xs = {computedX(), computedX() + b, computedX() + width() - b, computedX() + width()};
        float[] ys = {computedY(), computedY() + b, computedY() + height() - b, computedY() + height()};
        float[] us = {0f, uvBorderX, 1f - uvBorderX, 1f};
        float[] vs = {0f, uvBorderY, 1f - uvBorderY, 1f};
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                mesh.quad(texture(), xs[col], ys[row], xs[col + 1] - xs[col], ys[row + 1] - ys[row],
                        us[col], vs[row], us[col + 1], vs[row + 1], color());
            }
        }
    }
}
