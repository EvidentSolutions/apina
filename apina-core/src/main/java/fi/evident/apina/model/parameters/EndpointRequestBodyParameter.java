package fi.evident.apina.model.parameters;

import fi.evident.apina.model.type.ApiType;

public final class EndpointRequestBodyParameter extends EndpointParameter {

    public EndpointRequestBodyParameter(String name, ApiType type) {
        super(name, type);
    }

    @Override
    public String toString() {
        return "@RequestBody " + type + " " + name;
    }
}
