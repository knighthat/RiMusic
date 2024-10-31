package it.fast4x.rimusic.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.BackgroundProgress
import it.fast4x.rimusic.enums.CarouselSize
import it.fast4x.rimusic.enums.IconLikeType
import it.fast4x.rimusic.enums.MiniPlayerType
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.PlayerBackgroundColors
import it.fast4x.rimusic.enums.PlayerControlsType
import it.fast4x.rimusic.enums.PlayerInfoType
import it.fast4x.rimusic.enums.PlayerPlayButtonType
import it.fast4x.rimusic.enums.PlayerThumbnailSize
import it.fast4x.rimusic.enums.PlayerTimelineSize
import it.fast4x.rimusic.enums.PlayerTimelineType
import it.fast4x.rimusic.enums.PlayerType
import it.fast4x.rimusic.enums.PrevNextSongs
import it.fast4x.rimusic.enums.QueueType
import it.fast4x.rimusic.enums.SongsNumber
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.enums.ThumbnailType
import it.fast4x.rimusic.ui.components.themed.HeaderWithIcon
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.utils.actionExpandedKey
import it.fast4x.rimusic.utils.actionspacedevenlyKey
import it.fast4x.rimusic.utils.backgroundProgressKey
import it.fast4x.rimusic.utils.blackgradientKey
import it.fast4x.rimusic.utils.bottomgradientKey
import it.fast4x.rimusic.utils.buttonzoomoutKey
import it.fast4x.rimusic.utils.carouselKey
import it.fast4x.rimusic.utils.carouselSizeKey
import it.fast4x.rimusic.utils.clickOnLyricsTextKey
import it.fast4x.rimusic.utils.controlsExpandedKey
import it.fast4x.rimusic.utils.disablePlayerHorizontalSwipeKey
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.effectRotationKey
import it.fast4x.rimusic.utils.expandedlyricsKey
import it.fast4x.rimusic.utils.expandedplayertoggleKey
import it.fast4x.rimusic.utils.fadingedgeKey
import it.fast4x.rimusic.utils.iconLikeTypeKey
import it.fast4x.rimusic.utils.isAtLeastAndroid13
import it.fast4x.rimusic.utils.isLandscape
import it.fast4x.rimusic.utils.isShowingThumbnailInLockscreenKey
import it.fast4x.rimusic.utils.keepPlayerMinimizedKey
import it.fast4x.rimusic.utils.lastPlayerPlayButtonTypeKey
import it.fast4x.rimusic.utils.miniPlayerTypeKey
import it.fast4x.rimusic.utils.miniQueueExpandedKey
import it.fast4x.rimusic.utils.navigationBarPositionKey
import it.fast4x.rimusic.utils.noblurKey
import it.fast4x.rimusic.utils.playerBackgroundColorsKey
import it.fast4x.rimusic.utils.playerControlsTypeKey
import it.fast4x.rimusic.utils.playerEnableLyricsPopupMessageKey
import it.fast4x.rimusic.utils.playerInfoShowIconsKey
import it.fast4x.rimusic.utils.playerInfoTypeKey
import it.fast4x.rimusic.utils.playerPlayButtonTypeKey
import it.fast4x.rimusic.utils.playerSwapControlsWithTimelineKey
import it.fast4x.rimusic.utils.playerThumbnailSizeKey
import it.fast4x.rimusic.utils.playerTimelineSizeKey
import it.fast4x.rimusic.utils.playerTimelineTypeKey
import it.fast4x.rimusic.utils.playerTypeKey
import it.fast4x.rimusic.utils.prevNextSongsKey
import it.fast4x.rimusic.utils.queueDurationExpandedKey
import it.fast4x.rimusic.utils.queueTypeKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.showBackgroundLyricsKey
import it.fast4x.rimusic.utils.showButtonPlayerAddToPlaylistKey
import it.fast4x.rimusic.utils.showButtonPlayerArrowKey
import it.fast4x.rimusic.utils.showButtonPlayerDiscoverKey
import it.fast4x.rimusic.utils.showButtonPlayerDownloadKey
import it.fast4x.rimusic.utils.showButtonPlayerLoopKey
import it.fast4x.rimusic.utils.showButtonPlayerLyricsKey
import it.fast4x.rimusic.utils.showButtonPlayerMenuKey
import it.fast4x.rimusic.utils.showButtonPlayerShuffleKey
import it.fast4x.rimusic.utils.showButtonPlayerSleepTimerKey
import it.fast4x.rimusic.utils.showButtonPlayerSystemEqualizerKey
import it.fast4x.rimusic.utils.showButtonPlayerVideoKey
import it.fast4x.rimusic.utils.showDownloadButtonBackgroundPlayerKey
import it.fast4x.rimusic.utils.showLikeButtonBackgroundPlayerKey
import it.fast4x.rimusic.utils.showNextSongsInPlayerKey
import it.fast4x.rimusic.utils.showRemainingSongTimeKey
import it.fast4x.rimusic.utils.showTopActionsBarKey
import it.fast4x.rimusic.utils.showTotalTimeQueueKey
import it.fast4x.rimusic.utils.showalbumcoverKey
import it.fast4x.rimusic.utils.showlyricsthumbnailKey
import it.fast4x.rimusic.utils.showsongsKey
import it.fast4x.rimusic.utils.showthumbnailKey
import it.fast4x.rimusic.utils.showvisthumbnailKey
import it.fast4x.rimusic.utils.statsExpandedKey
import it.fast4x.rimusic.utils.statsfornerdsKey
import it.fast4x.rimusic.utils.swipeUpQueueKey
import it.fast4x.rimusic.utils.tapqueueKey
import it.fast4x.rimusic.utils.textoutlineKey
import it.fast4x.rimusic.utils.thumbnailRoundnessKey
import it.fast4x.rimusic.utils.thumbnailTapEnabledKey
import it.fast4x.rimusic.utils.thumbnailTypeKey
import it.fast4x.rimusic.utils.thumbnailpauseKey
import it.fast4x.rimusic.utils.timelineExpandedKey
import it.fast4x.rimusic.utils.titleExpandedKey
import it.fast4x.rimusic.utils.transparentBackgroundPlayerActionBarKey
import it.fast4x.rimusic.utils.transparentbarKey
import it.fast4x.rimusic.utils.visualizerEnabledKey
import me.knighthat.colorPalette
import me.knighthat.component.tab.toolbar.Search

@Composable
fun DefaultAppearanceSettings() {
    var isShowingThumbnailInLockscreen by rememberPreference(
        isShowingThumbnailInLockscreenKey,
        true
    )
    isShowingThumbnailInLockscreen = true
    var showthumbnail by rememberPreference(showthumbnailKey, false)
    showthumbnail = false
    var transparentbar by rememberPreference(transparentbarKey, true)
    transparentbar = true
    var blackgradient by rememberPreference(blackgradientKey, false)
    blackgradient = false
    var showlyricsthumbnail by rememberPreference(showlyricsthumbnailKey, false)
    showlyricsthumbnail = false
    var playerPlayButtonType by rememberPreference(
        playerPlayButtonTypeKey,
        PlayerPlayButtonType.Rectangular
    )
    playerPlayButtonType = PlayerPlayButtonType.Rectangular
    var bottomgradient by rememberPreference(bottomgradientKey, false)
    bottomgradient = false
    var textoutline by rememberPreference(textoutlineKey, false)
    textoutline = false
    var lastPlayerPlayButtonType by rememberPreference(
        lastPlayerPlayButtonTypeKey,
        PlayerPlayButtonType.Rectangular
    )
    lastPlayerPlayButtonType = PlayerPlayButtonType.Rectangular
    var disablePlayerHorizontalSwipe by rememberPreference(disablePlayerHorizontalSwipeKey, false)
    disablePlayerHorizontalSwipe = false
    var disableScrollingText by rememberPreference(disableScrollingTextKey, false)
    disableScrollingText = false
    var showLikeButtonBackgroundPlayer by rememberPreference(
        showLikeButtonBackgroundPlayerKey,
        true
    )
    showLikeButtonBackgroundPlayer = true
    var showDownloadButtonBackgroundPlayer by rememberPreference(
        showDownloadButtonBackgroundPlayerKey,
        true
    )
    showDownloadButtonBackgroundPlayer = true
    var visualizerEnabled by rememberPreference(visualizerEnabledKey, false)
    visualizerEnabled = false
    var playerTimelineType by rememberPreference(playerTimelineTypeKey, PlayerTimelineType.Default)
    playerTimelineType = PlayerTimelineType.Default
    var playerThumbnailSize by rememberPreference(
        playerThumbnailSizeKey,
        PlayerThumbnailSize.Biggest
    )
    playerThumbnailSize = PlayerThumbnailSize.Biggest
    var playerTimelineSize by rememberPreference(
        playerTimelineSizeKey,
        PlayerTimelineSize.Biggest
    )
    playerTimelineSize = PlayerTimelineSize.Biggest
    var effectRotationEnabled by rememberPreference(effectRotationKey, true)
    effectRotationEnabled = true
    var thumbnailTapEnabled by rememberPreference(thumbnailTapEnabledKey, false)
    thumbnailTapEnabled = false
    var showButtonPlayerAddToPlaylist by rememberPreference(showButtonPlayerAddToPlaylistKey, true)
    showButtonPlayerAddToPlaylist = true
    var showButtonPlayerArrow by rememberPreference(showButtonPlayerArrowKey, false)
    showButtonPlayerArrow = false
    var showButtonPlayerDownload by rememberPreference(showButtonPlayerDownloadKey, true)
    showButtonPlayerDownload = true
    var showButtonPlayerLoop by rememberPreference(showButtonPlayerLoopKey, true)
    showButtonPlayerLoop = true
    var showButtonPlayerLyrics by rememberPreference(showButtonPlayerLyricsKey, true)
    showButtonPlayerLyrics = true
    var expandedplayertoggle by rememberPreference(expandedplayertoggleKey, true)
    expandedplayertoggle = true
    var showButtonPlayerShuffle by rememberPreference(showButtonPlayerShuffleKey, true)
    showButtonPlayerShuffle = true
    var showButtonPlayerSleepTimer by rememberPreference(showButtonPlayerSleepTimerKey, false)
    showButtonPlayerSleepTimer = false
    var showButtonPlayerMenu by rememberPreference(showButtonPlayerMenuKey, false)
    showButtonPlayerMenu = false
    var showButtonPlayerSystemEqualizer by rememberPreference(
        showButtonPlayerSystemEqualizerKey,
        false
    )
    showButtonPlayerSystemEqualizer = false
    var showButtonPlayerDiscover by rememberPreference(showButtonPlayerDiscoverKey, false)
    showButtonPlayerDiscover = false
    var showButtonPlayerVideo by rememberPreference(showButtonPlayerVideoKey, false)
    showButtonPlayerVideo = false
    var navigationBarPosition by rememberPreference(
        navigationBarPositionKey,
        NavigationBarPosition.Bottom
    )
    navigationBarPosition = NavigationBarPosition.Bottom
    var showTotalTimeQueue by rememberPreference(showTotalTimeQueueKey, true)
    showTotalTimeQueue = true
    var backgroundProgress by rememberPreference(
        backgroundProgressKey,
        BackgroundProgress.MiniPlayer
    )
    backgroundProgress = BackgroundProgress.MiniPlayer
    var showNextSongsInPlayer by rememberPreference(showNextSongsInPlayerKey, false)
    showNextSongsInPlayer = false
    var showRemainingSongTime by rememberPreference(showRemainingSongTimeKey, true)
    showRemainingSongTime = true
    var clickLyricsText by rememberPreference(clickOnLyricsTextKey, true)
    clickLyricsText = true
    var showBackgroundLyrics by rememberPreference(showBackgroundLyricsKey, false)
    showBackgroundLyrics = false
    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )
    thumbnailRoundness = ThumbnailRoundness.Heavy
    var miniPlayerType by rememberPreference(
        miniPlayerTypeKey,
        MiniPlayerType.Modern
    )
    miniPlayerType = MiniPlayerType.Modern
    var playerBackgroundColors by rememberPreference(
        playerBackgroundColorsKey,
        PlayerBackgroundColors.BlurredCoverColor
    )
    playerBackgroundColors = PlayerBackgroundColors.BlurredCoverColor
    var showTopActionsBar by rememberPreference(showTopActionsBarKey, true)
    showTopActionsBar = true
    var playerControlsType by rememberPreference(playerControlsTypeKey, PlayerControlsType.Modern)
    playerControlsType = PlayerControlsType.Modern
    var playerInfoType by rememberPreference(playerInfoTypeKey, PlayerInfoType.Modern)
    playerInfoType = PlayerInfoType.Modern
    var transparentBackgroundActionBarPlayer by rememberPreference(
        transparentBackgroundPlayerActionBarKey,
        false
    )
    transparentBackgroundActionBarPlayer = false
    var iconLikeType by rememberPreference(iconLikeTypeKey, IconLikeType.Essential)
    iconLikeType = IconLikeType.Essential
    var playerSwapControlsWithTimeline by rememberPreference(
        playerSwapControlsWithTimelineKey,
        false
    )
    playerSwapControlsWithTimeline = false
    var playerEnableLyricsPopupMessage by rememberPreference(
        playerEnableLyricsPopupMessageKey,
        true
    )
    playerEnableLyricsPopupMessage = true
    var actionspacedevenly by rememberPreference(actionspacedevenlyKey, false)
    actionspacedevenly = false
    var thumbnailType by rememberPreference(thumbnailTypeKey, ThumbnailType.Modern)
    thumbnailType = ThumbnailType.Modern
    var showvisthumbnail by rememberPreference(showvisthumbnailKey, false)
    showvisthumbnail = false
    var expandedlyrics by rememberPreference(expandedlyricsKey, true)
    expandedlyrics = true
    var buttonzoomout by rememberPreference(buttonzoomoutKey, false)
    buttonzoomout = false
    var thumbnailpause by rememberPreference(thumbnailpauseKey, false)
    thumbnailpause = false
    var showsongs by rememberPreference(showsongsKey, SongsNumber.`2`)
    showsongs = SongsNumber.`2`
    var showalbumcover by rememberPreference(showalbumcoverKey, true)
    showalbumcover = true
    var prevNextSongs by rememberPreference(prevNextSongsKey, PrevNextSongs.twosongs)
    prevNextSongs = PrevNextSongs.twosongs
    var tapqueue by rememberPreference(tapqueueKey, true)
    tapqueue = true
    var swipeUpQueue by rememberPreference(swipeUpQueueKey, true)
    swipeUpQueue = true
    var statsfornerds by rememberPreference(statsfornerdsKey, false)
    statsfornerds = false
    var playerType by rememberPreference(playerTypeKey, PlayerType.Essential)
    playerType = PlayerType.Essential
    var queueType by rememberPreference(queueTypeKey, QueueType.Essential)
    queueType = QueueType.Essential
    var noblur by rememberPreference(noblurKey, true)
    noblur = true
    var fadingedge by rememberPreference(fadingedgeKey, false)
    fadingedge = false
    var carousel by rememberPreference(carouselKey, true)
    carousel = true
    var carouselSize by rememberPreference(carouselSizeKey, CarouselSize.Biggest)
    carouselSize = CarouselSize.Biggest
    var keepPlayerMinimized by rememberPreference(keepPlayerMinimizedKey,false)
    keepPlayerMinimized = false
    var playerInfoShowIcons by rememberPreference(playerInfoShowIconsKey, true)
    playerInfoShowIcons = true
}

@ExperimentalAnimationApi
@UnstableApi
@Composable
fun AppearanceSettings(
    navController: NavController,
) {

    var isShowingThumbnailInLockscreen by rememberPreference(
        isShowingThumbnailInLockscreenKey,
        true
    )

    var showthumbnail by rememberPreference(showthumbnailKey, false)
    var transparentbar by rememberPreference(transparentbarKey, true)
    var blackgradient by rememberPreference(blackgradientKey, false)
    var showlyricsthumbnail by rememberPreference(showlyricsthumbnailKey, false)
    var playerPlayButtonType by rememberPreference(
        playerPlayButtonTypeKey,
        PlayerPlayButtonType.Rectangular
    )
    var bottomgradient by rememberPreference(bottomgradientKey, false)
    var textoutline by rememberPreference(textoutlineKey, false)

    var lastPlayerPlayButtonType by rememberPreference(
        lastPlayerPlayButtonTypeKey,
        PlayerPlayButtonType.Rectangular
    )
    var disablePlayerHorizontalSwipe by rememberPreference(disablePlayerHorizontalSwipeKey, false)

    var disableScrollingText by rememberPreference(disableScrollingTextKey, false)
    var showLikeButtonBackgroundPlayer by rememberPreference(
        showLikeButtonBackgroundPlayerKey,
        true
    )
    var showDownloadButtonBackgroundPlayer by rememberPreference(
        showDownloadButtonBackgroundPlayerKey,
        true
    )
    var visualizerEnabled by rememberPreference(visualizerEnabledKey, false)
    /*
    var playerVisualizerType by rememberPreference(
        playerVisualizerTypeKey,
        PlayerVisualizerType.Disabled
    )
    */
    var playerTimelineType by rememberPreference(playerTimelineTypeKey, PlayerTimelineType.Default)
    var playerThumbnailSize by rememberPreference(
        playerThumbnailSizeKey,
        PlayerThumbnailSize.Biggest
    )
    var playerTimelineSize by rememberPreference(
        playerTimelineSizeKey,
        PlayerTimelineSize.Biggest
    )
    //

    var effectRotationEnabled by rememberPreference(effectRotationKey, true)

    var thumbnailTapEnabled by rememberPreference(thumbnailTapEnabledKey, false)


    var showButtonPlayerAddToPlaylist by rememberPreference(showButtonPlayerAddToPlaylistKey, true)
    var showButtonPlayerArrow by rememberPreference(showButtonPlayerArrowKey, false)
    var showButtonPlayerDownload by rememberPreference(showButtonPlayerDownloadKey, true)
    var showButtonPlayerLoop by rememberPreference(showButtonPlayerLoopKey, true)
    var showButtonPlayerLyrics by rememberPreference(showButtonPlayerLyricsKey, true)
    var expandedplayertoggle by rememberPreference(expandedplayertoggleKey, true)
    var showButtonPlayerShuffle by rememberPreference(showButtonPlayerShuffleKey, true)
    var showButtonPlayerSleepTimer by rememberPreference(showButtonPlayerSleepTimerKey, false)
    var showButtonPlayerMenu by rememberPreference(showButtonPlayerMenuKey, false)
    var showButtonPlayerSystemEqualizer by rememberPreference(
        showButtonPlayerSystemEqualizerKey,
        false
    )
    var showButtonPlayerDiscover by rememberPreference(showButtonPlayerDiscoverKey, false)
    var showButtonPlayerVideo by rememberPreference(showButtonPlayerVideoKey, false)

    val navigationBarPosition by rememberPreference(
        navigationBarPositionKey,
        NavigationBarPosition.Bottom
    )

    //var isGradientBackgroundEnabled by rememberPreference(isGradientBackgroundEnabledKey, false)
    var showTotalTimeQueue by rememberPreference(showTotalTimeQueueKey, true)
    var backgroundProgress by rememberPreference(
        backgroundProgressKey,
        BackgroundProgress.MiniPlayer
    )
    var showNextSongsInPlayer by rememberPreference(showNextSongsInPlayerKey, false)
    var showRemainingSongTime by rememberPreference(showRemainingSongTimeKey, true)
    var clickLyricsText by rememberPreference(clickOnLyricsTextKey, true)
    var showBackgroundLyrics by rememberPreference(showBackgroundLyricsKey, false)

    // Search states
    val visibleState = rememberSaveable { mutableStateOf( false ) }
    val focusState = rememberSaveable { mutableStateOf( false ) }
    val inputState = rememberSaveable { mutableStateOf( "" ) }

    val search = remember {
        object: Search{
            override val visibleState = visibleState
            override val focusState = focusState
            override val inputState = inputState
        }
    }

    // Search mutable
    val searchInput by search.inputState

    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    var miniPlayerType by rememberPreference(
        miniPlayerTypeKey,
        MiniPlayerType.Modern
    )
    var playerBackgroundColors by rememberPreference(
        playerBackgroundColorsKey,
        PlayerBackgroundColors.BlurredCoverColor
    )

    var showTopActionsBar by rememberPreference(showTopActionsBarKey, true)
    var playerControlsType by rememberPreference(playerControlsTypeKey, PlayerControlsType.Modern)
    var playerInfoType by rememberPreference(playerInfoTypeKey, PlayerInfoType.Modern)
    var transparentBackgroundActionBarPlayer by rememberPreference(
        transparentBackgroundPlayerActionBarKey,
        false
    )
    var iconLikeType by rememberPreference(iconLikeTypeKey, IconLikeType.Essential)
    var playerSwapControlsWithTimeline by rememberPreference(
        playerSwapControlsWithTimelineKey,
        false
    )
    var playerEnableLyricsPopupMessage by rememberPreference(
        playerEnableLyricsPopupMessageKey,
        true
    )
    var actionspacedevenly by rememberPreference(actionspacedevenlyKey, false)
    var thumbnailType by rememberPreference(thumbnailTypeKey, ThumbnailType.Modern)
    var showvisthumbnail by rememberPreference(showvisthumbnailKey, false)
    var expandedlyrics by rememberPreference(expandedlyricsKey, true)
    var buttonzoomout by rememberPreference(buttonzoomoutKey, false)
    var thumbnailpause by rememberPreference(thumbnailpauseKey, false)
    var showsongs by rememberPreference(showsongsKey, SongsNumber.`2`)
    var showalbumcover by rememberPreference(showalbumcoverKey, true)
    var prevNextSongs by rememberPreference(prevNextSongsKey, PrevNextSongs.twosongs)
    var tapqueue by rememberPreference(tapqueueKey, true)
    var swipeUpQueue by rememberPreference(swipeUpQueueKey, true)
    var statsfornerds by rememberPreference(statsfornerdsKey, false)

    var playerType by rememberPreference(playerTypeKey, PlayerType.Essential)
    var queueType by rememberPreference(queueTypeKey, QueueType.Essential)
    var noblur by rememberPreference(noblurKey, true)
    var fadingedge by rememberPreference(fadingedgeKey, false)
    var carousel by rememberPreference(carouselKey, true)
    var carouselSize by rememberPreference(carouselSizeKey, CarouselSize.Biggest)
    var keepPlayerMinimized by rememberPreference(keepPlayerMinimizedKey,false)
    var playerInfoShowIcons by rememberPreference(playerInfoShowIconsKey, true)
    var queueDurationExpanded by rememberPreference(queueDurationExpandedKey, true)
    var titleExpanded by rememberPreference(titleExpandedKey, true)
    var timelineExpanded by rememberPreference(timelineExpandedKey, true)
    var controlsExpanded by rememberPreference(controlsExpandedKey, true)
    var miniQueueExpanded by rememberPreference(miniQueueExpandedKey, true)
    var statsExpanded by rememberPreference(statsExpandedKey, true)
    var actionExpanded by rememberPreference(actionExpandedKey, true)

    Column(
        modifier = Modifier
            .background(colorPalette().background0)
            //.fillMaxSize()
            .fillMaxHeight()
            .fillMaxWidth(
                if (navigationBarPosition == NavigationBarPosition.Left ||
                    navigationBarPosition == NavigationBarPosition.Top ||
                    navigationBarPosition == NavigationBarPosition.Bottom
                ) 1f
                else Dimensions.contentWidthRightBar
            )
            .verticalScroll(rememberScrollState())
        /*
        .padding(
            LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                .asPaddingValues()
        )

         */
    ) {
        HeaderWithIcon(
            title = stringResource(R.string.player_appearance),
            iconId = R.drawable.color_palette,
            enabled = false,
            showIcon = true,
            modifier = Modifier,
            onClick = {}
        )

        search.ToolBarButton()
        search.SearchBar( this )

        //SettingsEntryGroupText(stringResource(R.string.user_interface))

        //SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.player))

        if (playerBackgroundColors != PlayerBackgroundColors.BlurredCoverColor)
            showthumbnail = true
        if (!visualizerEnabled) showvisthumbnail = false
        if (!showthumbnail) {showlyricsthumbnail = false; showvisthumbnail = false}
        if (playerType == PlayerType.Modern) {
            showlyricsthumbnail = false
            showvisthumbnail = false
            thumbnailpause = false
            //keepPlayerMinimized = false
        }

        if (showlyricsthumbnail) expandedlyrics = false

        if (searchInput.isBlank() || stringResource(R.string.show_player_top_actions_bar).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.show_player_top_actions_bar),
                text = "",
                isChecked = showTopActionsBar,
                onCheckedChange = { showTopActionsBar = it }
            )
        if (searchInput.isBlank() || stringResource(R.string.playertype).contains(
                searchInput,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.playertype),
                selectedValue = playerType,
                onValueSelected = {
                    playerType = it
                },
                valueText = {
                    when (it) {
                        PlayerType.Modern -> stringResource(R.string.pcontrols_modern)
                        PlayerType.Essential -> stringResource(R.string.pcontrols_essential)
                    }
                },
            )

        if (searchInput.isBlank() || stringResource(R.string.queuetype).contains(
                searchInput,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.queuetype),
                selectedValue = queueType,
                onValueSelected = {
                    queueType = it
                },
                valueText = {
                    when (it) {
                        QueueType.Modern -> stringResource(R.string.pcontrols_modern)
                        QueueType.Essential -> stringResource(R.string.pcontrols_essential)
                    }
                },
            )

        if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) {
            if (searchInput.isBlank() || stringResource(R.string.show_thumbnail).contains(
                    searchInput,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.show_thumbnail),
                    text = "",
                    isChecked = showthumbnail,
                    onCheckedChange = {showthumbnail = it},
                )
        }
        AnimatedVisibility(visible = showthumbnail) {
            Column {
                if (playerType == PlayerType.Modern) {
                    if (searchInput.isBlank() || stringResource(R.string.fadingedge).contains(
                            searchInput,
                            true
                        )
                    )
                        SwitchSettingEntry(
                            title = stringResource(R.string.fadingedge),
                            text = "",
                            isChecked = fadingedge,
                            onCheckedChange = { fadingedge = it },
                            modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                        )
                }

                if (playerType == PlayerType.Modern && !isLandscape) {
                    if (searchInput.isBlank() || stringResource(R.string.carousel).contains(
                            searchInput,
                            true
                        )
                    )
                        SwitchSettingEntry(
                            title = stringResource(R.string.carousel),
                            text = "",
                            isChecked = carousel,
                            onCheckedChange = { carousel = it },
                            modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                        )

                    if (searchInput.isBlank() || stringResource(R.string.carouselsize).contains(
                            searchInput,
                            true
                        )
                    )
                        EnumValueSelectorSettingsEntry(
                            title = stringResource(R.string.carouselsize),
                            selectedValue = carouselSize,
                            onValueSelected = { carouselSize = it },
                            valueText = {
                                when (it) {
                                    CarouselSize.Small -> stringResource(R.string.small)
                                    CarouselSize.Medium -> stringResource(R.string.medium)
                                    CarouselSize.Big -> stringResource(R.string.big)
                                    CarouselSize.Biggest -> stringResource(R.string.biggest)
                                    CarouselSize.Expanded -> stringResource(R.string.expanded)
                                }
                            },
                            modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                        )
                }
                if (playerType == PlayerType.Essential) {
                    if (searchInput.isBlank() || stringResource(R.string.thumbnailpause).contains(
                            searchInput,
                            true
                        )
                    )
                        SwitchSettingEntry(
                            title = stringResource(R.string.thumbnailpause),
                            text = "",
                            isChecked = thumbnailpause,
                            onCheckedChange = { thumbnailpause = it },
                            modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                        )

                    if (searchInput.isBlank() || stringResource(R.string.show_lyrics_thumbnail).contains(
                            searchInput,
                            true
                        )
                    )
                        SwitchSettingEntry(
                            title = stringResource(R.string.show_lyrics_thumbnail),
                            text = "",
                            isChecked = showlyricsthumbnail,
                            onCheckedChange = { showlyricsthumbnail = it },
                            modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                        )
                    if (visualizerEnabled) {
                        if (searchInput.isBlank() || stringResource(R.string.showvisthumbnail).contains(
                                searchInput,
                                true
                            )
                        )
                            SwitchSettingEntry(
                                title = stringResource(R.string.showvisthumbnail),
                                text = "",
                                isChecked = showvisthumbnail,
                                onCheckedChange = { showvisthumbnail = it },
                                modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                            )
                    }
                }

                if (searchInput.isBlank() || stringResource(R.string.player_thumbnail_size).contains(
                        searchInput,
                        true
                    )
                )
                    EnumValueSelectorSettingsEntry(
                        title = stringResource(R.string.player_thumbnail_size),
                        selectedValue = playerThumbnailSize,
                        onValueSelected = { playerThumbnailSize = it },
                        valueText = {
                            when (it) {
                                PlayerThumbnailSize.Small -> stringResource(R.string.small)
                                PlayerThumbnailSize.Medium -> stringResource(R.string.medium)
                                PlayerThumbnailSize.Big -> stringResource(R.string.big)
                                PlayerThumbnailSize.Biggest -> stringResource(R.string.biggest)
                                PlayerThumbnailSize.Expanded -> stringResource(R.string.expanded)
                            }
                        },
                        modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                    )
                if (searchInput.isBlank() || stringResource(R.string.thumbnailtype).contains(
                        searchInput,
                        true
                    )
                )
                    EnumValueSelectorSettingsEntry(
                        title = stringResource(R.string.thumbnailtype),
                        selectedValue = thumbnailType,
                        onValueSelected = {
                            thumbnailType = it
                        },
                        valueText = {
                            when (it) {
                                ThumbnailType.Modern -> stringResource(R.string.pcontrols_modern)
                                ThumbnailType.Essential -> stringResource(R.string.pcontrols_essential)
                            }
                        },
                        modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                    )

                if (searchInput.isBlank() || stringResource(R.string.thumbnail_roundness).contains(
                        searchInput,
                        true
                    )
                )
                    EnumValueSelectorSettingsEntry(
                        title = stringResource(R.string.thumbnail_roundness),
                        selectedValue = thumbnailRoundness,
                        onValueSelected = { thumbnailRoundness = it },
                        trailingContent = {
                            Spacer(
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = colorPalette().accent,
                                        shape = thumbnailRoundness.shape()
                                    )
                                    .background(
                                        color = colorPalette().background1,
                                        shape = thumbnailRoundness.shape()
                                    )
                                    .size(36.dp)
                            )
                        },
                        valueText = {
                            when (it) {
                                ThumbnailRoundness.None -> stringResource(R.string.none)
                                ThumbnailRoundness.Light -> stringResource(R.string.light)
                                ThumbnailRoundness.Heavy -> stringResource(R.string.heavy)
                                ThumbnailRoundness.Medium -> stringResource(R.string.medium)
                            }
                        },
                        modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                    )
            }
        }
        if (!showthumbnail) {
            if (searchInput.isBlank() || stringResource(R.string.noblur).contains(
                    searchInput,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.noblur),
                    text = "",
                    isChecked = noblur,
                    onCheckedChange = { noblur = it }
                )


        }

        if (!(showthumbnail && playerType == PlayerType.Essential)){
            if (searchInput.isBlank() || stringResource(R.string.statsfornerdsplayer).contains(
                    searchInput,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.statsfornerdsplayer),
                    text = "",
                    isChecked = statsfornerds,
                    onCheckedChange = { statsfornerds = it }
                )
        }

        if (!showlyricsthumbnail && !isLandscape)
            if (searchInput.isBlank() || stringResource(R.string.expandedlyrics).contains(
                    searchInput,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.expandedlyrics),
                    text = stringResource(R.string.expandedlyricsinfo),
                    isChecked = expandedlyrics,
                    onCheckedChange = { expandedlyrics = it }
                )

        if (searchInput.isBlank() || stringResource(R.string.timelinesize).contains(
                searchInput,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.timelinesize),
                selectedValue = playerTimelineSize,
                onValueSelected = { playerTimelineSize = it },
                valueText = {
                    when (it) {
                        PlayerTimelineSize.Small -> stringResource(R.string.small)
                        PlayerTimelineSize.Medium -> stringResource(R.string.medium)
                        PlayerTimelineSize.Big -> stringResource(R.string.big)
                        PlayerTimelineSize.Biggest -> stringResource(R.string.biggest)
                        PlayerTimelineSize.Expanded -> stringResource(R.string.expanded)
                    }
                }
            )

        if (searchInput.isBlank() || stringResource(R.string.pinfo_type).contains(
                searchInput,
                true
            )
        ) {
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.pinfo_type),
                selectedValue = playerInfoType,
                onValueSelected = {
                    playerInfoType = it
                },
                valueText = {
                    when (it) {
                        PlayerInfoType.Modern -> stringResource(R.string.pcontrols_modern)
                        PlayerInfoType.Essential -> stringResource(R.string.pcontrols_essential)
                    }
                },
            )
            SettingsDescription(text = stringResource(R.string.pinfo_album_and_artist_name))

            AnimatedVisibility( visible = playerInfoType == PlayerInfoType.Modern) {
                Column {
                    if (searchInput.isBlank() || stringResource(R.string.pinfo_show_icons).contains(
                            searchInput,
                            true
                        )
                    )
                        SwitchSettingEntry(
                            title = stringResource(R.string.pinfo_show_icons),
                            text = "",
                            isChecked = playerInfoShowIcons,
                            onCheckedChange = { playerInfoShowIcons = it },
                            modifier = Modifier
                                .padding(start = 25.dp)
                        )
                }
            }

        }



        if (searchInput.isBlank() || stringResource(R.string.miniplayertype).contains(
                searchInput,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.miniplayertype),
                selectedValue = miniPlayerType,
                onValueSelected = {
                    miniPlayerType = it
                },
                valueText = {
                    when (it) {
                        MiniPlayerType.Modern -> stringResource(R.string.pcontrols_modern)
                        MiniPlayerType.Essential -> stringResource(R.string.pcontrols_essential)
                    }
                },
            )

        if (searchInput.isBlank() || stringResource(R.string.player_swap_controls_with_timeline).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.player_swap_controls_with_timeline),
                text = "",
                isChecked = playerSwapControlsWithTimeline,
                onCheckedChange = { playerSwapControlsWithTimeline = it }
            )

        if (searchInput.isBlank() || stringResource(R.string.timeline).contains(
                searchInput,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.timeline),
                selectedValue = playerTimelineType,
                onValueSelected = { playerTimelineType = it },
                valueText = {
                    when (it) {
                        PlayerTimelineType.Default -> stringResource(R.string._default)
                        PlayerTimelineType.Wavy -> stringResource(R.string.wavy_timeline)
                        PlayerTimelineType.BodiedBar -> stringResource(R.string.bodied_bar)
                        PlayerTimelineType.PinBar -> stringResource(R.string.pin_bar)
                        PlayerTimelineType.FakeAudioBar -> stringResource(R.string.fake_audio_bar)
                        PlayerTimelineType.ThinBar -> stringResource(R.string.thin_bar)
                        //PlayerTimelineType.ColoredBar -> "Colored bar"
                    }
                }
            )

        if (searchInput.isBlank() || stringResource(R.string.transparentbar).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.transparentbar),
                text = "",
                isChecked = transparentbar,
                onCheckedChange = { transparentbar = it }
            )

        if (searchInput.isBlank() || stringResource(R.string.pcontrols_type).contains(
                searchInput,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.pcontrols_type),
                selectedValue = playerControlsType,
                onValueSelected = {
                    playerControlsType = it
                },
                valueText = {
                    when (it) {
                        PlayerControlsType.Modern -> stringResource(R.string.pcontrols_modern)
                        PlayerControlsType.Essential -> stringResource(R.string.pcontrols_essential)
                    }
                },
            )


        if (searchInput.isBlank() || stringResource(R.string.play_button).contains(
                searchInput,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.play_button),
                selectedValue = playerPlayButtonType,
                onValueSelected = {
                    playerPlayButtonType = it
                    lastPlayerPlayButtonType = it
                },
                valueText = {
                    when (it) {
                        PlayerPlayButtonType.Disabled -> stringResource(R.string.vt_disabled)
                        PlayerPlayButtonType.Default -> stringResource(R.string._default)
                        PlayerPlayButtonType.Rectangular -> stringResource(R.string.rectangular)
                        PlayerPlayButtonType.Square -> stringResource(R.string.square)
                        PlayerPlayButtonType.CircularRibbed -> stringResource(R.string.circular_ribbed)
                    }
                },
            )

        if (searchInput.isBlank() || stringResource(R.string.buttonzoomout).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.buttonzoomout),
                text = "",
                isChecked = buttonzoomout,
                onCheckedChange = { buttonzoomout = it }
            )


        if (searchInput.isBlank() || stringResource(R.string.play_button).contains(
                searchInput,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.icon_like_button),
                selectedValue = iconLikeType,
                onValueSelected = {
                    iconLikeType = it
                },
                valueText = {
                    when (it) {
                        IconLikeType.Essential -> stringResource(R.string.pcontrols_essential)
                        IconLikeType.Apple -> stringResource(R.string.icon_like_apple)
                        IconLikeType.Breaked -> stringResource(R.string.icon_like_breaked)
                        IconLikeType.Gift -> stringResource(R.string.icon_like_gift)
                        IconLikeType.Shape -> stringResource(R.string.icon_like_shape)
                        IconLikeType.Striped -> stringResource(R.string.icon_like_striped)
                        IconLikeType.Brilliant -> stringResource(R.string.icon_like_brilliant)
                    }
                },
            )

        /*

        if (filter.isNullOrBlank() || stringResource(R.string.use_gradient_background).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.use_gradient_background),
                text = "",
                isChecked = isGradientBackgroundEnabled,
                onCheckedChange = { isGradientBackgroundEnabled = it }
            )
         */

        if (searchInput.isBlank() || stringResource(R.string.background_colors).contains(
                searchInput,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.background_colors),
                selectedValue = playerBackgroundColors,
                onValueSelected = {
                    playerBackgroundColors = it
                },
                valueText = {
                    when (it) {
                        PlayerBackgroundColors.CoverColor -> stringResource(R.string.bg_colors_background_from_cover)
                        PlayerBackgroundColors.ThemeColor -> stringResource(R.string.bg_colors_background_from_theme)
                        PlayerBackgroundColors.CoverColorGradient -> stringResource(R.string.bg_colors_gradient_background_from_cover)
                        PlayerBackgroundColors.ThemeColorGradient -> stringResource(R.string.bg_colors_gradient_background_from_theme)
                        PlayerBackgroundColors.FluidThemeColorGradient -> stringResource(R.string.bg_colors_fluid_gradient_background_from_theme)
                        PlayerBackgroundColors.FluidCoverColorGradient -> stringResource(R.string.bg_colors_fluid_gradient_background_from_cover)
                        PlayerBackgroundColors.BlurredCoverColor -> stringResource(R.string.bg_colors_blurred_cover_background)
                    }
                },
            )

        if ((playerBackgroundColors == PlayerBackgroundColors.CoverColorGradient) || (playerBackgroundColors == PlayerBackgroundColors.ThemeColorGradient))
            if (searchInput.isBlank() || stringResource(R.string.blackgradient).contains(
                    searchInput,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.blackgradient),
                    text = "",
                    isChecked = blackgradient,
                    onCheckedChange = { blackgradient = it }
                )
        if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor)
            if (searchInput.isBlank() || stringResource(R.string.bottomgradient).contains(
                    searchInput,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.bottomgradient),
                    text = "",
                    isChecked = bottomgradient,
                    onCheckedChange = { bottomgradient = it }
                )
        if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor)
           if (searchInput.isBlank() || stringResource(R.string.textoutline).contains(
                searchInput,
                true
                )
           )
               SwitchSettingEntry(
                   title = stringResource(R.string.textoutline),
                   text = "",
                   isChecked = textoutline,
                   onCheckedChange = { textoutline = it }
               )
       if (searchInput.isBlank() || stringResource(R.string.show_total_time_of_queue).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.show_total_time_of_queue),
                text = "",
                isChecked = showTotalTimeQueue,
                onCheckedChange = { showTotalTimeQueue = it }
            )

        if (searchInput.isBlank() || stringResource(R.string.show_remaining_song_time).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.show_remaining_song_time),
                text = "",
                isChecked = showRemainingSongTime,
                onCheckedChange = { showRemainingSongTime = it }
            )

        if (searchInput.isBlank() || stringResource(R.string.show_next_songs_in_player).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.show_next_songs_in_player),
                text = "",
                isChecked = showNextSongsInPlayer,
                onCheckedChange = { showNextSongsInPlayer = it }
            )
        AnimatedVisibility( visible = showNextSongsInPlayer) {
          Column {
              if (searchInput.isBlank() || stringResource(R.string.showtwosongs).contains(searchInput,true))
                  EnumValueSelectorSettingsEntry(
                      title = stringResource(R.string.songs_number_to_show),
                      selectedValue = showsongs,
                      onValueSelected = {
                          showsongs = it
                      },
                      valueText = {
                          it.name
                      },
                      modifier = Modifier
                          .padding(start = 25.dp)
                  )


            if (searchInput.isBlank() || stringResource(R.string.showalbumcover).contains(
                    searchInput,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.showalbumcover),
                    text = "",
                    isChecked = showalbumcover,
                    onCheckedChange = { showalbumcover = it },
                      modifier = Modifier.padding(start = 25.dp)
                  )
          }
        }

        if (searchInput.isBlank() || stringResource(R.string.disable_scrolling_text).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.disable_scrolling_text),
                text = stringResource(R.string.scrolling_text_is_used_for_long_texts),
                isChecked = disableScrollingText,
                onCheckedChange = { disableScrollingText = it }
            )
        if (playerType == PlayerType.Essential) {
            if (searchInput.isBlank() || stringResource(R.string.disable_horizontal_swipe).contains(
                    searchInput,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.disable_horizontal_swipe),
                    text = stringResource(R.string.disable_song_switching_via_swipe),
                    isChecked = disablePlayerHorizontalSwipe,
                    onCheckedChange = { disablePlayerHorizontalSwipe = it }
                )
        }

        if (searchInput.isBlank() || stringResource(R.string.player_rotating_buttons).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.player_rotating_buttons),
                text = stringResource(R.string.player_enable_rotation_buttons),
                isChecked = effectRotationEnabled,
                onCheckedChange = { effectRotationEnabled = it }
            )

        if (searchInput.isBlank() || stringResource(R.string.toggle_lyrics).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.toggle_lyrics),
                text = stringResource(R.string.by_tapping_on_the_thumbnail),
                isChecked = thumbnailTapEnabled,
                onCheckedChange = { thumbnailTapEnabled = it }
            )

        if (searchInput.isBlank() || stringResource(R.string.click_lyrics_text).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.click_lyrics_text),
                text = "",
                isChecked = clickLyricsText,
                onCheckedChange = { clickLyricsText = it }
            )
        if (showlyricsthumbnail)
            if (searchInput.isBlank() || stringResource(R.string.show_background_in_lyrics).contains(
                    searchInput,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.show_background_in_lyrics),
                    text = "",
                    isChecked = showBackgroundLyrics,
                    onCheckedChange = { showBackgroundLyrics = it }
                )

        if (searchInput.isBlank() || stringResource(R.string.player_enable_lyrics_popup_message).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.player_enable_lyrics_popup_message),
                text = "",
                isChecked = playerEnableLyricsPopupMessage,
                onCheckedChange = { playerEnableLyricsPopupMessage = it }
            )

        if (searchInput.isBlank() || stringResource(R.string.background_progress_bar).contains(
                searchInput,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.background_progress_bar),
                selectedValue = backgroundProgress,
                onValueSelected = {
                    backgroundProgress = it
                },
                valueText = {
                    when (it) {
                        BackgroundProgress.Player -> stringResource(R.string.player)
                        BackgroundProgress.MiniPlayer -> stringResource(R.string.minimized_player)
                        BackgroundProgress.Both -> stringResource(R.string.both)
                        BackgroundProgress.Disabled -> stringResource(R.string.vt_disabled)
                    }
                },
            )


        if (searchInput.isBlank() || stringResource(R.string.visualizer).contains(
                searchInput,
                true
            )
        ) {
            SwitchSettingEntry(
                title = stringResource(R.string.visualizer),
                text = "",
                isChecked = visualizerEnabled,
                onCheckedChange = { visualizerEnabled = it }
            )
            /*
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.visualizer),
                selectedValue = playerVisualizerType,
                onValueSelected = { playerVisualizerType = it },
                valueText = {
                    when (it) {
                        PlayerVisualizerType.Fancy -> stringResource(R.string.vt_fancy)
                        PlayerVisualizerType.Circular -> stringResource(R.string.vt_circular)
                        PlayerVisualizerType.Disabled -> stringResource(R.string.vt_disabled)
                        PlayerVisualizerType.Stacked -> stringResource(R.string.vt_stacked)
                        PlayerVisualizerType.Oneside -> stringResource(R.string.vt_one_side)
                        PlayerVisualizerType.Doubleside -> stringResource(R.string.vt_double_side)
                        PlayerVisualizerType.DoublesideCircular -> stringResource(R.string.vt_double_side_circular)
                        PlayerVisualizerType.Full -> stringResource(R.string.vt_full)
                    }
                }
            )
            */
            ImportantSettingsDescription(text = stringResource(R.string.visualizer_require_mic_permission))
        }

        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.player_action_bar))

        if (searchInput.isBlank() || stringResource(R.string.action_bar_transparent_background).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_transparent_background),
                text = "",
                isChecked = transparentBackgroundActionBarPlayer,
                onCheckedChange = { transparentBackgroundActionBarPlayer = it }
            )

        if (searchInput.isBlank() || stringResource(R.string.actionspacedevenly).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.actionspacedevenly),
                text = "",
                isChecked = actionspacedevenly,
                onCheckedChange = { actionspacedevenly = it }
            )

        if (searchInput.isBlank() || stringResource(R.string.tapqueue).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.tapqueue),
                text = "",
                isChecked = tapqueue,
                onCheckedChange = { tapqueue = it }
            )

        if (searchInput.isBlank() || stringResource(R.string.swipe_up_to_open_the_queue).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.swipe_up_to_open_the_queue),
                text = "",
                isChecked = swipeUpQueue,
                onCheckedChange = { swipeUpQueue = it }
            )

        if (searchInput.isBlank() || stringResource(R.string.action_bar_show_video_button).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_video_button),
                text = "",
                isChecked = showButtonPlayerVideo,
                onCheckedChange = { showButtonPlayerVideo = it }
            )

        if (searchInput.isBlank() || stringResource(R.string.action_bar_show_discover_button).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_discover_button),
                text = "",
                isChecked = showButtonPlayerDiscover,
                onCheckedChange = { showButtonPlayerDiscover = it }
            )

        if (searchInput.isBlank() || stringResource(R.string.action_bar_show_download_button).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_download_button),
                text = "",
                isChecked = showButtonPlayerDownload,
                onCheckedChange = { showButtonPlayerDownload = it }
            )

        if (searchInput.isBlank() || stringResource(R.string.action_bar_show_add_to_playlist_button).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_add_to_playlist_button),
                text = "",
                isChecked = showButtonPlayerAddToPlaylist,
                onCheckedChange = { showButtonPlayerAddToPlaylist = it }
            )

        if (searchInput.isBlank() || stringResource(R.string.action_bar_show_loop_button).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_loop_button),
                text = "",
                isChecked = showButtonPlayerLoop,
                onCheckedChange = { showButtonPlayerLoop = it }
            )

        if (searchInput.isBlank() || stringResource(R.string.action_bar_show_shuffle_button).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_shuffle_button),
                text = "",
                isChecked = showButtonPlayerShuffle,
                onCheckedChange = { showButtonPlayerShuffle = it }
            )

        if (searchInput.isBlank() || stringResource(R.string.action_bar_show_lyrics_button).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_lyrics_button),
                text = "",
                isChecked = showButtonPlayerLyrics,
                onCheckedChange = { showButtonPlayerLyrics = it }
            )
        if (!isLandscape || !showthumbnail) {
            if (!showlyricsthumbnail and !expandedlyrics) {
                if (searchInput.isBlank() || stringResource(R.string.expandedplayer).contains(
                        searchInput,
                        true
                    )
                )
                    SwitchSettingEntry(
                        title = stringResource(R.string.expandedplayer),
                        text = "",
                        isChecked = expandedplayertoggle,
                        onCheckedChange = { expandedplayertoggle = it }
                    )
            }
        }

        if (searchInput.isBlank() || stringResource(R.string.action_bar_show_sleep_timer_button).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_sleep_timer_button),
                text = "",
                isChecked = showButtonPlayerSleepTimer,
                onCheckedChange = { showButtonPlayerSleepTimer = it }
            )

        if (searchInput.isBlank() || stringResource(R.string.show_equalizer).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.show_equalizer),
                text = "",
                isChecked = showButtonPlayerSystemEqualizer,
                onCheckedChange = { showButtonPlayerSystemEqualizer = it }
            )

        if (searchInput.isBlank() || stringResource(R.string.action_bar_show_arrow_button_to_open_queue).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_arrow_button_to_open_queue),
                text = "",
                isChecked = showButtonPlayerArrow,
                onCheckedChange = { showButtonPlayerArrow = it }
            )

        if (searchInput.isBlank() || stringResource(R.string.action_bar_show_menu_button).contains(
                searchInput,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_menu_button),
                text = "",
                isChecked = showButtonPlayerMenu,
                onCheckedChange = { showButtonPlayerMenu = it }
            )

        if (!showlyricsthumbnail && (expandedplayertoggle || expandedlyrics)) {
            SettingsGroupSpacer()
            SettingsEntryGroupText(title = stringResource(R.string.full_screen_lyrics_components))

            if (showTotalTimeQueue) {
                if (searchInput.isBlank() || stringResource(R.string.show_total_time_of_queue).contains(
                        searchInput,
                        true
                    )
                )
                    SwitchSettingEntry(
                        title = stringResource(R.string.show_total_time_of_queue),
                        text = "",
                        isChecked = queueDurationExpanded,
                        onCheckedChange = { queueDurationExpanded = it }
                    )
            }

            if (searchInput.isBlank() || stringResource(R.string.titleartist).contains(
                    searchInput,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.titleartist),
                    text = "",
                    isChecked = titleExpanded,
                    onCheckedChange = { titleExpanded = it }
                )

            if (searchInput.isBlank() || stringResource(R.string.timeline).contains(
                    searchInput,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.timeline),
                    text = "",
                    isChecked = timelineExpanded,
                    onCheckedChange = { timelineExpanded = it }
                )

            if (searchInput.isBlank() || stringResource(R.string.controls).contains(
                    searchInput,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.controls),
                    text = "",
                    isChecked = controlsExpanded,
                    onCheckedChange = { controlsExpanded = it }
                )

            if (statsfornerds){
                if (searchInput.isBlank() || stringResource(R.string.statsfornerds).contains(
                        searchInput,
                        true
                    )
                )
                    SwitchSettingEntry(
                        title = stringResource(R.string.statsfornerds),
                        text = "",
                        isChecked = statsExpanded,
                        onCheckedChange = { statsExpanded = it }
                    )
            }

            if (showNextSongsInPlayer) {
                if (searchInput.isBlank() || stringResource(R.string.miniqueue).contains(
                        searchInput,
                        true
                    )
                )
                    SwitchSettingEntry(
                        title = stringResource(R.string.miniqueue),
                        text = "",
                        isChecked = miniQueueExpanded,
                        onCheckedChange = { miniQueueExpanded = it }
                    )
            }

            if (
                showButtonPlayerDownload ||
                showButtonPlayerAddToPlaylist ||
                showButtonPlayerLoop ||
                showButtonPlayerShuffle ||
                showButtonPlayerLyrics ||
                showButtonPlayerSleepTimer ||
                showButtonPlayerSystemEqualizer ||
                showButtonPlayerArrow ||
                showButtonPlayerMenu ||
                expandedplayertoggle ||
                showButtonPlayerDiscover ||
                showButtonPlayerVideo
            ){
                if (searchInput.isBlank() || stringResource(R.string.actionbar).contains(
                        searchInput,
                        true
                    )
                )
                    SwitchSettingEntry(
                        title = stringResource(R.string.actionbar),
                        text = "",
                        isChecked = actionExpanded,
                        onCheckedChange = { actionExpanded = it }
                    )
            }

        }
        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.background_player))

        if (searchInput.isBlank() || stringResource(R.string.show_favorite_button).contains(
                searchInput,
                true
            )
        ) {
            SwitchSettingEntry(
                title = stringResource(R.string.show_favorite_button),
                text = stringResource(R.string.show_favorite_button_in_lock_screen_and_notification_area),
                isChecked = showLikeButtonBackgroundPlayer,
                onCheckedChange = { showLikeButtonBackgroundPlayer = it }
            )
            ImportantSettingsDescription(text = stringResource(R.string.restarting_rimusic_is_required))
        }
        if (searchInput.isBlank() || stringResource(R.string.show_download_button).contains(
                searchInput,
                true
            )
        ) {
            SwitchSettingEntry(
                title = stringResource(R.string.show_download_button),
                text = stringResource(R.string.show_download_button_in_lock_screen_and_notification_area),
                isChecked = showDownloadButtonBackgroundPlayer,
                onCheckedChange = { showDownloadButtonBackgroundPlayer = it }
            )

            ImportantSettingsDescription(text = stringResource(R.string.restarting_rimusic_is_required))
        }

        //SettingsGroupSpacer()
        //SettingsEntryGroupText(title = stringResource(R.string.text))


        if (searchInput.isBlank() || stringResource(R.string.show_song_cover).contains(
                searchInput,
                true
            )
        )
            if (!isAtLeastAndroid13) {
                SettingsGroupSpacer()

                SettingsEntryGroupText(title = stringResource(R.string.lockscreen))

                SwitchSettingEntry(
                    title = stringResource(R.string.show_song_cover),
                    text = stringResource(R.string.use_song_cover_on_lockscreen),
                    isChecked = isShowingThumbnailInLockscreen,
                    onCheckedChange = { isShowingThumbnailInLockscreen = it }
                )
            }

        var resetToDefault by remember { mutableStateOf(false) }
        val context = LocalContext.current
        ButtonBarSettingEntry(
            title = stringResource(R.string.settings_reset),
            text = stringResource(R.string.settings_restore_default_settings),
            icon = R.drawable.refresh,
            iconColor = colorPalette().text,
            onClick = { resetToDefault = true },
        )
        if (resetToDefault) {
            DefaultAppearanceSettings()
            resetToDefault = false
            navController.popBackStack()
            SmartMessage(stringResource(R.string.done), context = context)
        }

        SettingsGroupSpacer(
            modifier = Modifier.height(Dimensions.bottomSpacer)
        )
    }
}
