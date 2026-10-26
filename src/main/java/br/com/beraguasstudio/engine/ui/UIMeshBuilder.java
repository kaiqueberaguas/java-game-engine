package br.com.beraguasstudio.engine.ui;

import br.com.beraguasstudio.engine.render.Texture;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Acumula os quads da UI em ordem de desenho. Vértices: x, y, u, v, r, g, b, a (8 floats), 4 por quad.
 * Quads consecutivos com a mesma textura formam um {@link Batch}. Textura {@code null} = branca (cor sólida).
 */
public final class UIMeshBuilder {

    public static final int FLOATS_PER_VERTEX = 8;

    /** {@code firstQuad}/{@code quadCount} indexam quads; cada quad tem 6 índices. */
    public record Batch(Texture texture, int firstQuad, int quadCount) { }

    private float[] vertices = new float[FLOATS_PER_VERTEX * 4 * 64];
    private int quads;
    private final List<Batch> batches = new ArrayList<>();
    private Texture currentTexture;
    private int currentFirst;

    void clear() {
        quads = 0;
        batches.clear();
        currentTexture = null;
        currentFirst = 0;
    }

    void finish() {
        if (quads > currentFirst) {
            batches.add(new Batch(currentTexture, currentFirst, quads - currentFirst));
        }
        currentFirst = quads;
    }

    public void quad(Texture texture, float x, float y, float w, float h,
            float u0, float v0, float u1, float v1, Vector4f c) {
        if (quads > currentFirst && texture != currentTexture) {
            batches.add(new Batch(currentTexture, currentFirst, quads - currentFirst));
            currentFirst = quads;
        }
        currentTexture = texture;
        int needed = (quads + 1) * 4 * FLOATS_PER_VERTEX;
        if (needed > vertices.length) {
            vertices = Arrays.copyOf(vertices, Math.max(needed, vertices.length * 2));
        }
        int i = quads * 4 * FLOATS_PER_VERTEX;
        i = vertex(i, x, y, u0, v0, c);
        i = vertex(i, x + w, y, u1, v0, c);
        i = vertex(i, x + w, y + h, u1, v1, c);
        vertex(i, x, y + h, u0, v1, c);
        quads++;
    }

    private int vertex(int i, float x, float y, float u, float v, Vector4f c) {
        vertices[i] = x;
        vertices[i + 1] = y;
        vertices[i + 2] = u;
        vertices[i + 3] = v;
        vertices[i + 4] = c.x;
        vertices[i + 5] = c.y;
        vertices[i + 6] = c.z;
        vertices[i + 7] = c.w;
        return i + FLOATS_PER_VERTEX;
    }

    public float[] vertices() { return vertices; }
    public int quadCount() { return quads; }
    public int floatCount() { return quads * 4 * FLOATS_PER_VERTEX; }
    public List<Batch> batches() { return batches; }
}
