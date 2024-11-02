package me.knighthat.piped

import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import me.knighthat.common.HttpFetcher
import me.knighthat.common.HttpFetcher.genMatchAllTld

object Piped {

    internal val DOMAIN_NO_PATH_REGEX = Regex( "https?://(.*?)(?=\\s\\||/.* \\|)" )

    private const val INSTANCES_GITHUB =
        "https://raw.githubusercontent.com/wiki/TeamPiped/Piped-Frontend/Instances.md"

    private lateinit var API_INSTANCES: Array<String>
    private lateinit var UNREACHABLE_INSTANCES: MutableList<Regex>

    internal val REACHABLE_INSTANCES: Collection<String>
        get() = API_INSTANCES.filter {
            for ( regex in UNREACHABLE_INSTANCES )
                if ( regex.matches(it) )
                    return@filter false

            true
        }

    fun blacklistUrl( url: String ) {
        if( !::UNREACHABLE_INSTANCES.isInitialized )
            throw UninitializedPropertyAccessException( "Please initialize Piped instances with Piped#fetchPipedInstance()" )
        else
            UNREACHABLE_INSTANCES.add( genMatchAllTld( url ) )
    }

    suspend fun fetchPipedInstances() {

        try {
            val response = HttpFetcher.CLIENT
                                      .get( INSTANCES_GITHUB )
                                      .bodyAsText()

            API_INSTANCES = DOMAIN_NO_PATH_REGEX.findAll( response )
                                                .map { it.groups[1]?.value }
                                                .filterNotNull()
                                                .toList()
                                                .toTypedArray()
        } catch ( e: HttpRequestTimeoutException ) {
            println( "Failed to fetch Piped instances: ${e.message}" )

            API_INSTANCES = arrayOf()
        }


        // Reset unreachable urls
        UNREACHABLE_INSTANCES = mutableListOf()
    }
}