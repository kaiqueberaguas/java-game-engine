package br.com.beraguasstudio.engine.render;

import static org.lwjgl.opengl.GL20C.*;

import br.com.beraguasstudio.engine.io.Resources;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

/** Programa GLSL (vertex + fragment). Requer contexto OpenGL ativo. */
public final class Shader implements AutoCloseable {

    private final int program;
    private final Map<String, Integer> uniforms = new HashMap<>();

    private Shader(String vertexSource, String fragmentSource) {
        int vertex = compile(GL_VERTEX_SHADER, vertexSource);
        int fragment = compile(GL_FRAGMENT_SHADER, fragmentSource);

        program = glCreateProgram();
        glAttachShader(program, vertex);
        glAttachShader(program, fragment);
        glLinkProgram(program);
        boolean linked = glGetProgrami(program, GL_LINK_STATUS) != GL_FALSE;
        String log = glGetProgramInfoLog(program);

        glDeleteShader(vertex);
        glDeleteShader(fragment);

        if (!linked) {
            glDeleteProgram(program);
            throw new IllegalStateException("Falha ao linkar o shader: " + log);
        }
    }

    /** Carrega e compila os fontes GLSL a partir do classpath. */
    public static Shader fromResources(String vertexPath, String fragmentPath) {
        return new Shader(Resources.readText(vertexPath), Resources.readText(fragmentPath));
    }

    public void use() {
        glUseProgram(program);
    }

    public void uploadMat4f(String name, Matrix4f matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            matrix.get(buffer);
            glUniformMatrix4fv(location(name), false, buffer);
        }
    }

    public void uploadInt(String name, int value) {
        glUniform1i(location(name), value);
    }

    private int location(String name) {
        return uniforms.computeIfAbsent(name, n -> glGetUniformLocation(program, n));
    }

    @Override
    public void close() {
        glDeleteProgram(program);
    }

    private static int compile(int type, String source) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shader);
            glDeleteShader(shader);
            throw new IllegalStateException("Falha ao compilar o shader: " + log);
        }
        return shader;
    }
}
