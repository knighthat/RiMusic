package it.fast4x.rimusic.ui.screens.home


import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import it.fast4x.compose.persist.persistList
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.ArtistSortBy
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.SortOrder
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.models.Artist
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.fast4x.rimusic.ui.components.themed.HeaderInfo
import it.fast4x.rimusic.ui.components.themed.MultiFloatingActionsContainer
import it.fast4x.rimusic.ui.items.ArtistItem
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.utils.artistSortByKey
import it.fast4x.rimusic.utils.artistSortOrderKey
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.showFloatingIconKey
import kotlinx.coroutines.flow.Flow
import me.knighthat.colorPalette
import me.knighthat.component.tab.TabHeader
import me.knighthat.component.tab.toolbar.ItemSize
import me.knighthat.component.tab.toolbar.Randomizer
import me.knighthat.component.tab.toolbar.Search
import me.knighthat.component.tab.toolbar.SongsShuffle
import me.knighthat.component.tab.toolbar.Sort
import me.knighthat.preference.Preference
import me.knighthat.preference.Preference.HOME_ARTIST_ITEM_SIZE

@ExperimentalMaterial3Api
@UnstableApi
@ExperimentalMaterialApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun HomeArtistsModern(
    onArtistClick: (Artist) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    // Essentials
    val menuState = LocalMenuState.current
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current
    val coroutineScope = rememberCoroutineScope()
    val lazyGridState = rememberLazyGridState()

    // Search states
    val visibleState = rememberSaveable { mutableStateOf(false) }
    val focusState = rememberSaveable { mutableStateOf( false ) }
    val inputState = rememberSaveable { mutableStateOf( "" ) }
    // Sort states
    val sortBy = rememberPreference(artistSortByKey, ArtistSortBy.DateAdded)
    val sortOrder = rememberPreference(artistSortOrderKey, SortOrder.Descending)

    val disableScrollingText by rememberPreference(disableScrollingTextKey, false)

    // Size state
    val sizeState = Preference.remember( HOME_ARTIST_ITEM_SIZE )
    // Randomizer states
    val itemsState = persistList<Artist>( "home/albums" )
    val rotationState = rememberSaveable { mutableStateOf( false ) }
    val angleState = animateFloatAsState(
        targetValue = if (rotationState.value) 360F else 0f,
        animationSpec = tween(durationMillis = 300), label = ""
    )

    val search = remember {
        object: Search {
            override val visibleState = visibleState
            override val focusState = focusState
            override val inputState = inputState
        }
    }
    val sort = remember {
        object: Sort<ArtistSortBy> {
            override val menuState = menuState
            override val sortOrderState = sortOrder
            override val sortByEnum = ArtistSortBy.entries
            override val sortByState = sortBy
        }
    }
    val itemSize = remember {
        object: ItemSize {
            override val menuState = menuState
            override val sizeState = sizeState
        }
    }
    val randomizer = remember {
        object: Randomizer<Artist> {
            override val itemsState = itemsState
            override val rotationState = rotationState
            override val angleState = angleState

            override fun onClick(item: Artist) = onArtistClick(item)
        }
    }
    val shuffle = remember(binder) {
        object: SongsShuffle{
            override val binder = binder
            override val context = context

            override fun query(): Flow<List<Song>?> = Database.songsInAllFollowedArtists()
        }
    }

    // Search mutable
    var isSearchBarVisible by search.visibleState
    var isSearchBarFocused by search.focusState
    val searchInput by search.inputState
    // Items mutable
    var items by randomizer.itemsState

    LaunchedEffect(sort.sortByState.value, sort.sortOrderState.value, inputState) {
        Database.artists(sort.sortByState.value, sort.sortOrderState.value).collect { items = it }
    }

    if ( searchInput.isNotBlank() )
        items = items
            .filter {
                it.name?.contains( searchInput, true) ?: false
            }

    Box (
        modifier = Modifier
            .background(colorPalette().background0)
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
            TabHeader( R.string.artists ) {
                HeaderInfo(items.size.toString(), R.drawable.artists)
            }

            // Sticky tab's tool bar
            Row (
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(vertical = 4.dp)
                    .fillMaxWidth()
            ){
                sort.ToolBarButton()

                search.ToolBarButton()

                randomizer.ToolBarButton()

                shuffle.ToolBarButton()

                itemSize.ToolBarButton()
            }

            // Sticky search bar
            search.SearchBar( this )

            LazyVerticalGrid(
                state = lazyGridState,
                columns = GridCells.Adaptive( itemSize.sizeState.value.dp ),
                modifier = Modifier.background( colorPalette().background0 )
                                   .fillMaxSize(),
                contentPadding = PaddingValues( bottom = Dimensions.bottomSpacer )
            ) {
                items(items = items, key = Artist::id) { artist ->
                    ArtistItem(
                        artist = artist,
                        thumbnailSizeDp = itemSize.sizeState.value.dp,
                        thumbnailSizePx = itemSize.sizeState.value.px,
                        alternative = true,
                        modifier = Modifier.animateItem( fadeInSpec = null, fadeOutSpec = null )
                                           .clickable(onClick = {
                                               if ( isSearchBarVisible )
                                                   if ( searchInput.isBlank() )
                                                        isSearchBarVisible = false
                                                   else
                                                       isSearchBarFocused = false

                                               onArtistClick( artist )
                                           }),
                        disableScrollingText = disableScrollingText
                    )
                }
            }
        }

        FloatingActionsContainerWithScrollToTop(lazyGridState = lazyGridState)

        val showFloatingIcon by rememberPreference(showFloatingIconKey, false)
        if( UiType.ViMusic.isCurrent() && showFloatingIcon )
            MultiFloatingActionsContainer(
                iconId = R.drawable.search,
                onClick = onSearchClick,
                onClickSettings = onSettingsClick,
                onClickSearch = onSearchClick
            )
    }
}
