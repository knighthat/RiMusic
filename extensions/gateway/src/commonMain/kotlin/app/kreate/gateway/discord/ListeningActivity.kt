package app.kreate.gateway.discord


data class ListeningActivity(
    val timeStart: Long,
    val duration: Long,
    val songName: String,
    val thumbnailUrl: String?,
    val artistName: String,
    val artistUrl: String?,
    val artistThumbnailUrl: String?,
    val albumName: String
)