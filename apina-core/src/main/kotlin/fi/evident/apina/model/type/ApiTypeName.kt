package fi.evident.apina.model.type

data class ApiTypeName(val name: String) : Comparable<ApiTypeName> {
    override fun toString() = name
    override fun compareTo(other: ApiTypeName) = name.compareTo(other.name)
}
