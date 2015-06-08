package fi.evident.apina.model.type;

public final class ApiPrimitiveType extends ApiType {

    public static final ApiType ANY = new ApiPrimitiveType("any");
    public static final ApiType STRING = new ApiPrimitiveType("string");
    public static final ApiType BOOLEAN = new ApiPrimitiveType("boolean");
    public static final ApiType NUMBER = new ApiPrimitiveType("number");
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
