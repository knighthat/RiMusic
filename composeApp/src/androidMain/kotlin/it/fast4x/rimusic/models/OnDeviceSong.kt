package it.fast4x.rimusic.models

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity
data class OnDeviceSong (
    @PrimaryKey val id: String,
    val title: String,
    val artistsText: String? = null,
    val durationText: String?,
    val thumbnailUrl: String?,
    val likedAt: Long? = null,
    val relativePath: String,
    val totalPlayTimeMs: Long = 0
) {

    fun toggleLike(): OnDeviceSong {
        return copy(
            likedAt = if (likedAt == null) System.currentTimeMillis() else null
        )
    }
    fun toSong(): Song {
        return Song(
            id = id,
            title = title,
            artistsText = artistsText,
            durationText = durationText,
            thumbnailUrl = thumbnailUrl,
            likedAt = likedAt,
            totalPlayTimeMs = totalPlayTimeMs
        )
    }

    fun toSongEntity(): SongEntity {
        return SongEntity(
            Song(
                id = id,
                title = title,
                artistsText = artistsText,
                durationText = durationText,
                thumbnailUrl = thumbnailUrl,
                likedAt = likedAt,
                totalPlayTimeMs = totalPlayTimeMs
            ),
            albumTitle = "",
            contentLength = 0L
        )
    }
}
