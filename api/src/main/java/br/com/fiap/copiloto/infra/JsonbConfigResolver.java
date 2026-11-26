package br.com.fiap.copiloto.infra;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.PropertyVisibilityStrategy;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Yasson 3.0 does not recognise record accessors (sku()) as properties, so records
 * would serialize as {}. Reading fields directly works for records and plain DTOs alike.
 */
@Provider
public class JsonbConfigResolver implements ContextResolver<Jsonb> {

    private static final PropertyVisibilityStrategy CAMPOS = new PropertyVisibilityStrategy() {
        @Override
        public boolean isVisible(Field campo) {
            return !java.lang.reflect.Modifier.isStatic(campo.getModifiers());
        }

        @Override
        public boolean isVisible(Method metodo) {
            return false;
        }
    };

    private final Jsonb jsonb = JsonbBuilder.create(new JsonbConfig()
            .withPropertyVisibilityStrategy(CAMPOS)
            .withNullValues(true));

    @Override
    public Jsonb getContext(Class<?> tipo) {
        return jsonb;
    }
}
