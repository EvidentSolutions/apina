package fi.evident.apina.java.model.type;

public interface JavaTypeVisitor<C, R> {

    R visit(JavaArrayType type, C ctx);
    R visit(JavaBasicType type, C ctx);
    R visit(JavaParameterizedType type, C ctx);
    R visit(JavaTypeVariable type, C ctx);
    R visit(JavaWildcardType type, C ctx);
}
