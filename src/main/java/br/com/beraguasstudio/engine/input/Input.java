package br.com.beraguasstudio.engine.input;

import org.joml.Vector2f;

/** Visão de entrada exposta às regras de jogo. {@code keyCode} usa as constantes {@code GLFW_KEY_*}. */
public interface Input {

    boolean isKeyDown(int keyCode);

    /** {@code true} apenas no frame em que a tecla foi pressionada. */
    boolean isKeyJustPressed(int keyCode);

    /** Posição do cursor em coordenadas de janela (origem no canto superior esquerdo). */
    Vector2f getMousePosition();
}
