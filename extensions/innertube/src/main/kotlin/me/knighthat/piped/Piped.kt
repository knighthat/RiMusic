package me.knighthat.piped

import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import me.knighthat.common.HttpFetcher
import me.knighthat.common.HttpFetcher.genMatchAllTld
import okio.IOException

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

    internal suspend inline fun <reified T> asyncMultiRequest(
        method: HttpMethod,
        endpoint: String,
        crossinline body: (HttpRequestBuilder) -> Unit
    ): T? = coroutineScope {
        val reachableUrls = API_INSTANCES.filter {
            for( regex in UNREACHABLE_INSTANCES )
                if( regex.matches( it ) )
                    return@filter false

            true
        }

        val deferredResponses = reachableUrls.map { hostUrl ->
            async {
                try {
                    val response = HttpFetcher.CLIENT.request {
                        this.method = method
                        url {
                            protocol = URLProtocol.HTTPS
                            host = "$hostUrl$endpoint"
                        }
                        body(this)
                    }

                    // Only accept successful responses
                    if (response.status == HttpStatusCode.OK) {
                        println("Fetch $hostUrl$endpoint returned code: ${response.status}:")
                        println(response.bodyAsText().replace("\n", ""))

                        response.body<T>()
                    } else
                        null

                } catch ( e: Exception ) {
                    when( e ) {
                        /**
                         * This is a failsafe + performance boost.
                         * If a website returns unusable responses, it will
                         * be blacklisted to prevent from future use.
                         *
                         * This in turn makes the time it takes to fetch
                         * data in the future shorter because there's less
                         * link to fetch, also less error to check.
                         */
                        is IOException,
                        is NoTransformationFoundException -> {
                            blacklistUrl( hostUrl)
                            null
                        }
                        else -> throw e
                    }
                }
            }
        }

        // Wait for the first non-null response
        deferredResponses.firstNotNullOfOrNull { it.await() }
    }
}