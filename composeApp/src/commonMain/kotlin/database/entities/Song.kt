package database.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity
data class Song(
    @PrimaryKey val id: String,
    val title: String,
    val artistsText: String? = null,
    val durationText: String?,
    val thumbnailUrl: String?,
    val likedAt: Long? = null,
    val totalPlayTimeMs: Long = 0
) {
    fun toggleLike(): Song {
        return copy(
            likedAt = if (likedAt == null) System.currentTimeMillis() else null
        )
    }
}
