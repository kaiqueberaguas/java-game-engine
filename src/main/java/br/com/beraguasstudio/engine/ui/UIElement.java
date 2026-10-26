package br.com.beraguasstudio.engine.ui;

import br.com.beraguasstudio.engine.render.Texture;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Nó da árvore de UI. A posição é resolvida por âncora no pai ({@link UILayout}); qualquer mudança
 * marca a árvore suja e só então layout e malha são refeitos.
 */
public class UIElement {

    private UIElement parent;
    private final List<UIElement> children = new ArrayList<>();
    private Anchor anchor = Anchor.TOP_LEFT;
    private float offsetX;
    private float offsetY;
    private float width = 100;
    private float height = 30;
    private int zIndex;
    private boolean visible = true;
    private boolean interactive;
    private final Vector4f color = new Vector4f(1, 1, 1, 1);
    private Texture texture;
    private float computedX;
    private float computedY;

    public UIElement size(float width, float height) {
        if (this.width != width || this.height != height) {
            this.width = width;
            this.height = height;
            markDirty();
        }
        return this;
    }

    public UIElement anchor(Anchor anchor) {
        if (this.anchor != anchor) {
            this.anchor = anchor;
            markDirty();
        }
        return this;
    }

    /** Deslocamento para dentro do pai a partir da âncora (positivo afasta da borda da âncora). */
    public UIElement offset(float x, float y) {
        if (offsetX != x || offsetY != y) {
            offsetX = x;
            offsetY = y;
            markDirty();
        }
        return this;
    }

    public UIElement color(float r, float g, float b, float a) {
        if (color.x != r || color.y != g || color.z != b || color.w != a) {
            color.set(r, g, b, a);
            markDirty();
        }
        return this;
    }

    public UIElement texture(Texture texture) {
        if (this.texture != texture) {
            this.texture = texture;
            markDirty();
        }
        return this;
    }

    public UIElement zIndex(int zIndex) {
        if (this.zIndex != zIndex) {
            this.zIndex = zIndex;
            markDirty();
        }
        return this;
    }

    public UIElement visible(boolean visible) {
        if (this.visible != visible) {
            this.visible = visible;
            markDirty();
        }
        return this;
    }

    /** Elementos interativos recebem hover/clique e impedem o clique de chegar ao jogo. */
    public UIElement interactive(boolean interactive) {
        this.interactive = interactive;
        return this;
    }

    public UIElement add(UIElement child) {
        if (child.parent != null) {
            child.parent.remove(child);
        }
        child.parent = this;
        children.add(child);
        markDirty();
        return this;
    }

    public void remove(UIElement child) {
        if (children.remove(child)) {
            child.parent = null;
            markDirty();
        }
    }

    /** Marca a árvore suja: o canvas raiz refaz layout e malha no próximo frame. */
    protected void markDirty() {
        UIElement root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        root.onRootDirty();
    }

    void onRootDirty() { }

    /** Chamado pelo layout antes de resolver a posição (ex.: texto mede o próprio tamanho). */
    protected void beforeLayout() { }

    /** Emite os quads deste elemento (não os dos filhos). */
    protected void emit(UIMeshBuilder mesh) {
        if (color.w > 0f) {
            mesh.quad(texture, computedX, computedY, width, height, 0f, 0f, 1f, 1f, color);
        }
    }

    /** Hook de estado de interação; muda aparência marcando sujo. */
    protected void onInteraction(boolean hovered, boolean pressed) { }

    /** Disparado ao soltar o botão sobre o elemento em que foi pressionado. */
    protected void onClick() { }

    void setComputed(float x, float y) {
        computedX = x;
        computedY = y;
    }

    /** Filhos em ordem de desenho: zIndex crescente, estável para empates. */
    List<UIElement> drawOrder() {
        List<UIElement> sorted = new ArrayList<>(children);
        sorted.sort((a, b) -> Integer.compare(a.zIndex, b.zIndex));
        return sorted;
    }

    public boolean contains(float x, float y) {
        return x >= computedX && x <= computedX + width && y >= computedY && y <= computedY + height;
    }

    public Anchor anchor() { return anchor; }
    public float offsetX() { return offsetX; }
    public float offsetY() { return offsetY; }
    public float width() { return width; }
    public float height() { return height; }
    public int zIndex() { return zIndex; }
    public boolean visible() { return visible; }
    public boolean interactive() { return interactive; }
    public Vector4f color() { return color; }
    public Texture texture() { return texture; }
    public float computedX() { return computedX; }
    public float computedY() { return computedY; }
    public UIElement parent() { return parent; }
    public List<UIElement> children() { return Collections.unmodifiableList(children); }
}
