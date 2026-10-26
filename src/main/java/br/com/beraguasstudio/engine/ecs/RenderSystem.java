package br.com.beraguasstudio.engine.ecs;

import br.com.beraguasstudio.engine.render.SpriteRenderer;
import br.com.beraguasstudio.engine.render.Texture;
import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import org.joml.Vector2f;

/**
 * Desenha entidades com {@link TransformComponent} + {@link SpriteComponent}.
 * Sistema passivo: não roda em {@code world.process()} (passo fixo); o loop chama
 * {@link #render} a cada frame com o fator de interpolação.
 */
public class RenderSystem extends BaseEntitySystem {

    private ComponentMapper<TransformComponent> transforms;
    private ComponentMapper<SpriteComponent> sprites;

    private final SpriteRenderer renderer;
    private final Vector2f interpolated = new Vector2f();

    public RenderSystem(SpriteRenderer renderer) {
        super(Aspect.all(TransformComponent.class, SpriteComponent.class));
        this.renderer = renderer;
    }

    @Override
    protected boolean checkProcessing() {
        return false;
    }

    @Override
    protected void processSystem() {
        // passivo; veja render()
    }

    public void render(float alpha, int viewportWidth, int viewportHeight) {
        renderer.begin(viewportWidth, viewportHeight);

        IntBag entities = subscription.getEntities();
        int[] ids = entities.getData();
        for (int i = 0, n = entities.size(); i < n; i++) {
            int id = ids[i];
            Texture texture = sprites.get(id).texture;
            if (texture == null) {
                continue;
            }
            TransformComponent t = transforms.get(id);
            t.previousPosition.lerp(t.position, alpha, interpolated);
            renderer.draw(texture, interpolated.x, interpolated.y,
                    texture.width() * t.scale.x, texture.height() * t.scale.y, t.rotation);
        }
    }
}
