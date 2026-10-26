package br.com.beraguasstudio.engine.config;

import br.com.beraguasstudio.engine.io.EngineProperties;

/**
 * Configuração da janela/mundo. Por padrão lê {@code app.name}, {@code app.width} e {@code app.height} de
 * {@code engine.properties} (ou de {@code -Dchave=valor}); ausentes, valem {@code ""}, 800 e 600. Os
 * construtores com valores explícitos sobrescrevem as propriedades.
 */
public class EngineConfig {

    public static final String NAME_PROPERTY = "app.name";
    public static final String WIDTH_PROPERTY = "app.width";
    public static final String HEIGHT_PROPERTY = "app.height";

    public static final String DEFAULT_NAME = "";
    public static final int DEFAULT_WIDTH = 800;
    public static final int DEFAULT_HEIGHT = 600;

    private String title;
    private int width;
    private int height;
    private float clearR = 0.1f;
    private float clearG = 0.1f;
    private float clearB = 0.15f;

    /** Tudo vem de {@code engine.properties} do classpath. */
    public EngineConfig() {
        this(EngineProperties.load());
    }

    public EngineConfig(EngineProperties properties) {
        this.title = properties.get(NAME_PROPERTY).orElse(DEFAULT_NAME);
        this.width = intProperty(properties, WIDTH_PROPERTY, DEFAULT_WIDTH);
        this.height = intProperty(properties, HEIGHT_PROPERTY, DEFAULT_HEIGHT);
    }

    /** Valores informados sobrescrevem as propriedades. */
    public EngineConfig(String title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;
    }

    private static int intProperty(EngineProperties properties, String key, int fallback) {
        return properties.get(key).filter(v -> !v.isEmpty()).map(v -> {
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Propriedade " + key + " inválida: '" + v + "'", e);
            }
        }).orElse(fallback);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public float getClearR() {
        return clearR;
    }

    public void setClearR(float clearR) {
        this.clearR = clearR;
    }

    public float getClearG() {
        return clearG;
    }

    public void setClearG(float clearG) {
        this.clearG = clearG;
    }

    public float getClearB() {
        return clearB;
    }

    public void setClearB(float clearB) {
        this.clearB = clearB;
    }
}
