package fi.evident.apina.model.settings

enum class NestedClassNameMode {
    /**
     * Use unqualified names for nested classes (current behavior).
     * Example: Foo.Bar -> Bar
     */
    UNQUALIFIED,

    /**
     * Use qualified names for all nested classes.
     * Example: Foo.Bar -> Foo_Bar
     */
    QUALIFIED,

    /**
     * Use qualified names only for discriminated union members,
     * but e.g. controller-nested classes remain unqualified.
     *
     * Example: sealed class Foo { class Bar } -> Foo_Bar
     */
    QUALIFIED_DISCRIMINATED_UNIONS,
}
