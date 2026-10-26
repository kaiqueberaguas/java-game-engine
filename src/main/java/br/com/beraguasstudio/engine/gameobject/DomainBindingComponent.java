package br.com.beraguasstudio.engine.gameobject;

import com.artemis.Component;

/** Liga uma entidade do ECS ao {@link GameObject} que a representa. */
public class DomainBindingComponent extends Component {

    public GameObject instance;
}
