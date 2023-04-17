package fi.evident.apina.spring

import fi.evident.apina.java.model.JavaAnnotatedElement
import fi.evident.apina.java.model.type.JavaType

private val nullableAnnotations = listOf(
    "org.jetbrains.annotations.Nullable",
    "javax.annotation.Nullable",
    "jakarta.annotation.Nullable",
    "javax.annotation.CheckForNull",
    "edu.umd.cs.findbugs.annotations.Nullable",
    "android.support.annotation.Nullable",
    "androidx.annotation.Nullable",
    "androidx.annotation.RecentlyNullable",
    "org.checkerframework.checker.nullness.qual.Nullable",
    "org.checkerframework.checker.nullness.compatqual.NullableDecl",
    "org.checkerframework.checker.nullness.compatqual.NullableType"
).map { JavaType.Basic(it) }

val JavaAnnotatedElement.hasNullableAnnotation: Boolean
    get() = nullableAnnotations.any { hasAnnotation(it) }
