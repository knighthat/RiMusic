package it.fast4x.rimusic.enums

import android.app.PendingIntent
import androidx.annotation.DrawableRes
import androidx.annotation.OptIn
import androidx.annotation.StringRes
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.SessionCommand
import it.fast4x.rimusic.R
import it.fast4x.rimusic.service.modern.MediaSessionConstants.CommandSearch
import it.fast4x.rimusic.service.modern.MediaSessionConstants.CommandStartRadio
import it.fast4x.rimusic.service.modern.MediaSessionConstants.CommandToggleDownload
import it.fast4x.rimusic.service.modern.MediaSessionConstants.CommandToggleLike
import it.fast4x.rimusic.service.modern.MediaSessionConstants.CommandToggleRepeatMode
import it.fast4x.rimusic.service.modern.MediaSessionConstants.CommandToggleShuffle
import it.fast4x.rimusic.service.modern.PlayerServiceModern
import me.knighthat.enums.Drawable
import me.knighthat.enums.TextView

enum class NotificationButtons(
    val sessionCommand: SessionCommand,
    @field:StringRes override val textId: Int,
    @field:DrawableRes override val iconId: Int
): TextView, Drawable {

    Download( CommandToggleDownload, R.string.download, R.drawable.download ),

    Favorites( CommandToggleLike, R.string.favorites, R.drawable.heart_outline ),

    Repeat( CommandToggleRepeatMode, R.string.repeat, R.drawable.repeat ),

    Shuffle( CommandToggleShuffle, R.string.shuffle, R.drawable.shuffle ),

    Radio( CommandStartRadio, R.string.start_radio, R.drawable.radio ),

    Search( CommandSearch, R.string.search, R.drawable.search );

    val pendingIntent: PendingIntent
        @OptIn(UnstableApi::class)
        get() = when (this) {
            Download -> PlayerServiceModern.Action.download.pendingIntent
            Favorites -> PlayerServiceModern.Action.like.pendingIntent
            Repeat -> PlayerServiceModern.Action.repeat.pendingIntent
            Shuffle -> PlayerServiceModern.Action.shuffle.pendingIntent
            Radio -> PlayerServiceModern.Action.playradio.pendingIntent
            Search -> PlayerServiceModern.Action.search.pendingIntent
        }

    @OptIn(UnstableApi::class)
    fun getStateIcon(button: NotificationButtons, likedState: Long?, downloadState: Int, repeatMode: Int, shuffleMode: Boolean): Int {
        return when (button) {
            Download -> when (downloadState) {
                androidx.media3.exoplayer.offline.Download.STATE_COMPLETED -> R.drawable.downloaded
                androidx.media3.exoplayer.offline.Download.STATE_DOWNLOADING,
                androidx.media3.exoplayer.offline.Download.STATE_QUEUED -> R.drawable.download_progress
                else -> R.drawable.download
            }
            Favorites -> when (likedState) {
                -1L -> R.drawable.heart_dislike
                null -> R.drawable.heart_outline
                else -> R.drawable.heart
            }
            Repeat -> when (repeatMode) {
                REPEAT_MODE_OFF -> R.drawable.repeat
                REPEAT_MODE_ONE -> R.drawable.repeatone
                REPEAT_MODE_ALL -> R.drawable.infinite
                else -> throw IllegalStateException()
            }
            Shuffle -> if (shuffleMode) R.drawable.shuffle_filled else R.drawable.shuffle
            Radio -> R.drawable.radio
            Search -> R.drawable.search
        }

    }
}