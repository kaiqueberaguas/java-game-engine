package br.com.beraguasstudio.engine.ui;

import static org.lwjgl.stb.STBTruetype.stbtt_BakeFontBitmap;
import static org.lwjgl.stb.STBTruetype.stbtt_GetFontVMetrics;
import static org.lwjgl.stb.STBTruetype.stbtt_InitFont;
import static org.lwjgl.stb.STBTruetype.stbtt_ScaleForPixelHeight;

import br.com.beraguasstudio.engine.io.Resources;
import br.com.beraguasstudio.engine.render.Texture;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Atlas de glifos (Latin-1) gerado com stb_truetype a partir de uma TTF do classpath. Requer contexto GL
 * (cria a textura). O atlas é assado uma vez num tamanho fixo e os rótulos o escalam.
 */
public final class FontAtlas implements UIFont, AutoCloseable {

    /** Fonte padrão da engine (Roboto Regular, Apache 2.0). */
    public static final String DEFAULT_FONT = "fonts/Roboto-Regular.ttf";

    private static final int FIRST_CHAR = 32;
    private static final int CHAR_COUNT = 224; // 32..255
    private static final int ATLAS_SIZE = 512;

    private final float size;
    private final float ascent;
    private final float lineHeight;
    private final Glyph[] glyphs = new Glyph[CHAR_COUNT];
    private final Texture texture;

    public FontAtlas(String resourcePath, float pixelSize) {
        this.size = pixelSize;
        byte[] bytes = Resources.readBytes(resourcePath);
        ByteBuffer ttf = MemoryUtil.memAlloc(bytes.length);
        ttf.put(bytes).flip();
        STBTTBakedChar.Buffer chars = STBTTBakedChar.malloc(CHAR_COUNT);
        ByteBuffer coverage = MemoryUtil.memAlloc(ATLAS_SIZE * ATLAS_SIZE);
        ByteBuffer rgba = MemoryUtil.memAlloc(ATLAS_SIZE * ATLAS_SIZE * 4);
        try (STBTTFontinfo info = STBTTFontinfo.malloc(); MemoryStack stack = MemoryStack.stackPush()) {
            if (stbtt_BakeFontBitmap(ttf, pixelSize, coverage, ATLAS_SIZE, ATLAS_SIZE, FIRST_CHAR, chars) <= 0) {
                throw new IllegalStateException("Atlas de fonte pequeno demais para " + resourcePath);
            }
            if (!stbtt_InitFont(info, ttf)) {
                throw new IllegalStateException("Fonte inválida: " + resourcePath);
            }
            IntBuffer asc = stack.mallocInt(1);
            IntBuffer desc = stack.mallocInt(1);
            IntBuffer gap = stack.mallocInt(1);
            stbtt_GetFontVMetrics(info, asc, desc, gap);
            float scale = stbtt_ScaleForPixelHeight(info, pixelSize);
            ascent = asc.get(0) * scale;
            lineHeight = (asc.get(0) - desc.get(0) + gap.get(0)) * scale;

            for (int i = 0; i < CHAR_COUNT; i++) {
                STBTTBakedChar c = chars.get(i);
                glyphs[i] = new Glyph(c.xoff(), c.yoff(), c.x1() - c.x0(), c.y1() - c.y0(), c.xadvance(),
                        (float) c.x0() / ATLAS_SIZE, (float) c.y0() / ATLAS_SIZE,
                        (float) c.x1() / ATLAS_SIZE, (float) c.y1() / ATLAS_SIZE);
            }

            // branco com alfa = cobertura; linhas invertidas (convenção das texturas: linha 0 embaixo)
            for (int row = 0; row < ATLAS_SIZE; row++) {
                int src = (ATLAS_SIZE - 1 - row) * ATLAS_SIZE;
                for (int col = 0; col < ATLAS_SIZE; col++) {
                    rgba.put((byte) 255).put((byte) 255).put((byte) 255).put(coverage.get(src + col));
                }
            }
            rgba.flip();
            texture = Texture.fromPixels(ATLAS_SIZE, ATLAS_SIZE, rgba, Texture.Filter.LINEAR);
        } finally {
            MemoryUtil.memFree(rgba);
            MemoryUtil.memFree(coverage);
            chars.free();
            MemoryUtil.memFree(ttf);
        }
    }

    @Override
    public float size() {
        return size;
    }

    @Override
    public float ascent() {
        return ascent;
    }

    @Override
    public float lineHeight() {
        return lineHeight;
    }

    @Override
    public Glyph glyph(int codepoint) {
        int i = codepoint - FIRST_CHAR;
        return i >= 0 && i < CHAR_COUNT ? glyphs[i] : glyphs['?' - FIRST_CHAR];
    }

    @Override
    public Texture texture() {
        return texture;
    }

    @Override
    public void close() {
        texture.close();
    }
}
