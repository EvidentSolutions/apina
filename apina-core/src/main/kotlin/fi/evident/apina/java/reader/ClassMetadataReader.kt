package fi.evident.apina.java.reader

import fi.evident.apina.java.model.*
import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeSchema
import org.objectweb.asm.*
import java.io.InputStream
import java.util.*

/**
 * Reads class metadata.
 */
internal object ClassMetadataReader {

    fun loadMetadata(inputStream: InputStream): JavaClass {
        val visitor = MyClassVisitor()

        val classReader = ClassReader(inputStream)
        classReader.accept(visitor, ClassReader.SKIP_FRAMES)

        return visitor.getJavaClass()
    }

    private class MyClassVisitor : ClassVisitor(Opcodes.ASM9) {

        private var javaClass: JavaClass? = null

        override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<String>) {
            check(javaClass == null) { "classMetadata already initialized" }

            var type = parseObjectType(name)
            val superType: JavaType
            val interfaceTypes: List<JavaType>
            val schema: TypeSchema

            if (signature != null) {
                // If we have a generic signature available, parse it using our visitor
                val visitor = ClassSignatureVisitor.parse(signature)

                schema = visitor.schema
                if (!schema.isEmpty)
                    type = JavaType.Parameterized(type, schema.variables)

                superType = visitor.superClass ?: JavaType.Basic(Any::class.java)
                interfaceTypes = visitor.interfaces


            } else {
                superType = superName?.let(::parseObjectType) ?: JavaType.Basic("java.lang.Object")
                interfaceTypes = interfaces.map(::parseObjectType)
                schema = TypeSchema()
            }

            this.javaClass = JavaClass(type, superType, interfaceTypes, access, schema)
        }

        override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor {
            val annotation = JavaAnnotation(parseBasicTypeDescriptor(desc))
            getJavaClass().addAnnotation(annotation)

            return MyAnnotationVisitor(annotation)
        }

        override fun visitField(access: Int, name: String, desc: String, signature: String?, value: Any?): FieldVisitor {
            val field = JavaField(name, JavaVisibility.fromAccessFlags(access), parseJavaType(desc, signature), access)
            getJavaClass().addField(field)
            return MyFieldVisitor(field)
        }

        override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<String>?): MethodVisitor? {
            val javaClass = getJavaClass()
            if (javaClass.isEnum && name == "<init>") {
                // Skip constructors of enums
                return null
            }

            val methodSignature = parseMethodSignature(desc, signature)

            val method = JavaMethod(javaClass, name, JavaVisibility.fromAccessFlags(access), methodSignature.returnType, methodSignature.parameters, access, methodSignature.schema)
            javaClass.addMethod(method)
            return MyMethodVisitor(method)
        }

        fun getJavaClass(): JavaClass =
            javaClass ?: error("no ClassMetadata available")
    }

    private class MyAnnotationVisitor(private val annotation: JavaAnnotation) : AnnotationVisitor(Opcodes.ASM9) {

        override fun visit(name: String, value: Any) {
            annotation.setAttribute(name, if (value is Type) javaType(value) else value)
        }

        override fun visitEnum(name: String, desc: String, value: String) {
            val enumType = parseBasicTypeDescriptor(desc)

            annotation.setAttribute(name, EnumValue(enumType, value))
        }

        override fun visitAnnotation(name: String, desc: String): AnnotationVisitor {
            val nested = JavaAnnotation(parseTypeDescriptor(desc).toBasicType())
            annotation.setAttribute(name, nested)
            return MyAnnotationVisitor(nested)
        }

        override fun visitArray(name: String): AnnotationVisitor {
            return ArrayAnnotationVisitor(annotation, name)
        }
    }

    private class ArrayAnnotationVisitor(private val annotation: JavaAnnotation, private val name: String) : AnnotationVisitor(Opcodes.ASM9) {
        private val values = ArrayList<Any>()

        override fun visitEnd() {
            annotation.setAttribute(name, values.toTypedArray())
        }

        override fun visit(name: String?, value: Any) {
            values.add(value)
        }

        override fun visitEnum(name: String?, desc: String, value: String) {
            val enumType = parseBasicTypeDescriptor(desc)

            values.add(EnumValue(enumType, value))
        }

        override fun visitAnnotation(name: String?, desc: String): AnnotationVisitor {
            val nested = JavaAnnotation(parseBasicTypeDescriptor(desc))
            values.add(nested)
            return MyAnnotationVisitor(nested)
        }
    }

    private class MyFieldVisitor(private val field: JavaField) : FieldVisitor(Opcodes.ASM9) {

        override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor {
            val annotation = JavaAnnotation(parseBasicTypeDescriptor(desc))
            field.addAnnotation(annotation)
            return MyAnnotationVisitor(annotation)
        }
    }

    private class MyMethodVisitor(private val method: JavaMethod) : MethodVisitor(Opcodes.ASM9) {
        private var parameterIndex = 0

        /** For each parameter the table contains the index of local variable describing the parameter  */
        private val parameterIndicesInLVT: IntArray

        init {
            this.parameterIndicesInLVT = initLVTParameterIndices(method.isStatic, method.parameters)
        }

        override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor {
            val annotation = JavaAnnotation(parseBasicTypeDescriptor(desc))
            method.addAnnotation(annotation)
            return MyAnnotationVisitor(annotation)
        }

        override fun visitParameter(name: String?, access: Int) {
            val nextParameter = method.parameters[parameterIndex++]

            if (name != null)
                nextParameter.name = name
        }

        override fun visitLocalVariable(name: String, desc: String, signature: String?, start: Label, end: Label, index: Int) {
            for ((i, paramIndex) in parameterIndicesInLVT.withIndex())
                if (paramIndex == index)
                    method.parameters[i].name = name
        }

        override fun visitParameterAnnotation(parameter: Int, desc: String, visible: Boolean): AnnotationVisitor {
            val javaParameter = method.parameters[parameter]

            val annotation = JavaAnnotation(parseBasicTypeDescriptor(desc))
            javaParameter.addAnnotation(annotation)

            return MyAnnotationVisitor(annotation)
        }

        private fun initLVTParameterIndices(isStatic: Boolean, parameters: List<JavaParameter>): IntArray {
            val result = IntArray(parameters.size)

            var index = if (isStatic) 0 else 1
            for (i in result.indices) {
                result[i] = index
                if (parameters[i].type.isWide)
                    index += 2
                else
                    index++
            }
            return result
        }
    }
}
