package br.com.beraguasstudio.engine.gameobject;

import br.com.beraguasstudio.engine.ecs.SpriteComponent;
import br.com.beraguasstudio.engine.ecs.TransformComponent;
import br.com.beraguasstudio.engine.render.Texture;

/** Visão OO de um {@link SpriteComponent} junto ao transform do mesmo objeto. */
public final class Sprite {

    private final SpriteComponent component;
    private final TransformComponent transform;

    Sprite(SpriteComponent component, TransformComponent transform) {
        this.component = component;
        this.transform = transform;
    }

    public Texture texture() {
        return component.texture;
    }

    public void setTexture(Texture texture) {
        component.texture = texture;
    }

    /** Largura desenhada: textura × |scale.x|; 0 sem textura. */
    public float width() {
        return component.texture == null ? 0f : component.texture.width() * Math.abs(transform.scale.x);
    }

    public float height() {
        return component.texture == null ? 0f : component.texture.height() * Math.abs(transform.scale.y);
    }

    /** Colisão AABB entre os retângulos desenhados (posição = centro). */
    public boolean overlaps(Sprite other) {
        return Math.abs(transform.position.x - other.transform.position.x) < (width() + other.width()) / 2f
                && Math.abs(transform.position.y - other.transform.position.y) < (height() + other.height()) / 2f;
    }
}
