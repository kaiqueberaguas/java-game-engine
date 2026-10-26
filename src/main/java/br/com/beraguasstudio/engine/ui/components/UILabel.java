package br.com.beraguasstudio.engine.ui.components;

import br.com.beraguasstudio.engine.ui.UICanvas;
import br.com.beraguasstudio.engine.ui.UIElement;
import br.com.beraguasstudio.engine.ui.UIFont;
import br.com.beraguasstudio.engine.ui.UIMeshBuilder;
import br.com.beraguasstudio.engine.ui.UIFont.Glyph;

/**
 * Texto em uma ou mais linhas ({@code \n}). O tamanho do elemento é medido a partir do texto, então
 * âncoras funcionam sem definir {@code size}. A cor do elemento tinge os glifos.
 */
public class UILabel extends UIElement {

    private String text = "";
    private float fontSize = 16f;
    private UIFont font;

    public UILabel text(String text) {
        String value = text == null ? "" : text;
        if (!this.text.equals(value)) {
            this.text = value;
            markDirty();
        }
        return this;
    }

    public UILabel fontSize(float fontSize) {
        if (this.fontSize != fontSize) {
            this.fontSize = fontSize;
            markDirty();
        }
        return this;
    }

    /** Sobrescreve a fonte padrão do canvas. */
    public UILabel font(UIFont font) {
        this.font = font;
        markDirty();
        return this;
    }

    public String text() {
        return text;
    }

    private UIFont resolveFont() {
        if (font != null) {
            return font;
        }
        UIElement root = this;
        while (root.parent() != null) {
            root = root.parent();
        }
        return root instanceof UICanvas canvas ? canvas.font() : null;
    }

    @Override
    protected void beforeLayout() {
        UIFont f = resolveFont();
        if (f == null) {
            return;
        }
        float k = fontSize / f.size();
        float widest = 0f;
        float lineWidth = 0f;
        int lines = 1;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            if (cp == '\n') {
                widest = Math.max(widest, lineWidth);
                lineWidth = 0f;
                lines++;
                continue;
            }
            UIFont.Glyph g = f.glyph(cp);
            if (g != null) {
                lineWidth += g.advance() * k;
            }
        }
        widest = Math.max(widest, lineWidth);
        size(widest, lines * f.lineHeight() * k);
    }

    @Override
    protected void emit(UIMeshBuilder mesh) {
        UIFont f = resolveFont();
        if (f == null || text.isEmpty()) {
            return;
        }
        float k = fontSize / f.size();
        float x = computedX();
        float baseline = computedY() + f.ascent() * k;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            if (cp == '\n') {
                x = computedX();
                baseline += f.lineHeight() * k;
                continue;
            }
            UIFont.Glyph g = f.glyph(cp);
            if (g == null) {
                continue;
            }
            if (g.width() > 0f && g.height() > 0f) {
                mesh.quad(f.texture(), x + g.xOffset() * k, baseline + g.yOffset() * k,
                        g.width() * k, g.height() * k, g.u0(), g.v0(), g.u1(), g.v1(), color());
            }
            x += g.advance() * k;
        }
    }
}
