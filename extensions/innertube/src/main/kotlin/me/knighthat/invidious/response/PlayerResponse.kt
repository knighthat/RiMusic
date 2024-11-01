package me.knighthat.invidious.response

import kotlinx.serialization.Serializable
import me.knighthat.common.response.AudioFormat
import me.knighthat.common.response.MediaFormatContainer
import java.util.SortedSet

@Serializable
data class PlayerResponse(
    private val adaptiveFormats: List<AdaptiveFormat>
): MediaFormatContainer<PlayerResponse.AdaptiveFormat> {

    override val formats: SortedSet<out AdaptiveFormat> =
        sortedSetOf<AdaptiveFormat>().apply {
            val audioOnly = adaptiveFormats.filter {
                it.mimeType.startsWith("audio")
            }

            addAll( audioOnly )
        }

    @Serializable
    data class AdaptiveFormat(
        val type: String,
        override val itag: UByte,
        override val url: String,
        override val bitrate: Int
    ): AudioFormat {

        override val mimeType: String
            get() = type.split( ";" )[0].trim()
        override val codec: String
            get() = type.split( ";" )[1].trim()
    }
}