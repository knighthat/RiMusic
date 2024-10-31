package it.fast4x.rimusic.ui.screens.home

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import it.fast4x.compose.persist.persistList
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.MONTHLY_PREFIX
import it.fast4x.rimusic.PINNED_PREFIX
import it.fast4x.rimusic.PIPED_PREFIX
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.PlaylistSortBy
import it.fast4x.rimusic.enums.PlaylistsType
import it.fast4x.rimusic.enums.SortOrder
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.models.Playlist
import it.fast4x.rimusic.models.PlaylistPreview
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.SongPlaylistMap
import it.fast4x.rimusic.query
import it.fast4x.rimusic.ui.components.ButtonsRow
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.fast4x.rimusic.ui.components.themed.HeaderInfo
import it.fast4x.rimusic.ui.components.themed.MultiFloatingActionsContainer
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.items.PlaylistItem
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.utils.CheckMonthlyPlaylist
import it.fast4x.rimusic.utils.ImportPipedPlaylists
import it.fast4x.rimusic.utils.autosyncKey
import it.fast4x.rimusic.utils.createPipedPlaylist
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.enableCreateMonthlyPlaylistsKey
import it.fast4x.rimusic.utils.getPipedSession
import it.fast4x.rimusic.utils.isPipedEnabledKey
import it.fast4x.rimusic.utils.pipedApiTokenKey
import it.fast4x.rimusic.utils.playlistSortByKey
import it.fast4x.rimusic.utils.playlistTypeKey
import it.fast4x.rimusic.utils.rememberEncryptedPreference
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.showFloatingIconKey
import it.fast4x.rimusic.utils.showMonthlyPlaylistsKey
import it.fast4x.rimusic.utils.showPinnedPlaylistsKey
import it.fast4x.rimusic.utils.showPipedPlaylistsKey
import kotlinx.coroutines.flow.Flow
import me.knighthat.colorPalette
import me.knighthat.component.header.TabToolBar
import me.knighthat.component.tab.TabHeader
import me.knighthat.component.tab.toolbar.ImportSongsFromCSV
import me.knighthat.component.tab.toolbar.InputDialog
import me.knighthat.component.tab.toolbar.ItemSize
import me.knighthat.component.tab.toolbar.Search
import me.knighthat.component.tab.toolbar.SongsShuffle
import me.knighthat.component.tab.toolbar.Sort
import me.knighthat.preference.Preference
import me.knighthat.preference.Preference.HOME_LIBRARY_ITEM_SIZE


@ExperimentalMaterial3Api
@UnstableApi
@ExperimentalMaterialApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun HomeLibraryModern(
    onPlaylistClick: (Playlist) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    // Essentials
    val menuState = LocalMenuState.current
    val binder = LocalPlayerServiceBinder.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lazyGridState = rememberLazyGridState()

    // Non-vital
    val pipedSession = getPipedSession()
    var plistId by remember { mutableLongStateOf( 0L ) }
    var autosync by rememberPreference(autosyncKey, false)
    var playlistType by rememberPreference(playlistTypeKey, PlaylistsType.Playlist)
    val isPipedEnabled by rememberPreference(isPipedEnabledKey, false)
    val disableScrollingText by rememberPreference(disableScrollingTextKey, false)

    var items by persistList<PlaylistPreview>("home/playlists")

    // Search states
    val visibleState = rememberSaveable { mutableStateOf(false) }
    val focusState = rememberSaveable { mutableStateOf( false ) }
    val inputState = rememberSaveable { mutableStateOf("") }
    // Sort states
    val sortBy = rememberPreference(playlistSortByKey, PlaylistSortBy.DateAdded)
    val sortOrder = rememberEncryptedPreference(pipedApiTokenKey, SortOrder.Descending)
    // Size state
    val sizeState = Preference.remember( HOME_LIBRARY_ITEM_SIZE )
    // Dialog states
    val newPlaylistToggleState = remember { mutableStateOf( false ) }

    val search = remember {
        object: Search {
            override val visibleState = visibleState
            override val focusState = focusState
            override val inputState = inputState
        }
    }
    val sort = remember {
        object: Sort<PlaylistSortBy> {
            override val menuState = menuState
            override val sortOrderState = sortOrder
            override val sortByEnum = PlaylistSortBy.entries
            override val sortByState = sortBy
        }
    }
    val itemSize = remember {
        object: ItemSize {
            override val menuState = menuState
            override val sizeState = sizeState
        }
    }
    val shuffle = remember(binder) {
        object: SongsShuffle {
            override val binder = binder
            override val context = context

            override fun query(): Flow<List<Song>?> =
                when( playlistType ) {
                    PlaylistsType.Playlist -> Database.songsInAllPlaylists()
                    PlaylistsType.PinnedPlaylist -> Database.songsInAllPinnedPlaylists()
                    PlaylistsType.MonthlyPlaylist -> Database.songsInAllMonthlyPlaylists()
                    PlaylistsType.PipedPlaylist -> Database.songsInAllPipedPlaylists()
                }
        }
    }
    val newPlaylistDialog = remember {
        object: InputDialog {
            override val context = context
            override val toggleState = newPlaylistToggleState
            override val iconId = R.drawable.add_in_playlist
            override val titleId: Int = R.string.enter_the_playlist_name
            override val messageId: Int = R.string.create_new_playlist

            override fun onSet(newValue: String) {

                if ( isPipedEnabled && pipedSession.token.isNotEmpty() )
                    createPipedPlaylist(
                        context = context,
                        coroutineScope = coroutineScope,
                        pipedSession = pipedSession.toApiSession(),
                        name = newValue
                    )
                else
                    query {
                        Database.insert( Playlist( name = newValue ) )
                    }

                onDismiss()
            }
        }
    }
    // START - Import playlist
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        ImportSongsFromCSV.openFile(
            uri ?: return@rememberLauncherForActivityResult,
            context,
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
    }
    // END - Import playlist
    val importPlaylistDialog = remember {
        object: ImportSongsFromCSV {
            override val context = context

            override fun onShortClick() = importLauncher.launch( arrayOf("text/csv", "text/comma-separated-values") )
        }
    }

    // Mutable
    var isSearchBarVisible by search.visibleState
    var isSearchBarFocused by search.focusState
    val searchInput by search.inputState

    LaunchedEffect(sort.sortByState.value, sort.sortOrderState.value, searchInput) {
        Database.playlistPreviews(sort.sortByState.value, sort.sortOrderState.value).collect { items = it }
    }

    if ( searchInput.isNotBlank() )
        items = items.filter {
            it.playlist.name.contains( searchInput, true )
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
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(vertical = 4.dp)
                    .fillMaxWidth()
            ) {
                sort.ToolBarButton()

                TabToolBar.Icon(
                    iconId = R.drawable.sync,
                    tint = if (autosync) colorPalette().text else colorPalette().textDisabled,
                    onShortClick = { autosync = !autosync },
                    onLongClick = {
                        SmartMessage(
                            context.resources.getString(R.string.autosync),
                            context = context
                        )
                    }
                )

                search.ToolBarButton()

                shuffle.ToolBarButton()

                newPlaylistDialog.ToolBarButton()

                importPlaylistDialog.ToolBarButton()

                itemSize.ToolBarButton()
            }

            // Sticky search bar
            search.SearchBar( this )

            LazyVerticalGrid(
                state = lazyGridState,
                columns = GridCells.Adaptive( itemSize.sizeState.value.dp ),
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
                        modifier = Modifier.padding(end = 12.dp)
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
                    items = items.filter( condition ),
                    key = { it.playlist.id }
                ) { preview ->
                    PlaylistItem(
                        playlist = preview,
                        thumbnailSizeDp = itemSize.sizeState.value.dp,
                        thumbnailSizePx = itemSize.sizeState.value.px,
                        alternative = true,
                        modifier = Modifier.fillMaxSize()
                                           .animateItem( fadeInSpec = null, fadeOutSpec = null )
                                           .clickable(onClick = {
                                               if ( isSearchBarVisible )
                                                   if ( searchInput.isBlank() )
                                                       isSearchBarVisible = false
                                                   else
                                                       isSearchBarFocused = false

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