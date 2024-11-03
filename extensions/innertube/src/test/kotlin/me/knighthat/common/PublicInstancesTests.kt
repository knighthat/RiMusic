package me.knighthat.common

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class PublicInstancesTests {

    companion object {

        private val DOMAINS: JsonObject

        init {
            val file = FakePublicInstances::class.java
                                                 .classLoader
                                                 ?.getResource("common/domains.json")
                                                 ?.readText()!!
            DOMAINS = Json.parseToJsonElement( file ).jsonObject
        }
    }

    lateinit var fakeInstance: FakePublicInstances

    private val executable: () -> Unit = {
        fakeInstance.blacklistUrl( "https://example.org" )
    }

    private fun fetchInstances() = runBlocking {
        fakeInstance.fetchInstances()
    }

    @BeforeEach
    fun setUp() { fakeInstance = FakePublicInstances( DOMAINS ) }

    @Test
    fun testReachableInstances() {
        fetchInstances()
        assertDoesNotThrow( executable )

        assertEquals(
            DOMAINS["exclusive"]!!.jsonArray.mapNotNull( JsonElement::jsonPrimitive ).mapNotNull( JsonPrimitive::content ),
            fakeInstance.reachableInstances
        )
    }

    /**
     *  [PublicInstances.instances] and [PublicInstances.unreachableInstances] must be
     *  initialize by [PublicInstances.fetchInstances]
     */
    @Test
    fun testFetchInstances() {
        assertFalse( fakeInstance.isInstancesInitialized() )
        assertFalse( fakeInstance.isUnreachableInstancesInitialized() )

        fetchInstances()

        assertTrue( fakeInstance.isInstancesInitialized() )
        assertTrue( fakeInstance.isUnreachableInstancesInitialized() )
    }


    @Test
    fun testBlacklistUrl() {
        assertThrows( UninitializedPropertyAccessException::class.java, executable )

        fetchInstances()

        assertDoesNotThrow( executable )
    }
}