package br.com.beraguasstudio.engine.window;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

/** Janela GLFW com contexto OpenGL 3.3 Core. Deve ser criada e usada na thread principal. */
public final class Window implements AutoCloseable {

    private final long handle;
    private int width;
    private int height;
    private int framebufferWidth;
    private int framebufferHeight;

    public Window(String title, int width, int height) {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Falha ao inicializar o GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE); // exigido no macOS

        handle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (handle == NULL) {
            glfwTerminate();
            throw new IllegalStateException("Falha ao criar a janela GLFW");
        }
        this.width = width;
        this.height = height;

        glfwSetWindowSizeCallback(handle, (w, newWidth, newHeight) -> {
            this.width = newWidth;
            this.height = newHeight;
        });
        glfwSetFramebufferSizeCallback(handle, (w, fbWidth, fbHeight) -> {
            this.framebufferWidth = fbWidth;
            this.framebufferHeight = fbHeight;
        });

        glfwMakeContextCurrent(handle);
        glfwSwapInterval(1); // V-Sync
        GL.createCapabilities();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer fbWidth = stack.mallocInt(1);
            IntBuffer fbHeight = stack.mallocInt(1);
            glfwGetFramebufferSize(handle, fbWidth, fbHeight);
            framebufferWidth = fbWidth.get(0);
            framebufferHeight = fbHeight.get(0);
        }

        glfwShowWindow(handle);
    }

    public long handle() {
        return handle;
    }

    /** Largura lógica da janela (coordenadas de tela, não pixels do framebuffer). */
    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    /** Tamanho em pixels do framebuffer (pode diferir da janela em telas HiDPI). */
    public int framebufferWidth() {
        return framebufferWidth;
    }

    public int framebufferHeight() {
        return framebufferHeight;
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }

    public void requestClose() {
        glfwSetWindowShouldClose(handle, true);
    }

    public void pollEvents() {
        glfwPollEvents();
    }

    public void swapBuffers() {
        glfwSwapBuffers(handle);
    }

    @Override
    public void close() {
        Callbacks.glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);
        glfwTerminate();
        GLFWErrorCallback callback = glfwSetErrorCallback(null);
        if (callback != null) {
            callback.free();
        }
    }
}
