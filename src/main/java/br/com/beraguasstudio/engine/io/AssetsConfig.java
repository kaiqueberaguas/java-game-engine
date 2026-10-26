package br.com.beraguasstudio.engine.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * Pastas de assets no classpath. Padrão: {@code /assets/sprites} e {@code /assets/sounds}; podem ser
 * trocadas em {@code engine.properties} (raiz do classpath, opcional) ou por propriedade de sistema
 * ({@code -Dengine.assets.sprites.path=...}), que tem precedência.
 */
public record AssetsConfig(String spritesPath, String soundsPath) {

    public static final String DEFAULT_SPRITES_PATH = "/assets/sprites";
    public static final String DEFAULT_SOUNDS_PATH = "/assets/sounds";
    public static final String SPRITES_PROPERTY = "engine.assets.sprites.path";
    public static final String SOUNDS_PROPERTY = "engine.assets.sounds.path";
    public static final String PROPERTIES_FILE = "engine.properties";

    public AssetsConfig {
        spritesPath = normalize(spritesPath);
        soundsPath = normalize(soundsPath);
    }

    public static AssetsConfig defaults() {
        return new AssetsConfig(DEFAULT_SPRITES_PATH, DEFAULT_SOUNDS_PATH);
    }

    /** Propriedades ausentes ou em branco mantêm o padrão. */
    public static AssetsConfig from(Properties properties) {
        return new AssetsConfig(
                value(properties, SPRITES_PROPERTY, DEFAULT_SPRITES_PATH),
                value(properties, SOUNDS_PROPERTY, DEFAULT_SOUNDS_PATH));
    }

    /** Lê {@code engine.properties} do classpath (se existir) e aplica as propriedades de sistema por cima. */
    public static AssetsConfig load() {
        Properties properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = AssetsConfig.class.getClassLoader();
        }
        try (InputStream in = loader.getResourceAsStream(PROPERTIES_FILE)) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler " + PROPERTIES_FILE, e);
        }
        for (String key : new String[] {SPRITES_PROPERTY, SOUNDS_PROPERTY}) {
            String override = System.getProperty(key);
            if (override != null) {
                properties.setProperty(key, override);
            }
        }
        return from(properties);
    }

    /** Caminho de classpath (sem barra inicial) do sprite {@code name}, que pode conter subpastas. */
    public String sprite(String name) {
        return resolve(spritesPath, name);
    }

    public String sound(String name) {
        return resolve(soundsPath, name);
    }

    private static String resolve(String base, String name) {
        String file = normalize(name);
        return base.isEmpty() ? file : base + "/" + file;
    }

    private static String value(Properties properties, String key, String fallback) {
        String value = properties.getProperty(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    /** Remove espaços e barras nas pontas: {@code "/assets/sprites/"} vira {@code "assets/sprites"}. */
    private static String normalize(String path) {
        String p = path.trim().replace('\\', '/');
        int start = 0;
        int end = p.length();
        while (start < end && p.charAt(start) == '/') {
            start++;
        }
        while (end > start && p.charAt(end - 1) == '/') {
            end--;
        }
        return p.substring(start, end);
    }
}
