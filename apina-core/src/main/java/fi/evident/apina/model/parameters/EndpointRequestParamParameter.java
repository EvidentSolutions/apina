package fi.evident.apina.model.parameters;

import fi.evident.apina.model.type.ApiType;

import java.util.Optional;

public final class EndpointRequestParamParameter extends EndpointParameter {

    private final String queryParameter;

    public EndpointRequestParamParameter(String name, Optional<String> queryParameter, ApiType type) {
        super(name, type);

        this.queryParameter = queryParameter.orElse(name);
    }

    public String getQueryParameter() {
        return queryParameter;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("@RequestParam");

        if (!queryParameter.equals(name))
            sb.append("(\"").append(queryParameter).append("\")");

        sb.append(' ').append(type).append(' ').append(name);
        return sb.toString();
    }
}
