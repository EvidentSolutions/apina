package fi.evident.apina.model.parameters;

import fi.evident.apina.model.type.ApiType;

import java.util.Optional;

public final class EndpointPathVariableParameter extends EndpointParameter {

    private final String pathVariable;

    public EndpointPathVariableParameter(String name, Optional<String> pathVariable, ApiType type) {
        super(name, type);

        this.pathVariable = pathVariable.orElse(name);
    }

    public String getPathVariable() {
        return pathVariable;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("@PathVariable");

        if (!pathVariable.equals(name))
            sb.append("(\"").append(pathVariable).append("\")");

        sb.append(' ').append(type).append(' ').append(name);
        return sb.toString();
    }
}
