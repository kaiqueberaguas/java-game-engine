package br.com.beraguasstudio.engine.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.Properties;

/**
 * Propriedades da aplicação: {@code engine.properties} na raiz do classpath (opcional). Propriedades de
 * sistema ({@code -Dchave=valor}) têm precedência sobre o arquivo.
 */
public final class EngineProperties {

    private final Properties properties;

    public EngineProperties(Properties properties) {
        this.properties = properties;
    }

    public static EngineProperties empty() {
        return new EngineProperties(new Properties());
    }

    /** Lê {@link AssetsConfig#PROPERTIES_FILE} do classpath, se existir. */
    public static EngineProperties load() {
        Properties properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = EngineProperties.class.getClassLoader();
        }
        try (InputStream in = loader.getResourceAsStream(AssetsConfig.PROPERTIES_FILE)) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler " + AssetsConfig.PROPERTIES_FILE, e);
        }
        return new EngineProperties(properties);
    }

    /** Valor da chave (propriedade de sistema primeiro), sem espaços nas pontas; vazio se ausente. */
    public Optional<String> get(String key) {
        String value = System.getProperty(key);
        if (value == null) {
            value = properties.getProperty(key);
        }
        return Optional.ofNullable(value).map(String::trim);
    }
}
