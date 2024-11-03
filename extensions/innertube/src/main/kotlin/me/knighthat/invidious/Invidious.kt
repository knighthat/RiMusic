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
    internal const val SECTION_START =
        "## List of public Invidious Instances (sorted from oldest to newest):"
    internal const val SECTION_END = "### Tor Onion Services:"

    /**
     * This pattern translates to:
     * "match any HTTP(S) url (path included) within opening and closing parentheses,
     * but only capture the domain name"
     *
     * - `\(` and `\)`: Match the opening and closing parentheses.
     * - `https?:\/\/`: Match either **_http://_** or **_https://_**.
     * - `([^)|\s]+\.[a-z]{2,})`: Capture only the domain name (excluding paths).
     * - `(?:\/[^)]*)?`: This is a non-capturing group that matches any path after the domain, but doesn’t capture it in the results.
     */
    internal val DOMAIN_NO_PATH_REGEX = Regex( "\\(https?://([^)|\\s]+\\.[a-z]{2,})(?:/[^)]*)?\\)" )

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
            val response = HttpFetcher.CLIENT
                                      .get( url )
                                      .bodyAsText()
                                      .substringAfter( SECTION_START )
                                      .substringBefore( SECTION_END )

            instances = getDistinctFirstGroup( response, DOMAIN_NO_PATH_REGEX )
        } catch ( e: HttpRequestTimeoutException ) {
            println( "Failed to fetch Invidious instances: ${e.message}" )
        }
    }
}