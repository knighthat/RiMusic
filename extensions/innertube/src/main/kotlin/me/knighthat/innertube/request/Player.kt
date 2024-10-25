package me.knighthat.innertube.request

import io.ktor.http.HttpMethod
import it.fast4x.innertube.utils.runCatchingNonCancellable
import me.knighthat.innertube.Piped
import me.knighthat.innertube.response.PlayerResponse

suspend fun Piped.player( videoId: String ): Result<PlayerResponse?>? =
    runCatchingNonCancellable {
        asyncMultiRequest<PlayerResponse>(
            HttpMethod.Get,
            "/streams/$videoId"
        ) {}
    }
