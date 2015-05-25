package fi.evident.apina.model.parameters;

import fi.evident.apina.model.type.ApiType;

public abstract class EndpointParameter {

    protected final String name;
    protected final ApiType type;

    // Package protected so that there are no implementation outside
    EndpointParameter(String name, ApiType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public ApiType getType() {
        return type;
    }

    @Override
    public abstract String toString();
}
