package br.com.beraguasstudio.engine.gameobject;

import br.com.beraguasstudio.engine.input.Input;

import java.util.Objects;
import java.util.Optional;

/**
 * Objeto de jogo em estilo OO que esconde o ECS: a engine cria a entidade, o transform, o sprite e o
 * ciclo de vida; a subclasse só implementa {@link #onStart()} e {@link #update(double)}. Instanciado
 * pela engine via {@code Engine.spawn(Classe.class)}, portanto a subclasse precisa de construtor sem
 * argumentos que repasse o caminho do sprite a {@code super(...)}.
 */
public abstract class GameObject {

    private final String spritePath;
    private GameObjectManager manager;
    private Sprite sprite;
    private boolean active = true;

    /** Posição, escala e rotação deste objeto; disponível a partir do spawn (não no construtor). */
    protected Transform transform;

    /**
     * @param spritePath arquivo do sprite relativo à pasta de sprites configurada
     *     ({@code engine.assets.sprites.path}, padrão {@code /assets/sprites}), ex.: {@code "player.png"}
     */
    protected GameObject(String spritePath) {
        this.spritePath = Objects.requireNonNull(spritePath, "spritePath");
    }

    /** Chamado uma vez, antes do primeiro {@link #update}. Estado inicial (posição, escala) entra aqui. */
    public void onStart() {
    }

    /** Chamado a cada passo fixo de simulação. */
    public void update(double deltaTime) {
    }

    /** Remove o objeto (e a entidade) no próximo passo. */
    public final void destroy() {
        active = false;
    }

    public final boolean isActive() {
        return active;
    }

    /** Acesso de fora para posicionar outro objeto (ex.: logo após {@link #spawn}); dentro use o campo. */
    public final Transform transform() {
        return transform;
    }

    public final Sprite sprite() {
        return sprite;
    }

    /** Largura do mundo em unidades de jogo (a resolução virtual, independente do tamanho da janela). */
    protected final int worldWidth() {
        return manager.worldWidth();
    }

    protected final int worldHeight() {
        return manager.worldHeight();
    }

    protected final Input input() {
        return manager.input();
    }

    /** Primeiro objeto ativo do tipo dado. */
    protected final <T extends GameObject> Optional<T> find(Class<T> type) {
        return manager.find(type);
    }

    /**
     * Cria outro objeto de jogo (construtor sem argumentos). O transform já está disponível no retorno,
     * antes do {@link #onStart()} do novo objeto, que roda no próximo passo.
     */
    protected final <T extends GameObject> T spawn(Class<T> type) {
        return manager.spawn(type);
    }

    /** Pede o fim do jogo (fecha a janela). */
    protected final void stopGame() {
        manager.stopGame();
    }

    final String spritePath() {
        return spritePath;
    }

    final void attach(GameObjectManager manager, Transform transform, Sprite sprite) {
        this.manager = manager;
        this.transform = transform;
        this.sprite = sprite;
    }
}
