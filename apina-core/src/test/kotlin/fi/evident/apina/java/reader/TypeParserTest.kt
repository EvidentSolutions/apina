package fi.evident.apina.java.reader

import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeSchema
import fi.evident.apina.java.reader.JavaTypeMatchers.arrayType
import fi.evident.apina.java.reader.JavaTypeMatchers.basicType
import fi.evident.apina.java.reader.JavaTypeMatchers.genericType
import fi.evident.apina.java.reader.JavaTypeMatchers.singletonSchema
import fi.evident.apina.java.reader.JavaTypeMatchers.typeVariable
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.objectweb.asm.signature.SignatureReader
import java.io.Serializable
import java.util.function.Function
import kotlin.test.assertEquals

class TypeParserTest {

    private val schema = TypeSchema()

    @Test
    fun `parsing java types without generic signatures`() {
        assertThat(parseTypeDescriptor("I"), basicType(Int::class.javaPrimitiveType!!))
        assertThat(parseTypeDescriptor("V"), basicType(Void.TYPE))
        assertThat(parseTypeDescriptor("Z"), basicType(Boolean::class.javaPrimitiveType!!))
        assertThat(parseTypeDescriptor("J"), basicType(Long::class.javaPrimitiveType!!))

        assertThat(parseTypeDescriptor("Ljava/lang/Integer;"), basicType(Int::class.javaObjectType))
        assertThat(parseTypeDescriptor("Ljava/util/List;"), basicType(List::class.java))
    }

    @Test
    fun `parsing array types without generic signatures`() {
        assertThat(parseTypeDescriptor("[I"), arrayType(basicType(Int::class.javaPrimitiveType!!)))
        assertThat(parseTypeDescriptor("[[[I"), arrayType(arrayType(arrayType(basicType(Int::class.javaPrimitiveType!!)))))
        assertThat(parseTypeDescriptor("[Ljava/lang/Integer;"), arrayType(basicType(Int::class.javaObjectType)))
        assertThat(parseTypeDescriptor("[[Ljava/lang/Integer;"), arrayType(arrayType(basicType(Int::class.javaObjectType))))
    }

    @Test
    fun `parsing generic primitive signatures`() {
        assertThat(parseGenericType("I"), basicType(Int::class.javaPrimitiveType!!))
        assertThat(parseGenericType("V"), basicType(Void.TYPE))
    }

    @Test
    fun `parsing concrete generic signatures`() {
        assertThat(parseGenericType("Ljava/util/List<Ljava/lang/Integer;>;"),
                genericType(List::class.java, basicType(Int::class.javaObjectType)))

        assertThat(parseGenericType("Ljava/util/Map<Ljava/lang/Integer;Ljava/lang/String;>;"),
                genericType(Map::class.java, basicType(Int::class.javaObjectType), basicType(String::class.java)))
    }

    @Test
    fun `parsing wildcard types`() {
        assertThat(parseGenericType("Ljava/util/List<*>;"), genericType(List::class.java, equalTo(JavaType.Wildcard.unbounded())))
        assertThat(parseGenericType("Ljava/util/List<+Ljava/lang/String;>;"), genericType(List::class.java, equalTo(JavaType.Wildcard.extending(JavaType.Basic(String::class.java)))))
        assertThat(parseGenericType("Ljava/util/List<-Ljava/lang/String;>;"), genericType(List::class.java, equalTo(JavaType.Wildcard.withSuper(JavaType.Basic(String::class.java)))))
    }

    @Test
    fun `parsing type variables`() {
        schema.add(JavaType.Variable("T"))

        assertThat(parseGenericType("TT;"), typeVariable("T"))
        assertThat(parseGenericType("Ljava/util/List<TT;>;"), genericType(List::class.java, typeVariable("T")))
    }

    @Test
    fun `parsing generic array types`() {
        assertThat(parseGenericType("[Ljava/lang/String;"), arrayType(basicType(String::class.java)))
    }

    @Test
    fun `parsing generic array type for type variable`() {
        schema.add(JavaType.Variable("A"))

        assertThat(parseGenericType("[TA;"), arrayType(typeVariable("A")))
    }

    @Test
    fun `parsing object type`() {
        assertThat(parseObjectType("java/lang/Integer"), basicType(Int::class.javaObjectType))
    }

    @Test
    fun `parsing non-generic method signatures with single parameter`() {
        val signature = parseMethodDescriptor("(Ljava/lang/Integer;)Ljava/lang/String;")

        assertThat(signature.returnType, basicType(String::class.java))
        assertEquals(1, signature.argumentTypes.size)
        assertThat(signature.argumentTypes[0], basicType(Int::class.javaObjectType))
    }

    @Test
    fun `parsing non generic method signatures with multiple parameters`() {
        val signature = parseMethodDescriptor("(Ljava/lang/Class;Ljava/util/function/Function;Ljava/lang/Object;)Ljava/lang/Enum;")

        assertThat(signature.returnType, basicType(Enum::class.java))
        assertEquals(3, signature.argumentTypes.size)
        assertThat(signature.argumentTypes[0], basicType(Class::class.java))
        assertThat(signature.argumentTypes[1], basicType(Function::class.java))
        assertThat(signature.argumentTypes[2], basicType(Any::class.java))
    }

    @Nested
    inner class `parsing generic method signatures` {

        @Test
        fun `no declared variables`() {
            val signature = parseGenericMethodSignature("(Ljava/util/List<Ljava/lang/String;>;)Ljava/util/Set<Ljava/lang/Integer;>;")

            assertThat(signature.returnType, genericType(Set::class.java, basicType(Int::class.javaObjectType)))

            assertEquals(1, signature.argumentTypes.size)
            assertThat(signature.argumentTypes[0], genericType(List::class.java, basicType(String::class.java)))
        }

        @Test
        fun `declared variables`() {
            val signature = parseGenericMethodSignature("<T:Ljava/io/Serializable;>(TT;)Ljava/util/List<TT;>;")

            assertThat(signature.schema, singletonSchema("T", basicType(Serializable::class.java)))

            assertThat(signature.argumentTypes.size, `is`(1))
            assertThat(signature.argumentTypes[0], typeVariable("T"))
            assertThat(signature.returnType, genericType(List::class.java, typeVariable("T")))
        }

        @Test
        fun `variable with multiple bounds`() {
            val signature = parseGenericMethodSignature("<T:Ljava/lang/Cloneable;:Ljava/io/Serializable;>()Ljava/util/List<TT;>;")

            assertThat(signature.schema, singletonSchema("T", basicType(Cloneable::class.java), basicType(Serializable::class.java)))
            assertThat(signature.returnType, genericType(List::class.java, typeVariable("T")))
        }
    }

    @Test
    fun `inner classes inside generic classes`() {
        val signature = parseGenericMethodSignature("()Lfoo/Bar\$Baz<TT;>.Bar;")

        assertThat(signature.returnType, notNullValue())
    }

    private fun parseGenericType(signature: String): JavaType {
        val visitor = TypeBuildingSignatureVisitor()

        SignatureReader(signature).acceptType(visitor)

        return visitor.get()
    }
}
