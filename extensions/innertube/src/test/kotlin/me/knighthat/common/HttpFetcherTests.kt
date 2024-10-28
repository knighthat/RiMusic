package me.knighthat.common

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.io.InputStreamReader

class HttpFetcherTests {

    companion object {

        private lateinit var TLD_SUB_DOMAINS_MAP: Map<String, Array<String>>
        private lateinit var DOMAINS_WITH_PATHS: Map<String, String>

        @JvmStatic
        @BeforeAll
        fun setUp() {
            val classLoader = HttpFetcher::class.java.classLoader
            val gson = Gson()

            var inStream: InputStream?
            var jsonFile: JsonElement

            // TLD_SUB_DOMAINS_MAP
            inStream = classLoader.getResourceAsStream( "tld-sub-domains-map.json" )
            jsonFile = gson.fromJson( InputStreamReader( inStream!! ), JsonArray::class.java )

            val tldSubDomainsMap = mutableMapOf<String, Array<String>>()
            jsonFile.map( JsonElement::getAsJsonObject ).forEach {
                tldSubDomainsMap[it["domain"].asString] = it["subdomains"].asJsonArray
                    .map( JsonElement::getAsString )
                    .toTypedArray()
            }
            TLD_SUB_DOMAINS_MAP = tldSubDomainsMap
            // TLD_SUB_DOMAINS_MAP

            // DOMAINS_WITH_PATHS
            inStream = classLoader.getResourceAsStream( "domains-with-paths.json" )
            jsonFile = gson.fromJson( InputStreamReader( inStream!! ), JsonArray::class.java )

            val domainsWithPathsMap = mutableMapOf<String, String>()
            jsonFile.map( JsonElement::getAsJsonObject ).forEach {
                domainsWithPathsMap[it["domain"].asString] = it["full"].asString
            }
            DOMAINS_WITH_PATHS = domainsWithPathsMap
            // DOMAINS_WITH_PATHS
        }
    }

    /**
     * [HttpFetcher.CLIENT] must be able to connect to a website
     * given the address and configuration are correct.
     *
     *  TODO: Add test cases to test [HttpFetcher.CONNECT_TIMEOUT] & [HttpFetcher.REQUEST_TIMEOUT]
     */
    @Test
    fun testClient(): Unit = runBlocking {
        try {
            HttpFetcher.CLIENT.get("https://example.org")
        } catch ( e: Exception ) {
            fail( e.message )
        }

        val invalidUrl = "htp://example,com"
        assertThrows( IllegalArgumentException::class.java ) {
            runBlocking {
                HttpFetcher.CLIENT.get( invalidUrl )
            }
        }

        // Adding 1,000 millis to ensure waiting time exceeds defined value
        val socketTimeout = "https://httpstat.us/200?sleep=${HttpFetcher.SOCKET_TIMEOUT + 1000L}"
        assertThrows( SocketTimeoutException::class.java ) {
            runBlocking {
                HttpFetcher.CLIENT.get( socketTimeout )
            }
        }
    }


    @Test
    fun testGetDomainName() {
        DOMAINS_WITH_PATHS.forEach { (domain, full) ->
            assertEquals( domain, HttpFetcher.getDomainName( full ) )
        }
    }

    @Test
    fun testGetTld() {
        TLD_SUB_DOMAINS_MAP.forEach { (tld, subs) ->
            val subSet = subs.map( HttpFetcher::getTld).toSet()

            assertEquals( 1, subSet.size )
            assertEquals( tld, subSet.first() )
        }
    }

    @Test
    fun testGenMatchAllTld() {
        TLD_SUB_DOMAINS_MAP.forEach { (tld, subs) ->
            val regex = HttpFetcher.genMatchAllTld( tld )
            subs.forEach { assertTrue( regex.matches( it ) ) }
        }
    }
}