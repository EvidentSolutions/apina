package fi.evident.apina.java.reader;

import fi.evident.apina.java.model.MethodSignature;
import fi.evident.apina.java.model.type.*;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static fi.evident.apina.java.reader.TypeParser.parseObjectType;
import static fi.evident.apina.java.reader.TypeParser.parseTypeDescriptor;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TypeParserTest {

    private final Map<String,JavaTypeVariable> typeVariableMap = new HashMap<>();

    @Test
    public void parsingJavaTypesWithoutGenericSignatures() {
        assertThat(parseTypeDescriptor("I"), is(basicType("int")));
        assertThat(parseTypeDescriptor("V"), is(basicType("void")));
        assertThat(parseTypeDescriptor("Z"), is(basicType("boolean")));
        assertThat(parseTypeDescriptor("J"), is(basicType("long")));

        assertThat(parseTypeDescriptor("Ljava/lang/Integer;"), is(basicType("java.lang.Integer")));
        assertThat(parseTypeDescriptor("Ljava/util/List;"), is(basicType("java.util.List")));
    }

    @Test
    public void parsingArrayTypesWithoutGenericSignatures() {
        assertThat(parseTypeDescriptor("[I"), is(arrayType(basicType("int"))));
        assertThat(parseTypeDescriptor("[[[I"), is(arrayType(arrayType(arrayType(basicType("int"))))));
        assertThat(parseTypeDescriptor("[Ljava/lang/Integer;"), is(arrayType(basicType("java.lang.Integer"))));
        assertThat(parseTypeDescriptor("[[Ljava/lang/Integer;"), is(arrayType(arrayType(basicType("java.lang.Integer")))));
    }

    @Test
    public void parsingGenericPrimitiveSignatures() {
        assertThat(parseGenericType("I"), is(basicType("int")));
        assertThat(parseGenericType("V"), is(basicType("void")));
    }

    @Test
    public void parsingConcreteGenericSignatures() {
        assertThat(parseGenericType("Ljava/util/List<Ljava/lang/Integer;>;"),
                is(genericType("java.util.List", basicType("java.lang.Integer"))));

        assertThat(parseGenericType("Ljava/util/Map<Ljava/lang/Integer;Ljava/lang/String;>;"),
                is(genericType("java.util.Map", basicType("java.lang.Integer"), basicType("java.lang.String"))));
    }

    @Test
    public void parsingWildcardTypes() {
        assertThat(parseGenericType("Ljava/util/List<*>;"), is(genericType("java.util.List", JavaWildcardType.unbounded())));
        assertThat(parseGenericType("Ljava/util/List<+Ljava/lang/String;>;"), is(genericType("java.util.List", JavaWildcardType.extending(basicType("java.lang.String")))));
        assertThat(parseGenericType("Ljava/util/List<-Ljava/lang/String;>;"), is(genericType("java.util.List", JavaWildcardType.withSuper(basicType("java.lang.String")))));
    }

    @Test
    public void parsingTypeVariables() {
        typeVariableMap.put("T", typeVariable("T"));

        assertThat(parseGenericType("TT;"), is(typeVariable("T")));
        assertThat(parseGenericType("Ljava/util/List<TT;>;"), is(genericType("java.util.List", typeVariable("T"))));
    }

    @Test
    public void parsingGenericArrayTypes() {
        assertThat(parseGenericType("[Ljava/lang/String;"), is(arrayType(basicType("java.lang.String"))));
    }

    @Test
    public void parsingGenericArrayTypeForTypeVariable() {
        typeVariableMap.put("A", typeVariable("A"));
        assertThat(parseGenericType("[TA;"), is(arrayType(typeVariable("A"))));
    }

    @Test
    public void parsingObjectType() {
        assertThat(parseObjectType("java/lang/Integer"), is(basicType("java.lang.Integer")));
    }

    @Test
    public void parsingNonGenericMethodSignaturesWithSingleParameter() {
        MethodSignature signature = TypeParser.parseMethodDescriptor("(Ljava/lang/Integer;)Ljava/lang/String;");

        assertThat(signature.getReturnType(), is(basicType("java.lang.String")));
        assertThat(signature.getArgumentTypes(), is(singletonList(basicType("java.lang.Integer"))));
    }

    @Test
    public void parsingNonGenericMethodSignaturesWithMultipleParameters() {
        MethodSignature signature = TypeParser.parseMethodDescriptor("(Ljava/lang/Class;Ljava/util/function/Function;Ljava/lang/Object;)Ljava/lang/Enum;");

        assertThat(signature.getReturnType(), is(basicType("java.lang.Enum")));
        assertThat(signature.getArgumentTypes(), is(asList(
                basicType("java.lang.Class"), basicType("java.util.function.Function"), basicType("java.lang.Object"))));
    }

    @Test
    public void parsingGenericMethodSignatures_noDeclaredVariables() {
        MethodSignature signature = parseGenericMethodSignature("(Ljava/util/List<Ljava/lang/String;>;)Ljava/util/Set<Ljava/lang/Integer;>;");

        assertThat(signature.getReturnType(), is(genericType("java.util.Set", basicType("java.lang.Integer"))));
        assertThat(signature.getArgumentTypes(), is(singletonList(genericType("java.util.List", basicType("java.lang.String")))));
    }

    @Test
    public void parsingGenericMethodSignatures_declaredVariables() {
        MethodSignature signature = parseGenericMethodSignature("<T:Ljava/io/Serializable;>(TT;)Ljava/util/List<TT;>;");

        assertThat(signature.getArgumentTypes(), is(singletonList(typeVariable("T", basicType("java.io.Serializable")))));
        assertThat(signature.getReturnType(), is(genericType("java.util.List", typeVariable("T", basicType("java.io.Serializable")))));
    }

    @Test
    public void parsingGenericMethodSignatures_variableWithMultipleBounds() {
        MethodSignature signature = parseGenericMethodSignature("<T:Ljava/lang/Cloneable;:Ljava/io/Serializable;>()Ljava/util/List<TT;>;");

        assertThat(signature.getReturnType(), is(genericType("java.util.List", typeVariable("T", basicType("java.lang.Cloneable"), basicType("java.io.Serializable")))));
    }

    private JavaType parseGenericType(String signature) {
        return TypeParser.parseGenericType(signature, typeVariableMap);
    }

    private MethodSignature parseGenericMethodSignature(String signature) {
        return TypeParser.parseGenericMethodSignature(signature, typeVariableMap);
    }

    private static JavaTypeVariable typeVariable(String name, JavaType... bounds) {
        return new JavaTypeVariable(name, asList(bounds));
    }

    private static JavaType basicType(String name) {
        return new JavaBasicType(name);
    }

    private static JavaType arrayType(JavaType elementType) {
        return new JavaArrayType(elementType);
    }

    private static JavaType genericType(String name, JavaType... args) {
        return new JavaParameterizedType(basicType(name), asList(args));
    }
}
