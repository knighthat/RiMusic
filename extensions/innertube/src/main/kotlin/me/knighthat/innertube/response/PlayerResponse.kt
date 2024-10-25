package me.knighthat.innertube.response

import kotlinx.serialization.Serializable

@Serializable
data class PlayerResponse(
    val audioStreams: List<AudioStream>
) {

    val autoMaxQualityFormat: AudioStream
        get() = highestQualityFormat

    val highestQualityFormat: AudioStream
        get() = audioStreams.sortedBy(AudioStream::bitrate).last()

    val lowestQualityFormat: AudioStream
        get() = audioStreams.sortedBy(AudioStream::bitrate).first()

    val mediumQualityFormat: AudioStream
        get() = audioStreams.sortedBy(AudioStream::bitrate)[audioStreams.size / 2]


    @Serializable
    data class AudioStream(
        val url: String,
        val bitrate: Int
    )
}