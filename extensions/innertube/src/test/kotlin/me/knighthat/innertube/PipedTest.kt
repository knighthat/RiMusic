package me.knighthat.innertube

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.io.InputStreamReader

class PipedTest {

    companion object {

        private val MARKDOWN_FILE =
            PipedTest::class.java.classLoader.getResource("piped-instances.md")?.readText() ?: ""

        private val DOMAIN_NO_PATTERN_MATCHES = arrayOf(
            "pipedapi.kavin.rocks",
            "pipedapi.tokhmi.xyz",
            "pipedapi.moomoo.me",
            "pipedapi.syncpundit.io",
            "api-piped.mha.fi",
            "piped-api.garudalinux.org",
            "pipedapi.rivo.lol",
            "pipedapi.leptons.xyz",
            "piped-api.lunar.icu",
            "ytapi.dc09.ru",
            "pipedapi.colinslegacy.com",
            "yapi.vyper.me",
            "api.looleh.xyz",
            "piped-api.cfe.re",
            "pipedapi.r4fo.com",
            "pipedapi.nosebs.ru",
            "pipedapi-libre.kavin.rocks",
            "pa.mint.lgbt",
            "pa.il.ax",
            "piped-api.privacy.com.de",
            "api.piped.projectsegfau.lt",
            "pipedapi.in.projectsegfau.lt",
            "pipedapi.us.projectsegfau.lt",
            "watchapi.whatever.social",
            "api.piped.privacydev.net",
            "pipedapi.palveluntarjoaja.eu",
            "pipedapi.smnz.de",
            "pipedapi.adminforge.de",
            "pipedapi.qdi.fi",
            "piped-api.hostux.net",
            "pdapi.vern.cc",
            "pipedapi.pfcd.me",
            "pipedapi.frontendfriendly.xyz",
            "api.piped.yt",
            "pipedapi.astartes.nl",
            "pipedapi.osphost.fi",
            "pipedapi.simpleprivacy.fr",
            "pipedapi.drgns.space",
            "piapi.ggtyler.dev",
            "api.watch.pluto.lat",
            "piped-backend.seitan-ayoub.lol",
            "pipedapi.owo.si",
            "api.piped.minionflo.net",
            "pipedapi.nezumi.party",
            "pipedapi.ducks.party",
            "pipedapi.ngn.tf",
            "pipedapi.coldforge.xyz",
            "piped-api.codespace.cz",
            "pipedapi.reallyaweso.me",
            "pipedapi.phoenixthrush.com",
            "api.piped.private.coffee",
            "schaunapi.ehwurscht.at",
            "pipedapi.darkness.services",
            "pipedapi.andreafortuna.org"
        )

        private lateinit var TLD_SUB_DOMAINS_MAP: Map<String, Array<String>>
        private lateinit var DOMAINS_WITH_PATHS: Map<String, String>

        @JvmStatic
        @BeforeAll
        fun setUp() {
            val classLoader = PipedTest::class.java.classLoader
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
     * This function tests whether the pattern Pipe#DOMAIN_NO_PATH_PATTERN
     * extracts all domains from given markdown file (which fetched from the internet).
     *
     *  These cases should be included in the test (will be added more in the future):
     *  1. Whether size of matches equals to pre-defined ones
     *  2. Ensure matches is included inside pre-defines values
     */
    @Test
    fun testDomainNoPathPattern() {
        Piped.domainNoPathPattern
             .findAll( MARKDOWN_FILE )          // Look for all matches
             .map { it.groups[1]?.value }       // Use first group of each match
             .filterNotNull()                   // Ignore if result is null
             .also {
                 assertTrue( DOMAIN_NO_PATTERN_MATCHES.size == it.toList().size )
             }
             .forEach {
                 assertTrue( DOMAIN_NO_PATTERN_MATCHES.contains( it ) )
             }
    }

    @Test
    fun testGetDomainName() {
        DOMAINS_WITH_PATHS.forEach { (domain, full) ->
            assertEquals( domain, Piped.domainName( full ) )
        }
    }

    @Test
    fun testGetTld() {
        TLD_SUB_DOMAINS_MAP.forEach { (tld, subs) ->
            val subSet = subs.map( Piped::tld ).toSet()

            assertEquals( 1, subSet.size )
            assertEquals( tld, subSet.first() )
        }
    }

    @Test
    fun testGenMatchAllTld() {
        TLD_SUB_DOMAINS_MAP.forEach { (tld, subs) ->
            val regex = Piped.matchAllRegex( tld )
            subs.forEach { assertTrue( regex.matches( it ) ) }
        }
    }
}