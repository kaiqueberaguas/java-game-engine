package br.com.beraguasstudio.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.beraguasstudio.engine.ecs.RenderSystem;
import br.com.beraguasstudio.engine.gameobject.GameObject;
import br.com.beraguasstudio.engine.gameobject.GameObjectManager;
import br.com.beraguasstudio.engine.gameobject.Value;
import br.com.beraguasstudio.engine.input.GameInputSystem;
import br.com.beraguasstudio.engine.io.AssetsConfig;
import br.com.beraguasstudio.engine.io.EngineProperties;
import com.artemis.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

/** {@code @Value} num World headless. */
class ValueInjectionTest {

    enum Mode { EASY, HARD }

    public static class Base extends GameObject {
        @Value("test.name")
        String name;

        Base() {
            super("base.png");
        }
    }

    public static class Configured extends Base {
        @Value("test.speed")
        float speed;
        @Value("test.lives")
        int lives;
        @Value("test.god")
        boolean god;
        @Value("test.mode")
        Mode mode;
        @Value(value = "test.missing", defaultValue = "42")
        Integer fallback;
        @Value("test.override")
        String override;
        int startedWithLives;

        public Configured() {
        }

        @Override
        public void onStart() {
            startedWithLives = lives;
        }
    }

    public static class Missing extends GameObject {
        @Value("test.absent")
        String value;

        public Missing() {
            super("m.png");
        }
    }

    public static class BadNumber extends GameObject {
        @Value("test.name")
        int value;

        public BadNumber() {
            super("b.png");
        }
    }

    private World world;
    private GameObjectManager manager;

    @BeforeEach
    void setUp() {
        Properties props = new Properties();
        props.setProperty("test.name", " hero ");
        props.setProperty("test.speed", "2.5");
        props.setProperty("test.lives", "3");
        props.setProperty("test.god", "true");
        props.setProperty("test.mode", "HARD");
        props.setProperty("test.override", "file");
        System.setProperty("test.override", "system");

        manager = new GameObjectManager(new GameInputSystem(), AssetsConfig.defaults(), new EngineProperties(props),
                800, 600, path -> null, () -> { });
        world = Engine.buildWorld(new GameInputSystem(), new RenderSystem(null), manager, b -> { });
        manager.bind(world);
        world.setDelta(1f / 60f);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("test.override");
        world.dispose();
    }

    @Test
    void injectsConvertedValuesIncludingInheritedFields() {
        Configured obj = manager.spawn(Configured.class);

        assertEquals("hero", obj.name);
        assertEquals(2.5f, obj.speed);
        assertEquals(3, obj.lives);
        assertTrue(obj.god);
        assertEquals(Mode.HARD, obj.mode);
    }

    @Test
    void defaultValueUsedWhenPropertyMissing() {
        assertEquals(42, manager.spawn(Configured.class).fallback);
    }

    @Test
    void systemPropertyOverridesFile() {
        assertEquals("system", manager.spawn(Configured.class).override);
    }

    @Test
    void valuesAreAvailableInOnStart() {
        Configured obj = manager.spawn(Configured.class);
        world.process();

        assertEquals(3, obj.startedWithLives);
    }

    @Test
    void missingPropertyWithoutDefaultFails() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.spawn(Missing.class));
        assertTrue(e.getMessage().contains("test.absent"));
    }

    @Test
    void invalidValueFails() {
        assertThrows(IllegalStateException.class, () -> manager.spawn(BadNumber.class));
    }
}
