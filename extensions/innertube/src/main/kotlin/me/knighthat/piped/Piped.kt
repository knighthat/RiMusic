package me.knighthat.piped

import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import me.knighthat.common.HttpFetcher
import me.knighthat.common.HttpFetcher.genMatchAllTld
import me.knighthat.common.PublicInstances

object Piped: PublicInstances() {

    private const val INSTANCES_GITHUB =
        "https://raw.githubusercontent.com/wiki/TeamPiped/Piped-Frontend/Instances.md"

    internal val DOMAIN_NO_PATH_REGEX = Regex( "https?://(.*?)(?=\\s\\||/.* \\|)" )

    override suspend fun fetchInstances() {
        super.fetchInstances()

        try {
            val response = HttpFetcher.CLIENT
                                      .get( INSTANCES_GITHUB )
                                      .bodyAsText()

            instances = getDistinctFirstGroup( response, DOMAIN_NO_PATH_REGEX )
        } catch ( e: HttpRequestTimeoutException ) {
            println( "Failed to fetch Piped instances: ${e.message}" )
        }
    }
}