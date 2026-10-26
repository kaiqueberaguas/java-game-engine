package br.com.beraguasstudio.engine.gameobject;

import br.com.beraguasstudio.engine.io.EngineProperties;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/** Preenche os campos {@link Value @Value} de um objeto, percorrendo a hierarquia de classes. */
final class ValueInjector {

    private final EngineProperties properties;

    ValueInjector(EngineProperties properties) {
        this.properties = properties;
    }

    void inject(Object target) {
        for (Class<?> type = target.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                Value annotation = field.getAnnotation(Value.class);
                if (annotation != null) {
                    set(target, field, annotation);
                }
            }
        }
    }

    private void set(Object target, Field field, Value annotation) {
        if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
            throw new IllegalStateException("@Value não pode ser usado em campo static ou final: " + describe(field));
        }
        String raw = properties.get(annotation.value())
                .orElseGet(() -> annotation.defaultValue().equals(Value.NONE) ? null : annotation.defaultValue());
        if (raw == null) {
            throw new IllegalStateException(
                    "Propriedade '" + annotation.value() + "' não encontrada para " + describe(field));
        }
        try {
            field.setAccessible(true);
            field.set(target, convert(raw, field.getType()));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Valor '" + raw + "' inválido para '" + annotation.value()
                    + "' em " + describe(field) + " (" + field.getType().getSimpleName() + ")", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Falha ao injetar " + describe(field), e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object convert(String raw, Class<?> type) {
        if (type == String.class) {
            return raw;
        }
        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(raw);
        }
        if (type == long.class || type == Long.class) {
            return Long.parseLong(raw);
        }
        if (type == float.class || type == Float.class) {
            return Float.parseFloat(raw);
        }
        if (type == double.class || type == Double.class) {
            return Double.parseDouble(raw);
        }
        if (type == short.class || type == Short.class) {
            return Short.parseShort(raw);
        }
        if (type == byte.class || type == Byte.class) {
            return Byte.parseByte(raw);
        }
        if (type == boolean.class || type == Boolean.class) {
            if (!raw.equalsIgnoreCase("true") && !raw.equalsIgnoreCase("false")) {
                throw new IllegalArgumentException("não é boolean");
            }
            return Boolean.parseBoolean(raw);
        }
        if (type == char.class || type == Character.class) {
            if (raw.length() != 1) {
                throw new IllegalArgumentException("esperado um único caractere");
            }
            return raw.charAt(0);
        }
        if (type.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) type, raw);
        }
        throw new IllegalStateException("Tipo sem suporte para @Value: " + type.getName());
    }

    private static String describe(Field field) {
        return field.getDeclaringClass().getSimpleName() + "." + field.getName();
    }
}
