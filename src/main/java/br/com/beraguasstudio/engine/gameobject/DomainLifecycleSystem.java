package br.com.beraguasstudio.engine.gameobject;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;

/** Dirige o ciclo de vida dos {@link GameObject}s: {@code onStart}, {@code update} a cada passo fixo e remoção. */
public class DomainLifecycleSystem extends IteratingSystem {

    @Wire
    private GameObjectManager manager;
    private ComponentMapper<DomainBindingComponent> bindings;

    public DomainLifecycleSystem() {
        super(Aspect.all(DomainBindingComponent.class));
    }

    @Override
    protected void inserted(int entityId) {
        bindings.get(entityId).instance.onStart();
    }

    @Override
    protected void process(int entityId) {
        GameObject obj = bindings.get(entityId).instance;
        if (obj.isActive()) {
            obj.update(world.getDelta());
        } else {
            manager.unregister(obj);
            world.delete(entityId);
        }
    }
}
