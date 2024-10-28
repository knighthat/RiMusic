package me.knighthat.piped.request

import io.ktor.http.HttpMethod
import it.fast4x.innertube.utils.runCatchingNonCancellable
import me.knighthat.piped.Piped
import me.knighthat.piped.response.PlayerResponse

suspend fun Piped.player( videoId: String ): Result<PlayerResponse?>? =
    runCatchingNonCancellable {
        asyncMultiRequest<PlayerResponse>(
            HttpMethod.Get,
            "/streams/$videoId"
        ) {}
    }
