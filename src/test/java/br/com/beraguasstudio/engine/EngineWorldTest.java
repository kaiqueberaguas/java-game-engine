package br.com.beraguasstudio.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

import br.com.beraguasstudio.engine.ecs.PreviousTransformSystem;
import br.com.beraguasstudio.engine.ecs.RenderSystem;
import br.com.beraguasstudio.engine.ecs.TransformComponent;
import br.com.beraguasstudio.engine.input.GameInputSystem;
import br.com.beraguasstudio.engine.input.Input;
import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import org.junit.jupiter.api.Test;

/** Monta o World sem janela/GPU. */
class EngineWorldTest {

    /** Sistema de jogo de exemplo: sobe 1 unidade por passo enquanto W estiver pressionada. */
    static class MoveUpSystem extends IteratingSystem {
        @Wire
        Input input;
        ComponentMapper<TransformComponent> transforms;

        MoveUpSystem() {
            super(Aspect.all(TransformComponent.class));
        }

        @Override
        protected void process(int entityId) {
            if (input.isKeyDown(GLFW_KEY_W)) {
                transforms.get(entityId).position.y += 1f;
            }
        }
    }

    private final GameInputSystem input = new GameInputSystem();

    @Test
    void injectsInputIntoGameSystems() {
        MoveUpSystem system = new MoveUpSystem();
        World world = Engine.buildWorld(input, new RenderSystem(null), b -> b.with(system));

        assertSame(input, system.input);
        world.dispose();
    }

    @Test
    void runsPreviousTransformSystemBeforeGameSystems() {
        World world = Engine.buildWorld(input, new RenderSystem(null), b -> b.with(new MoveUpSystem()));
        input.onKey(GLFW_KEY_W, org.lwjgl.glfw.GLFW.GLFW_PRESS);

        int id = world.create();
        TransformComponent t = world.edit(id).create(TransformComponent.class);
        t.position.set(10f, 20f);

        world.setDelta(1f / 60f);
        world.process(); // entidade entra no sistema; previous == position
        assertEquals(20f, t.previousPosition.y);
        assertEquals(21f, t.position.y);

        world.process();
        assertEquals(21f, t.previousPosition.y); // posição antes do passo
        assertEquals(22f, t.position.y);
        world.dispose();
    }

    @Test
    void previousTransformSystemIsRegistered() {
        World world = Engine.buildWorld(input, new RenderSystem(null), b -> { });

        assertSame(PreviousTransformSystem.class, world.getSystem(PreviousTransformSystem.class).getClass());
        world.dispose();
    }
}
