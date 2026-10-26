package br.com.beraguasstudio.engine;

import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL11C.glDisable;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL11C.glScissor;
import static org.lwjgl.opengl.GL11C.glViewport;

import br.com.beraguasstudio.engine.config.EngineConfig;
import br.com.beraguasstudio.engine.core.GameLoop;
import br.com.beraguasstudio.engine.ecs.PreviousTransformSystem;
import br.com.beraguasstudio.engine.ecs.RenderSystem;
import br.com.beraguasstudio.engine.input.GameInputSystem;
import br.com.beraguasstudio.engine.input.Input;
import br.com.beraguasstudio.engine.gameobject.DomainLifecycleSystem;
import br.com.beraguasstudio.engine.gameobject.GameObject;
import br.com.beraguasstudio.engine.gameobject.GameObjectManager;
import br.com.beraguasstudio.engine.io.AssetsConfig;
import br.com.beraguasstudio.engine.io.EngineProperties;
import br.com.beraguasstudio.engine.render.SpriteRenderer;
import br.com.beraguasstudio.engine.render.Viewport;
import br.com.beraguasstudio.engine.render.Texture;
import br.com.beraguasstudio.engine.window.Window;
import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Junta janela, entrada, renderer e o World do artemis-odb. Deve ser usada na thread principal.
 * Sistemas do jogo recebem {@link Input} via {@code @Wire}.
 */
public final class Engine implements AutoCloseable {

    private final EngineConfig config;
    private final Window window;
    private final GameInputSystem input = new GameInputSystem();
    private final SpriteRenderer spriteRenderer;
    private final RenderSystem renderSystem;
    private final GameObjectManager gameObjects;
    private final Map<String, Texture> textures = new HashMap<>();
    private final World world;
    private GameLoop loop;

    private Engine(EngineConfig config, Consumer<WorldConfigurationBuilder> customize) {
        this.config = config;
        this.window = new Window(config.getTitle(), config.getWidth(), config.getHeight());
        input.attach(window.handle());
        this.spriteRenderer = new SpriteRenderer();
        this.renderSystem = new RenderSystem(spriteRenderer);
        this.gameObjects = new GameObjectManager(input, AssetsConfig.load(), EngineProperties.load(),
                config.getWidth(), config.getHeight(), this::loadTexture, this::stop);
        this.world = buildWorld(input, renderSystem, gameObjects, customize);
        gameObjects.bind(world);
    }

    private Texture loadTexture(String resourcePath) {
        return textures.computeIfAbsent(resourcePath, p -> Texture.load(p, Texture.Filter.NEAREST));
    }

    /**
     * Cria janela e contexto OpenGL. {@code customize} registra os sistemas do jogo
     * (executados depois dos sistemas internos, em ordem).
     */
    public static Engine start(EngineConfig config, Consumer<WorldConfigurationBuilder> customize) {
        return new Engine(config, customize);
    }

    /** Usa {@code new EngineConfig()}: título e tamanho vêm de {@code engine.properties}. */
    public static Engine start() {
        return start(new EngineConfig());
    }

    public static Engine start(EngineConfig config) {
        return start(config, builder -> { });
    }

    /** Sem dependência de GL: permite testar a montagem do World headless. */
    static World buildWorld(Input input, RenderSystem renderSystem, GameObjectManager gameObjects,
            Consumer<WorldConfigurationBuilder> customize) {
        WorldConfigurationBuilder builder = new WorldConfigurationBuilder()
                .with(new PreviousTransformSystem())
                .with(new DomainLifecycleSystem());
        customize.accept(builder);
        builder.with(renderSystem);

        WorldConfiguration worldConfig = builder.build();
        worldConfig.register(Input.class.getName(), input);
        worldConfig.register(GameObjectManager.class.getName(), gameObjects);
        return new World(worldConfig);
    }

    /** Sem GL e sem texturas: monta o World com um gerenciador de GameObjects próprio, já ligado. */
    static World buildWorld(Input input, RenderSystem renderSystem, Consumer<WorldConfigurationBuilder> customize) {
        GameObjectManager manager = new GameObjectManager(input, AssetsConfig.defaults(), 800, 600, null, () -> { });
        World world = buildWorld(input, renderSystem, manager, customize);
        manager.bind(world);
        return world;
    }

    /**
     * Cria um {@link GameObject} por reflection (construtor sem argumentos) e o registra no mundo;
     * o {@code onStart} roda no próximo passo fixo.
     */
    public <T extends GameObject> T spawn(Class<T> type) {
        return gameObjects.spawn(type);
    }

    public World world() {
        return world;
    }

    public Input input() {
        return input;
    }

    public Window window() {
        return window;
    }

    /** Bloqueia até a janela fechar ou {@link #stop()} ser chamado. */
    public void run() {
        loop = new GameLoop(new GameLoop.Callbacks() {
            @Override
            public boolean shouldClose() {
                return window.shouldClose();
            }

            @Override
            public void pollEvents() {
                input.beginFrame();
                window.pollEvents();
            }

            @Override
            public void fixedUpdate(double dt) {
                world.setDelta((float) dt);
                world.process();
            }

            @Override
            public void render(float alpha) {
                int fbWidth = window.framebufferWidth();
                int fbHeight = window.framebufferHeight();
                Viewport view = Viewport.fit(config.getWidth(), config.getHeight(), fbWidth, fbHeight);

                // barras do letterbox, depois o mundo virtual dentro da área útil
                glDisable(GL_SCISSOR_TEST);
                glViewport(0, 0, fbWidth, fbHeight);
                glClearColor(0f, 0f, 0f, 1f);
                glClear(GL_COLOR_BUFFER_BIT);

                glViewport(view.x(), view.y(), view.width(), view.height());
                glEnable(GL_SCISSOR_TEST);
                glScissor(view.x(), view.y(), view.width(), view.height());
                glClearColor(config.getClearR(), config.getClearG(), config.getClearB(), 1f);
                glClear(GL_COLOR_BUFFER_BIT);
                renderSystem.render(alpha, config.getWidth(), config.getHeight());
                glDisable(GL_SCISSOR_TEST);
                window.swapBuffers();
            }
        });
        loop.run();
    }

    public void stop() {
        window.requestClose();
    }

    @Override
    public void close() {
        world.dispose();
        textures.values().forEach(Texture::close);
        textures.clear();
        spriteRenderer.close();
        window.close();
    }
}
