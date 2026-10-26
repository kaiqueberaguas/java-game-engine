package br.com.beraguasstudio.engine.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import org.junit.jupiter.api.Test;

class GameInputSystemTest {

    private final GameInputSystem input = new GameInputSystem();

    @Test
    void keyIsJustPressedOnlyOnItsFirstFrame() {
        input.beginFrame();
        input.onKey(GLFW_KEY_SPACE, GLFW_PRESS);
        assertTrue(input.isKeyDown(GLFW_KEY_SPACE));
        assertTrue(input.isKeyJustPressed(GLFW_KEY_SPACE));

        input.beginFrame();
        assertTrue(input.isKeyDown(GLFW_KEY_SPACE));
        assertFalse(input.isKeyJustPressed(GLFW_KEY_SPACE));
    }

    @Test
    void directionsAcceptWasdAndArrows() {
        input.onKey(GLFW_KEY_W, GLFW_PRESS);
        input.onKey(GLFW_KEY_LEFT, GLFW_PRESS);
        assertTrue(input.up());
        assertTrue(input.left());
        assertFalse(input.down());
        assertFalse(input.right());

        input.onKey(GLFW_KEY_DOWN, GLFW_PRESS);
        input.onKey(GLFW_KEY_D, GLFW_PRESS);
        assertTrue(input.down());
        assertTrue(input.right());
    }

    @Test
    void releaseClearsKey() {
        input.onKey(GLFW_KEY_SPACE, GLFW_PRESS);
        input.beginFrame();
        input.onKey(GLFW_KEY_SPACE, GLFW_RELEASE);

        assertFalse(input.isKeyDown(GLFW_KEY_SPACE));
    }

    @Test
    void unknownKeysAreIgnored() {
        input.onKey(-1, GLFW_PRESS);

        assertFalse(input.isKeyDown(-1));
        assertFalse(input.isKeyJustPressed(-1));
    }

    @Test
    void tracksMousePosition() {
        input.onCursorPos(12.5, 40);

        assertEquals(12.5f, input.getMousePosition().x);
        assertEquals(40f, input.getMousePosition().y);
    }
}
