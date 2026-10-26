package br.com.beraguasstudio.engine.ui;

import br.com.beraguasstudio.engine.render.Viewport;

import java.util.List;

/**
 * Raiz da UI. Trabalha em "pixels de referência": {@code scale = framebufferHeight / referenceHeight}.
 * Layout e malha são cacheados e só refeitos quando algo muda ({@link #update()}); em frames estáticos
 * o custo é uma checagem de flag. Sem dependência de GL.
 */
public final class UICanvas extends UIElement {

    private final float referenceWidth;
    private final float referenceHeight;
    private int screenWidth;
    private int screenHeight;
    private float scale = 1f;
    private UIFont font;

    private final UIMeshBuilder mesh = new UIMeshBuilder();
    private boolean dirty = true;
    private int meshVersion;
    private int layoutPasses;

    private final UIElement worldArea = new UIElement();

    public UICanvas(int referenceWidth, int referenceHeight) {
        this.referenceWidth = referenceWidth;
        this.referenceHeight = referenceHeight;
        this.screenWidth = referenceWidth;
        this.screenHeight = referenceHeight;
        this.scale = 1f;
        worldArea.anchor(Anchor.CENTER);
        worldArea.color(0f, 0f, 0f, 0f); // só container: não desenha nada
        add(worldArea);
    }

    /** Tamanho do framebuffer em pixels; só suja a UI se mudou. */
    public void setScreenSize(int width, int height) {
        if (width <= 0 || height <= 0 || (width == screenWidth && height == screenHeight)) {
            return;
        }
        screenWidth = width;
        screenHeight = height;
        scale = height / referenceHeight;
        dirty = true;
    }

    /**
     * Container com a proporção do mundo (a área do letterbox) em unidades de UI; use para HUDs que
     * devem alinhar à área do jogo em vez das bordas da janela.
     */
    public UIElement worldArea() {
        return worldArea;
    }

    public void setFont(UIFont font) {
        this.font = font;
        dirty = true;
    }

    public UIFont font() {
        return font;
    }

    /** Refaz layout e malha se houve mudança. Retorna {@code true} quando a malha foi reconstruída. */
    public boolean update() {
        if (!dirty) {
            return false;
        }
        dirty = false;
        float w = screenWidth / scale;
        float h = screenHeight / scale;
        Viewport v = Viewport.fit((int) referenceWidth, (int) referenceHeight, screenWidth, screenHeight);
        worldArea.size(v.width() / scale, v.height() / scale);
        dirty = false; // o size() acima pode ter sujado

        UILayout.layout(this, 0, 0, w, h);
        layoutPasses++;
        dirty = false; // size() de elementos auto-medidos (texto) durante o layout não conta como mudança

        mesh.clear();
        emitTree(this);
        mesh.finish();
        meshVersion++;
        return true;
    }

    private void emitTree(UIElement element) {
        if (!element.visible()) {
            return;
        }
        element.emit(mesh);
        List<UIElement> ordered = element.drawOrder();
        for (UIElement child : ordered) {
            emitTree(child);
        }
    }

    @Override
    void onRootDirty() {
        dirty = true;
    }

    @Override
    protected void beforeLayout() {
        size(screenWidth / scale, screenHeight / scale);
        dirty = false;
    }

    @Override
    protected void emit(UIMeshBuilder mesh) { }

    public UIMeshBuilder mesh() { return mesh; }
    public float scale() { return scale; }
    public int screenWidth() { return screenWidth; }
    public int screenHeight() { return screenHeight; }
    /** Incrementa a cada reconstrução da malha; o renderer só reenvia à GPU quando muda. */
    public int meshVersion() { return meshVersion; }
    /** Quantas vezes o layout rodou; útil para provar que frames estáticos não recalculam. */
    public int layoutPasses() { return layoutPasses; }
}
