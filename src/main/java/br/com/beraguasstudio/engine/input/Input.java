package br.com.beraguasstudio.engine.input;

import org.joml.Vector2f;

/** Visão de entrada exposta às regras de jogo. {@code keyCode} usa as constantes {@code GLFW_KEY_*}. */
public interface Input {

    boolean isKeyDown(int keyCode);

    /** {@code true} apenas no frame em que a tecla foi pressionada. */
    boolean isKeyJustPressed(int keyCode);

    /** Posição do cursor em coordenadas de janela (origem no canto superior esquerdo). */
    Vector2f getMousePosition();

    /** {@code button} usa as constantes {@code GLFW_MOUSE_BUTTON_*}. */
    boolean isMouseDown(int button);

    /**
     * {@code true} apenas no frame em que o botão foi pressionado, a menos que a UI tenha consumido
     * o clique ({@link #consumeClick()}); assim o gameplay não reage a cliques na interface.
     */
    boolean isMouseJustPressed(int button);

    /** Marca o clique deste frame como tratado pela UI. */
    void consumeClick();

    boolean isClickConsumed();

    /** W ou seta para cima. */
    boolean up();

    /** S ou seta para baixo. */
    boolean down();

    /** A ou seta para a esquerda. */
    boolean left();

    /** D ou seta para a direita. */
    boolean right();
}
