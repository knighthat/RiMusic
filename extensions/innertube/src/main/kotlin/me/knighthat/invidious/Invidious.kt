package me.knighthat.invidious

import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import me.knighthat.common.HttpFetcher

object Invidious {

    private const val VERIFIED_PUBLIC_INSTANCES =
        "https://raw.githubusercontent.com/iv-org/documentation/refs/heads/master/docs/instances.md"
    private const val UNOFFICIAL_PUBLIC_INSTANCES =
        "https://raw.githubusercontent.com/foreign-affairs/invidious-documentation/refs/heads/master/docs/instances.md"

    internal val DOMAIN_NO_PATH_REGEX = Regex( "\\((https?://[^)]+?)\\)" )

    private lateinit var API_INSTANCES: Array<String>
    private lateinit var UNREACHABLE_INSTANCES: MutableList<Regex>

    internal val REACHABLE_INSTANCES: Collection<String>
        get() = API_INSTANCES.filter {
            for ( regex in UNREACHABLE_INSTANCES)
                if ( regex.matches(it) )
                    return@filter false

            true
        }

    suspend fun fetchInvidiousInstances( unofficial: Boolean ) {
        val url = if( unofficial ) UNOFFICIAL_PUBLIC_INSTANCES else VERIFIED_PUBLIC_INSTANCES

        try {
            val sectionStart = "## List of public Invidious Instances (sorted from oldest to newest):"
            val sectionEnd = "### Tor Onion Services:"
            val response = HttpFetcher.CLIENT
                                      .get( url )
                                      .bodyAsText()
                                      .substringAfter( sectionStart )
                                      .substringBefore( sectionEnd )

            API_INSTANCES = DOMAIN_NO_PATH_REGEX.findAll( response )
                                                .map { it.groups[1]?.value }
                                                .filterNotNull()
                                                .toList()
                                                .toTypedArray()
        } catch ( e: HttpRequestTimeoutException ) {
            println( "Failed to fetch Invidious instances: ${e.message}" )

            API_INSTANCES = arrayOf()
        }

        // Reset unreachable urls
        UNREACHABLE_INSTANCES = mutableListOf()
    }

    fun blacklistUrl( url: String ) {
        if( !::UNREACHABLE_INSTANCES.isInitialized )
            throw UninitializedPropertyAccessException( "Please initialize Invidious instances with Invidious#fetchInvidiousInstances()" )
        else
            UNREACHABLE_INSTANCES.add( HttpFetcher.genMatchAllTld( url ) )
    }
}