package fi.evident.apina.java.reader;

import fi.evident.apina.java.model.*;
import fi.evident.apina.java.model.type.JavaBasicType;
import fi.evident.apina.java.model.type.JavaParameterizedType;
import fi.evident.apina.java.model.type.JavaType;
import fi.evident.apina.java.model.type.JavaTypeVariable;
import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Reads class metadata.
 */
final class ClassMetadataReader {

    public static JavaClass loadMetadata(InputStream in) {

        try {
            MyClassVisitor visitor = new MyClassVisitor();

            ClassReader classReader = new ClassReader(in);
            classReader.accept(visitor, ClassReader.SKIP_DEBUG);

            return visitor.getJavaClass();

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static JavaVisibility parseVisibility(int access) {
        if ((access & Opcodes.ACC_PUBLIC) != 0)
            return JavaVisibility.PUBLIC;
        else if ((access & Opcodes.ACC_PROTECTED) != 0)
            return JavaVisibility.PROTECTED;
        else if ((access & Opcodes.ACC_PRIVATE) != 0)
            return JavaVisibility.PRIVATE;
        else return JavaVisibility.PACKAGE;
    }

    private static final class MyClassVisitor extends ClassVisitor {

        private JavaClass javaClass;

        private Map<String, JavaTypeVariable> typeVariableMap = emptyMap();

        public MyClassVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            if (javaClass != null)
                throw new IllegalStateException("classMetadata already initialized");

            JavaType type = TypeParser.parseObjectType(name);
            JavaType superType;
            List<JavaType> interfaceTypes;

            if (signature != null) {
                // If we have a generic signature available, parse it using our visitor
                ClassSignatureVisitor visitor = ClassSignatureVisitor.parse(signature);

                TypeVariableCollection typeVariables = visitor.getTypeVariables();
                if (!typeVariables.isEmpty()) {
                    type = new JavaParameterizedType(type, typeVariables.getTypeVariables());
                    typeVariableMap = typeVariables.getTypeVariableMap();
                }

                superType = visitor.getSuperClass().orElse(new JavaBasicType(Object.class));
                interfaceTypes = visitor.getInterfaces();
            } else {
                superType = TypeParser.parseObjectType(superName);
                interfaceTypes = Stream.of(interfaces).map(TypeParser::parseObjectType).collect(toList());
            }

            this.javaClass = new JavaClass(type, superType, interfaceTypes, access);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            JavaAnnotation annotation = new JavaAnnotation(TypeParser.parseBasicTypeDescriptor(desc));
            getJavaClass().addAnnotation(annotation);

            return new MyAnnotationVisitor(annotation);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            JavaField field = new JavaField(name, parseVisibility(access), TypeParser.parseJavaType(desc, signature, typeVariableMap), access);
            getJavaClass().addField(field);
            return new MyFieldVisitor(field);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (getJavaClass().isEnum() && name.equals("<init>")) {
                // Skip constructors of enums
                return null;
            }

            MethodSignature methodSignature = TypeParser.parseMethodSignature(desc, signature, typeVariableMap);

            JavaMethod method = new JavaMethod(name, parseVisibility(access), methodSignature.getReturnType(), methodSignature.getParameters(), access);
            getJavaClass().addMethod(method);
            return new MyMethodVisitor(method);
        }

        public JavaClass getJavaClass() {
            if (javaClass == null)
                throw new IllegalStateException("no ClassMetadata available");

            return javaClass;
        }
    }

    private static final class MyAnnotationVisitor extends AnnotationVisitor {

        private final JavaAnnotation annotation;

        public MyAnnotationVisitor(JavaAnnotation annotation) {
            super(Opcodes.ASM5);

            this.annotation = requireNonNull(annotation);
        }

        @Override
        public void visit(String name, Object value) {
            annotation.setAttribute(name, value);
        }

        @Override
        public void visitEnum(String name, String desc, String value) {
            JavaBasicType enumType = TypeParser.parseBasicTypeDescriptor(desc);

            annotation.setAttribute(name, new EnumValue(enumType, value));
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            JavaAnnotation nested = new JavaAnnotation(TypeParser.parseTypeDescriptor(desc).toBasicType());
            annotation.setAttribute(name, nested);
            return new MyAnnotationVisitor(nested);
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            return new ArrayAnnotationVisitor(annotation, name);
        }
    }

    private static final class ArrayAnnotationVisitor extends AnnotationVisitor {
        private final JavaAnnotation annotation;
        private final String name;
        private final List<Object> values = new ArrayList<>();

        public ArrayAnnotationVisitor(JavaAnnotation annotation, String name) {
            super(Opcodes.ASM5);
            this.name = requireNonNull(name);
            this.annotation = requireNonNull(annotation);
        }

        @Override
        public void visitEnd() {
            annotation.setAttribute(name, values.toArray());
        }

        @Override
        public void visit(String name, Object value) {
            values.add(value);
        }

        @Override
        public void visitEnum(String name, String desc, String value) {
            JavaBasicType enumType = TypeParser.parseBasicTypeDescriptor(desc);

            values.add(new EnumValue(enumType, value));
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            JavaAnnotation nested = new JavaAnnotation(TypeParser.parseBasicTypeDescriptor(desc));
            values.add(nested);
            return new MyAnnotationVisitor(nested);
        }
    }

    private static final class MyFieldVisitor extends FieldVisitor {

        private final JavaField field;

        public MyFieldVisitor(JavaField field) {
            super(Opcodes.ASM5);
            this.field = field;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            JavaAnnotation annotation = new JavaAnnotation(TypeParser.parseBasicTypeDescriptor(desc));
            field.addAnnotation(annotation);
            return new MyAnnotationVisitor(annotation);
        }
    }

    private static final class MyMethodVisitor extends MethodVisitor {

        private final JavaMethod method;
        private int parameterIndex = 0;

        public MyMethodVisitor(JavaMethod method) {
            super(Opcodes.ASM5);
            this.method = method;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            JavaAnnotation annotation = new JavaAnnotation(TypeParser.parseBasicTypeDescriptor(desc));
            method.addAnnotation(annotation);
            return new MyAnnotationVisitor(annotation);
        }

        @Override
        public void visitParameter(String name, int access) {
            JavaParameter nextParameter = method.getParameters().get(parameterIndex++);

            if (name != null)
                nextParameter.initName(name);
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
            JavaParameter javaParameter = method.getParameters().get(parameter);

            JavaAnnotation annotation = new JavaAnnotation(TypeParser.parseBasicTypeDescriptor(desc));
            javaParameter.addAnnotation(annotation);

            return new MyAnnotationVisitor(annotation);
        }
    }
}
