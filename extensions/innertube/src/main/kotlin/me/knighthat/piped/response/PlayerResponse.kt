package me.knighthat.piped.response

import kotlinx.serialization.Serializable
import me.knighthat.common.response.AudioFormat
import me.knighthat.common.response.MediaFormatContainer
import java.util.SortedSet

@Serializable
data class PlayerResponse(
    private val audioStreams: List<AudioStream>,
): MediaFormatContainer<PlayerResponse.AudioStream> {

    override val formats: SortedSet<AudioStream> =
        sortedSetOf<AudioStream>().apply { addAll( audioStreams ) }

    @Serializable
    data class AudioStream(
        val contentLength: UInt,
        override val itag: UByte,
        override val url: String,
        override val mimeType: String,
        override val codec: String,
        override val bitrate: Int
    ): AudioFormat
}