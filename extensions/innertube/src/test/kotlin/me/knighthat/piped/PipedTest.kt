package me.knighthat.piped

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.knighthat.piped.Piped.DOMAIN_NO_PATH_REGEX
import me.knighthat.piped.Piped.getDistinctFirstGroup
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PipedTest {

    companion object {
        fun getResourceAsText( path: String ) =
            PipedTest::class.java.classLoader?.getResource( path )?.readText()!!
    }

    @Test
    fun testDomainNoPathRegex() {
        val markdown = getResourceAsText( "piped/instances.md" )

        val jsonFile = getResourceAsText( "piped/domain-name-regex.json" )
        val json = Json.parseToJsonElement( jsonFile ).jsonObject

        json["matches"]!!.jsonArray.map( JsonElement::toString ).forEach {
            assertTrue(
                DOMAIN_NO_PATH_REGEX.find( it ) != null,
                "\"${DOMAIN_NO_PATH_REGEX.pattern}\" should match $it"
            )
        }

        json["not_matches"]!!.jsonArray.map( JsonElement::toString ).forEach {
            assertFalse(
                DOMAIN_NO_PATH_REGEX.find( it ) != null,
                "\"${DOMAIN_NO_PATH_REGEX.pattern}\" should NOT match $it"
            )
        }

        val instances = json["instances"]!!.jsonArray
            .mapNotNull( JsonElement::jsonPrimitive )
            .mapNotNull( JsonPrimitive::content )
        getDistinctFirstGroup(
            markdown,
            DOMAIN_NO_PATH_REGEX
        ).also {
            assertEquals( instances.size, it.size )
            assertArrayEquals( instances.toTypedArray(), it )
        }
    }
}