package fi.evident.apina.java.reader

import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeSchema
import fi.evident.apina.java.reader.JavaTypeMatchers.arrayType
import fi.evident.apina.java.reader.JavaTypeMatchers.basicType
import fi.evident.apina.java.reader.JavaTypeMatchers.genericType
import fi.evident.apina.java.reader.JavaTypeMatchers.singletonSchema
import fi.evident.apina.java.reader.JavaTypeMatchers.typeVariable
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.objectweb.asm.signature.SignatureReader
import java.io.Serializable
import java.util.function.Function

class TypeParserTest {

    private val schema = TypeSchema()

    @Test
    fun parsingJavaTypesWithoutGenericSignatures() {
        assertThat(parseTypeDescriptor("I"), `is`(basicType(Int::class.javaPrimitiveType!!)))
        assertThat(parseTypeDescriptor("V"), `is`(basicType(Void.TYPE)))
        assertThat(parseTypeDescriptor("Z"), `is`(basicType(Boolean::class.javaPrimitiveType!!)))
        assertThat(parseTypeDescriptor("J"), `is`(basicType(Long::class.javaPrimitiveType!!)))

        assertThat(parseTypeDescriptor("Ljava/lang/Integer;"), `is`(basicType(Int::class.javaObjectType)))
        assertThat(parseTypeDescriptor("Ljava/util/List;"), `is`<JavaType>(basicType(List::class.java)))
    }

    @Test
    fun parsingArrayTypesWithoutGenericSignatures() {
        assertThat(parseTypeDescriptor("[I"), `is`(arrayType(basicType(Int::class.javaPrimitiveType!!))))
        assertThat(parseTypeDescriptor("[[[I"), `is`(arrayType(arrayType(arrayType(basicType(Int::class.javaPrimitiveType!!))))))
        assertThat(parseTypeDescriptor("[Ljava/lang/Integer;"), `is`(arrayType(basicType(Int::class.javaObjectType))))
        assertThat(parseTypeDescriptor("[[Ljava/lang/Integer;"), `is`(arrayType(arrayType(basicType(Int::class.javaObjectType)))))
    }

    @Test
    fun parsingGenericPrimitiveSignatures() {
        assertThat(parseGenericType("I"), `is`(basicType(Int::class.javaPrimitiveType!!)))
        assertThat(parseGenericType("V"), `is`(basicType(Void.TYPE)))
    }

    @Test
    fun parsingConcreteGenericSignatures() {
        assertThat(parseGenericType("Ljava/util/List<Ljava/lang/Integer;>;"),
                `is`(genericType(List::class.java, basicType(Int::class.javaObjectType))))

        assertThat(parseGenericType("Ljava/util/Map<Ljava/lang/Integer;Ljava/lang/String;>;"),
                `is`(genericType(Map::class.java, basicType(Int::class.javaObjectType), basicType(String::class.java))))
    }

    @Test
    fun parsingWildcardTypes() {
        assertThat(parseGenericType("Ljava/util/List<*>;"), `is`(genericType(List::class.java, `is`(JavaType.Wildcard.unbounded()))))
        assertThat(parseGenericType("Ljava/util/List<+Ljava/lang/String;>;"), `is`(genericType(List::class.java, `is`(JavaType.Wildcard.extending(JavaType.Basic(String::class.java))))))
        assertThat(parseGenericType("Ljava/util/List<-Ljava/lang/String;>;"), `is`(genericType(List::class.java, `is`(JavaType.Wildcard.withSuper(JavaType.Basic(String::class.java))))))
    }

    @Test
    fun parsingTypeVariables() {
        schema.add(JavaType.Variable("T"))

        assertThat(parseGenericType("TT;"), `is`(typeVariable("T")))
        assertThat(parseGenericType("Ljava/util/List<TT;>;"), `is`(genericType(List::class.java, typeVariable("T"))))
    }

    @Test
    fun parsingGenericArrayTypes() {
        assertThat(parseGenericType("[Ljava/lang/String;"), `is`(arrayType(basicType(String::class.java))))
    }

    @Test
    fun parsingGenericArrayTypeForTypeVariable() {
        schema.add(JavaType.Variable("A"))

        assertThat(parseGenericType("[TA;"), `is`(arrayType(typeVariable("A"))))
    }

    @Test
    fun parsingObjectType() {
        assertThat(parseObjectType("java/lang/Integer"), `is`(basicType(Int::class.javaObjectType)))
    }

    @Test
    fun parsingNonGenericMethodSignaturesWithSingleParameter() {
        val signature = parseMethodDescriptor("(Ljava/lang/Integer;)Ljava/lang/String;")

        assertThat(signature.returnType, `is`(basicType(String::class.java)))
        assertThat(signature.argumentTypes.size, `is`(1))
        assertThat(signature.argumentTypes[0], `is`(basicType(Int::class.javaObjectType)))
    }

    @Test
    fun parsingNonGenericMethodSignaturesWithMultipleParameters() {
        val signature = parseMethodDescriptor("(Ljava/lang/Class;Ljava/util/function/Function;Ljava/lang/Object;)Ljava/lang/Enum;")

        assertThat<JavaType>(signature.returnType, `is`<JavaType>(basicType(Enum::class.java)))
        assertThat(signature.argumentTypes.size, `is`(3))
        assertThat<JavaType>(signature.argumentTypes[0], `is`<JavaType>(basicType(Class::class.java)))
        assertThat<JavaType>(signature.argumentTypes[1], `is`<JavaType>(basicType(Function::class.java)))
        assertThat(signature.argumentTypes[2], `is`(basicType(Any::class.java)))
    }

    @Test
    fun parsingGenericMethodSignatures_noDeclaredVariables() {
        val signature = parseGenericMethodSignature("(Ljava/util/List<Ljava/lang/String;>;)Ljava/util/Set<Ljava/lang/Integer;>;")

        assertThat(signature.returnType, `is`(genericType(Set::class.java, basicType(Int::class.javaObjectType))))

        assertThat(signature.argumentTypes.size, `is`(1))
        assertThat(signature.argumentTypes[0], `is`(genericType(List::class.java, basicType(String::class.java))))
    }

    @Test
    fun parsingGenericMethodSignatures_declaredVariables() {
        val signature = parseGenericMethodSignature("<T:Ljava/io/Serializable;>(TT;)Ljava/util/List<TT;>;")

        assertThat(signature.schema, `is`(singletonSchema("T", basicType(Serializable::class.java))))

        assertThat(signature.argumentTypes.size, `is`(1))
        assertThat(signature.argumentTypes[0], typeVariable("T"))
        assertThat(signature.returnType, `is`(genericType(List::class.java, typeVariable("T"))))
    }

    @Test
    fun parsingGenericMethodSignatures_variableWithMultipleBounds() {
        val signature = parseGenericMethodSignature("<T:Ljava/lang/Cloneable;:Ljava/io/Serializable;>()Ljava/util/List<TT;>;")

        assertThat(signature.schema, singletonSchema("T", basicType(Cloneable::class.java), basicType(Serializable::class.java)))
        assertThat(signature.returnType, `is`(genericType(List::class.java, typeVariable("T"))))
    }

    @Test
    fun innerClassesInsideGenericClasses() {
        val signature = parseGenericMethodSignature("()Lfoo/Bar\$Baz<TT;>.Bar;")

        assertThat(signature.returnType, `is`(notNullValue()))
    }

    private fun parseGenericType(signature: String): JavaType {
        val visitor = TypeBuildingSignatureVisitor()

        SignatureReader(signature).acceptType(visitor)

        return visitor.get()
    }
}
