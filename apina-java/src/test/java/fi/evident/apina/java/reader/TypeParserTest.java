package fi.evident.apina.java.reader;

import fi.evident.apina.java.model.MethodSignature;
import fi.evident.apina.java.model.type.*;
import org.junit.Test;

import static fi.evident.apina.java.reader.TypeParser.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TypeParserTest {

    @Test
    public void parsingJavaTypesWithoutGenericSignatures() {
        assertThat(parseJavaType("I", null), is(basicType("int")));
        assertThat(parseJavaType("V", null), is(basicType("void")));
        assertThat(parseJavaType("Z", null), is(basicType("boolean")));
        assertThat(parseJavaType("J", null), is(basicType("long")));

        assertThat(parseJavaType("Ljava/lang/Integer;", null), is(basicType("java.lang.Integer")));
        assertThat(parseJavaType("Ljava/util/List;", null), is(basicType("java.util.List")));

        assertThat(parseJavaType("[I", null), is(basicType("int[]")));
        assertThat(parseJavaType("[[[[I", null), is(basicType("int[][][][]")));
        assertThat(parseJavaType("[Ljava/lang/Integer;", null), is(basicType("java.lang.Integer[]")));
        assertThat(parseJavaType("[[Ljava/lang/Integer;", null), is(basicType("java.lang.Integer[][]")));
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
        assertThat(parseGenericType("TT;"), is(typeVariable("T")));
        assertThat(parseGenericType("Ljava/util/List<TT;>;"), is(genericType("java.util.List", typeVariable("T"))));
    }

    @Test
    public void parsingGenericArrayTypes() {
        // TODO: implement generic array types
    }

    @Test
    public void parsingObjectType() {
        assertThat(parseObjectType("java/lang/Integer"), is(basicType("java.lang.Integer")));
    }

    @Test
    public void parsingNonGenericMethodSignaturesWithSingleParameter() {
        MethodSignature signature = parseMethodSignature("(Ljava/lang/Integer;)Ljava/lang/String;", null);

        assertThat(signature.getReturnType(), is(basicType("java.lang.String")));
        assertThat(signature.getArgumentTypes(), is(singletonList(basicType("java.lang.Integer"))));
    }

    @Test
    public void parsingNonGenericMethodSignaturesWithMultipleParameters() {
        MethodSignature signature = parseMethodSignature(
                "(Ljava/lang/Class;Ljava/util/function/Function;Ljava/lang/Object;)Ljava/lang/Enum;", null);

        assertThat(signature.getReturnType(), is(basicType("java.lang.Enum")));
        assertThat(signature.getArgumentTypes(), is(asList(
                basicType("java.lang.Class"), basicType("java.util.function.Function"), basicType("java.lang.Object"))));
    }

    private static JavaType typeVariable(String name) {
        return new JavaTypeVariable(name);
    }

    private static JavaBasicType basicType(String name) {
        return new JavaBasicType(name);
    }

    private static JavaType genericType(String name, JavaType... args) {
        return new JavaParameterizedType(basicType(name), asList(args));
    }
}
