package br.com.beraguasstudio.engine.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.beraguasstudio.engine.render.Texture;
import br.com.beraguasstudio.engine.ui.components.UILabel;
import br.com.beraguasstudio.engine.ui.components.UIPanel;
import br.com.beraguasstudio.engine.ui.components.UIProgressBar;

import org.junit.jupiter.api.Test;

class UICanvasTest {

    private final UICanvas canvas = new UICanvas(800, 600);

    private UIElement box(Anchor anchor, float ox, float oy) {
        UIElement e = new UIElement().size(100, 50).anchor(anchor).offset(ox, oy);
        canvas.add(e);
        return e;
    }

    @Test
    void anchorsResolveAgainstScreen() {
        UIElement tl = box(Anchor.TOP_LEFT, 10, 20);
        UIElement tr = box(Anchor.TOP_RIGHT, 10, 20);
        UIElement c = box(Anchor.CENTER, 5, -5);
        UIElement bl = box(Anchor.BOTTOM_LEFT, 10, 20);
        UIElement br = box(Anchor.BOTTOM_RIGHT, 10, 20);
        canvas.update();

        assertEquals(10, tl.computedX());
        assertEquals(20, tl.computedY());
        assertEquals(800 - 100 - 10, tr.computedX());
        assertEquals(20, tr.computedY());
        assertEquals(400 - 50 + 5, c.computedX());
        assertEquals(300 - 25 - 5, c.computedY());
        assertEquals(10, bl.computedX());
        assertEquals(600 - 50 - 20, bl.computedY());
        assertEquals(690, br.computedX());
        assertEquals(530, br.computedY());
    }

    @Test
    void childrenAreRelativeToParent() {
        UIElement parent = box(Anchor.CENTER, 0, 0);
        parent.size(200, 100);
        UIElement child = new UIElement().size(20, 20).anchor(Anchor.BOTTOM_RIGHT).offset(4, 4);
        parent.add(child);
        canvas.update();

        assertEquals(parent.computedX() + 200 - 20 - 4, child.computedX());
        assertEquals(parent.computedY() + 100 - 20 - 4, child.computedY());
    }

    @Test
    void resizeScalesByHeightAndReanchorsToRealEdges() {
        UIElement tr = box(Anchor.TOP_RIGHT, 0, 0);
        canvas.setScreenSize(1600, 600); // mais larga: escala 1, a UI ganha largura
        canvas.update();
        assertEquals(1.0f, canvas.scale());
        assertEquals(1600 - 100, tr.computedX());

        canvas.setScreenSize(1600, 1200); // altura dobra: escala 2, largura em unidades de UI = 800
        canvas.update();
        assertEquals(2.0f, canvas.scale());
        assertEquals(800 - 100, tr.computedX());
    }

    @Test
    void worldAreaKeepsWorldAspectInsideWindow() {
        canvas.setScreenSize(1600, 600);
        canvas.update();
        UIElement area = canvas.worldArea();
        assertEquals(800, area.width(), 0.01f);
        assertEquals(600, area.height(), 0.01f);
        assertEquals(400, area.computedX(), 0.01f);
    }

    @Test
    void staticFramesDoNotRecomputeLayoutOrMesh() {
        box(Anchor.TOP_LEFT, 0, 0).color(1, 0, 0, 1);
        assertTrue(canvas.update());
        int layouts = canvas.layoutPasses();
        int version = canvas.meshVersion();

        for (int i = 0; i < 100; i++) {
            canvas.setScreenSize(800, 600); // mesmo tamanho: não suja
            assertFalse(canvas.update());
        }
        assertEquals(layouts, canvas.layoutPasses());
        assertEquals(version, canvas.meshVersion());

        canvas.setScreenSize(1024, 768);
        assertTrue(canvas.update());
        assertEquals(layouts + 1, canvas.layoutPasses());
    }

    @Test
    void settingSameValueDoesNotDirty() {
        UIElement e = box(Anchor.TOP_LEFT, 0, 0).color(1, 0, 0, 1);
        canvas.update();
        e.color(1, 0, 0, 1).size(100, 50).offset(0, 0);
        assertFalse(canvas.update());
        e.color(0, 1, 0, 1);
        assertTrue(canvas.update());
    }

    @Test
    void progressBarFillIsProportionalAndDrawnAfterBackground() {
        UIProgressBar bar = new UIProgressBar();
        bar.size(200, 20).color(0.1f, 0.1f, 0.1f, 1);
        bar.range(0, 100).value(25);
        canvas.add(bar);
        canvas.update();

        UIMeshBuilder mesh = canvas.mesh();
        assertEquals(2, mesh.quadCount());
        float[] v = mesh.vertices();
        int stride = UIMeshBuilder.FLOATS_PER_VERTEX;
        float bgWidth = v[stride] - v[0];
        float fillWidth = v[4 * stride + stride] - v[4 * stride];
        assertEquals(200, bgWidth);
        assertEquals(50, fillWidth);
        assertEquals(0.25f, bar.normalized());
    }

    @Test
    void zIndexControlsDrawOrderAndHiddenElementsAreSkipped() {
        box(Anchor.TOP_LEFT, 0, 0).zIndex(5).color(1, 0, 0, 1);
        box(Anchor.TOP_LEFT, 50, 0).zIndex(1).color(0, 1, 0, 1);
        box(Anchor.TOP_LEFT, 99, 0).visible(false);
        canvas.update();

        UIMeshBuilder mesh = canvas.mesh();
        assertEquals(2, mesh.quadCount());
        // o primeiro quad desenhado é o de menor zIndex (verde)
        assertEquals(0f, mesh.vertices()[4]);
        assertEquals(1f, mesh.vertices()[5]);
    }

    @Test
    void nineSliceEmitsNineQuadsWithFixedCorners() {
        UIPanel panel = new UIPanel();
        panel.size(100, 60);
        panel.nineSlice(8, 32, 32);
        canvas.add(panel);
        canvas.update();

        UIMeshBuilder mesh = canvas.mesh();
        assertEquals(9, mesh.quadCount());
        int stride = UIMeshBuilder.FLOATS_PER_VERTEX;
        float[] v = mesh.vertices();
        assertEquals(8, v[stride] - v[0]); // canto superior esquerdo com 8 de largura
        assertEquals(0.25f, v[stride + 2]); // u final do canto = 8/32
    }

    @Test
    void labelMeasuresItselfAndEmitsOneQuadPerVisibleGlyph() {
        canvas.setFont(new FakeFont());
        UILabel label = new UILabel().text("ab c").fontSize(20);
        label.anchor(Anchor.TOP_RIGHT);
        canvas.add(label);
        canvas.update();

        // 4 caracteres * advance 10 * (20/10) = 80 de largura; o espaço não gera quad
        assertEquals(80, label.width());
        assertEquals(20, label.height());
        assertEquals(800 - 80, label.computedX());
        assertEquals(3, canvas.mesh().quadCount());

        label.text("ab c");
        assertFalse(canvas.update());
        label.text("x");
        assertTrue(canvas.update());
        assertEquals(1, canvas.mesh().quadCount());
    }

    @Test
    void batchesSplitOnlyWhenTextureChanges() {
        box(Anchor.TOP_LEFT, 0, 0);
        box(Anchor.TOP_LEFT, 0, 60);
        canvas.update();
        assertEquals(1, canvas.mesh().batches().size());
        assertEquals(2, canvas.mesh().batches().get(0).quadCount());
    }

    /** Fonte sem GL: cada glifo avança 10 e tem 8x10, exceto o espaço (sem desenho). */
    static final class FakeFont implements UIFont {
        public float size() { return 10; }
        public float ascent() { return 8; }
        public float lineHeight() { return 10; }
        public Glyph glyph(int cp) {
            return cp == ' ' ? new Glyph(0, 0, 0, 0, 10, 0, 0, 0, 0) : new Glyph(0, -8, 8, 10, 10, 0, 0, 1, 1);
        }
        public Texture texture() { return null; }
    }
}
