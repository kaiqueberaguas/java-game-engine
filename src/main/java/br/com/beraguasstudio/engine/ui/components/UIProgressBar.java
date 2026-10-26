package br.com.beraguasstudio.engine.ui.components;

import org.joml.Vector4f;

import br.com.beraguasstudio.engine.ui.UIElement;
import br.com.beraguasstudio.engine.ui.UIMeshBuilder;

/**
 * Barra de progresso: fundo (cor/textura do elemento) + preenchimento proporcional ao valor.
 * Mudar o valor suja a UI, mas só reconstrói a malha (layout não muda).
 */
public class UIProgressBar extends UIElement {

    private final Vector4f fillColor = new Vector4f(0.2f, 0.8f, 0.2f, 1f);
    private float min;
    private float max = 1f;
    private float value = 1f;

    public UIProgressBar range(float min, float max) {
        this.min = min;
        this.max = max;
        markDirty();
        return this;
    }

    public UIProgressBar value(float value) {
        if (this.value != value) {
            this.value = value;
            markDirty();
        }
        return this;
    }

    public UIProgressBar fillColor(float r, float g, float b, float a) {
        fillColor.set(r, g, b, a);
        markDirty();
        return this;
    }

    public float value() {
        return value;
    }

    /** Valor normalizado em [0, 1]. */
    public float normalized() {
        if (max <= min) {
            return 0f;
        }
        return Math.max(0f, Math.min(1f, (value - min) / (max - min)));
    }

    @Override
    protected void emit(UIMeshBuilder mesh) {
        super.emit(mesh);
        float fill = width() * normalized();
        if (fill > 0f) {
            mesh.quad(null, computedX(), computedY(), fill, height(), 0f, 0f, 1f, 1f, fillColor);
        }
    }
}
