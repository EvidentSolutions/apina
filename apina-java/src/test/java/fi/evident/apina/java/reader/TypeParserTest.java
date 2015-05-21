package fi.evident.apina.java.reader;

import fi.evident.apina.java.model.JavaType;
import fi.evident.apina.java.model.MethodSignature;
import fi.evident.apina.java.model.QualifiedName;
import org.junit.Test;

import static fi.evident.apina.java.reader.TypeParser.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TypeParserTest {

    @Test
    public void parsingJavaTypesWithoutGenericSignatures() {
        assertThat(parseJavaType("I", null), is(simpleType("int")));
        assertThat(parseJavaType("V", null), is(simpleType("void")));
        assertThat(parseJavaType("Z", null), is(simpleType("boolean")));
        assertThat(parseJavaType("J", null), is(simpleType("long")));

        assertThat(parseJavaType("Ljava/lang/Integer;", null), is(simpleType("java.lang.Integer")));
        assertThat(parseJavaType("Ljava/util/List;", null), is(simpleType("java.util.List")));

        assertThat(parseJavaType("[I", null), is(simpleType("int[]")));
        assertThat(parseJavaType("[[[[I", null), is(simpleType("int[][][][]")));
        assertThat(parseJavaType("[Ljava/lang/Integer;", null), is(simpleType("java.lang.Integer[]")));
        assertThat(parseJavaType("[[Ljava/lang/Integer;", null), is(simpleType("java.lang.Integer[][]")));
    }

    @Test
    public void parsingObjectType() {
        assertThat(parseObjectType("java/lang/Integer"), is(new QualifiedName("java.lang.Integer")));
    }

    @Test
    public void parsingNonGenericMethodSignaturesWithSingleParameter() {
        MethodSignature signature = parseMethodSignature("(Ljava/lang/Integer;)Ljava/lang/String;", null);

        assertThat(signature.getReturnType(), is(simpleType("java.lang.String")));
        assertThat(signature.getArgumentTypes(), is(singletonList(simpleType("java.lang.Integer"))));
    }

    @Test
    public void parsingNonGenericMethodSignaturesWithMultipleParameters() {
        MethodSignature signature = parseMethodSignature(
                "(Ljava/lang/Class;Ljava/util/function/Function;Ljava/lang/Object;)Ljava/lang/Enum;", null);

        assertThat(signature.getReturnType(), is(simpleType("java.lang.Enum")));
        assertThat(signature.getArgumentTypes(), is(asList(
                simpleType("java.lang.Class"), simpleType("java.util.function.Function"), simpleType("java.lang.Object"))));
    }

    private static JavaType simpleType(String name) {
        return new JavaType(new QualifiedName(name));
    }
}
