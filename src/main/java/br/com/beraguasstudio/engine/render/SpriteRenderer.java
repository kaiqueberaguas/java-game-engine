package br.com.beraguasstudio.engine.render;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

import org.joml.Matrix4f;

/**
 * Desenha sprites como quads texturizados. O quad unitário é centrado na origem,
 * então a posição passada a {@link #draw} é o centro do sprite. Coordenadas de mundo
 * em pixels, origem no canto inferior esquerdo, eixo Y para cima.
 */
public final class SpriteRenderer implements AutoCloseable {

    // posição (x, y) + UV (u, v)
    private static final float[] VERTICES = {
            -0.5f, -0.5f, 0f, 0f,
             0.5f, -0.5f, 1f, 0f,
             0.5f,  0.5f, 1f, 1f,
            -0.5f,  0.5f, 0f, 1f,
    };
    private static final int[] INDICES = {0, 1, 2, 2, 3, 0};
    private static final int FLOAT_BYTES = Float.BYTES;

    private final Shader shader = Shader.fromResources("shaders/sprite.vert", "shaders/sprite.frag");
    private final Matrix4f projection = new Matrix4f();
    private final Matrix4f model = new Matrix4f();
    private final int vao;
    private final int vbo;
    private final int ebo;

    public SpriteRenderer() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, VERTICES, GL_STATIC_DRAW);

        ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, INDICES, GL_STATIC_DRAW);

        int stride = 4 * FLOAT_BYTES;
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0L);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 2L * FLOAT_BYTES);
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);
    }

    /** Prepara o desenho de um frame para uma viewport de {@code width x height} unidades de mundo. */
    public void begin(int width, int height) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        projection.identity().ortho(0, width, 0, height, -1f, 1f);
        shader.use();
        shader.uploadMat4f("uProjection", projection);
        shader.uploadInt("uTexture", 0);
        glBindVertexArray(vao);
    }

    public void draw(Texture texture, float x, float y, float width, float height, float rotation) {
        model.identity().translate(x, y, 0f).rotateZ(rotation).scale(width, height, 1f);
        shader.uploadMat4f("uModel", model);
        texture.bind();
        glDrawElements(GL_TRIANGLES, INDICES.length, GL_UNSIGNED_INT, 0L);
    }

    @Override
    public void close() {
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteVertexArrays(vao);
        shader.close();
    }
}
