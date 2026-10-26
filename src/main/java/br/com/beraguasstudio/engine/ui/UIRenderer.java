package br.com.beraguasstudio.engine.ui;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

import br.com.beraguasstudio.engine.render.Shader;
import br.com.beraguasstudio.engine.render.Texture;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Desenha a malha em cache do {@link UICanvas}. O VBO só é reenviado quando {@code meshVersion} muda;
 * em frames estáticos o custo é um bind e um draw por textura. Passe final: viewport e projeção próprias,
 * sem câmera, sem luz — por isso a UI não é afetada por efeitos do mundo.
 */
public final class UIRenderer implements AutoCloseable {

    private static final int STRIDE = UIMeshBuilder.FLOATS_PER_VERTEX * Float.BYTES;

    private final Shader shader = Shader.fromResources("shaders/ui.vert", "shaders/ui.frag");
    private final Matrix4f projection = new Matrix4f();
    private final Texture white;
    private final int vao;
    private final int vbo;
    private final int ebo;
    private int uploadedVersion = -1;
    private int indexCapacityQuads;

    public UIRenderer() {
        ByteBuffer pixel = MemoryUtil.memAlloc(4);
        pixel.put((byte) 255).put((byte) 255).put((byte) 255).put((byte) 255).flip();
        white = Texture.fromPixels(1, 1, pixel, Texture.Filter.NEAREST);
        MemoryUtil.memFree(pixel);

        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, STRIDE, 0L);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, STRIDE, 2L * Float.BYTES);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 4, GL_FLOAT, false, STRIDE, 4L * Float.BYTES);
        glEnableVertexAttribArray(2);
        glBindVertexArray(0);
    }

    /** Desenha a UI no framebuffer inteiro ({@code fbWidth x fbHeight} pixels). */
    public void render(UICanvas canvas, int fbWidth, int fbHeight) {
        UIMeshBuilder mesh = canvas.mesh();
        if (mesh.quadCount() == 0) {
            return;
        }
        glViewport(0, 0, fbWidth, fbHeight);
        glDisable(GL_SCISSOR_TEST);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shader.use();
        projection.identity().ortho(0, fbWidth / canvas.scale(), fbHeight / canvas.scale(), 0, -1f, 1f);
        shader.uploadMat4f("uProjection", projection);
        shader.uploadInt("uTexture", 0);

        glBindVertexArray(vao);
        if (uploadedVersion != canvas.meshVersion()) {
            upload(mesh);
            uploadedVersion = canvas.meshVersion();
        }
        for (UIMeshBuilder.Batch batch : mesh.batches()) {
            (batch.texture() == null ? white : batch.texture()).bind();
            glDrawElements(GL_TRIANGLES, batch.quadCount() * 6, GL_UNSIGNED_INT, (long) batch.firstQuad() * 6 * Integer.BYTES);
        }
        glBindVertexArray(0);
    }

    private void upload(UIMeshBuilder mesh) {
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        // 'orphan' + preenchimento: evita sincronizar com o frame anterior ainda em uso pela GPU
        glBufferData(GL_ARRAY_BUFFER, (long) mesh.floatCount() * Float.BYTES, GL_DYNAMIC_DRAW);
        java.nio.FloatBuffer data = MemoryUtil.memAllocFloat(mesh.floatCount());
        data.put(mesh.vertices(), 0, mesh.floatCount()).flip();
        glBufferSubData(GL_ARRAY_BUFFER, 0L, data);
        MemoryUtil.memFree(data);

        if (mesh.quadCount() > indexCapacityQuads) {
            indexCapacityQuads = Math.max(mesh.quadCount(), indexCapacityQuads * 2);
            IntBuffer indices = MemoryUtil.memAllocInt(indexCapacityQuads * 6);
            for (int q = 0; q < indexCapacityQuads; q++) {
                int v = q * 4;
                indices.put(v).put(v + 1).put(v + 2).put(v + 2).put(v + 3).put(v);
            }
            indices.flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
            MemoryUtil.memFree(indices);
        }
    }

    @Override
    public void close() {
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteVertexArrays(vao);
        white.close();
        shader.close();
    }
}
