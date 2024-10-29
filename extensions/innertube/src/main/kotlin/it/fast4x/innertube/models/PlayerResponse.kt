package it.fast4x.innertube.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.knighthat.common.response.AudioFormat
import me.knighthat.common.response.MediaFormatContainer
import java.util.SortedSet

@Serializable
data class PlayerResponse(
    val playabilityStatus: PlayabilityStatus?,
    val playerConfig: PlayerConfig?,
    val streamingData: StreamingData?,
    val videoDetails: VideoDetails?,
) {
    @Serializable
    data class PlayabilityStatus(
        val status: String?,
        val reason: String?
    )

    @Serializable
    data class PlayerConfig(
        val audioConfig: AudioConfig?
    ) {
        @Serializable
        data class AudioConfig(
            val loudnessDb: Float?
        ) {
            // For music clients only
            val normalizedLoudnessDb: Float?
                get() = loudnessDb?.plus(7)
        }
    }

    @Serializable
    data class StreamingData(
        val expiresInSeconds: Long?,
        private val adaptiveFormats: List<AdaptiveFormat>,
    ): MediaFormatContainer<StreamingData.AdaptiveFormat> {

        override val formats: SortedSet<AdaptiveFormat> =
            sortedSetOf<AdaptiveFormat>().apply {
                // Should filter format starts with "audio" as in "audio/webm"
                addAll( adaptiveFormats.filter { it.mimeType.startsWith("audio") } )
            }

        @Serializable
        data class AdaptiveFormat(
            val averageBitrate: Long?,
            val contentLength: Long?,
            val approxDurationMs: Long?,
            val lastModified: Long?,
            val width: Int?,
            @SerialName("mimeType")
            val mimeTypeCodec: String,
            override val itag: UShort,
            override val url: String,
            override val bitrate: UInt
        ): AudioFormat {

            override val mimeType: String
                get() = mimeTypeCodec.split( ";" )[0].trim()
            override val codec: String
                get() = mimeTypeCodec.split( ";" )[1].trim()

            val isAudio: Boolean
                get() = width == null

            val isVideo: Boolean
                get() = width != null
        }
    }

    @Serializable
    data class VideoDetails(
        val videoId: String?
    )
}
