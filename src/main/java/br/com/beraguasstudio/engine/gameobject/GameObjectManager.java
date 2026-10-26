package br.com.beraguasstudio.engine.gameobject;

import br.com.beraguasstudio.engine.ecs.SpriteComponent;
import br.com.beraguasstudio.engine.ecs.TransformComponent;
import br.com.beraguasstudio.engine.input.Input;
import br.com.beraguasstudio.engine.io.AssetsConfig;
import br.com.beraguasstudio.engine.io.EngineProperties;
import br.com.beraguasstudio.engine.render.Texture;
import com.artemis.EntityEdit;
import com.artemis.World;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Cria {@link GameObject}s por reflection (construtor resolvido uma vez por classe), carrega o sprite e
 * liga cada um a uma entidade do World. Só a thread principal deve usá-lo.
 */
public final class GameObjectManager {

    private final Input input;
    private final AssetsConfig assets;
    private final int worldWidth;
    private final int worldHeight;
    private final Function<String, Texture> textureLoader;
    private final Runnable stopAction;
    private final ValueInjector valueInjector;
    private final Map<Class<?>, Constructor<? extends GameObject>> constructors = new HashMap<>();
    private final List<GameObject> objects = new ArrayList<>();
    private World world;

    public GameObjectManager(Input input, AssetsConfig assets, int worldWidth, int worldHeight,
            Function<String, Texture> textureLoader, Runnable stopAction) {
        this(input, assets, EngineProperties.empty(), worldWidth, worldHeight, textureLoader, stopAction);
    }

    public GameObjectManager(Input input, AssetsConfig assets, EngineProperties properties, int worldWidth,
            int worldHeight, Function<String, Texture> textureLoader, Runnable stopAction) {
        this.valueInjector = new ValueInjector(properties);
        this.input = input;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.assets = assets;
        this.textureLoader = textureLoader;
        this.stopAction = stopAction;
    }

    /** Liga ao World já construído; deve ser chamado antes do primeiro {@link #spawn}. */
    public void bind(World world) {
        this.world = world;
    }

    public <T extends GameObject> T spawn(Class<T> type) {
        if (world == null) {
            throw new IllegalStateException("GameObjectManager não está ligado a um World");
        }
        T obj = type.cast(instantiate(constructors.computeIfAbsent(type, GameObjectManager::constructorOf)));
        valueInjector.inject(obj);

        EntityEdit edit = world.edit(world.create());
        TransformComponent transformComponent = edit.create(TransformComponent.class);
        SpriteComponent spriteComponent = edit.create(SpriteComponent.class);
        spriteComponent.texture = loadTexture(assets.sprite(obj.spritePath()));
        edit.create(DomainBindingComponent.class).instance = obj;

        obj.attach(this, new Transform(transformComponent), new Sprite(spriteComponent, transformComponent));
        objects.add(obj);
        return obj;
    }

    int worldWidth() {
        return worldWidth;
    }

    int worldHeight() {
        return worldHeight;
    }

    Input input() {
        return input;
    }

    Texture loadTexture(String resourcePath) {
        if (textureLoader == null) {
            throw new IllegalStateException("Sem carregador de texturas configurado");
        }
        return textureLoader.apply(resourcePath);
    }

    void stopGame() {
        stopAction.run();
    }

    <T extends GameObject> Optional<T> find(Class<T> type) {
        for (GameObject obj : objects) {
            if (obj.isActive() && type.isInstance(obj)) {
                return Optional.of(type.cast(obj));
            }
        }
        return Optional.empty();
    }

    void unregister(GameObject obj) {
        objects.remove(obj);
    }

    private static Constructor<? extends GameObject> constructorOf(Class<?> type) {
        try {
            Constructor<? extends GameObject> constructor = type.asSubclass(GameObject.class).getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(type.getName() + " precisa de construtor sem argumentos", e);
        }
    }

    private static GameObject instantiate(Constructor<? extends GameObject> constructor) {
        try {
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Falha ao instanciar " + constructor.getDeclaringClass(), e);
        }
    }
}
