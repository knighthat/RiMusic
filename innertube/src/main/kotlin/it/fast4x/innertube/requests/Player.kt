package it.fast4x.innertube.requests

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.fast4x.innertube.Innertube
import it.fast4x.innertube.models.Context
import it.fast4x.innertube.models.PlayerResponse
import it.fast4x.innertube.models.bodies.PlayerBody
import it.fast4x.innertube.utils.runCatchingNonCancellable
import kotlinx.serialization.Serializable

suspend fun Innertube.player(body: PlayerBody) = runCatchingNonCancellable {
    val response = client.post(player) {
        setBody(body)
        mask("playabilityStatus.status,playerConfig.audioConfig,streamingData.adaptiveFormats,videoDetails.videoId")
    }.body<PlayerResponse>()

    println("mediaItem requests Player response $response")

    when( response.playabilityStatus?.status ) {
        "OK", "LOGIN_REQUIRED", "UNPLAYABLE" -> response
        else -> {
            @Serializable
            data class AudioStream(
                val url: String,
                val bitrate: Long
            )

            @Serializable
            data class PipedResponse(
                val audioStreams: List<AudioStream>
            )

            val safePlayerResponse = client.post(player) {
                setBody(
                    body.copy(
                        //context = Context.DefaultAgeRestrictionBypass.copy(
                        context = Context.DefaultWeb.copy(
                            thirdParty = Context.ThirdParty(
                                embedUrl = "https://www.youtube.com/watch?v=${body.videoId}"
                            )
                        ),
                    )
                )
                mask("playabilityStatus.status,playerConfig.audioConfig,streamingData.adaptiveFormats,videoDetails.videoId")
            }.body<PlayerResponse>()

            if (safePlayerResponse.playabilityStatus?.status != "OK") {
                return@runCatchingNonCancellable response
            }

            val audioStreams = client.get("https://watchapi.whatever.social/streams/${body.videoId}") {
                contentType(ContentType.Application.Json)
            }.body<PipedResponse>().audioStreams

            safePlayerResponse.copy(
                streamingData = safePlayerResponse.streamingData?.copy(
                    adaptiveFormats = safePlayerResponse.streamingData.adaptiveFormats?.map { adaptiveFormat ->
                        adaptiveFormat.copy(
                            url = audioStreams.find { it.bitrate == adaptiveFormat.bitrate }?.url
                        )
                    }
                )
            )
        }
    }
}
