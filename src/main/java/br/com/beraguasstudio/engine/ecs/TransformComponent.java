package br.com.beraguasstudio.engine.ecs;

import com.artemis.Component;
import org.joml.Vector2f;

public class TransformComponent extends Component {

    public final Vector2f position = new Vector2f();
    /** Posição no passo fixo anterior; usada para interpolar o render. Mantida por {@link PreviousTransformSystem}. */
    public final Vector2f previousPosition = new Vector2f();
    public final Vector2f scale = new Vector2f(1f, 1f);
    /** Rotação em radianos, anti-horária. */
    public float rotation;
}
