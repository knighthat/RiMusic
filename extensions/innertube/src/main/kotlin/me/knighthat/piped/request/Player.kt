package me.knighthat.piped.request

import io.ktor.http.HttpMethod
import it.fast4x.innertube.utils.runCatchingNonCancellable
import me.knighthat.common.HttpFetcher
import me.knighthat.piped.Piped
import me.knighthat.piped.response.PlayerResponse

suspend fun Piped.player( videoId: String ): Result<PlayerResponse?>? =
    runCatchingNonCancellable {
        HttpFetcher.asyncMultiRequestGetFirstValid<PlayerResponse>(
            HttpMethod.Get,
            reachableInstances,
            "/streams/$videoId"
        ) { _, hostUrl ->
            /**
             * This is a failsafe + performance boost.
             * If a website returns unusable responses, it will
             * be blacklisted to prevent from future use.
             *
             * This in turn makes the time it takes to fetch
             * data in the future shorter because there's less
             * link to fetch, also less error to check.
             */
            blacklistUrl( hostUrl )
            null
        }
    }
