package br.com.beraguasstudio.engine.render;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBImage.*;

import br.com.beraguasstudio.engine.io.Resources;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/** Textura 2D RGBA carregada de um recurso do classpath via stb_image. Requer contexto OpenGL ativo. */
public final class Texture implements AutoCloseable {

    public enum Filter {
        /** Ideal para pixel art. */
        NEAREST(GL_NEAREST),
        LINEAR(GL_LINEAR);

        private final int glValue;

        Filter(int glValue) {
            this.glValue = glValue;
        }
    }

    private final int id;
    private final int width;
    private final int height;

    private Texture(int id, int width, int height) {
        this.id = id;
        this.width = width;
        this.height = height;
    }

    public static Texture load(String resourcePath, Filter filter) {
        byte[] bytes = Resources.readBytes(resourcePath);
        ByteBuffer encoded = MemoryUtil.memAlloc(bytes.length);
        encoded.put(bytes).flip();

        ByteBuffer pixels;
        int width;
        int height;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            stbi_set_flip_vertically_on_load(true); // origem OpenGL: canto inferior esquerdo
            pixels = stbi_load_from_memory(encoded, w, h, channels, 4);
            if (pixels == null) {
                throw new IllegalStateException(
                        "Falha ao decodificar a imagem " + resourcePath + ": " + stbi_failure_reason());
            }
            width = w.get(0);
            height = h.get(0);
        } finally {
            MemoryUtil.memFree(encoded);
        }

        try {
            int id = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, id);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter.glValue);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter.glValue);
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
            return new Texture(id, width, height);
        } finally {
            stbi_image_free(pixels);
        }
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    @Override
    public void close() {
        glDeleteTextures(id);
    }
}
