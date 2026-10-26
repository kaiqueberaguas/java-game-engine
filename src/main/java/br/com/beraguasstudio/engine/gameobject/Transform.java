package br.com.beraguasstudio.engine.gameobject;

import br.com.beraguasstudio.engine.ecs.TransformComponent;
import org.joml.Vector2f;

/** Visão OO de um {@link TransformComponent}; escreve direto nos dados do ECS. */
public final class Transform {

    private final TransformComponent component;

    Transform(TransformComponent component) {
        this.component = component;
    }

    /** Centro do sprite, em pixels (Y para cima). Mutável. */
    public Vector2f position() {
        return component.position;
    }

    /** Fator sobre o tamanho da textura; valor negativo espelha o sprite no eixo. Mutável. */
    public Vector2f scale() {
        return component.scale;
    }

    /** Radianos, anti-horária. */
    public float rotation() {
        return component.rotation;
    }

    public void setRotation(float rotation) {
        component.rotation = rotation;
    }

    /** Move sem interpolar com a posição anterior (saltos como respawn). */
    public void teleport(float x, float y) {
        component.position.set(x, y);
        component.previousPosition.set(component.position);
    }
}
