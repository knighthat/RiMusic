package me.knighthat.common

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.annotations.TestOnly

@TestOnly
class FakePublicInstances(
    private val json: JsonObject
): PublicInstances() {

    override suspend fun fetchInstances() {
        super.fetchInstances()
        instances = json["all"]!!.jsonArray
                                 .mapNotNull( JsonElement::jsonPrimitive )
                                 .mapNotNull( JsonPrimitive::content )
                                 .toTypedArray()
    }
}