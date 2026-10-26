package br.com.beraguasstudio.engine.gameobject;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injeta num campo de {@link GameObject} o valor de uma propriedade de {@code engine.properties}
 * (ou de {@code -Dchave=valor}, que tem precedência). A injeção ocorre no spawn, antes do
 * {@code onStart()}; no construtor o campo ainda não está preenchido.
 *
 * <p>Tipos aceitos: {@code String}, {@code int}, {@code long}, {@code float}, {@code double},
 * {@code boolean}, {@code short}, {@code byte}, {@code char}, seus wrappers e enums. Sem a propriedade e
 * sem {@link #defaultValue()}, o spawn falha com {@link IllegalStateException}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Value {

    /** Marca "sem padrão" (um literal vazio é um padrão válido). */
    String NONE = "\u0000";

    /** Chave da propriedade, ex.: {@code "player.speed"}. */
    String value();

    /** Usado quando a propriedade não existe. */
    String defaultValue() default NONE;
}
