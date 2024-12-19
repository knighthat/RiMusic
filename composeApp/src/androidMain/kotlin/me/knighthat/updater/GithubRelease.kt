package me.knighthat.updater

import android.text.format.Formatter
import it.fast4x.rimusic.appContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubRelease(
    val id: UInt,
    @SerialName("tag_name") val tagName: String,
    val name: String,
    @SerialName("assets") val builds: List<Build>
) {

    @Serializable
    data class Build(
        val id: UInt,
        val url: String,
        val name: String,
        val size: UInt,
    ) {
        fun readableSize(): String = Formatter.formatShortFileSize( appContext(), this.size.toLong() )
    }
}