package me.knighthat.invidious.request

import io.ktor.http.HttpMethod
import it.fast4x.innertube.utils.runCatchingNonCancellable
import me.knighthat.invidious.Invidious
import me.knighthat.invidious.response.PlayerResponse

suspend fun Invidious.player( videoId: String ): Result<PlayerResponse?>? =
    runCatchingNonCancellable {
        asyncMultiRequest<PlayerResponse>(
            HttpMethod.Get,
            "/api/v1/videos/$videoId"
        ) {}
    }