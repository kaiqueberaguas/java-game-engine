package br.com.beraguasstudio.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.beraguasstudio.engine.ecs.RenderSystem;
import br.com.beraguasstudio.engine.gameobject.DomainLifecycleSystem;
import br.com.beraguasstudio.engine.gameobject.GameObject;
import br.com.beraguasstudio.engine.gameobject.GameObjectManager;
import br.com.beraguasstudio.engine.gameobject.Transform;
import br.com.beraguasstudio.engine.input.GameInputSystem;
import br.com.beraguasstudio.engine.io.AssetsConfig;
import com.artemis.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Ciclo de vida dos GameObjects num World headless (sem GL; sprites sem textura). */
class GameObjectTest {

    public static class Probe extends GameObject {
        int starts;
        final List<Double> deltas = new ArrayList<>();

        public Probe() {
            super("probe.png");
        }

        @Override
        public void onStart() {
            starts++;
        }

        @Override
        public void update(double deltaTime) {
            deltas.add(deltaTime);
        }

        Transform exposedTransform() {
            return transform;
        }

        Optional<Probe> findProbe() {
            return find(Probe.class);
        }
    }

    public static class NoDefaultConstructor extends GameObject {
        public NoDefaultConstructor(int ignored) {
            super("x.png");
        }
    }

    private World world;
    private GameObjectManager manager;

    @BeforeEach
    void setUp() {
        manager = new GameObjectManager(new GameInputSystem(), AssetsConfig.defaults(), 800, 600, path -> null, () -> { });
        world = Engine.buildWorld(new GameInputSystem(), new RenderSystem(null), manager, b -> { });
        manager.bind(world);
        world.setDelta(1f / 60f);
    }

    @AfterEach
    void tearDown() {
        world.dispose();
    }

    @Test
    void spawnInjectsTransformByType() {
        Probe probe = manager.spawn(Probe.class);

        assertNotNull(probe.exposedTransform());
    }

    @Test
    void onStartRunsOnceAndUpdateEveryStep() {
        Probe probe = manager.spawn(Probe.class);

        world.process();
        world.process();
        world.process();

        assertEquals(1, probe.starts);
        assertEquals(3, probe.deltas.size());
        assertEquals(1d / 60d, probe.deltas.get(0), 1e-6);
    }

    @Test
    void destroyedObjectIsRemovedOnNextStep() {
        Probe probe = manager.spawn(Probe.class);
        world.process();

        probe.destroy();
        world.process();
        int updatesAfterDestroy = probe.deltas.size();
        world.process();

        assertFalse(probe.isActive());
        assertEquals(updatesAfterDestroy, probe.deltas.size());
        assertTrue(probe.findProbe().isEmpty());
    }

    @Test
    void findReturnsActiveObjectOfType() {
        Probe probe = manager.spawn(Probe.class);

        assertSame(probe, probe.findProbe().orElseThrow());
    }

    @Test
    void teleportDoesNotInterpolateFromOldPosition() {
        Probe probe = manager.spawn(Probe.class);
        world.process();
        probe.exposedTransform().position().set(5f, 5f);

        probe.exposedTransform().teleport(300f, 40f);

        assertEquals(300f, probe.exposedTransform().position().x);
        world.process(); // PreviousTransformSystem não pode ter sobrescrito com valor antigo
        assertEquals(40f, probe.exposedTransform().position().y);
    }

    @Test
    void spritesOverlapUsingAbsoluteScale() {
        Probe a = manager.spawn(Probe.class);
        Probe b = manager.spawn(Probe.class);
        world.process();

        // Sem textura a largura é 0: só sobrepõe se as posições forem exatamente iguais.
        assertEquals(0f, a.sprite().width());
        a.exposedTransform().scale().x = -1f;
        assertEquals(0f, a.sprite().width());
        assertFalse(a.sprite().overlaps(b.sprite()));
    }

    @Test
    void spawnRejectsClassesWithoutDefaultConstructor() {
        assertThrows(IllegalArgumentException.class, () -> manager.spawn(NoDefaultConstructor.class));
    }

    @Test
    void lifecycleSystemIsRegistered() {
        assertNotNull(world.getSystem(DomainLifecycleSystem.class));
    }
}
