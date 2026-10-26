package br.com.beraguasstudio.engine.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/** Leitura de recursos do classpath. */
public final class Resources {

    private Resources() {
    }

    public static byte[] readBytes(String path) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = Resources.class.getClassLoader();
        }
        try (InputStream in = loader.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalArgumentException("Recurso não encontrado no classpath: " + path);
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler recurso: " + path, e);
        }
    }

    public static String readText(String path) {
        return new String(readBytes(path), StandardCharsets.UTF_8);
    }
}
