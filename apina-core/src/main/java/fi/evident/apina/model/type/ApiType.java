package fi.evident.apina.model.type;

public abstract class ApiType {

    // Package private to prevent extension outside
    ApiType() { }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();
}
