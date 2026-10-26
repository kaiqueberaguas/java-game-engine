package br.com.beraguasstudio.engine.ui.components;

import org.joml.Vector4f;

import br.com.beraguasstudio.engine.ui.Anchor;

/** Botão: painel interativo com cores de hover/pressed e callback de clique. */
public class UIButton extends UIPanel {

    private final Vector4f baseColor = new Vector4f(0.25f, 0.25f, 0.3f, 1f);
    private final Vector4f hoverColor = new Vector4f(0.35f, 0.35f, 0.42f, 1f);
    private final Vector4f pressedColor = new Vector4f(0.15f, 0.15f, 0.2f, 1f);
    private boolean hovered;
    private boolean pressed;
    private Runnable onClick = () -> { };

    public UIButton() {
        interactive(true);
        applyStateColor();
    }

    /** Cores dos três estados; o estado atual é reaplicado. */
    public UIButton colors(Vector4f base, Vector4f hover, Vector4f pressed) {
        baseColor.set(base);
        hoverColor.set(hover);
        pressedColor.set(pressed);
        applyStateColor();
        return this;
    }

    public UIButton onClick(Runnable onClick) {
        this.onClick = onClick == null ? () -> { } : onClick;
        return this;
    }

    /** Adiciona um rótulo centralizado no botão. */
    public UIButton text(String text) {
        add(new UILabel().text(text).anchor(Anchor.CENTER));
        return this;
    }

    public boolean hovered() { return hovered; }
    public boolean pressed() { return pressed; }

    @Override
    protected void onInteraction(boolean hovered, boolean pressed) {
        if (this.hovered != hovered || this.pressed != pressed) {
            this.hovered = hovered;
            this.pressed = pressed;
            applyStateColor();
        }
    }

    @Override
    protected void onClick() {
        onClick.run();
    }

    private void applyStateColor() {
        Vector4f c = pressed ? pressedColor : hovered ? hoverColor : baseColor;
        color(c.x, c.y, c.z, c.w);
    }
}
