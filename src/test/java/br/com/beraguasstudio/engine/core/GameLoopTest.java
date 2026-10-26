package br.com.beraguasstudio.engine.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GameLoopTest {

    private static final double STEP = 0.1;
    private static final double MAX_FRAME = 0.25;

    private double now;
    private boolean closeRequested;
    private int updates;
    private int renders;
    private float lastAlpha;

    private final GameLoop.Callbacks callbacks = new GameLoop.Callbacks() {
        @Override
        public boolean shouldClose() {
            return closeRequested;
        }

        @Override
        public void pollEvents() {
        }

        @Override
        public void fixedUpdate(double dt) {
            updates++;
            assertEquals(STEP, dt);
        }

        @Override
        public void render(float alpha) {
            renders++;
            lastAlpha = alpha;
        }
    };

    private final GameLoop loop = new GameLoop(callbacks, () -> now, STEP, MAX_FRAME);

    @Test
    void consumesAccumulatedTimeInFixedSteps() {
        now = 0.25;
        assertTrue(loop.tick());

        assertEquals(2, updates);
        assertEquals(1, renders);
        assertEquals(0.5f, lastAlpha, 1e-5f);
    }

    @Test
    void carriesRemainderAcrossFrames() {
        now = 0.06;
        loop.tick();
        assertEquals(0, updates);
        assertEquals(0.6f, lastAlpha, 1e-5f);

        now = 0.12;
        loop.tick();
        assertEquals(1, updates);
        assertEquals(0.2f, lastAlpha, 1e-5f);
    }

    @Test
    void capsFrameTimeToAvoidSpiralOfDeath() {
        now = 10.0;
        loop.tick();

        assertEquals(2, updates); // 0.25 / 0.1
    }

    @Test
    void stopsWhenWindowCloses() {
        closeRequested = true;

        assertFalse(loop.tick());
        assertEquals(0, renders);
    }

    @Test
    void stopsAfterStopCall() {
        loop.stop();

        assertFalse(loop.tick());
    }
}
