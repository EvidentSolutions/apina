package fi.evident.apina.model.type;

public final class ApiPrimitiveType extends ApiType {

    public static final ApiType UNKNOWN = new ApiPrimitiveType("unknown");
    public static final ApiType STRING = new ApiPrimitiveType("string");
    public static final ApiType DICTIONARY = new ApiPrimitiveType("dictionary");
    public static final ApiType VOID = new ApiPrimitiveType("void");
    private final String name;

    private ApiPrimitiveType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
}
