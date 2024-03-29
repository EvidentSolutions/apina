package fi.evident.apina.spring

import fi.evident.apina.model.ApiDefinition
import fi.evident.apina.model.ClassDefinition
import fi.evident.apina.model.type.ApiType
import fi.evident.apina.model.type.ApiTypeName
import kotlin.test.assertEquals

fun assertHasProperties(classDefinition: ClassDefinition, vararg properties: Pair<String, ApiType>) {
    assertEquals(
        properties.toMap().toSortedMap(),
        classDefinition.properties.associate { it.name to it.type }.toSortedMap(),
        "Properties don't match"
    )
}

fun assertHasProperties(classDefinition: ClassDefinition, vararg properties: String) {
    assertEquals(
        properties.toSortedSet(),
        classDefinition.properties.map { it.name }.toSortedSet(),
        "Properties don't match"
    )
}

fun assertHasTypeAlias(api: ApiDefinition, from: String, to: ApiType) {
    assertEquals(to, api.typeAliases[ApiTypeName(from)], "Type alias for '$from' was invalid.")
}
