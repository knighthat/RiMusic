package me.knighthat.invidious

import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.knighthat.common.HttpFetcher
import me.knighthat.common.PublicInstances

object Invidious: PublicInstances() {

    private const val VERIFIED_PUBLIC_INSTANCES =
        "https://raw.githubusercontent.com/iv-org/documentation/refs/heads/master/docs/instances.md"
    private const val UNOFFICIAL_PUBLIC_INSTANCES =
        "https://raw.githubusercontent.com/foreign-affairs/invidious-documentation/refs/heads/master/docs/instances.md"

    internal val DOMAIN_NO_PATH_REGEX = Regex( "\\((https?://[^)]+?)\\)" )

    var useUnofficialInstances: Boolean = false
        set(value) {
            if( field == value )
                return
            else
                field = value

            // Re-fetch instances when boolean is flipped
            CoroutineScope( Dispatchers.IO ).launch {
                this@Invidious.fetchInstances()
            }
        }

    override suspend fun fetchInstances() {
        super.fetchInstances()

        val url = if( useUnofficialInstances ) UNOFFICIAL_PUBLIC_INSTANCES else VERIFIED_PUBLIC_INSTANCES

        try {
            val sectionStart = "## List of public Invidious Instances (sorted from oldest to newest):"
            val sectionEnd = "### Tor Onion Services:"
            val response = HttpFetcher.CLIENT
                                      .get( url )
                                      .bodyAsText()
                                      .substringAfter( sectionStart )
                                      .substringBefore( sectionEnd )

            instances = getDistinctFirstGroup( response, DOMAIN_NO_PATH_REGEX )
        } catch ( e: HttpRequestTimeoutException ) {
            println( "Failed to fetch Invidious instances: ${e.message}" )
        }
    }
}