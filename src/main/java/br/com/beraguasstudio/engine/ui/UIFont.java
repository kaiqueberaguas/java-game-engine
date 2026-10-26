package br.com.beraguasstudio.engine.ui;

import br.com.beraguasstudio.engine.render.Texture;

/** Fonte de glifos pré-renderizados em um atlas. Interface para o layout de texto rodar sem GL. */
public interface UIFont {

    /**
     * Medidas em pixels do tamanho {@link #size()}, relativas ao cursor na baseline; UVs normalizados
     * (v crescente para baixo no atlas).
     */
    record Glyph(float xOffset, float yOffset, float width, float height, float advance,
            float u0, float v0, float u1, float v1) { }

    /** Tamanho em pixels em que o atlas foi gerado. */
    float size();

    /** Distância do topo da linha até a baseline, em pixels de {@link #size()}. */
    float ascent();

    float lineHeight();

    /** {@code null} se o caractere não existir na fonte. */
    Glyph glyph(int codepoint);

    Texture texture();
}
