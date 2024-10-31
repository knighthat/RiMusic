package me.knighthat.component.tab.toolbar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.MutableIntState
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.R
import it.fast4x.rimusic.query
import it.fast4x.rimusic.service.PlayerService
import it.fast4x.rimusic.service.isLocal
import it.fast4x.rimusic.utils.manageDownload

@UnstableApi
interface DeleteDownloadsDialog: ConfirmationDialog {

    val binder: PlayerService.Binder?
    val downloadState: MutableIntState

    override val iconId: Int
        @DrawableRes
        get() = R.drawable.download
    override val titleId: Int
        @StringRes
        get() = R.string.do_you_really_want_to_delete_download
    override val messageId: Int
        @StringRes
        get() = R.string.info_remove_all_downloaded_songs

    fun listToProcess(): List<MediaItem>

    override fun onConfirm() {
        listToProcess().forEach {
            if(binder == null){ // binder has to be non-null for remove from cache to work
                return
            }
            binder?.cache?.removeResource(it.mediaId)
            query {
                Database.resetFormatContentLength(it.mediaId)
            }

            if (!it.isLocal)
                manageDownload(
                    context = context,
                    mediaItem = it,
                    downloadState = true
                )
        }

        onDismiss()
    }
}