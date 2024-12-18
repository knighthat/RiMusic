package it.fast4x.rimusic.ui.items

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import coil.compose.AsyncImage
import it.fast4x.innertube.Innertube
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.EXPLICIT_PREFIX
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.enums.DownloadedStateMedia
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.service.MyDownloadService
import it.fast4x.rimusic.service.isLocal
import it.fast4x.rimusic.thumbnailShape
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.ui.components.tab.toolbar.Clickable
import it.fast4x.rimusic.ui.components.tab.toolbar.Descriptive
import it.fast4x.rimusic.ui.components.tab.toolbar.Icon
import it.fast4x.rimusic.ui.components.themed.*
import it.fast4x.rimusic.ui.styling.*
import it.fast4x.rimusic.utils.*
import it.fast4x.rimusic.utils.thumbnail
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOf


@UnstableApi
@Composable
fun SongItem(
    song: Innertube.SongItem,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    onDownloadClick: () -> Unit,
    downloadState: Int,
    thumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
    disableScrollingText: Boolean,
    isNowPlaying: Boolean = false,
    forceRecompose: Boolean = false
) {
    SongItem(
        thumbnailUrl = song.thumbnail?.size(thumbnailSizePx),
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        onDownloadClick = {
            CoroutineScope(Dispatchers.IO).launch {
                Database.upsert(song.asSong)
            }
            onDownloadClick()
        },
        downloadState = downloadState,
        mediaItem = song.asMediaItem,
        onThumbnailContent = thumbnailContent,
        disableScrollingText = disableScrollingText,
        isNowPlaying = isNowPlaying,
        forceRecompose = forceRecompose
    )
}

@UnstableApi
@Composable
fun SongItem(
    song: MediaItem,
    thumbnailSizeDp: Dp,
    thumbnailSizePx: Int,
    modifier: Modifier = Modifier,
    onThumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onDownloadClick: () -> Unit,
    downloadState: Int,
    isRecommended: Boolean = false,
    disableScrollingText: Boolean,
    isNowPlaying: Boolean = false,
    forceRecompose: Boolean = false
) {
    SongItem(
        thumbnailUrl = song.mediaMetadata.artworkUri.thumbnail(thumbnailSizePx)?.toString(),
        thumbnailSizeDp = thumbnailSizeDp,
        onThumbnailContent = onThumbnailContent,
        trailingContent = trailingContent,
        modifier = modifier,
        onDownloadClick = {
            CoroutineScope(Dispatchers.IO).launch {
                Database.upsert(song.asSong)
            }
            onDownloadClick()
        },
        downloadState = downloadState,
        isRecommended = isRecommended,
        mediaItem = song,
        disableScrollingText = disableScrollingText,
        isNowPlaying = isNowPlaying,
        forceRecompose = forceRecompose
    )
}

@UnstableApi
@Composable
fun SongItem(
    song: Song,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    onThumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onDownloadClick: () -> Unit,
    downloadState: Int,
    disableScrollingText: Boolean,
    isNowPlaying: Boolean = false,
    forceRecompose: Boolean = false
) {
    SongItem(
        thumbnailUrl = song.thumbnailUrl?.thumbnail(thumbnailSizePx),
        thumbnailSizeDp = thumbnailSizeDp,
        onThumbnailContent = onThumbnailContent,
        trailingContent = trailingContent,
        modifier = modifier,
        onDownloadClick = {
            CoroutineScope(Dispatchers.IO).launch {
                Database.upsert(song)
            }
            onDownloadClick()
        },
        downloadState = downloadState,
        mediaItem = song.asMediaItem,
        disableScrollingText = disableScrollingText,
        isNowPlaying = isNowPlaying,
        forceRecompose = forceRecompose
    )
}

@UnstableApi
@Composable
fun SongItem(
    thumbnailUrl: String?,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    onThumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onDownloadClick: () -> Unit,
    downloadState: Int,
    isRecommended: Boolean = false,
    mediaItem: MediaItem,
    disableScrollingText: Boolean,
    isNowPlaying: Boolean = false,
    forceRecompose: Boolean = false
) {
    val binder = LocalPlayerServiceBinder.current

    SongItem(
        thumbnailSizeDp = thumbnailSizeDp,
        thumbnailContent = {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(thumbnailShape())
                    .fillMaxSize()
            )

            onThumbnailContent?.invoke(this)

            NowPlayingSongIndicator(
                mediaId = mediaItem.mediaId,
                player = binder?.player
            )
        },
        modifier = modifier,
        trailingContent = trailingContent,
        onDownloadClick = onDownloadClick,
        downloadState = downloadState,
        isRecommended = isRecommended,
        mediaItem = mediaItem,
        disableScrollingText = disableScrollingText,
        isNowPlaying = isNowPlaying,
        forceRecompose = forceRecompose
    )
}

/*
@Composable
fun SongItem(
    thumbnailContent: @Composable BoxScope.() -> Unit,
    title: String?,
    authors: String?,
    duration: String?,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
    isDownloaded: Boolean,
    onDownloadClick: () -> Unit,
    disableScrollingText: Boolean
) {
    ItemContainer(
        alternative = false,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(thumbnailSizeDp)
        ) {
            thumbnailContent()
        }

        ItemInfoContainer {
            trailingContent?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicText(
                        text = title ?: "",
                        style = typography().xs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
                    )

                    it()
                }
            } ?: BasicText(
                text = title ?: "",
                style = typography().xs.semiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
            )


            Row(verticalAlignment = Alignment.CenterVertically) {

                IconButton(
                    onClick = onDownloadClick,
                    icon = if (isDownloaded) R.drawable.downloaded else R.drawable.download,
                    color = if (isDownloaded) colorPalette().text else colorPalette().textDisabled,
                    modifier = Modifier
                        .size(16.dp)
                )

                Spacer(modifier = Modifier.padding(horizontal = 2.dp))

                BasicText(
                    text = authors ?: "",
                    style = typography().xs.semiBold.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .weight(1f)
                        .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
                )

                duration?.let {
                    BasicText(
                        text = duration,
                        style = typography().xxs.secondary.medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
*/

@OptIn(ExperimentalFoundationApi::class)
@UnstableApi
@Composable
fun SongItem(
    thumbnailContent: @Composable BoxScope.() -> Unit,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
    onDownloadClick: () -> Unit,
    downloadState: Int,
    isRecommended: Boolean = false,
    mediaItem: MediaItem,
    disableScrollingText: Boolean,
    isNowPlaying: Boolean = false,
    forceRecompose: Boolean = false
) {
    var downloadedStateMedia by remember { mutableStateOf(DownloadedStateMedia.NOT_CACHED_OR_DOWNLOADED) }
    downloadedStateMedia = if (!mediaItem.isLocal) downloadedStateMedia(mediaItem.mediaId)
    else DownloadedStateMedia.DOWNLOADED

    val isExplicit = mediaItem.mediaMetadata.title?.startsWith(EXPLICIT_PREFIX) == true
    val title = mediaItem.mediaMetadata.title.toString()
    val authors = mediaItem.mediaMetadata.artist.toString()
    val duration = mediaItem.mediaMetadata.extras?.getString("durationText")

    val playlistindicator by rememberPreference(playlistindicatorKey,false)
    var songPlaylist by remember {
        mutableIntStateOf(0)
    }
    if (playlistindicator)
        LaunchedEffect(Unit, forceRecompose) {
            withContext(Dispatchers.IO) {
                songPlaylist = Database.songUsedInPlaylists(mediaItem.mediaId)
            }
        }


    val context = LocalContext.current
    val colorPalette = LocalAppearance.current.colorPalette

    ItemContainer(
        alternative = false,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .conditional(isNowPlaying) {
                background(colorPalette.favoritesOverlay)
            }

    ) {
        Box(
            modifier = Modifier
                .size(thumbnailSizeDp)
        ) {
            thumbnailContent()


            var likedAt by remember {
                mutableStateOf<Long?>(null)
            }
            LaunchedEffect(Unit, mediaItem.mediaId) {
                Database.likedAt(mediaItem.mediaId).collect { likedAt = it }
            }
            if (likedAt != null)
                HeaderIconButton(
                    onClick = {},
                    icon = getLikeState(mediaItem.mediaId),
                    color = colorPalette().favoritesIcon,
                    iconSize = 12.dp,
                    modifier = Modifier
                        //.padding(start = 4.dp)
                        .align(Alignment.BottomStart)
                        .absoluteOffset(-8.dp, 0.dp)

                )
            /*
            if (totalPlayTimeMs != null) {
                if (totalPlayTimeMs <= 0 ) {
                    HeaderIconButton(
                        onClick = {},
                        icon = R.drawable.noteslashed,
                        color = colorPalette().favoritesIcon,
                        iconSize = 12.dp,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .align(Alignment.BottomStart)
                    )
                }
            }
             */

            /*
            BasicText(
                text = totalPlayTimeMs.toString() ?: "",
                style = typography().xs.semiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(all = 16.dp)
            )
             */
        }

        ItemInfoContainer {
            trailingContent?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isRecommended)
                        IconButton(
                            icon = R.drawable.smart_shuffle,
                            color = colorPalette().accent,
                            enabled = true,
                            onClick = {},
                            modifier = Modifier
                                .size(18.dp)
                        )

                    if (playlistindicator && (songPlaylist > 0)) {
                        IconButton(
                            icon = R.drawable.add_in_playlist,
                            color = colorPalette().text,
                            enabled = true,
                            onClick = {},
                            modifier = Modifier
                                .size(14.dp)
                                .background(colorPalette().accent, CircleShape)
                                .padding(all = 3.dp)
                                .combinedClickable(onClick = {}, onLongClick = {
                                    SmartMessage(
                                        context.resources.getString(R.string.playlistindicatorinfo2),
                                        context = context
                                    )
                                })
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 3.dp))
                    }

                    if (isExplicit)
                        IconButton(
                            icon = R.drawable.explicit,
                            color = colorPalette().text,
                            enabled = true,
                            onClick = {},
                            modifier = Modifier
                                .size(18.dp)
                        )

                    BasicText(
                        text = cleanPrefix(title),
                        style = typography().xs.semiBold,
                        /*
                        style = TextStyle(
                            color = if (isRecommended) colorPalette().accent else colorPalette().text,
                            fontStyle = typography().xs.semiBold.fontStyle,
                            fontSize = typography().xs.semiBold.fontSize
                        ),
                         */
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
                    )

                    /*
                    if (playlistindicator && (songPlaylist > 0)) {
                        IconButton(
                            icon = R.drawable.add_in_playlist,
                            color = colorPalette().text,
                            enabled = true,
                            onClick = {},
                            modifier = Modifier
                                .size(18.dp)
                                .background(colorPalette().accent, CircleShape)
                                .padding(all = 3.dp)
                                .combinedClickable(onClick = {}, onLongClick = {
                                    SmartMessage(context.resources.getString(R.string.playlistindicatorinfo2), context = context)
                                })
                        )
                    }
                     */

                    it()
                }
            } ?: Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isRecommended)
                        IconButton(
                            icon = R.drawable.smart_shuffle,
                            color = colorPalette().accent,
                            enabled = true,
                            onClick = {},
                            modifier = Modifier
                                .size(18.dp)
                        )

                    if (isExplicit)
                        IconButton(
                            icon = R.drawable.explicit,
                            color = colorPalette().text,
                            enabled = true,
                            onClick = {},
                            modifier = Modifier
                                .size(18.dp)
                        )
                    BasicText(
                        text = cleanPrefix(title),
                        style = typography().xs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
                            .weight(1f)
                    )
                if (playlistindicator && (songPlaylist > 0)) {
                    IconButton(
                        icon = R.drawable.add_in_playlist,
                        color = colorPalette().text,
                        enabled = true,
                        onClick = {},
                        modifier = Modifier
                            .size(18.dp)
                            .background(colorPalette().accent, CircleShape)
                            .padding(all = 3.dp)
                            .combinedClickable(onClick = {}, onLongClick = {
                                SmartMessage(
                                    context.resources.getString(R.string.playlistindicatorinfo2),
                                    context = context
                                )
                            })
                    )
                }
            }


            Row(verticalAlignment = Alignment.CenterVertically) {

                //Log.d("downloadState",downloadState.toString())

                /*
                if ((downloadState == Download.STATE_DOWNLOADING
                            || downloadState == Download.STATE_QUEUED
                            || downloadState == Download.STATE_RESTARTING
                        )
                    && !isDownloaded) {
                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            DownloadService.sendRemoveDownload(
                                context,
                                MyDownloadService::class.java,
                                mediaId,
                                false
                            )
                        },
                        icon = R.drawable.download_progress,
                        color = colorPalette().text,
                        modifier = Modifier
                            .size(16.dp)
                    )
                    /*
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = colorPalette().text,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                DownloadService.sendRemoveDownload(
                                        context,
                                        MyDownloadService::class.java,
                                        mediaId,
                                        false
                                    )
                            }
                    )
                     */
                } else {
                   IconButton(
                        onClick = onDownloadClick,
                        icon = if (isDownloaded) R.drawable.downloaded else R.drawable.download,
                        color = if (isDownloaded) colorPalette().text else colorPalette().textDisabled,
                        modifier = Modifier
                            .size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                */

                BasicText(
                    text = authors,
                    style = typography().xs.semiBold.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .weight(1f)
                        .conditional(!disableScrollingText) { basicMarquee(iterations = Int.MAX_VALUE) }
                )

                duration?.let {
                    BasicText(
                        text = duration,
                        style = typography().xxs.secondary.medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.padding(horizontal = 4.dp))

                //println("downloadutil $mediaId $downloadState: $downloadState")

                if ((downloadState == Download.STATE_DOWNLOADING
                            || downloadState == Download.STATE_QUEUED
                            || downloadState == Download.STATE_RESTARTING
                            )
                    && downloadedStateMedia == DownloadedStateMedia.NOT_CACHED_OR_DOWNLOADED) {
                    //val context = LocalContext.current
                    IconButton(
                        onClick = {
                            DownloadService.sendRemoveDownload(
                                context,
                                MyDownloadService::class.java,
                                mediaItem.mediaId,
                                false
                            )
                        },
                        icon = R.drawable.download_progress,
                        color = colorPalette().text,
                        modifier = Modifier
                            .size(20.dp)
                    )
                } else {
                    IconButton(
                        onClick = onDownloadClick,
                        icon = downloadedStateMedia.iconId,
                        color = when(downloadedStateMedia) {
                            DownloadedStateMedia.NOT_CACHED_OR_DOWNLOADED -> colorPalette().textDisabled
                            else -> colorPalette().text
                        },
                        modifier = Modifier
                            .size(20.dp)
                    )
                }

            }
        }
    }
}


@Composable
fun SongItemPlaceholder(
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier
) {
    ItemContainer(
        alternative = false,
        thumbnailSizeDp =thumbnailSizeDp,
        modifier = modifier
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette().shimmer, shape = thumbnailShape())
                .size(thumbnailSizeDp)
        )

        ItemInfoContainer {
            TextPlaceholder()
            TextPlaceholder()
        }
    }
}

/**
 * New component is more resemble to the final
 * SongItem that's currently being used.
 */
@Composable
fun SongItemPlaceholder( thumbnailSizeDp: Dp ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy( 12.dp ),
        modifier = Modifier.fillMaxWidth()
                           .padding(
                               vertical = 8.dp,
                               horizontal = 16.dp
                           )
    ) {
        Box(
            Modifier.size( thumbnailSizeDp )
                    .clip( RoundedCornerShape(12.dp) )
                    .shimmerEffect()
        )

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth( .7f )
            ) {
                BasicText(
                    text = "",
                    style = typography().xs.semiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight( 1f ).shimmerEffect()
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box( Modifier.weight( 1f ).fillMaxWidth() ) {
                    BasicText(
                        text = "",
                        style = typography().xs.semiBold.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.fillMaxWidth( .3f ).shimmerEffect()
                    )
                }

                BasicText(
                    text = "0:00",
                    style = typography().xxs.secondary.medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding( top = 4.dp )
                )

                Spacer(modifier = Modifier.padding( horizontal = 4.dp ))

                IconButton(
                    onClick = {},
                    icon = DownloadedStateMedia.NOT_CACHED_OR_DOWNLOADED.iconId,
                    color = colorPalette().textDisabled,
                    modifier = Modifier.size( 20.dp ),
                    enabled = false
                )
            }
        }
    }
}

private interface SongIndicator: Icon {
    override val sizeDp: Dp
        get() = 18.dp

    override fun onShortClick() { /* Does nothing */ }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun ToolBarButton() {
        val modifier = this.modifier
            .size(sizeDp)
            .combinedClickable(
                onClick = ::onShortClick,
                onLongClick = {
                    if(this is Clickable)
                        this.onLongClick()
                }
            )

        IconButton(
            icon = iconId,
            color = color,
            enabled = isEnabled,
            onClick = {},
            modifier = modifier
        )

        Spacer( Modifier.padding(horizontal = 3.dp) )
    }
}

@Composable
fun SongText(
    text: String,
    style: TextStyle,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    modifier: Modifier = Modifier
) = BasicText(
    text = text,
    style = style,
    maxLines = 1,
    overflow = overflow,
    modifier = modifier
)

/**
 *  Displays information of a song.
 *
 *  @param song record from database
 *  @param navController optional field to detect whether the
 *  current location is playlist to hide playlist indicator.
 *  @param isRecommended whether this song is selected by algorithm
 *  @param modifier applied to the outermost layer but not its content
 *  @param showThumbnail whether to fetch/show thumbnail of this song.
 *  [thumbnailOverlay] will still being shown regardless the state of this value
 *  @param trailingContent content being placed to the rightmost of the card
 */
@UnstableApi
@ExperimentalFoundationApi
@Composable
fun SongItem(
    song: Song,
    navController: NavController? = null,
    isRecommended: Boolean = false,
    modifier: Modifier = Modifier,
    showThumbnail: Boolean = true,
    trailingContent: @Composable (RowScope.() -> Unit)? = null,
    thumbnailOverlay: @Composable BoxScope.() -> Unit = {}
) {
    // Essentials
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current
    val disableScrollingText by rememberPreference( disableScrollingTextKey, false )

    val colorPalette = colorPalette()
    val isPlaying = binder?.player?.isNowPlaying( song.id ) ?: false

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy( 12.dp ),
        modifier = modifier.clip( RoundedCornerShape(10.dp) )
                           .fillMaxWidth()
                           .conditional( isPlaying ) {
                               background( colorPalette.favoritesOverlay )
                           }
                           .padding(
                               vertical = Dimensions.itemsVerticalPadding,
                               horizontal = 16.dp
                           )
    ) {
        // Song's thumbnail
        Box(
            Modifier.size( Dimensions.thumbnails.song )
        ) {
            /*
                Thumbnail size
                It always fetches a fixed size to prevent cache
                from storing multiple images.
                Let the OS do the resizing for more efficient outcome.
                TODO: Make a simple system to detect network speed and/or
                TODO: data saver that automatically lower the quality to
                TODO: reduce loading time and to preserve data usage.
             */
            val thumbnailSizePx = Dimensions.thumbnails.song.px

            // Actual thumbnail (from cache or fetch from url)
            if( showThumbnail )
                AsyncImage(
                    model = song.thumbnailUrl
                               ?.thumbnail( thumbnailSizePx ),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(thumbnailShape())
                )

            /*
                To avoid the app from rendering components
                on top of each other without showing the
                previous, is condition is here to force it
                to display either overlay.
             */
            if( isPlaying )
                NowPlayingSongIndicator(
                    mediaId = song.id,
                    player = binder?.player
                )
            else
                thumbnailOverlay()

            val likedAt by remember {
                Database.likedAt( song.id )
            }.collectAsState( initial = null, context = Dispatchers.IO )

            if( likedAt != null )
                HeaderIconButton(
                    onClick = {},
                    icon = getLikedIcon(),
                    color = colorPalette().favoritesIcon,
                    iconSize = 12.dp,
                    modifier = Modifier.align( Alignment.BottomStart )
                                       .absoluteOffset( x = (-8).dp )
                )
        }

        // Song's information
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy( 4.dp ),
            modifier = Modifier.weight( 1f )
        ) {
            Row( verticalAlignment = Alignment.CenterVertically ) {
                // Show icon if song is recommended by the algorithm
                if( isRecommended )
                    object: SongIndicator {
                        override val iconId: Int = R.drawable.smart_shuffle
                        override val color: Color
                            @Composable
                            get() = colorPalette().accent
                    }.ToolBarButton()

                val showInPlaylistIndicator by rememberPreference( playlistindicatorKey,false )
                val isInPlaylistScreen = navController != null && NavRoutes.localPlaylist.isHere( navController )
                // Show icon if song belongs to a playlist,
                // except when in playlist.
                if( showInPlaylistIndicator && !isInPlaylistScreen ) {

                    val isExistedInAPlaylist by remember( showInPlaylistIndicator ) {
                        if( !showInPlaylistIndicator )
                            flowOf( false )
                        else
                            Database.isSongMappedToPlaylist( song.id )
                    }.collectAsState( initial = false, context = Dispatchers.IO )

                    if( isExistedInAPlaylist )
                        object: SongIndicator, Descriptive {
                            override val iconId: Int = R.drawable.add_in_playlist
                            override val messageId: Int = R.string.playlistindicatorinfo2
                            override val sizeDp: Dp = 10.dp
                            override val modifier: Modifier =
                                Modifier.background(colorPalette().accent, CircleShape)
                                        .padding(all = 3.dp)

                            override fun onShortClick() = super.onShortClick()
                        }.ToolBarButton()
                }

                if( song.title.startsWith(EXPLICIT_PREFIX) )
                    object: SongIndicator {
                        override val iconId: Int = R.drawable.explicit
                    }.ToolBarButton()

                // Song's name
                SongText(
                    text = cleanPrefix( song.title ),
                    style = typography().xs.semiBold,
                    modifier = Modifier.weight( 1f )
                                       .conditional(!disableScrollingText) {
                                           basicMarquee(iterations = Int.MAX_VALUE)
                                       }
                )

            }

            Row( verticalAlignment = Alignment.CenterVertically ) {
                // Song's author
                SongText(
                    text = song.artistsText.toString(),
                    style = typography().xs.semiBold.secondary,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.weight( 1f )
                                       .conditional(!disableScrollingText) {
                                           basicMarquee(iterations = Int.MAX_VALUE)
                                       }
                )

                /*
                    Song's duration
                    If it's "null", show 0:00 instead of leaving it empty
                 */
                SongText(
                    text = song.durationText ?: "0:00",
                    style = typography().xxs.secondary.medium,
                    modifier = Modifier.padding( top = 4.dp )
                )

                Spacer( Modifier.padding(horizontal = 4.dp) )

                // Show download icon when song is NOT local
                if( !song.isLocal ) {
                    // Temporal cache, acquire by listening to the song
                    var cacheState by remember {
                        mutableStateOf(DownloadedStateMedia.NOT_CACHED_OR_DOWNLOADED)
                    }
                    cacheState = downloadedStateMedia( song.id )
                    // "Permanent" cache, requires explicit interaction to start downloading
                    var downloadState by remember {
                        mutableIntStateOf( Download.STATE_STOPPED )
                    }
                    downloadState = getDownloadState( song.id )

                    val icon =when( downloadState ) {
                        Download.STATE_DOWNLOADING,
                        Download.STATE_REMOVING     -> R.drawable.download_progress

                        else                        -> cacheState.iconId
                    }
                    val color = when( cacheState ) {
                        DownloadedStateMedia.NOT_CACHED_OR_DOWNLOADED -> colorPalette().textDisabled

                        else                                          -> colorPalette().text
                    }
                    val isDownloaded = when( cacheState ) {
                        DownloadedStateMedia.CACHED_AND_DOWNLOADED,
                        DownloadedStateMedia.DOWNLOADED             -> true

                        else                                        -> false
                    }

                    IconButton(
                        icon = icon,
                        color = color,
                        modifier = Modifier.size( 20.dp ),
                        onClick = {
                            val mediaItem = song.asMediaItem

                            // TODO: Confirmation dialog upon delete
                            binder?.cache?.removeResource( mediaItem.mediaId )
                            Database.asyncTransaction {
                                deleteFormat( mediaItem.mediaId )
                            }

                            manageDownload(
                                context = context,
                                mediaItem = mediaItem,
                                downloadState = isDownloaded
                            )
                        }
                    )
                }
            }
        }

        trailingContent?.invoke( this )
    }
}