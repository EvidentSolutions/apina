package fi.evident.apina.spring

import fi.evident.apina.model.ClassDefinition
import fi.evident.apina.model.type.ApiType
import kotlin.test.assertEquals

fun assertHasProperties(classDefinition: ClassDefinition, vararg properties: Pair<String, ApiType>) {
    assertEquals(
        classDefinition.properties.associate { it.name to it.type }.toSortedMap(),
        properties.toMap().toSortedMap(),
        "Properties don't match"
    )
}

fun assertHasProperties(classDefinition: ClassDefinition, vararg properties: String) {
    assertEquals(
        classDefinition.properties.map { it.name }.toSortedSet(),
        properties.toSortedSet(),
        "Properties don't match"
    )
}
