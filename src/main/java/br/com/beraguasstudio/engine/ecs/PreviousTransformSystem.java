package br.com.beraguasstudio.engine.ecs;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

/**
 * Guarda a posição do passo anterior antes dos demais sistemas moverem a entidade.
 * Deve ser o primeiro sistema do passo fixo.
 */
public class PreviousTransformSystem extends IteratingSystem {

    private ComponentMapper<TransformComponent> transforms;

    public PreviousTransformSystem() {
        super(Aspect.all(TransformComponent.class));
    }

    /** Entidade nova começa sem interpolação (previous == position). */
    @Override
    protected void inserted(int entityId) {
        TransformComponent t = transforms.get(entityId);
        t.previousPosition.set(t.position);
    }

    @Override
    protected void process(int entityId) {
        TransformComponent t = transforms.get(entityId);
        t.previousPosition.set(t.position);
    }
}
