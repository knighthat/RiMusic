package it.fast4x.rimusic.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import it.fast4x.compose.persist.persistList
import it.fast4x.rimusic.*
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.*
import it.fast4x.rimusic.models.*
import it.fast4x.rimusic.ui.components.ButtonsRow
import it.fast4x.rimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.fast4x.rimusic.ui.components.themed.HeaderInfo
import it.fast4x.rimusic.ui.components.themed.MultiFloatingActionsContainer
import it.fast4x.rimusic.ui.items.PlaylistItem
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.utils.*
import kotlinx.coroutines.flow.map
import me.knighthat.colorPalette
import me.knighthat.component.Search
import me.knighthat.component.TextInputDialog
import me.knighthat.component.header.TabToolBar
import me.knighthat.component.screen.playlistSync
import me.knighthat.component.tab.*
import me.knighthat.component.tab.toolbar.Descriptive
import me.knighthat.component.tab.toolbar.MenuIcon
import me.knighthat.component.tab.toolbar.SongsShuffle
import me.knighthat.preference.Preference.HOME_LIBRARY_ITEM_SIZE


@ExperimentalMaterial3Api
@UnstableApi
@ExperimentalMaterialApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun HomeLibrary(
    onPlaylistClick: (Playlist) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    // Essentials
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lazyGridState = rememberLazyGridState()

    // Non-vital
    val pipedSession = getPipedSession()
    var plistId by remember { mutableLongStateOf( 0L ) }
    var playlistType by rememberPreference(playlistTypeKey, PlaylistsType.Playlist)
    val isPipedEnabled by rememberPreference(isPipedEnabledKey, false)
    val disableScrollingText by rememberPreference(disableScrollingTextKey, false)

    var items by persistList<PlaylistPreview>("home/playlists")

    var itemsOnDisplay by persistList<PlaylistPreview>("home/playlists/on_display")

    // Dialog states
    val newPlaylistToggleState = remember { mutableStateOf( false ) }

    val search = Search.init()

    val sort = Sort.init(
        playlistSortOrderKey,
        PlaylistSortBy.entries,
        rememberPreference(playlistSortByKey, PlaylistSortBy.DateAdded)
    )

    val itemSize = ItemSize.init( HOME_LIBRARY_ITEM_SIZE )

    val shuffle = SongsShuffle.init {
        when( playlistType ) {
            PlaylistsType.Playlist -> Database.songsInAllPlaylists()
            PlaylistsType.PinnedPlaylist -> Database.songsInAllPinnedPlaylists()
            PlaylistsType.MonthlyPlaylist -> Database.songsInAllMonthlyPlaylists()
            PlaylistsType.PipedPlaylist -> Database.songsInAllPipedPlaylists()
        }.map { it.map( Song::asMediaItem ) }
    }
    //<editor-fold desc="New playlist dialog">
    val newPlaylistDialog = object: TextInputDialog, Descriptive, MenuIcon {

        override val messageId: Int = R.string.create_new_playlist
        override val iconId: Int = R.drawable.add_in_playlist
        override val dialogTitle: String
            @Composable
            get() = stringResource( R.string.enter_the_playlist_name )
        override val menuIconTitle: String
            @Composable
            get() = stringResource( messageId )

        override var isActive: Boolean = newPlaylistToggleState.value
            set(value) {
                newPlaylistToggleState.value = value
                field = value
            }
        override val allowEmpty: Boolean = false

        // TODO: Add a random name generator
        override var value: String = ""

        override fun onShortClick() = super.onShortClick()

        override fun onSet(newValue: String) {
            if ( isPipedEnabled && pipedSession.token.isNotEmpty() )
                createPipedPlaylist(
                    context = context,
                    coroutineScope = coroutineScope,
                    pipedSession = pipedSession.toApiSession(),
                    name = newValue
                )
            else
                Database.asyncTransaction {
                    insert( Playlist( name = newValue ) )
                }

            onDismiss()
        }

    }
    //</editor-fold>
    val importPlaylistDialog = ImportSongsFromCSV.init(
        beforeTransaction = { _, row ->
            plistId = row["PlaylistName"]?.let {
                Database.playlistExistByName( it )
            } ?: 0L

            if (plistId == 0L)
                plistId = row["PlaylistName"]?.let {
                    Database.insert( Playlist( plistId, it, row["PlaylistBrowseId"] ) )
                }!!
        },
        afterTransaction = { index, song ->
            if (song.id.isBlank()) return@init

            Database.insert(song)
            Database.insert(
                SongPlaylistMap(
                    songId = song.id,
                    playlistId = plistId,
                    position = index
                )
            )
        }
    )
    val sync = playlistSync()

    LaunchedEffect( sort.sortBy, sort.sortOrder ) {
        Database.playlistPreviews( sort.sortBy, sort.sortOrder ).collect { items = it }
    }
    LaunchedEffect( items, search.input ) {
        val scrollIndex = lazyGridState.firstVisibleItemIndex
        val scrollOffset = lazyGridState.firstVisibleItemScrollOffset

        itemsOnDisplay = items.filter {
            it.playlist.name.contains( search.input, true )
        }

        lazyGridState.scrollToItem( scrollIndex, scrollOffset )
    }

    // START: Additional playlists
    val showPinnedPlaylists by rememberPreference(showPinnedPlaylistsKey, true)
    val showMonthlyPlaylists by rememberPreference(showMonthlyPlaylistsKey, true)
    val showPipedPlaylists by rememberPreference(showPipedPlaylistsKey, true)

    var buttonsList = listOf(PlaylistsType.Playlist to stringResource(R.string.playlists))
    if (showPipedPlaylists) buttonsList +=
        PlaylistsType.PipedPlaylist to stringResource(R.string.piped_playlists)
    if (showPinnedPlaylists) buttonsList +=
        PlaylistsType.PinnedPlaylist to stringResource(R.string.pinned_playlists)
    if (showMonthlyPlaylists) buttonsList +=
        PlaylistsType.MonthlyPlaylist to stringResource(R.string.monthly_playlists)
    // END - Additional playlists

    // START - Piped
    if (isPipedEnabled)
        ImportPipedPlaylists()
    // END - Piped

    // START - New playlist
    newPlaylistDialog.Render()
    // END - New playlist

    // START - Monthly playlist
    val enableCreateMonthlyPlaylists by rememberPreference(enableCreateMonthlyPlaylistsKey, true)
    if (enableCreateMonthlyPlaylists)
        CheckMonthlyPlaylist()
    // END - Monthly playlist

    Box(
        modifier = Modifier
            .background(colorPalette().background0)
            //.fillMaxSize()
            .fillMaxHeight()
            .fillMaxWidth(
                if( NavigationBarPosition.Right.isCurrent() )
                    Dimensions.contentWidthRightBar
                else
                    1f
            )
    ) {
        Column( Modifier.fillMaxSize() ) {
            // Sticky tab's title
            TabHeader( R.string.playlists ) {
                HeaderInfo( items.size.toString(), R.drawable.playlist )
            }

            // Sticky tab's tool bar
            TabToolBar.Buttons( sort, sync, search, shuffle, newPlaylistDialog, importPlaylistDialog, itemSize )

            // Sticky search bar
            search.SearchBar( this )

            LazyVerticalGrid(
                state = lazyGridState,
                columns = GridCells.Adaptive( itemSize.size.dp ),
                modifier = Modifier
                    .background(colorPalette().background0)
            ) {
                item(
                    key = "separator",
                    contentType = 0,
                    span = { GridItemSpan(maxLineSpan) }) {
                    ButtonsRow(
                        chips = buttonsList,
                        currentValue = playlistType,
                        onValueUpdate = { playlistType = it },
                        modifier = Modifier.padding(start = 12.dp, end = 12.dp)
                    )
                }

                val listPrefix =
                    when( playlistType ) {
                        PlaylistsType.Playlist -> ""    // Matches everything
                        PlaylistsType.PinnedPlaylist -> PINNED_PREFIX
                        PlaylistsType.MonthlyPlaylist -> MONTHLY_PREFIX
                        PlaylistsType.PipedPlaylist -> PIPED_PREFIX
                    }
                val condition: (PlaylistPreview) -> Boolean = {
                    it.playlist.name.startsWith( listPrefix, true )
                }
                items(
                    items = itemsOnDisplay.filter( condition ),
                    key = { it.playlist.id }
                ) { preview ->
                    PlaylistItem(
                        playlist = preview,
                        thumbnailSizeDp = itemSize.size.dp,
                        thumbnailSizePx = itemSize.size.px,
                        alternative = true,
                        modifier = Modifier.fillMaxSize()
                                           .animateItem( fadeInSpec = null, fadeOutSpec = null )
                                           .clickable(onClick = {
                                               search.onItemSelected()
                                               onPlaylistClick( preview.playlist )
                                           }),
                        disableScrollingText = disableScrollingText
                    )
                }

                item(
                    key = "footer",
                    contentType = 0,
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
                }

            }
        }

        FloatingActionsContainerWithScrollToTop(lazyGridState = lazyGridState)

        val showFloatingIcon by rememberPreference(showFloatingIconKey, false)
        if (UiType.ViMusic.isCurrent() && showFloatingIcon)
            MultiFloatingActionsContainer(
                iconId = R.drawable.search,
                onClick = onSearchClick,
                onClickSettings = onSettingsClick,
                onClickSearch = onSearchClick
            )
    }
}