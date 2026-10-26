package br.com.beraguasstudio.engine.ecs;

import br.com.beraguasstudio.engine.render.Texture;
import com.artemis.Component;

public class SpriteComponent extends Component {

    /** Ciclo de vida da textura é de quem a carregou; o componente só referencia. */
    public Texture texture;
}
