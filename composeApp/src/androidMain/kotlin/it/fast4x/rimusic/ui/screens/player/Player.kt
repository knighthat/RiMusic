package it.fast4x.rimusic.ui.screens.player

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.BackgroundProgress
import it.fast4x.rimusic.enums.ColorPaletteMode
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.PlayerBackgroundColors
import it.fast4x.rimusic.enums.PlayerThumbnailSize
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.models.Info
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.ui.toUiMedia
import it.fast4x.rimusic.query
import it.fast4x.rimusic.ui.components.CustomModalBottomSheet
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.themed.BlurParamsDialog
import it.fast4x.rimusic.ui.components.themed.ThumbnailOffsetDialog
import it.fast4x.rimusic.ui.components.themed.CircularSlider
import it.fast4x.rimusic.ui.components.themed.ConfirmationDialog
import it.fast4x.rimusic.ui.components.themed.DefaultDialog
import it.fast4x.rimusic.ui.components.themed.DownloadStateIconButton
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.components.themed.MiniPlayerMenu
import it.fast4x.rimusic.ui.components.themed.PlayerMenu
import it.fast4x.rimusic.ui.components.themed.SecondaryTextButton
import it.fast4x.rimusic.ui.components.themed.animateBrushRotation
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.collapsedPlayerProgressBar
import it.fast4x.rimusic.ui.styling.dynamicColorPaletteOf
import it.fast4x.rimusic.ui.styling.favoritesOverlay
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.BlurTransformation
import it.fast4x.rimusic.utils.DisposableListener
import it.fast4x.rimusic.utils.backgroundProgressKey
import it.fast4x.rimusic.utils.blurDarkenFactorKey
import it.fast4x.rimusic.utils.blurStrengthKey
import it.fast4x.rimusic.utils.colorPaletteModeKey
import it.fast4x.rimusic.utils.currentWindow
import it.fast4x.rimusic.utils.disablePlayerHorizontalSwipeKey
import it.fast4x.rimusic.utils.durationTextToMillis
import it.fast4x.rimusic.utils.effectRotationKey
import it.fast4x.rimusic.utils.forceSeekToNext
import it.fast4x.rimusic.utils.formatAsDuration
import it.fast4x.rimusic.utils.formatAsTime
import it.fast4x.rimusic.utils.getBitmapFromUrl
import it.fast4x.rimusic.utils.getDownloadState
import it.fast4x.rimusic.utils.isLandscape
import it.fast4x.rimusic.utils.manageDownload
import it.fast4x.rimusic.utils.mediaItems
import it.fast4x.rimusic.utils.playerBackgroundColorsKey
import it.fast4x.rimusic.utils.playerThumbnailSizeKey
import it.fast4x.rimusic.utils.positionAndDurationState
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.shouldBePlaying
import it.fast4x.rimusic.utils.showButtonPlayerAddToPlaylistKey
import it.fast4x.rimusic.utils.showButtonPlayerArrowKey
import it.fast4x.rimusic.utils.showButtonPlayerDownloadKey
import it.fast4x.rimusic.utils.showButtonPlayerLoopKey
import it.fast4x.rimusic.utils.showButtonPlayerLyricsKey
import it.fast4x.rimusic.utils.showButtonPlayerMenuKey
import it.fast4x.rimusic.utils.showButtonPlayerShuffleKey
import it.fast4x.rimusic.utils.showButtonPlayerSleepTimerKey
import it.fast4x.rimusic.utils.showButtonPlayerSystemEqualizerKey
import it.fast4x.rimusic.utils.showNextSongsInPlayerKey
import it.fast4x.rimusic.utils.showTopActionsBarKey
import it.fast4x.rimusic.utils.showTotalTimeQueueKey
import it.fast4x.rimusic.utils.shuffleQueue
import it.fast4x.rimusic.utils.thumbnail
import it.fast4x.rimusic.utils.thumbnailTapEnabledKey
import it.fast4x.rimusic.utils.transparentBackgroundPlayerActionBarKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.times
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import androidx.media3.common.PlaybackException
import androidx.media3.common.Timeline
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import it.fast4x.rimusic.enums.CarouselSize
import it.fast4x.rimusic.enums.PlayerType
import it.fast4x.rimusic.enums.QueueType
import it.fast4x.rimusic.enums.SongsNumber
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.enums.ThumbnailType
import it.fast4x.rimusic.transaction
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.utils.SearchYoutubeEntity
import it.fast4x.rimusic.utils.actionspacedevenlyKey
import it.fast4x.rimusic.utils.addNext
import it.fast4x.rimusic.utils.expandedplayerKey
import it.fast4x.rimusic.utils.expandedplayertoggleKey
import it.fast4x.rimusic.utils.showthumbnailKey
import it.fast4x.rimusic.utils.showlyricsthumbnailKey
import it.fast4x.rimusic.utils.blackgradientKey
import it.fast4x.rimusic.utils.visualizerEnabledKey
import it.fast4x.rimusic.utils.bottomgradientKey
import it.fast4x.rimusic.utils.carouselKey
import it.fast4x.rimusic.utils.carouselSizeKey
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.enums.QueueLoopType
import it.fast4x.rimusic.utils.VerticalfadingEdge2
import it.fast4x.rimusic.utils.actionExpandedKey
import it.fast4x.rimusic.utils.textoutlineKey
import kotlin.Float.Companion.POSITIVE_INFINITY
import it.fast4x.rimusic.utils.clickOnLyricsTextKey
import it.fast4x.rimusic.utils.conditional
import it.fast4x.rimusic.utils.controlsExpandedKey
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.discoverKey
import it.fast4x.rimusic.utils.doubleShadowDrop
import it.fast4x.rimusic.utils.expandedlyricsKey
import it.fast4x.rimusic.utils.extraspaceKey
import it.fast4x.rimusic.utils.fadingedgeKey
import it.fast4x.rimusic.utils.forcePlayAtIndex
import it.fast4x.rimusic.utils.forceSeekToPrevious
import it.fast4x.rimusic.utils.horizontalFadingEdge
import it.fast4x.rimusic.utils.miniQueueExpandedKey
import it.fast4x.rimusic.utils.noblurKey
import it.fast4x.rimusic.utils.playerTypeKey
import it.fast4x.rimusic.utils.playlistindicatorKey
import it.fast4x.rimusic.utils.queueDurationExpandedKey
import it.fast4x.rimusic.utils.queueLoopTypeKey
import it.fast4x.rimusic.utils.queueTypeKey
import it.fast4x.rimusic.utils.resize
import it.fast4x.rimusic.utils.setQueueLoopState
import it.fast4x.rimusic.utils.showButtonPlayerDiscoverKey
import it.fast4x.rimusic.utils.showButtonPlayerVideoKey
import it.fast4x.rimusic.utils.showalbumcoverKey
import it.fast4x.rimusic.utils.showsongsKey
import it.fast4x.rimusic.utils.showvisthumbnailKey
import it.fast4x.rimusic.utils.statsfornerdsKey
import it.fast4x.rimusic.utils.swipeUpQueueKey
import it.fast4x.rimusic.utils.tapqueueKey
import it.fast4x.rimusic.utils.thumbnailOffsetKey
import it.fast4x.rimusic.utils.thumbnailRoundnessKey
import it.fast4x.rimusic.utils.thumbnailSpacingKey
import it.fast4x.rimusic.utils.thumbnailTypeKey
import it.fast4x.rimusic.utils.timelineExpandedKey
import it.fast4x.rimusic.utils.titleExpandedKey
import it.fast4x.rimusic.utils.verticalFadingEdge
import it.fast4x.rimusic.utils.getIconQueueLoopState
import it.fast4x.rimusic.utils.isDownloadedSong
import it.fast4x.rimusic.utils.playNext
import it.fast4x.rimusic.utils.playPrevious
import it.fast4x.rimusic.utils.statsExpandedKey
import it.fast4x.rimusic.utils.thumbnailFadeKey
import me.knighthat.colorPalette
import me.knighthat.thumbnailShape
import me.knighthat.typography


@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation", "RememberReturnType")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@UnstableApi
@Composable
fun Player(
    navController: NavController,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    val menuState = LocalMenuState.current

    val effectRotationEnabled by rememberPreference(effectRotationKey, true)

    val playerThumbnailSize by rememberPreference(
        playerThumbnailSizeKey,
        PlayerThumbnailSize.Biggest
    )

    val disablePlayerHorizontalSwipe by rememberPreference(disablePlayerHorizontalSwipeKey, false)
    val showlyricsthumbnail by rememberPreference(showlyricsthumbnailKey, false)
    val binder = LocalPlayerServiceBinder.current

    binder?.player ?: return
    if (binder.player.currentTimeline.windowCount == 0) return

    var nullableMediaItem by remember {
        mutableStateOf(binder.player.currentMediaItem, neverEqualPolicy())
    }

    var shouldBePlaying by remember {
        mutableStateOf(binder.player.shouldBePlaying)
    }

    //val shouldBePlayingTransition = updateTransition(shouldBePlaying, label = "shouldBePlaying")

    var isRotated by rememberSaveable { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isRotated) 360F else 0f,
        animationSpec = tween(durationMillis = 200), label = ""
    )

    val visualizerEnabled by rememberPreference(visualizerEnabledKey, false)

    val defaultStrength = 5f
    val defaultDarkenFactor = 0.2f
    val defaultOffset = 0f
    val defaultSpacing = 0f
    val defaultFade = 5f
    var blurStrength by rememberPreference(blurStrengthKey, defaultStrength)
    var thumbnailOffset  by rememberPreference(thumbnailOffsetKey, defaultOffset)
    var thumbnailSpacing  by rememberPreference(thumbnailSpacingKey, defaultSpacing)
    var thumbnailFade  by rememberPreference(thumbnailFadeKey, defaultFade)
    var blurDarkenFactor by rememberPreference(blurDarkenFactorKey, defaultDarkenFactor)
    var showBlurPlayerDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var showThumbnailOffsetDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var isShowingLyrics by rememberSaveable {
        mutableStateOf(false)
    }
    val showvisthumbnail by rememberPreference(showvisthumbnailKey, false)
    var isShowingVisualizer by remember {
        mutableStateOf(false)
    }
    val expandedlyrics by rememberPreference(expandedlyricsKey, true)

    if (showBlurPlayerDialog) {

         BlurParamsDialog(
             onDismiss = { showBlurPlayerDialog = false},
             scaleValue = { blurStrength = it },
             darkenFactorValue = { blurDarkenFactor = it}
        )

    }

    if (showThumbnailOffsetDialog) {

        ThumbnailOffsetDialog(
            onDismiss = { showThumbnailOffsetDialog = false},
            scaleValue = { thumbnailOffset = it },
            spacingValue = { thumbnailSpacing = it },
            fadeValue = { thumbnailFade = it }
        )
    }



    val context = LocalContext.current

    var mediaItems by remember {
        mutableStateOf(binder.player.currentTimeline.mediaItems)
    }
    var mediaItemIndex by remember {
        mutableIntStateOf(if (binder.player.mediaItemCount == 0) -1 else binder.player.currentMediaItemIndex)
    }

    var playerError by remember {
        mutableStateOf<PlaybackException?>(binder.player.playerError)
    }

    val queueDurationExpanded by rememberPreference(queueDurationExpandedKey, true)
    val miniQueueExpanded by rememberPreference(miniQueueExpandedKey, true)
    val statsExpanded by rememberPreference(statsExpandedKey, true)
    val actionExpanded by rememberPreference(actionExpandedKey, true)

    binder.player.DisposableListener {
        object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                nullableMediaItem = mediaItem
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                shouldBePlaying = if (playerError == null) binder.player.shouldBePlaying else false
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                playerError = binder.player.playerError
                shouldBePlaying = if (playerError == null) binder.player.shouldBePlaying else false
            }
            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                mediaItems = timeline.mediaItems
                mediaItemIndex = binder.player.currentMediaItemIndex
            }
            override fun onPlayerError(playbackException: PlaybackException) {
                playerError = playbackException
                //binder.stopRadio()
            }
        }
    }

    val mediaItem = nullableMediaItem ?: return

    val pagerState = rememberPagerState(pageCount = { mediaItems.size })

    //Temporaly commented for debug
    //playerError?.let { PlayerError(error = it) }

    var isShowingSleepTimerDialog by remember {
        mutableStateOf(false)
    }

    var delayedSleepTimer by remember {
        mutableStateOf(false)
    }

    val sleepTimerMillisLeft by (binder.sleepTimerMillisLeft
        ?: flowOf(null))
        .collectAsState(initial = null)

    val positionAndDuration by binder.player.positionAndDurationState()
    var timeRemaining by remember { mutableIntStateOf(0) }
    timeRemaining = positionAndDuration.second.toInt() - positionAndDuration.first.toInt()

    if (sleepTimerMillisLeft != null)
        if (sleepTimerMillisLeft!! < timeRemaining.toLong() && !delayedSleepTimer)  {
            binder.cancelSleepTimer()
            binder.startSleepTimer(timeRemaining.toLong())
            delayedSleepTimer = true
            SmartMessage(stringResource(R.string.info_sleep_timer_delayed_at_end_of_song), context = context)
        }

    val windowInsets = WindowInsets.systemBars

    var albumInfo by remember {
        mutableStateOf(mediaItem.mediaMetadata.extras?.getString("albumId")?.let { albumId ->
            Info(albumId, null)
        })
    }

    var artistsInfo by remember {
        mutableStateOf(
            mediaItem.mediaMetadata.extras?.getStringArrayList("artistNames")?.let { artistNames ->
                mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds")?.let { artistIds ->
                    artistNames.zip(artistIds).map { (authorName, authorId) ->
                        Info(authorId, authorName)
                    }
                }
            }
        )
    }
    val actionspacedevenly by rememberPreference(actionspacedevenlyKey, false)
    var expandedplayer by rememberPreference(expandedplayerKey, false)

    if (expandedlyrics && !isLandscape) {
        if ((isShowingLyrics && !showlyricsthumbnail) || (isShowingVisualizer && !showvisthumbnail)) expandedplayer = true
        else expandedplayer = false
    }
    if (showlyricsthumbnail) expandedplayer = false

    LaunchedEffect(mediaItem.mediaId) {
        withContext(Dispatchers.IO) {
            albumInfo = Database.songAlbumInfo(mediaItem.mediaId)
            artistsInfo = Database.songArtistInfo(mediaItem.mediaId)
        }
    }


    val ExistIdsExtras =
        mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds")?.size.toString()
    val ExistAlbumIdExtras = mediaItem.mediaMetadata.extras?.getString("albumId")

    var albumId = albumInfo?.id
    if (albumId == null) albumId = ExistAlbumIdExtras

    var artistIds = arrayListOf<String>()
    var artistNames = arrayListOf<String>()


    artistsInfo?.forEach { (id) -> artistIds = arrayListOf(id) }
    if (ExistIdsExtras.equals(0)
            .not()
    ) mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds")?.toCollection(artistIds)

    artistsInfo?.forEach { (name) -> artistNames = arrayListOf(name) }
    if (ExistIdsExtras.equals(0)
            .not()
    ) mediaItem.mediaMetadata.extras?.getStringArrayList("artistNames")?.toCollection(artistNames)



    if (artistsInfo?.isEmpty() == true && ExistIdsExtras.equals(0).not()) {
        artistsInfo = artistNames.let { artistNames ->
            artistIds.let { artistIds ->
                artistNames.zip(artistIds).map {
                    Info(it.second, it.first)
                }
            }
        }
    }

    var likedAt by rememberSaveable {
        mutableStateOf<Long?>(null)
    }
    LaunchedEffect(mediaItem.mediaId) {
        Database.likedAt(mediaItem.mediaId).distinctUntilChanged().collect { likedAt = it }
    }


    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }
    downloadState = getDownloadState(mediaItem.mediaId)

    var isDownloaded by rememberSaveable { mutableStateOf(false) }
    isDownloaded = isDownloadedSong(mediaItem.mediaId)
    var showthumbnail by rememberPreference(showthumbnailKey, false)

    val showButtonPlayerAddToPlaylist by rememberPreference(showButtonPlayerAddToPlaylistKey, true)
    val showButtonPlayerArrow by rememberPreference(showButtonPlayerArrowKey, false)
    val showButtonPlayerDownload by rememberPreference(showButtonPlayerDownloadKey, true)
    val showButtonPlayerLoop by rememberPreference(showButtonPlayerLoopKey, true)
    val showButtonPlayerLyrics by rememberPreference(showButtonPlayerLyricsKey, true)
    val expandedplayertoggle by rememberPreference(expandedplayertoggleKey, true)
    val showButtonPlayerShuffle by rememberPreference(showButtonPlayerShuffleKey, true)
    val showButtonPlayerSleepTimer by rememberPreference(showButtonPlayerSleepTimerKey, false)
    val showButtonPlayerMenu by rememberPreference(showButtonPlayerMenuKey, false)
    val showButtonPlayerSystemEqualizer by rememberPreference(
        showButtonPlayerSystemEqualizerKey,
        false
    )
    val showButtonPlayerVideo by rememberPreference(showButtonPlayerVideoKey, false)

    val showTotalTimeQueue by rememberPreference(showTotalTimeQueueKey, true)
    val backgroundProgress by rememberPreference(
        backgroundProgressKey,
        BackgroundProgress.MiniPlayer
    )

    var queueLoopType by rememberPreference(queueLoopTypeKey, defaultValue = QueueLoopType.Default)
    var showCircularSlider by remember {
        mutableStateOf(false)
    }
    val showsongs by rememberPreference(showsongsKey, SongsNumber.`2`)
    val showalbumcover by rememberPreference(showalbumcoverKey, true)
    val tapqueue by rememberPreference(tapqueueKey, true)
    val swipeUpQueue by rememberPreference(swipeUpQueueKey, true)
    val playerType by rememberPreference(playerTypeKey, PlayerType.Essential)
    val queueType by rememberPreference(queueTypeKey, QueueType.Essential)
    val noblur by rememberPreference(noblurKey, true)
    val fadingedge by rememberPreference(fadingedgeKey, false)
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    if (isShowingSleepTimerDialog) {
        if (sleepTimerMillisLeft != null) {
            ConfirmationDialog(
                text = stringResource(R.string.stop_sleep_timer),
                cancelText = stringResource(R.string.no),
                confirmText = stringResource(R.string.stop),
                onDismiss = { isShowingSleepTimerDialog = false },
                onConfirm = {
                    binder.cancelSleepTimer()
                    delayedSleepTimer = false
                    //onDismiss()
                }
            )
        } else {
            DefaultDialog(
                onDismiss = { isShowingSleepTimerDialog = false }
            ) {
                var amount by remember {
                    mutableStateOf(1)
                }

                BasicText(
                    text = stringResource(R.string.set_sleep_timer),
                    style = typography().s.semiBold,
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 24.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 16.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                ) {
                    if (!showCircularSlider) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .alpha(if (amount <= 1) 0.5f else 1f)
                                .clip(CircleShape)
                                .clickable(enabled = amount > 1) { amount-- }
                                .size(48.dp)
                                .background(colorPalette().background0)
                        ) {
                            BasicText(
                                text = "-",
                                style = typography().xs.semiBold
                            )
                        }

                        Box(contentAlignment = Alignment.Center) {
                            BasicText(
                                text = stringResource(
                                    R.string.left,
                                    formatAsDuration(amount * 5 * 60 * 1000L)
                                ),
                                style = typography().s.semiBold,
                                modifier = Modifier
                                    .clickable {
                                        showCircularSlider = !showCircularSlider
                                    }
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .alpha(if (amount >= 60) 0.5f else 1f)
                                .clip(CircleShape)
                                .clickable(enabled = amount < 60) { amount++ }
                                .size(48.dp)
                                .background(colorPalette().background0)
                        ) {
                            BasicText(
                                text = "+",
                                style = typography().xs.semiBold
                            )
                        }

                    } else {
                        CircularSlider(
                            stroke = 40f,
                            thumbColor = colorPalette().accent,
                            text = formatAsDuration(amount * 5 * 60 * 1000L),
                            modifier = Modifier
                                .size(300.dp),
                            onChange = {
                                amount = (it * 120).toInt()
                            }
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .fillMaxWidth()
                ) {
                    SecondaryTextButton(
                        text = stringResource(R.string.set_to) + " "
                                + formatAsDuration(timeRemaining.toLong())
                                + " " + stringResource(R.string.end_of_song),
                        onClick = {
                            binder.startSleepTimer(timeRemaining.toLong())
                            isShowingSleepTimerDialog = false
                        }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {

                    IconButton(
                        onClick = { showCircularSlider = !showCircularSlider },
                        icon = R.drawable.time,
                        color = colorPalette().text
                    )
                    IconButton(
                        onClick = { isShowingSleepTimerDialog = false },
                        icon = R.drawable.close,
                        color = colorPalette().text
                    )
                    IconButton(
                        enabled = amount > 0,
                        onClick = {
                            binder.startSleepTimer(amount * 5 * 60 * 1000L)
                            isShowingSleepTimerDialog = false
                        },
                        icon = R.drawable.checkmark,
                        color = colorPalette().accent
                    )
                }
            }
        }
    }

    val color = colorPalette()
    var dynamicColorPalette by remember { mutableStateOf( color ) }
    val colorPaletteMode by rememberPreference(colorPaletteModeKey, ColorPaletteMode.Dark)
    val playerBackgroundColors by rememberPreference(
        playerBackgroundColorsKey,
        PlayerBackgroundColors.BlurredCoverColor
    )
    val isGradientBackgroundEnabled =
        playerBackgroundColors == PlayerBackgroundColors.ThemeColorGradient ||
                playerBackgroundColors == PlayerBackgroundColors.CoverColorGradient ||
                playerBackgroundColors == PlayerBackgroundColors.FluidThemeColorGradient ||
                playerBackgroundColors == PlayerBackgroundColors.FluidCoverColorGradient

    if (playerBackgroundColors == PlayerBackgroundColors.CoverColorGradient ||
        playerBackgroundColors == PlayerBackgroundColors.CoverColor ||
        playerBackgroundColors == PlayerBackgroundColors.FluidCoverColorGradient
    ) {
        //val context = LocalContext.current
        val isSystemDarkMode = isSystemInDarkTheme()
        LaunchedEffect(mediaItem.mediaId) {
            try {
                dynamicColorPalette = dynamicColorPaletteOf(
                    getBitmapFromUrl(
                        context,
                        binder.player.currentWindow?.mediaItem?.mediaMetadata?.artworkUri.toString()
                    ),
                    isSystemDarkMode,
                    colorPaletteMode == ColorPaletteMode.PitchBlack
                ) ?: color
            } catch (e: Exception) {
                dynamicColorPalette = color
                e.printStackTrace()
            }

        }
    }

    /*  */
    var sizeShader by remember { mutableStateOf(Size.Zero) }

    val shaderA = LinearGradientShader(
        Offset(sizeShader.width / 2f, 0f),
        Offset(sizeShader.width / 2f, sizeShader.height),
        listOf(
            dynamicColorPalette.background2,
            colorPalette().background2,
        ),
        listOf(0f, 1f)
    )

    val shaderB = LinearGradientShader(
        Offset(sizeShader.width / 2f, 0f),
        Offset(sizeShader.width / 2f, sizeShader.height),
        listOf(
            colorPalette().background1,
            dynamicColorPalette.accent,
        ),
        listOf(0f, 1f)
    )

    val shaderMask = LinearGradientShader(
        Offset(sizeShader.width / 2f, 0f),
        Offset(sizeShader.width / 2f, sizeShader.height),
        listOf(
            //Color.White,
            colorPalette().background2,
            Color.Transparent,
        ),
        listOf(0f, 1f)
    )

    val brushA by animateBrushRotation(shaderA, sizeShader, 20_000, true)
    val brushB by animateBrushRotation(shaderB, sizeShader, 12_000, false)
    val brushMask by animateBrushRotation(shaderMask, sizeShader, 15_000, true)
    /*  */

    val (thumbnailSizeDp, thumbnailSizePx) = Dimensions.thumbnails.player.song.let {
        it to (it - 64.dp).px
    }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(
                mediaItem.mediaMetadata.artworkUri.thumbnail(
                    thumbnailSizePx
                )
            )
            .size(coil.size.Size.ORIGINAL)
            .transformations(
                listOf(
                  if (showthumbnail) {
                      BlurTransformation(
                          scale = 0.5f,
                          radius = blurStrength.toInt(),
                          //darkenFactor = blurDarkenFactor
                      )

                 } else
                    BlurTransformation(
                        scale = 0.5f,
                        //radius = blurStrength2.toInt(),
                        radius = if ((isShowingLyrics && !isShowingVisualizer) || !noblur) blurStrength.toInt() else 0,
                        //darkenFactor = blurDarkenFactor
                    )
                )
            )
            .build()
    )



    var totalPlayTimes = 0L
    mediaItems.forEach {
        totalPlayTimes += it.mediaMetadata.extras?.getString("durationText")?.let { it1 ->
            durationTextToMillis(it1)
        }?.toLong() ?: 0
    }
//    println("mediaItem totalPlayTimes $totalPlayTimes")


    var isShowingStatsForNerds by rememberSaveable {
        mutableStateOf(false)
    }

    val thumbnailTapEnabled by rememberPreference(thumbnailTapEnabledKey, false)
    val showNextSongsInPlayer by rememberPreference(showNextSongsInPlayerKey, false)

    var showQueue by rememberSaveable { mutableStateOf(false) }
    var showFullLyrics by rememberSaveable { mutableStateOf(false) }
    var showSearchEntity by rememberSaveable { mutableStateOf(false) }

    val transparentBackgroundActionBarPlayer by rememberPreference(transparentBackgroundPlayerActionBarKey, false)
    val showTopActionsBar by rememberPreference(showTopActionsBarKey, true)

    var containerModifier = Modifier
        //.padding(bottom = bottomDp)
        .padding(bottom = 0.dp)
    var deltaX by remember { mutableStateOf(0f) }
    val blackgradient by rememberPreference(blackgradientKey, false)
    val bottomgradient by rememberPreference(bottomgradientKey, false)
    val disableScrollingText by rememberPreference(disableScrollingTextKey, false)

    var discoverIsEnabled by rememberPreference(discoverKey, false)
    val hapticFeedback = LocalHapticFeedback.current
    val titleExpanded by rememberPreference(titleExpandedKey, true)
    val timelineExpanded by rememberPreference(timelineExpandedKey, true)
    val controlsExpanded by rememberPreference(controlsExpandedKey, true)



    if (!isGradientBackgroundEnabled) {
        if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor && (playerType == PlayerType.Essential || showthumbnail)) {
            containerModifier = containerModifier
                .background(dynamicColorPalette.background1)
                .paint(
                    painter = painter,
                    contentScale = ContentScale.Crop,
                    sizeToIntrinsics = false
                )
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        1.0f to if (bottomgradient) if (colorPaletteMode == ColorPaletteMode.Light) Color.White.copy(
                            if (isLandscape) 0.8f else 0.75f
                        ) else Color.Black.copy(if (isLandscape) 0.8f else 0.75f) else Color.Transparent,
                        startY = if (isLandscape) 600f else if (expandedplayer) 1300f else 950f,
                        endY = POSITIVE_INFINITY
                    )
                )
                .background(
                    if (bottomgradient) if (isLandscape) if (colorPaletteMode == ColorPaletteMode.Light) Color.White.copy(
                        0.25f
                    ) else Color.Black.copy(0.25f) else Color.Transparent else Color.Transparent
                )
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (thumbnailTapEnabled) {
                            if (isShowingVisualizer) isShowingVisualizer = false
                            isShowingLyrics = !isShowingLyrics
                        }
                    },
                    onDoubleClick = {
                        if (!showlyricsthumbnail && !showvisthumbnail)
                            showthumbnail = !showthumbnail
                    },
                    onLongClick = {
                        if (showthumbnail || (isShowingLyrics && !isShowingVisualizer) || !noblur)
                            showBlurPlayerDialog = true
                    }
                )
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            deltaX = dragAmount
                        },
                        onDragStart = {
                            //Log.d("mediaItemGesture","ondragStart offset ${it}")
                        },
                        onDragEnd = {
                            if (!disablePlayerHorizontalSwipe && playerType == PlayerType.Essential) {
                                if (deltaX > 5) {
                                    binder.player.playPrevious()
                                    //Log.d("mediaItem","Swipe to LEFT")
                                } else if (deltaX < -5) {
                                    binder.player.playNext()
                                    //Log.d("mediaItem","Swipe to RIGHT")
                                }

                            }

                        }

                    )
                }

        } else {
            containerModifier = containerModifier
                .conditional (playerType == PlayerType.Essential) {
                    background(
                        //dynamicColorPalette.background1
                        color.background1
                    )
                }
        }
    } else {
        when (playerBackgroundColors) {
            PlayerBackgroundColors.FluidThemeColorGradient,
            PlayerBackgroundColors.FluidCoverColorGradient -> {
                containerModifier = containerModifier
                    .onSizeChanged {
                        sizeShader = Size(it.width.toFloat(), it.height.toFloat())
                    }
                    .drawBehind {
                        drawRect(brush = brushA)
                        drawRect(brush = brushMask, blendMode = BlendMode.DstOut)
                        drawRect(brush = brushB, blendMode = BlendMode.DstAtop)
                    }
            }

            else -> {
                containerModifier = containerModifier
                    .background(
                        Brush.verticalGradient(
                            0.5f to dynamicColorPalette.background2,
                            1.0f to if (blackgradient) Color.Black else colorPalette().background2,
                            //0.0f to colorPalette().background0,
                            //1.0f to colorPalette().background2,
                            startY = 0.0f,
                            endY = 1500.0f
                        )
                    )

            }
        }

    }

    val thumbnailContent: @Composable (
        //modifier: Modifier
    ) -> Unit = { //modifier ->
        var deltaX by remember { mutableStateOf(0f) }
        //var direction by remember { mutableIntStateOf(-1)}
        Thumbnail(
            thumbnailTapEnabledKey = thumbnailTapEnabled,
            isShowingLyrics = isShowingLyrics,
            onShowLyrics = { isShowingLyrics = it },
            isShowingStatsForNerds = isShowingStatsForNerds,
            onShowStatsForNerds = { isShowingStatsForNerds = it },
            isShowingVisualizer = isShowingVisualizer,
            onShowEqualizer = { isShowingVisualizer = it },
            showthumbnail = showthumbnail,
            onMaximize = {
                showFullLyrics = true
            },
            onDoubleTap = {
                val currentMediaItem = binder.player.currentMediaItem
                query {
                    if (Database.like(
                            mediaItem.mediaId,
                            if (likedAt == null) System.currentTimeMillis() else null
                        ) == 0
                    ) {
                        currentMediaItem
                            ?.takeIf { it.mediaId == mediaItem.mediaId }
                            ?.let {
                                Database.insert(currentMediaItem, Song::toggleLike)
                            }
                    }
                }
                if (effectRotationEnabled) isRotated = !isRotated
            },
            modifier = modifier
                //.nestedScroll( connection = scrollConnection )
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            deltaX = dragAmount
                        },
                        onDragStart = {
                            //Log.d("mediaItemGesture","ondragStart offset ${it}")
                        },
                        onDragEnd = {
                            if (!disablePlayerHorizontalSwipe && playerType == PlayerType.Essential) {
                                if (deltaX > 5) {
                                    binder.player.playPrevious()
                                    //Log.d("mediaItem","Swipe to LEFT")
                                } else if (deltaX < -5) {
                                    binder.player.playNext()
                                    //Log.d("mediaItem","Swipe to RIGHT")
                                }

                            }

                        }

                    )
                }
                .padding(
                    vertical = playerThumbnailSize.size.dp,
                    horizontal = playerThumbnailSize.size.dp
                )
                .thumbnailpause(
                    shouldBePlaying = shouldBePlaying
                )

        )
    }


    val controlsContent: @Composable (
        modifier: Modifier
    ) -> Unit = { modifierValue ->
        Controls(
            navController = navController,
            onCollapse = onDismiss,
            expandedplayer = expandedplayer,
            titleExpanded = titleExpanded,
            timelineExpanded = timelineExpanded,
            controlsExpanded = controlsExpanded,
            isShowingLyrics = isShowingLyrics,
            media = mediaItem.toUiMedia(positionAndDuration.second),
            mediaId = mediaItem.mediaId,
            title = mediaItem.mediaMetadata.title?.toString() ?: "",
            artist = mediaItem.mediaMetadata.artist?.toString(),
            artistIds = artistsInfo,
            albumId = albumId,
            shouldBePlaying = shouldBePlaying,
            position = positionAndDuration.first,
            duration = positionAndDuration.second,
            modifier = modifierValue,
            onBlurScaleChange = { blurStrength = it }
        )
    }
    val textoutline by rememberPreference(textoutlineKey, false)

    fun Modifier.conditional(condition : Boolean, modifier : Modifier.() -> Modifier) : Modifier {
        return if (condition) {
            then(modifier(Modifier))
        } else {
            this
        }
    }

    var songPlaylist by remember {
        mutableStateOf(0)
    }
    LaunchedEffect(Unit, mediaItem.mediaId) {
        withContext(Dispatchers.IO) {
            songPlaylist = Database.songUsedInPlaylists(mediaItem.mediaId)
        }
    }
    val playlistindicator by rememberPreference(playlistindicatorKey, false)
    val carousel by rememberPreference(carouselKey, true)
    val carouselSize by rememberPreference(carouselSizeKey, CarouselSize.Biggest)

    var showButtonPlayerDiscover by rememberPreference(showButtonPlayerDiscoverKey, false)
    val hazeState = remember { HazeState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val actionsBarContent: @Composable () -> Unit = {
            if ((!showButtonPlayerDownload &&
                !showButtonPlayerAddToPlaylist &&
                !showButtonPlayerLoop &&
                !showButtonPlayerShuffle &&
                !showButtonPlayerLyrics &&
                !showButtonPlayerSleepTimer &&
                !showButtonPlayerSystemEqualizer &&
                !showButtonPlayerArrow &&
                !showButtonPlayerMenu &&
                !expandedplayertoggle &&
                !showButtonPlayerDiscover &&
                !showButtonPlayerVideo) ||
                (expandedplayer && isShowingLyrics && !actionExpanded)
            ) {
                Row(
                ) {
                }
            } else
            Row(
                modifier = Modifier
                    .align(if (isLandscape) Alignment.BottomEnd else Alignment.BottomCenter)
                    .requiredHeight(if (showNextSongsInPlayer && (!expandedplayer || !isShowingLyrics || miniQueueExpanded)) 90.dp else 50.dp)
                    .fillMaxWidth(if (isLandscape) 0.8f else 1f)
                    .conditional(tapqueue) { clickable { showQueue = true } }
                    .background(
                        colorPalette().background2.copy(
                            alpha = if ((transparentBackgroundActionBarPlayer) || ((playerBackgroundColors == PlayerBackgroundColors.CoverColorGradient) || (playerBackgroundColors == PlayerBackgroundColors.ThemeColorGradient)) && blackgradient) 0.0f else 0.7f // 0.0 > 0.1
                        )
                    )
                    .pointerInput(Unit) {
                        if (swipeUpQueue)
                            detectVerticalDragGestures(
                                onVerticalDrag = { _, dragAmount ->
                                    if (dragAmount < 0) showQueue = true
                                }
                            )
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (showNextSongsInPlayer) {
                        if (!expandedplayer || !isShowingLyrics || miniQueueExpanded) {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    //.background(colorPalette().background2.copy(alpha = 0.3f))
                                    .background(
                                        colorPalette().background2.copy(
                                            alpha = if (transparentBackgroundActionBarPlayer) 0.0f else 0.3f
                                        )
                                    )
                                    .padding(horizontal = 12.dp)
                                    .fillMaxWidth()
                            ) {
                                val nextMediaItemIndex = binder.player.nextMediaItemIndex
                                val pagerStateQueue =
                                    rememberPagerState(pageCount = { mediaItems.size })
                                val scope = rememberCoroutineScope()
                                val fling = PagerDefaults.flingBehavior(
                                    state = pagerStateQueue,
                                    snapPositionalThreshold = 0.15f,
                                    pagerSnapDistance = PagerSnapDistance.atMost(showsongs.number)
                                )
                                LaunchedEffect(binder.player.currentMediaItemIndex) {
                                    pagerStateQueue.animateScrollToPage(binder.player.currentMediaItemIndex)
                                }
                                Row(
                                    modifier = Modifier
                                        .padding(vertical = 7.5.dp)
                                        .weight(0.07f)
                                ) {
                                    Icon(
                                        painter = painterResource(id = if (pagerStateQueue.settledPage >= binder.player.currentMediaItemIndex) R.drawable.chevron_forward else R.drawable.chevron_back),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(25.dp)
                                            .clip(CircleShape)
                                            .clickable(
                                                indication = ripple(bounded = false),
                                                interactionSource = remember { MutableInteractionSource() },
                                                onClick = {
                                                    scope.launch {
                                                        pagerStateQueue.animateScrollToPage(binder.player.currentMediaItemIndex)
                                                    }
                                                }
                                            ),
                                        tint = colorPalette().accent
                                    )
                                }

                                val threePagesPerViewport = object : PageSize {
                                    override fun Density.calculateMainAxisPageSize(
                                        availableSpace: Int,
                                        pageSpacing: Int
                                    ): Int {
                                        return if (showsongs == SongsNumber.`1`) (availableSpace) else ((availableSpace - 2 * pageSpacing) / (showsongs.number))
                                    }
                                }

                                HorizontalPager(
                                    state = pagerStateQueue,
                                    pageSize = threePagesPerViewport,
                                    pageSpacing = 10.dp,
                                    flingBehavior = fling,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    binder.player.forcePlayAtIndex(
                                                        mediaItems,
                                                        if (it + 1 <= mediaItems.size - 1) it + 1 else it
                                                    )
                                                },
                                                onLongClick = {
                                                    if (it < mediaItems.size) {
                                                        binder.player.addNext(
                                                            binder.player.getMediaItemAt(it + 1)
                                                        )
                                                        SmartMessage(
                                                            context.resources.getString(R.string.addednext),
                                                            type = PopupType.Info,
                                                            context = context
                                                        )
                                                        hapticFeedback.performHapticFeedback(
                                                            HapticFeedbackType.LongPress
                                                        )
                                                    }
                                                }
                                            )
                                        //.width(IntrinsicSize.Min)
                                    ) {
                                        if (showalbumcover) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.CenterVertically)
                                            ) {
                                                AsyncImage(
                                                    model = binder.player.getMediaItemAt(
                                                        if (it + 1 <= mediaItems.size - 1) it + 1 else it
                                                    ).mediaMetadata.artworkUri.thumbnail(
                                                        Dimensions.thumbnails.song.px / 2
                                                    ),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .padding(end = 5.dp)
                                                        .clip(RoundedCornerShape(5.dp))
                                                        .size(30.dp)
                                                )
                                            }
                                        }
                                        Column(
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .height(40.dp)
                                        ) {
                                            Box(

                                            ) {
                                                BasicText(
                                                    text = cleanPrefix(
                                                        binder.player.getMediaItemAt(
                                                            if (it + 1 <= mediaItems.size - 1) it + 1 else it
                                                        ).mediaMetadata.title?.toString()
                                                            ?: ""
                                                    ),
                                                    style = TextStyle(
                                                        color = colorPalette().text,
                                                        fontSize = typography().xxxs.semiBold.fontSize,
                                                    ),
                                                    maxLines = 1,
                                                    //overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.conditional(!disableScrollingText) { basicMarquee() }
                                                )
                                                BasicText(
                                                    text = cleanPrefix(
                                                        binder.player.getMediaItemAt(
                                                            if (it + 1 <= mediaItems.size - 1) it + 1 else it
                                                        ).mediaMetadata.title?.toString()
                                                            ?: ""
                                                    ),
                                                    style = TextStyle(
                                                        drawStyle = Stroke(
                                                            width = 0.25f,
                                                            join = StrokeJoin.Round
                                                        ),
                                                        color = if (!textoutline) Color.Transparent
                                                        else if (colorPaletteMode == ColorPaletteMode.Light || (colorPaletteMode == ColorPaletteMode.System && (!isSystemInDarkTheme()))) Color.White.copy(
                                                            0.65f
                                                        )
                                                        else Color.Black,
                                                        fontSize = typography().xxxs.semiBold.fontSize,
                                                    ),
                                                    maxLines = 1,
                                                    //overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.conditional(!disableScrollingText) { basicMarquee() }
                                                )
                                            }

                                            Box(

                                            ) {
                                                BasicText(
                                                    text = binder.player.getMediaItemAt(
                                                        if (it + 1 <= mediaItems.size - 1) it + 1 else it
                                                    ).mediaMetadata.artist?.toString()
                                                        ?: "",
                                                    style = TextStyle(
                                                        color = colorPalette().text,
                                                        fontSize = typography().xxxs.semiBold.fontSize,
                                                    ),
                                                    maxLines = 1,
                                                    //overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.conditional(!disableScrollingText) { basicMarquee() }
                                                )
                                                BasicText(
                                                    text = binder.player.getMediaItemAt(
                                                        if (it + 1 <= mediaItems.size - 1) it + 1 else it
                                                    ).mediaMetadata.artist?.toString()
                                                        ?: "",
                                                    style = TextStyle(
                                                        drawStyle = Stroke(
                                                            width = 0.25f,
                                                            join = StrokeJoin.Round
                                                        ),
                                                        color = if (!textoutline) Color.Transparent
                                                        else if (colorPaletteMode == ColorPaletteMode.Light || (colorPaletteMode == ColorPaletteMode.System && (!isSystemInDarkTheme()))) Color.White.copy(
                                                            0.65f
                                                        )
                                                        else Color.Black,
                                                        fontSize = typography().xxxs.semiBold.fontSize,
                                                    ),
                                                    maxLines = 1,
                                                    //overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.conditional(!disableScrollingText) { basicMarquee() }
                                                )
                                            }
                                        }
                                    }
                                }
                                if (showsongs == SongsNumber.`1`) {
                                    IconButton(
                                        icon = R.drawable.trash,
                                        color = Color.White,
                                        enabled = true,
                                        onClick = {
                                            binder.player.removeMediaItem(nextMediaItemIndex)
                                        },
                                        modifier = Modifier
                                            .weight(0.07f)
                                            .size(40.dp)
                                            .padding(vertical = 7.5.dp),
                                    )
                                }

                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = if (actionspacedevenly) Arrangement.SpaceEvenly else Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .fillMaxWidth()
                    ) {
                        if (showButtonPlayerVideo)
                            IconButton(
                                icon = R.drawable.video,
                                color = colorPalette().accent,
                                enabled = true,
                                onClick = {
                                    binder.callPause {}
                                    showSearchEntity = true
                                },
                                modifier = Modifier
                                    .size(24.dp),
                            )

                        if (showButtonPlayerDiscover)
                            IconButton(
                                icon = R.drawable.star_brilliant,
                                color = if (discoverIsEnabled) colorPalette().text else colorPalette().textDisabled,
                                onClick = {},
                                modifier = Modifier
                                    .size(24.dp)
                                    .combinedClickable(
                                        onClick = { discoverIsEnabled = !discoverIsEnabled },
                                        onLongClick = {
                                            SmartMessage(
                                                context.resources.getString(R.string.discoverinfo),
                                                context = context
                                            )
                                        }

                                    )
                            )


                        if (showButtonPlayerDownload)
                            DownloadStateIconButton(
                                icon = if (isDownloaded) R.drawable.downloaded else R.drawable.download,
                                color = if (isDownloaded) colorPalette().accent else Color.Gray,
                                downloadState = downloadState,
                                onClick = {
                                    manageDownload(
                                        context = context,
                                        mediaItem = mediaItem,
                                        downloadState = isDownloaded
                                    )
                                },
                                onCancelButtonClicked = {
                                    manageDownload(
                                        context = context,
                                        mediaItem = mediaItem,
                                        downloadState = true
                                    )
                                },
                                modifier = Modifier
                                    //.padding(start = 12.dp)
                                    .size(24.dp)
                            )


                        if (showButtonPlayerAddToPlaylist)
                            IconButton(
                                icon = R.drawable.add_in_playlist,
                                color = if (songPlaylist > 0 && playlistindicator) colorPalette().text else colorPalette().accent,
                                onClick = {
                                    menuState.display {
                                        MiniPlayerMenu(
                                            navController = navController,
                                            onDismiss = {
                                                menuState.hide()
                                                transaction {
                                                    songPlaylist = Database.songUsedInPlaylists(mediaItem.mediaId)
                                                }
                                            },
                                            mediaItem = mediaItem,
                                            binder = binder,
                                            onClosePlayer = {
                                                onDismiss()
                                            },
                                            disableScrollingText = disableScrollingText
                                        )
                                    }
                                },
                                modifier = Modifier
                                    //.padding(horizontal = 4.dp)
                                    .size(24.dp)
                                    .conditional(songPlaylist > 0 && playlistindicator) {
                                        background(
                                            color.accent,
                                            CircleShape
                                        )
                                    }
                                    .conditional(songPlaylist > 0 && playlistindicator) {
                                        padding(
                                            all = 5.dp
                                        )
                                    }
                            )



                        if (showButtonPlayerLoop)
                            IconButton(
                                icon = getIconQueueLoopState(queueLoopType),
                                color = colorPalette().accent,
                                onClick = {
                                    queueLoopType = setQueueLoopState(queueLoopType)
                                    if (effectRotationEnabled) isRotated = !isRotated
                                },
                                modifier = Modifier
                                    //.padding(horizontal = 4.dp)
                                    .size(24.dp)
                            )

                        if (showButtonPlayerShuffle)
                            IconButton(
                                icon = R.drawable.shuffle,
                                color = colorPalette().accent,
                                enabled = true,
                                onClick = {
                                    binder.player.shuffleQueue()
                                },
                                modifier = Modifier
                                    .size(24.dp),
                            )

                        if (showButtonPlayerLyrics)
                            IconButton(
                                icon = R.drawable.song_lyrics,
                                color = if (isShowingLyrics)  colorPalette().accent else Color.Gray,
                                enabled = true,
                                onClick = {
                                    if (isShowingVisualizer) isShowingVisualizer = !isShowingVisualizer
                                    isShowingLyrics = !isShowingLyrics
                                },
                                modifier = Modifier
                                    .size(24.dp),
                            )
                        if (!isLandscape || ((playerType == PlayerType.Essential) && !showthumbnail))
                         if (expandedplayertoggle && (!showlyricsthumbnail) && !expandedlyrics)
                            IconButton(
                                icon = R.drawable.minmax,
                                color = if (expandedplayer) colorPalette().accent else Color.Gray,
                                enabled = true,
                                onClick = {
                                    expandedplayer = !expandedplayer
                                },
                                modifier = Modifier
                                    .size(20.dp),
                            )


                        if (visualizerEnabled)
                            IconButton(
                                icon = R.drawable.sound_effect,
                                color = if (isShowingVisualizer) colorPalette().text else colorPalette().textDisabled,
                                enabled = true,
                                onClick = {
                                    if (isShowingLyrics) isShowingLyrics = !isShowingLyrics
                                    isShowingVisualizer = !isShowingVisualizer
                                },
                                modifier = Modifier
                                    .size(24.dp)
                            )


                        if (showButtonPlayerSleepTimer)
                            IconButton(
                                icon = R.drawable.sleep,
                                color = if (sleepTimerMillisLeft != null) colorPalette().accent else Color.Gray,
                                enabled = true,
                                onClick = {
                                    isShowingSleepTimerDialog = true
                                },
                                modifier = Modifier
                                    .size(24.dp),
                            )

                        if (showButtonPlayerSystemEqualizer) {
                            val activityResultLauncher =
                                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

                            IconButton(
                                icon = R.drawable.equalizer,
                                color = colorPalette().accent,
                                enabled = true,
                                onClick = {
                                    try {
                                        activityResultLauncher.launch(
                                            Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                                                putExtra(
                                                    AudioEffect.EXTRA_AUDIO_SESSION,
                                                    binder.player.audioSessionId
                                                )
                                                putExtra(
                                                    AudioEffect.EXTRA_PACKAGE_NAME,
                                                    context.packageName
                                                )
                                                putExtra(
                                                    AudioEffect.EXTRA_CONTENT_TYPE,
                                                    AudioEffect.CONTENT_TYPE_MUSIC
                                                )
                                            }
                                        )
                                    } catch (e: ActivityNotFoundException) {
                                        SmartMessage(
                                            context.resources.getString(R.string.info_not_find_application_audio),
                                            type = PopupType.Warning, context = context
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .size(20.dp),
                            )
                        }

                        if (showButtonPlayerArrow)
                            IconButton(
                                icon = R.drawable.chevron_up,
                                color = colorPalette().accent,
                                enabled = true,
                                onClick = {
                                    showQueue = true
                                },
                                modifier = Modifier
                                    //.padding(end = 12.dp)
                                    .size(24.dp),
                            )

                        if (showButtonPlayerMenu && !isLandscape)
                            IconButton(
                                icon = R.drawable.ellipsis_vertical,
                                color = colorPalette().accent,
                                onClick = {
                                    menuState.display {
                                        PlayerMenu(
                                            navController = navController,
                                            onDismiss = menuState::hide,
                                            mediaItem = mediaItem,
                                            binder = binder,
                                            onClosePlayer = {
                                                onDismiss()
                                            },
                                            disableScrollingText = disableScrollingText
                                        )
                                    }
                                },
                                modifier = Modifier
                                    //.padding(end = 12.dp)
                                    .size(24.dp)
                            )


                        if (isLandscape) {
                            IconButton(
                                icon = R.drawable.ellipsis_horizontal,
                                color = colorPalette().accent,
                                onClick = {
                                    menuState.display {
                                        PlayerMenu(
                                            navController = navController,
                                            onDismiss = menuState::hide,
                                            mediaItem = mediaItem,
                                            binder = binder,
                                            onClosePlayer = {
                                                onDismiss()
                                            },
                                            disableScrollingText = disableScrollingText
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .size(24.dp)
                            )
                        }
                    }


                }
            }
        }
        //val binder = LocalPlayerServiceBinder.current
        val player = binder.player ?: return
        val clickLyricsText by rememberPreference(clickOnLyricsTextKey, true)
        var extraspace by rememberPreference(extraspaceKey, false)
        //val nextMediaItemIndex = binder.player.nextMediaItemIndex
        //val prevMediaItemIndex = binder.player.previousMediaItemIndex
        /*
        val nextMediaItem = if (binder.player.hasNextMediaItem())
            binder.player.getMediaItemAt(binder.player.nextMediaItemIndex)
        else MediaItem.EMPTY
        val nextNextMediaItem = try {
            binder.player.getMediaItemAt(nextMediaItemIndex + 1)
        } catch (e: Exception) {
            MediaItem.EMPTY
        }
        val prevMediaItem = if (binder.player.hasPreviousMediaItem())
            binder.player.getMediaItemAt(binder.player.previousMediaItemIndex)
        else MediaItem.EMPTY
        val prevPrevMediaItem = try {
            binder.player.getMediaItemAt(prevMediaItemIndex - 1)
        } catch (e: Exception) {
            MediaItem.EMPTY
        }
        */
        val nextmedia = if(binder.player.mediaItemCount > 1
            && binder.player.currentMediaItemIndex + 1 < binder.player.mediaItemCount )
            binder.player.getMediaItemAt(binder.player.currentMediaItemIndex + 1) else MediaItem.EMPTY

        var songPlaylist1 by remember {
            mutableStateOf(0)
        }
        LaunchedEffect(Unit, nextmedia.mediaId) {
            withContext(Dispatchers.IO) {
                songPlaylist1 = Database.songUsedInPlaylists(nextmedia.mediaId)
            }
        }

        var songLiked by remember {
            mutableStateOf(0)
        }

        LaunchedEffect(Unit, nextmedia.mediaId) {
            withContext(Dispatchers.IO) {
                songLiked = Database.songliked(nextmedia.mediaId)
            }
        }

        val thumbnailRoundness by rememberPreference(thumbnailRoundnessKey, ThumbnailRoundness.Heavy)
        val thumbnailType by rememberPreference(thumbnailTypeKey, ThumbnailType.Modern)
        val statsfornerds by rememberPreference(statsfornerdsKey, false)


        //if (discoverIsEnabled) ApplyDiscoverToQueue()


        if (isLandscape) {
         Box(
             modifier = Modifier.haze(state = hazeState, style = HazeDefaults.style(backgroundColor = Color.Transparent, tint = Color.Black.copy(0.5f),blurRadius = 8.dp))
         ){
             if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor && playerType == PlayerType.Modern && !showthumbnail) {
                 val fling = PagerDefaults.flingBehavior(
                     state = pagerState,
                     snapPositionalThreshold = 0.20f
                 )
                 
                 LaunchedEffect(binder.player.currentMediaItemIndex){
                     pagerState.animateScrollToPage(binder.player.currentMediaItemIndex)
                 }

                 LaunchedEffect(pagerState) {
                     var previousPage = pagerState.settledPage
                     snapshotFlow { pagerState.settledPage }.distinctUntilChanged().collect {
                         if (previousPage != it) {
                             if (it != binder.player.currentMediaItemIndex) binder.player.forcePlayAtIndex(mediaItems,it)
                         }
                         previousPage = it
                     }
                 }

                 HorizontalPager(
                     state = pagerState,
                     beyondViewportPageCount = 1,
                     flingBehavior = fling,
                     modifier = Modifier
                 ) { it ->

                     AsyncImage(
                         model = ImageRequest.Builder(LocalContext.current)
                             .data(binder.player.getMediaItemAt(it).mediaMetadata.artworkUri.toString().resize(1200, 1200))
                             .transformations(
                                 listOf(
                                     if (showthumbnail) {
                                         BlurTransformation(
                                             scale = 0.5f,
                                             radius = blurStrength.toInt(),
                                             //darkenFactor = blurDarkenFactor
                                         )

                                     } else
                                         BlurTransformation(
                                             scale = 0.5f,
                                             //radius = blurStrength2.toInt(),
                                             radius = if ((isShowingLyrics && !isShowingVisualizer) || !noblur) blurStrength.toInt() else 0,
                                             //darkenFactor = blurDarkenFactor
                                         )
                                 )
                             )
                             .build(),
                         contentDescription = "",
                         contentScale = ContentScale.Crop,
                         modifier = Modifier
                             .fillMaxHeight()
                             .combinedClickable(
                                 interactionSource = remember { MutableInteractionSource() },
                                 indication = null,
                                 onClick = {
                                     if (thumbnailTapEnabled) {
                                         if (isShowingVisualizer) isShowingVisualizer = false
                                         isShowingLyrics = !isShowingLyrics
                                     }
                                 },
                                 onDoubleClick = {
                                     if (!showlyricsthumbnail && !showvisthumbnail)
                                         showthumbnail = !showthumbnail
                                 },
                                 onLongClick = {
                                     if (showthumbnail || (isShowingLyrics && !isShowingVisualizer) || !noblur)
                                         showBlurPlayerDialog = true
                                 }
                             )
                     )
                 }
                 Column(modifier = Modifier
                     .matchParentSize()
                     .background(
                         Brush.verticalGradient(
                             0.0f to Color.Transparent,
                             1.0f to if (bottomgradient) if (colorPaletteMode == ColorPaletteMode.Light) Color.White.copy(
                                 if (isLandscape) 0.8f else 0.75f
                             ) else Color.Black.copy(if (isLandscape) 0.8f else 0.75f) else Color.Transparent,
                             startY = if (isLandscape) 600f else if (expandedplayer) 1300f else 950f,
                             endY = POSITIVE_INFINITY
                         )
                     )
                     .background(
                         if (bottomgradient) if (isLandscape) if (colorPaletteMode == ColorPaletteMode.Light) Color.White.copy(
                             0.25f
                         ) else Color.Black.copy(0.25f) else Color.Transparent else Color.Transparent
                     )){}
             }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = containerModifier
                    .padding(top = if (playerType == PlayerType.Essential) 40.dp else 20.dp)
                    .padding(top = if (extraspace) 10.dp else 0.dp)
                    .drawBehind {
                        if (backgroundProgress == BackgroundProgress.Both || backgroundProgress == BackgroundProgress.Player) {
                            drawRect(
                                color = color.favoritesOverlay,
                                topLeft = Offset.Zero,
                                size = Size(
                                    width = positionAndDuration.first.toFloat() /
                                            positionAndDuration.second.absoluteValue * size.width,
                                    height = size.maxDimension
                                )
                            )
                        }
                    }
            ) {
                Column (
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxHeight()
                        .animateContentSize()
                       // .border(BorderStroke(1.dp, Color.Blue))
                ) {
                    if (showthumbnail && (playerType == PlayerType.Essential)) {
                        Box(
                            contentAlignment = Alignment.Center,
                            /*modifier = Modifier
                            .weight(1f)*/
                            //.padding(vertical = 10.dp)
                        ) {
                            if ((!isShowingLyrics && !isShowingVisualizer) || (isShowingVisualizer && showvisthumbnail) || (isShowingLyrics && showlyricsthumbnail))
                                thumbnailContent()
                        }
                    }
                    if (isShowingVisualizer && !showvisthumbnail && playerType == PlayerType.Essential) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .pointerInput(Unit) {
                                    detectHorizontalDragGestures(
                                        onHorizontalDrag = { change, dragAmount ->
                                            deltaX = dragAmount
                                        },
                                        onDragStart = {
                                        },
                                        onDragEnd = {
                                            if (!disablePlayerHorizontalSwipe && playerType == PlayerType.Essential) {
                                                if (deltaX > 5) {
                                                    binder.player.playPrevious()
                                                } else if (deltaX < -5) {
                                                    binder.player.playNext()
                                                }

                                            }

                                        }

                                    )
                                }
                        ) {
                            NextVisualizer(
                                    isDisplayed = isShowingVisualizer
                                )
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .navigationBarsPadding()
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onHorizontalDrag = { change, dragAmount ->
                                        deltaX = dragAmount
                                    },
                                    onDragStart = {
                                    },
                                    onDragEnd = {
                                        if (!disablePlayerHorizontalSwipe) {
                                            if (deltaX > 5) {
                                                binder.player.playPrevious()
                                            } else if (deltaX < -5) {
                                                binder.player.playNext()
                                            }

                                        }

                                    }

                                )
                            }
                    ){
                        if (!showlyricsthumbnail)
                            Lyrics(
                                mediaId = mediaItem.mediaId,
                                isDisplayed = isShowingLyrics,
                                onDismiss = {
                                        isShowingLyrics = false
                                },
                                ensureSongInserted = { Database.insert(mediaItem) },
                                size = 1000.dp,
                                mediaMetadataProvider = mediaItem::mediaMetadata,
                                durationProvider = player::getDuration,
                                isLandscape = isLandscape,
                                onMaximize = {
                                    showFullLyrics = true
                                },
                                clickLyricsText = clickLyricsText,
                            )
                    }
                }
                Column (
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (playerType == PlayerType.Modern) {
                         Box(
                             contentAlignment = Alignment.Center,
                             modifier = Modifier
                                 .weight(1f)
                             /*modifier = Modifier
                            .weight(1f)*/
                             //.padding(vertical = 10.dp)
                         ) {
                             if (showthumbnail) {
                                 if ((!isShowingLyrics && !isShowingVisualizer) || (isShowingVisualizer && showvisthumbnail) || (isShowingLyrics && showlyricsthumbnail)) {
                                     val fling = PagerDefaults.flingBehavior(state = pagerState,snapPositionalThreshold = 0.25f)
                                     val pageSpacing = thumbnailSpacing.toInt()*0.01*(screenWidth) - (2.5*playerThumbnailSize.size.dp)
                                     LaunchedEffect(binder.player.currentMediaItemIndex){
                                         pagerState.animateScrollToPage(binder.player.currentMediaItemIndex)
                                     }

                                     LaunchedEffect(pagerState) {
                                         var previousPage = pagerState.settledPage
                                         snapshotFlow { pagerState.settledPage }.distinctUntilChanged().collect {
                                             if (previousPage != it) {
                                                 if (it != binder.player.currentMediaItemIndex) binder.player.forcePlayAtIndex(mediaItems,it)
                                             }
                                             previousPage = it
                                         }
                                     }
                                     HorizontalPager(
                                         state = pagerState,
                                         pageSize = PageSize.Fixed(thumbnailSizeDp),
                                         pageSpacing = thumbnailSpacing.toInt()*0.01*(screenWidth) - (2.5*playerThumbnailSize.size.dp),
                                         contentPadding = PaddingValues(start = thumbnailOffset.toInt()*0.01*(screenWidth), end = thumbnailOffset.toInt()*0.01*(screenWidth) + if (pageSpacing < 0.dp) (-(pageSpacing)) else 0.dp),
                                         beyondViewportPageCount = 3,
                                         flingBehavior = fling,
                                         modifier = Modifier
                                             .padding(
                                                 all = (if (thumbnailType == ThumbnailType.Modern) -(10.dp) else 0.dp).coerceAtLeast(
                                                     0.dp
                                                 )
                                             )
                                             .conditional(fadingedge) {horizontalFadingEdge()}
                                         ) { it ->

                                         AsyncImage(
                                             model = ImageRequest.Builder(LocalContext.current)
                                                 .data(binder.player.getMediaItemAt(it).mediaMetadata.artworkUri.toString().resize(1200, 1200))
                                                 .build(),
                                             contentDescription = "",
                                             contentScale = ContentScale.Fit,
                                             modifier = Modifier
                                                 .padding(all = playerThumbnailSize.size.dp)
                                                 .zIndex(
                                                     if (it == pagerState.currentPage) 1f
                                                     else if (it == (pagerState.currentPage + 1) || it == (pagerState.currentPage - 1)) 0.85f
                                                     else if (it == (pagerState.currentPage + 2) || it == (pagerState.currentPage - 2)) 0.78f
                                                     else if (it == (pagerState.currentPage + 3) || it == (pagerState.currentPage - 3)) 0.73f
                                                     else if (it == (pagerState.currentPage + 4) || it == (pagerState.currentPage - 4)) 0.68f
                                                     else if (it == (pagerState.currentPage + 5) || it == (pagerState.currentPage - 5)) 0.63f
                                                     else 0.57f
                                                 )
                                                 .graphicsLayer {
                                                     val pageOffSet =
                                                         ((pagerState.currentPage - it) + pagerState.currentPageOffsetFraction).absoluteValue
                                                     alpha = lerp(
                                                         start = 0.9f,
                                                         stop = 1f,
                                                         fraction = 1f - pageOffSet.coerceIn(0f, 1f)
                                                     )
                                                     scaleY = lerp(
                                                         start = if (it == (pagerState.currentPage + 1) || it == (pagerState.currentPage - 1)) 0.85f
                                                         else if (it == (pagerState.currentPage + 2) || it == (pagerState.currentPage - 2)) 0.78f
                                                         else if (it == (pagerState.currentPage + 3) || it == (pagerState.currentPage - 3)) 0.73f
                                                         else if (it == (pagerState.currentPage + 4) || it == (pagerState.currentPage - 4)) 0.68f
                                                         else if (it == (pagerState.currentPage + 5) || it == (pagerState.currentPage - 5)) 0.63f
                                                         else 0.57f,
                                                         stop = 1f,
                                                         fraction = 1f - pageOffSet.coerceIn(0f, 1f)
                                                     )
                                                     scaleX = lerp(
                                                         start = if (it == (pagerState.currentPage + 1) || it == (pagerState.currentPage - 1)) 0.85f
                                                         else if (it == (pagerState.currentPage + 2) || it == (pagerState.currentPage - 2)) 0.78f
                                                         else if (it == (pagerState.currentPage + 3) || it == (pagerState.currentPage - 3)) 0.73f
                                                         else if (it == (pagerState.currentPage + 4) || it == (pagerState.currentPage - 4)) 0.68f
                                                         else if (it == (pagerState.currentPage + 5) || it == (pagerState.currentPage - 5)) 0.63f
                                                         else 0.57f,
                                                         stop = 1f,
                                                         fraction = 1f - pageOffSet.coerceIn(0f, 1f)
                                                     )
                                                 }
                                                 .conditional(thumbnailType == ThumbnailType.Modern) {
                                                     padding(
                                                         all = 10.dp
                                                     )
                                                 }
                                                 .conditional(thumbnailType == ThumbnailType.Modern) {
                                                     doubleShadowDrop(
                                                         thumbnailRoundness.shape(),
                                                         4.dp,
                                                         8.dp
                                                     )
                                                 }
                                                 .clip(thumbnailRoundness.shape())
                                                 .combinedClickable(
                                                     interactionSource = remember { MutableInteractionSource() },
                                                     indication = null,
                                                     onClick = {
                                                         if (it == pagerState.settledPage && thumbnailTapEnabled) {
                                                             if (isShowingVisualizer) isShowingVisualizer =
                                                                 false
                                                             isShowingLyrics = !isShowingLyrics
                                                         }
                                                         if (it != pagerState.settledPage) {
                                                             binder.player.forcePlayAtIndex(
                                                                 mediaItems,
                                                                 it
                                                             )
                                                         }
                                                     },
                                                     onLongClick = {
                                                         if (it == pagerState.settledPage)
                                                             showThumbnailOffsetDialog = true
                                                     }
                                                 )

                                         )
                                     }
                                 }
                            }
                            if (isShowingVisualizer && !showvisthumbnail) {
                                Box(
                                    modifier = Modifier
                                        .pointerInput(Unit) {
                                            detectHorizontalDragGestures(
                                                onHorizontalDrag = { change, dragAmount ->
                                                    deltaX = dragAmount
                                                },
                                                onDragStart = {
                                                },
                                                onDragEnd = {
                                                    if (!disablePlayerHorizontalSwipe && playerType == PlayerType.Essential) {
                                                        if (deltaX > 5) {
                                                            binder.player.playPrevious()
                                                        } else if (deltaX < -5) {
                                                            binder.player.playNext()
                                                        }

                                                    }

                                                }

                                            )
                                        }
                                ) {
                                    NextVisualizer(
                                        isDisplayed = isShowingVisualizer
                                    )
                                }
                            }
                        }
                    }
                    if (playerType == PlayerType.Essential || isShowingVisualizer) {
                        controlsContent(
                            Modifier
                                .padding(vertical = 8.dp)
                                .conditional(playerType == PlayerType.Essential) { fillMaxHeight() }
                                .conditional(playerType == PlayerType.Essential) { weight(1f) }

                        )
                    } else {

                                Controls(
                                    navController = navController,
                                    onCollapse = onDismiss,
                                    expandedplayer = expandedplayer,
                                    titleExpanded = titleExpanded,
                                    timelineExpanded = timelineExpanded,
                                    controlsExpanded = controlsExpanded,
                                    isShowingLyrics = isShowingLyrics,
                                    media = mediaItem.toUiMedia(positionAndDuration.second),
                                    mediaId = mediaItem.mediaId,
                                    title = mediaItem.mediaMetadata.title?.toString(),
                                    artist = mediaItem.mediaMetadata.artist?.toString(),
                                    artistIds = artistsInfo,
                                    albumId = albumId,
                                    shouldBePlaying = shouldBePlaying,
                                    position = positionAndDuration.first,
                                    duration = positionAndDuration.second,
                                    modifier = Modifier
                                        .padding(vertical = 8.dp),
                                    onBlurScaleChange = { blurStrength = it }
                                )

                    }
                    if (!showthumbnail || playerType == PlayerType.Modern) {
                        StatsForNerds(
                            mediaId = mediaItem.mediaId,
                            isDisplayed = statsfornerds,
                            onDismiss = {}
                        )
                    }
                    actionsBarContent()
                }
            }
         }
        } else {
           Box(
               modifier = Modifier.haze(state = hazeState, style = HazeDefaults.style(backgroundColor = Color.Transparent, tint = Color.Black.copy(0.5f),blurRadius = 8.dp))
           ) {
               if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor && playerType == PlayerType.Modern && !showthumbnail) {
                    val fling = PagerDefaults.flingBehavior(
                        state = pagerState,
                        snapPositionalThreshold = 0.20f
                    )
                    LaunchedEffect(binder.player.currentMediaItemIndex){
                        pagerState.animateScrollToPage(binder.player.currentMediaItemIndex)
                    }

                    LaunchedEffect(pagerState) {
                        var previousPage = pagerState.settledPage
                        snapshotFlow { pagerState.settledPage }.distinctUntilChanged().collect {
                            if (previousPage != it) {
                                if (it != binder.player.currentMediaItemIndex) binder.player.forcePlayAtIndex(mediaItems,it)
                            }
                            previousPage = it
                        }
                    }
                    HorizontalPager(
                        state = pagerState,
                        beyondViewportPageCount = 1,
                        flingBehavior = fling,
                        modifier = Modifier
                    ) { it ->

                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(binder.player.getMediaItemAt(it).mediaMetadata.artworkUri.toString().resize(1200, 1200))
                                .transformations(
                                    listOf(
                                        if (showthumbnail) {
                                            BlurTransformation(
                                                scale = 0.5f,
                                                radius = blurStrength.toInt(),
                                                //darkenFactor = blurDarkenFactor
                                            )

                                        } else
                                            BlurTransformation(
                                                scale = 0.5f,
                                                //radius = blurStrength2.toInt(),
                                                radius = if ((isShowingLyrics && !isShowingVisualizer) || !noblur) blurStrength.toInt() else 0,
                                                //darkenFactor = blurDarkenFactor
                                            )
                                    )
                                )
                                .build(),
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxHeight()
                                .combinedClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        if (thumbnailTapEnabled) {
                                            if (isShowingVisualizer) isShowingVisualizer = false
                                            isShowingLyrics = !isShowingLyrics
                                        }
                                    },
                                    onDoubleClick = {
                                        if (!showlyricsthumbnail && !showvisthumbnail)
                                            showthumbnail = !showthumbnail
                                    },
                                    onLongClick = {
                                        if (showthumbnail || (isShowingLyrics && !isShowingVisualizer) || !noblur)
                                            showBlurPlayerDialog = true
                                    }
                                )
                        )
                    }
                    Column(modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                1.0f to if (bottomgradient) if (colorPaletteMode == ColorPaletteMode.Light) Color.White.copy(
                                    if (isLandscape) 0.8f else 0.75f
                                ) else Color.Black.copy(if (isLandscape) 0.8f else 0.75f) else Color.Transparent,
                                startY = if (isLandscape) 600f else if (expandedplayer) 1300f else 950f,
                                endY = POSITIVE_INFINITY
                            )
                        )
                        .background(
                            if (bottomgradient) if (isLandscape) if (colorPaletteMode == ColorPaletteMode.Light) Color.White.copy(
                                0.25f
                            ) else Color.Black.copy(0.25f) else Color.Transparent else Color.Transparent
                        )){}
                }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = containerModifier
                    //.padding(top = 10.dp)
                    .drawBehind {
                        if (backgroundProgress == BackgroundProgress.Both || backgroundProgress == BackgroundProgress.Player) {
                            drawRect(
                                color = color.favoritesOverlay,
                                topLeft = Offset.Zero,
                                size = Size(
                                    width = positionAndDuration.first.toFloat() /
                                            positionAndDuration.second.absoluteValue * size.width,
                                    height = size.maxDimension
                                )
                            )
                        }
                    }
            ) {


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(
                            windowInsets
                                .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                                .asPaddingValues()
                        )
                        //.padding(top = 5.dp)
                        .fillMaxWidth(0.9f)
                        .height(30.dp)
                ) {

                    if (showTopActionsBar) {
                        Image(
                            painter = painterResource(R.drawable.chevron_down),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette().collapsedPlayerProgressBar),
                            modifier = Modifier
                                .clickable {
                                    onDismiss()
                                }
                                .rotate(rotationAngle)
                                //.padding(10.dp)
                                .size(24.dp)
                        )


                        Image(
                            painter = painterResource(R.drawable.app_icon),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette().collapsedPlayerProgressBar),
                            modifier = Modifier
                                .clickable {
                                    onDismiss()
                                    navController.navigate(NavRoutes.home.name)
                                }
                                .rotate(rotationAngle)
                                //.padding(10.dp)
                                .size(24.dp)

                        )

                        if (!showButtonPlayerMenu)
                            Image(
                                painter = painterResource(R.drawable.ellipsis_vertical),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette().collapsedPlayerProgressBar),
                                modifier = Modifier
                                    .clickable {
                                        menuState.display {
                                            PlayerMenu(
                                                navController = navController,
                                                onDismiss = menuState::hide,
                                                mediaItem = mediaItem,
                                                binder = binder,
                                                onClosePlayer = {
                                                    onDismiss()
                                                },
                                                disableScrollingText = disableScrollingText
                                            )
                                        }
                                    }
                                    .rotate(rotationAngle)
                                    //.padding(10.dp)
                                    .size(24.dp)

                            )

                    }
                }

                Spacer(
                    modifier = Modifier
                        .height(5.dp)
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                ) {

                      if (showthumbnail) {
                         if ((!isShowingLyrics && !isShowingVisualizer) || (isShowingVisualizer && showvisthumbnail) || (isShowingLyrics && showlyricsthumbnail)) {
                             if (playerType == PlayerType.Modern) {
                                 val fling = PagerDefaults.flingBehavior(state = pagerState,snapPositionalThreshold = 0.25f)

                                 LaunchedEffect(binder.player.currentMediaItemIndex) {
                                     pagerState.animateScrollToPage(binder.player.currentMediaItemIndex)
                                 }

                                 LaunchedEffect(pagerState) {
                                     var previousPage = pagerState.settledPage
                                     snapshotFlow { pagerState.settledPage }.distinctUntilChanged().collect {
                                         if (previousPage != it) {
                                             if (it != binder.player.currentMediaItemIndex) binder.player.forcePlayAtIndex(mediaItems,it)
                                         }
                                         previousPage = it
                                     }
                                 }
                                 
                                 val screenHeight = configuration.screenHeightDp.dp
                                 val pageSpacing = (thumbnailSpacing.toInt()*0.01*(screenHeight) - if (carousel) (3*carouselSize.size.dp) else (2*playerThumbnailSize.size.dp))
                                 VerticalPager(
                                     state = pagerState,
                                     pageSize = PageSize.Fixed(thumbnailSizeDp),
                                     contentPadding = PaddingValues(top = if (expandedplayer) (thumbnailOffset.toInt()*0.01*(screenHeight)) else 0.dp, bottom = (thumbnailOffset.toInt()*0.01*(screenHeight)) + if (pageSpacing < 0.dp) (-(pageSpacing)) else 0.dp),
                                     pageSpacing = if (expandedplayer) (thumbnailSpacing.toInt()*0.01*(screenHeight) - if (carousel) (3*carouselSize.size.dp) else (2*playerThumbnailSize.size.dp)) else 10.dp,
                                     beyondViewportPageCount = 2,
                                     flingBehavior = fling,
                                     modifier = modifier
                                         .padding(top = if (expandedplayer) 0.dp else 8.dp)
                                         .padding(
                                             all = (if (expandedplayer) 0.dp else if (thumbnailType == ThumbnailType.Modern) -(10.dp) else 0.dp).coerceAtLeast(
                                                 0.dp
                                             )
                                         )
                                         .conditional(fadingedge && !expandedplayer) {
                                             padding(
                                                 vertical = 2.5.dp
                                             )
                                         }
                                         .conditional(fadingedge) {
                                             VerticalfadingEdge2(fade = thumbnailFade*0.05f)
                                         }
                                 ){ it ->

                                     AsyncImage(
                                         model = ImageRequest.Builder(LocalContext.current)
                                             .data(binder.player.getMediaItemAt(it).mediaMetadata.artworkUri.toString().resize(1200, 1200))
                                             .build(),
                                         contentDescription = "",
                                         contentScale = ContentScale.Fit,
                                         modifier = Modifier
                                             .fillMaxWidth()
                                             .padding(all = if (expandedplayer) carouselSize.size.dp else playerThumbnailSize.size.dp)
                                             .zIndex(
                                                 if (it == pagerState.currentPage) 1f
                                                 else if (it == (pagerState.currentPage + 1) || it == (pagerState.currentPage - 1)) 0.85f
                                                 else if (it == (pagerState.currentPage + 2) || it == (pagerState.currentPage - 2)) 0.78f
                                                 else if (it == (pagerState.currentPage + 3) || it == (pagerState.currentPage - 3)) 0.73f
                                                 else if (it == (pagerState.currentPage + 4) || it == (pagerState.currentPage - 4)) 0.68f
                                                 else if (it == (pagerState.currentPage + 5) || it == (pagerState.currentPage - 5)) 0.63f
                                                 else 0.57f
                                             )
                                             .conditional(carousel)
                                             {
                                                 graphicsLayer {
                                                     val pageOffSet =
                                                         ((pagerState.currentPage - it) + pagerState.currentPageOffsetFraction).absoluteValue
                                                     alpha = lerp(
                                                         start = 0.9f,
                                                         stop = 1f,
                                                         fraction = 1f - pageOffSet.coerceIn(0f, 1f)
                                                     )
                                                     scaleY = lerp(
                                                         start = if (it == (pagerState.currentPage + 1) || it == (pagerState.currentPage - 1)) 0.85f
                                                         else if (it == (pagerState.currentPage + 2) || it == (pagerState.currentPage - 2)) 0.78f
                                                         else if (it == (pagerState.currentPage + 3) || it == (pagerState.currentPage - 3)) 0.73f
                                                         else if (it == (pagerState.currentPage + 4) || it == (pagerState.currentPage - 4)) 0.68f
                                                         else if (it == (pagerState.currentPage + 5) || it == (pagerState.currentPage - 5)) 0.63f
                                                         else 0.57f,
                                                         stop = 1f,
                                                         fraction = 1f - pageOffSet.coerceIn(0f, 1f)
                                                     )
                                                     scaleX = lerp(
                                                         start = if (it == (pagerState.currentPage + 1) || it == (pagerState.currentPage - 1)) 0.85f
                                                         else if (it == (pagerState.currentPage + 2) || it == (pagerState.currentPage - 2)) 0.78f
                                                         else if (it == (pagerState.currentPage + 3) || it == (pagerState.currentPage - 3)) 0.73f
                                                         else if (it == (pagerState.currentPage + 4) || it == (pagerState.currentPage - 4)) 0.68f
                                                         else if (it == (pagerState.currentPage + 5) || it == (pagerState.currentPage - 5)) 0.63f
                                                         else 0.57f,
                                                         stop = 1f,
                                                         fraction = 1f - pageOffSet.coerceIn(0f, 1f)
                                                     )
                                                 }
                                             }
                                             .conditional(thumbnailType == ThumbnailType.Modern) {
                                                 padding(
                                                     all = 10.dp
                                                 )
                                             }
                                             .conditional(thumbnailType == ThumbnailType.Modern) {
                                                 doubleShadowDrop(
                                                     thumbnailRoundness.shape(),
                                                     4.dp,
                                                     8.dp
                                                 )
                                             }
                                             .clip(thumbnailRoundness.shape())
                                             .combinedClickable(
                                                 interactionSource = remember { MutableInteractionSource() },
                                                 indication = null,
                                                 onClick = {
                                                     if (it == pagerState.settledPage && thumbnailTapEnabled) {
                                                         if (isShowingVisualizer) isShowingVisualizer =
                                                             false
                                                         isShowingLyrics = !isShowingLyrics
                                                     }
                                                     if (it != pagerState.settledPage) {
                                                         binder.player.forcePlayAtIndex(
                                                             mediaItems,
                                                             it
                                                         )
                                                     }
                                                 },
                                                 onLongClick = {
                                                     if (it == pagerState.settledPage)
                                                         showThumbnailOffsetDialog = true
                                                 }
                                             )

                                     )
                                 }
                             } else {
                                 thumbnailContent(
                                     /*
                                     KOTLIN 2
                                     modifier = Modifier
                                         .clip(thumbnailShape)
                                         .padding(
                                             horizontal = playerThumbnailSize.size.dp,
                                             vertical = 4.dp,
                                         )
                                         .thumbnailpause(
                                             shouldBePlaying = shouldBePlaying
                                         )

                                      */
                                 )
                             }
                         }
                      }

                   Box(
                        modifier = Modifier
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onHorizontalDrag = { change, dragAmount ->
                                        deltaX = dragAmount
                                    },
                                    onDragStart = {
                                    },
                                    onDragEnd = {
                                        if (!disablePlayerHorizontalSwipe) {
                                            if (deltaX > 5) {
                                                binder.player.playPrevious()
                                            } else if (deltaX <-5){
                                                binder.player.playNext()
                                            }

                                        }

                                    }

                                )
                            }
                    ) {
                        if (!showlyricsthumbnail)
                            Lyrics(
                                mediaId = mediaItem.mediaId,
                                isDisplayed = isShowingLyrics,
                                onDismiss = {
                                        isShowingLyrics = false
                                },
                                ensureSongInserted = { Database.insert(mediaItem) },
                                size = 1000.dp,
                                mediaMetadataProvider = mediaItem::mediaMetadata,
                                durationProvider = player::getDuration,
                                isLandscape = isLandscape,
                                onMaximize = {
                                    showFullLyrics = true
                                },
                                clickLyricsText = clickLyricsText,
                            )
                        if (!showvisthumbnail)
                            NextVisualizer(
                                isDisplayed = isShowingVisualizer
                            )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                    .conditional(!expandedplayer){weight(1f)}
                ){
                if (!expandedplayer || !isShowingLyrics || queueDurationExpanded) {
                    if (showTotalTimeQueue)
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                        ) {
                            Image(
                                painter = painterResource(R.drawable.time),
                                colorFilter = ColorFilter.tint(colorPalette().accent),
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(horizontal = 5.dp),
                                contentDescription = "Background Image",
                                contentScale = ContentScale.Fit
                            )

                            Box {
                                BasicText(
                                    text = " ${formatAsTime(totalPlayTimes)}",
                                    style = typography().xxs.semiBold.merge(
                                        TextStyle(
                                            textAlign = TextAlign.Center,
                                            color = colorPalette().text,
                                        )
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                BasicText(
                                    text = " ${formatAsTime(totalPlayTimes)}",
                                    style = typography().xxs.semiBold.merge(
                                        TextStyle(
                                            textAlign = TextAlign.Center,
                                            drawStyle = Stroke(
                                                width = 1f,
                                                join = StrokeJoin.Round
                                            ),
                                            color = if (!textoutline) Color.Transparent
                                            else if (colorPaletteMode == ColorPaletteMode.Light ||
                                                (colorPaletteMode == ColorPaletteMode.System && (!isSystemInDarkTheme()))
                                            )
                                                Color.White.copy(0.5f)
                                            else Color.Black,
                                        )
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }


                    Spacer(
                        modifier = Modifier
                            .height(10.dp)
                    )
                }
                Box(modifier = Modifier
                    .conditional(!expandedplayer){weight(1f)}) {
                    if (playerType == PlayerType.Essential || isShowingLyrics || isShowingVisualizer) {
                        controlsContent(
                            Modifier
                                .padding(vertical = 4.dp)
                                .fillMaxWidth()
                            //.weight(1f)

                        )
                    } else {
                                Controls(
                                    navController = navController,
                                    onCollapse = onDismiss,
                                    expandedplayer = expandedplayer,
                                    titleExpanded = titleExpanded,
                                    timelineExpanded = timelineExpanded,
                                    controlsExpanded = controlsExpanded,
                                    isShowingLyrics = isShowingLyrics,
                                    media = mediaItem.toUiMedia(positionAndDuration.second),
                                    mediaId = mediaItem.mediaId,
                                    title = mediaItem.mediaMetadata.title?.toString(),

                                    /*
                                    TODO: fix this because index not always correct and cause crash
                                    title = binder.player.getMediaItemAt(index).mediaMetadata.title?.toString()
                                        ?: "",

                                     */
                                    artist = mediaItem.mediaMetadata.artist?.toString(),
                                    //TODO: fix this because index not always correct and cause crash
                                    //artist = binder.player.getMediaItemAt(index).mediaMetadata.artist?.toString(),
                                    artistIds = artistsInfo,
                                    albumId = albumId,
                                    shouldBePlaying = shouldBePlaying,
                                    position = positionAndDuration.first,
                                    duration = positionAndDuration.second,
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .fillMaxWidth(),
                                            //.weight(1f),
                                        onBlurScaleChange = { blurStrength = it }
                                )

                    }
                }

                if (!showthumbnail || playerType == PlayerType.Modern) {
                    if (!expandedplayer || !isShowingLyrics || statsExpanded) {
                        StatsForNerds(
                            mediaId = mediaItem.mediaId,
                            isDisplayed = statsfornerds,
                            onDismiss = {}
                        )
                    }
                }
                actionsBarContent()
              }
            }
           }
        }

        CustomModalBottomSheet(
            showSheet = showQueue,
            onDismissRequest = { showQueue = false },
            containerColor = if (queueType == QueueType.Modern) Color.Transparent else colorPalette().background2,
            contentColor = if (queueType == QueueType.Modern) Color.Transparent else colorPalette().background2,
            modifier = Modifier
                .fillMaxWidth()
                .hazeChild(state = hazeState),
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = {
                Surface(
                    modifier = Modifier.padding(vertical = 0.dp),
                    color = colorPalette().background0,
                    shape = thumbnailShape()
                ) {}
            },
            shape = thumbnailRoundness.shape()
        ) {
            QueueModern(
                navController = navController,
                onDismiss = {
                    queueLoopType = it
                    showQueue = false
                },
            )
        }

        CustomModalBottomSheet(
            showSheet = showFullLyrics,
            onDismissRequest = { showFullLyrics = false },
            containerColor = colorPalette().background2,
            contentColor = colorPalette().background2,
            modifier = Modifier.fillMaxWidth(),
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = {
                Surface(
                    modifier = Modifier.padding(vertical = 0.dp),
                    color = colorPalette().background0,
                    shape = thumbnailShape()
                ) {}
            },
            shape = thumbnailRoundness.shape()
        ) {
            FullLyricsSheetModern(
                onMaximize = { showFullLyrics = false },
                onRefresh = {}
            )
        }

        CustomModalBottomSheet(
            showSheet = showSearchEntity,
            onDismissRequest = { showSearchEntity = false },
            containerColor = if (playerType == PlayerType.Modern) Color.Transparent else colorPalette().background2,
            contentColor = if (playerType == PlayerType.Modern) Color.Transparent else colorPalette().background2,
            modifier = Modifier
                .fillMaxWidth()
                .hazeChild(state = hazeState),
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = {
                Surface(
                    modifier = Modifier.padding(vertical = 0.dp),
                    color = colorPalette().background0,
                    shape = thumbnailShape()
                ) {}
            },
            shape = thumbnailRoundness.shape()
        ) {
            SearchYoutubeEntity(
                navController = navController,
                onDismiss = { showSearchEntity = false },
                query = "${mediaItem.mediaMetadata.artist.toString()} - ${mediaItem.mediaMetadata.title.toString()}",
                disableScrollingText = disableScrollingText
            )
        }

    }

}



