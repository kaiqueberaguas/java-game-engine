package br.com.beraguasstudio.engine.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ViewportTest {

    @Test
    void sameSizeFillsFramebuffer() {
        assertEquals(new Viewport(0, 0, 800, 600), Viewport.fit(800, 600, 800, 600));
    }

    @Test
    void biggerWindowScalesUp() {
        assertEquals(new Viewport(0, 0, 1600, 1200), Viewport.fit(800, 600, 1600, 1200));
    }

    @Test
    void widerWindowGetsSideBars() {
        assertEquals(new Viewport(200, 0, 800, 600), Viewport.fit(800, 600, 1200, 600));
    }

    @Test
    void tallerWindowGetsTopAndBottomBars() {
        assertEquals(new Viewport(0, 150, 800, 600), Viewport.fit(800, 600, 800, 900));
    }

    @Test
    void minimizedWindowYieldsEmptyViewport() {
        assertEquals(new Viewport(0, 0, 0, 0), Viewport.fit(800, 600, 0, 0));
    }
}
