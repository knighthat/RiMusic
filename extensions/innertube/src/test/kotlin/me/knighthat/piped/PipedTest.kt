package me.knighthat.piped

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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
        Piped.DOMAIN_NO_PATH_REGEX
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
}