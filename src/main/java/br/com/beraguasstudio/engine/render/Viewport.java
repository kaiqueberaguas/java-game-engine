package br.com.beraguasstudio.engine.render;

/** Retângulo de pixels do framebuffer onde o mundo virtual é desenhado (origem no canto inferior esquerdo). */
public record Viewport(int x, int y, int width, int height) {

    /** Maior área com a proporção do mundo que cabe no framebuffer, centralizada (letterbox/pillarbox). */
    public static Viewport fit(int worldWidth, int worldHeight, int framebufferWidth, int framebufferHeight) {
        if (framebufferWidth <= 0 || framebufferHeight <= 0) {
            return new Viewport(0, 0, 0, 0);
        }
        double scale = Math.min((double) framebufferWidth / worldWidth, (double) framebufferHeight / worldHeight);
        int width = (int) Math.round(worldWidth * scale);
        int height = (int) Math.round(worldHeight * scale);
        return new Viewport((framebufferWidth - width) / 2, (framebufferHeight - height) / 2, width, height);
    }
}
