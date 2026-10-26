package br.com.beraguasstudio.engine.ui;

/** Ponto do pai ao qual um elemento se prende. {@code fx}/{@code fy}: 0 = esquerda/topo, 1 = direita/base. */
public enum Anchor {
    TOP_LEFT(0f, 0f), TOP_CENTER(0.5f, 0f), TOP_RIGHT(1f, 0f),
    CENTER_LEFT(0f, 0.5f), CENTER(0.5f, 0.5f), CENTER_RIGHT(1f, 0.5f),
    BOTTOM_LEFT(0f, 1f), BOTTOM_CENTER(0.5f, 1f), BOTTOM_RIGHT(1f, 1f);

    final float fx;
    final float fy;

    Anchor(float fx, float fy) {
        this.fx = fx;
        this.fy = fy;
    }
}
