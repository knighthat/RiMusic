package it.fast4x.rimusic.ui.screens.localplaylist

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.github.doyaaaaaken.kotlincsv.client.KotlinCsvExperimental
import it.fast4x.compose.persist.persist
import it.fast4x.compose.persist.persistList
import it.fast4x.compose.reordering.draggedItem
import it.fast4x.compose.reordering.rememberReorderingState
import it.fast4x.compose.reordering.reorder
import it.fast4x.innertube.Innertube
import it.fast4x.innertube.models.bodies.BrowseBody
import it.fast4x.innertube.models.bodies.NextBody
import it.fast4x.innertube.requests.playlistPage
import it.fast4x.innertube.requests.relatedSongs
import it.fast4x.rimusic.*
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.*
import it.fast4x.rimusic.models.*
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.SwipeableQueueItem
import it.fast4x.rimusic.ui.components.navigation.header.TabToolBar
import it.fast4x.rimusic.ui.components.tab.ExportSongsToCSVDialog
import it.fast4x.rimusic.ui.components.tab.LocateComponent
import it.fast4x.rimusic.ui.components.tab.toolbar.*
import it.fast4x.rimusic.ui.components.tab.toolbar.Button
import it.fast4x.rimusic.ui.components.themed.*
import it.fast4x.rimusic.ui.items.SongItem
import it.fast4x.rimusic.ui.styling.*
import it.fast4x.rimusic.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.util.UUID


@KotlinCsvExperimental
@ExperimentalMaterialApi
@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun LocalPlaylistSongs(
    navController: NavController,
    playlistId: Long,
    onDelete: () -> Unit,
) {
    // Essentials
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val uriHandler = LocalUriHandler.current

    var playlistPreview by persist<PlaylistPreview?>("localPlaylist/playlist")
    var items by persistList<SongEntity>("localPlaylist/$playlistId/itemsOffShelve")
    var itemsOnDisplay by persistList<SongEntity>("localPlaylist/$playlistId/songs/on_display")
    // List should be cleared when tab changed
    val selectedItems = remember { mutableListOf<SongEntity>() }

    fun getMediaItems() = selectedItems.ifEmpty { itemsOnDisplay }.map( SongEntity::asMediaItem )

    // Non-vital
    val parentalControlEnabled by rememberPreference(parentalControlEnabledKey, false)
    val isPipedEnabled by rememberPreference(isPipedEnabledKey, false)
    val disableScrollingText by rememberPreference(disableScrollingTextKey, false)
    val pipedSession = getPipedSession()
    var isRecommendationEnabled by rememberPreference(isRecommendationEnabledKey, false)
    // Playlist non-vital
    val playlistName = remember { mutableStateOf( "" ) }
    val thumbnailUrl = remember { mutableStateOf("") }

    val search = Search.init()

    val sort = PlaylistSongsSort.init()

    val shuffle = SongsShuffle.init { flowOf( getMediaItems() ) }
    val renameDialog = RenameDialog.init( pipedSession, coroutineScope, { isPipedEnabled }, playlistName, { playlistPreview } )
    val exportDialog = ExportSongsToCSVDialog.init( playlistName, ::getMediaItems )
    val deleteDialog = DeletePlaylist {
        Database.asyncTransaction {
            playlistPreview?.playlist?.let( ::delete )
        }

        if (
            playlistPreview?.playlist?.name?.startsWith(PIPED_PREFIX) == true
            && isPipedEnabled
            && pipedSession.token.isNotEmpty()
        )
            deletePipedPlaylist(
                context = context,
                coroutineScope = coroutineScope,
                pipedSession = pipedSession.toApiSession(),
                id = UUID.fromString(playlistPreview?.playlist?.browseId)
            )

        onDismiss()

        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
            navController.popBackStack()
    }
    val renumberDialog = Reposition(
        { playlistPreview?.playlist?.id },
        { items.map(SongEntity::song) }
    )
    val downloadAllDialog = DownloadAllDialog.init( ::getMediaItems )
    val deleteDownloadsDialog = DelAllDownloadedDialog.init( ::getMediaItems )
    val editThumbnailLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            val thumbnailName = "playlist_${playlistPreview?.playlist?.id}"
            val permaUri = saveImageToInternalStorage(context, uri, "thumbnail", thumbnailName)
            thumbnailUrl.value = permaUri.toString()
        } else {
            SmartMessage(context.resources.getString(R.string.thumbnail_not_selected), context = context)
        }
    }
    val pin = pin( playlistPreview, playlistId )
    val positionLock = PositionLock.init( sort.sortOrder )

    val itemSelector = ItemSelector.init()
    LaunchedEffect( itemSelector.isActive ) {
        // Clears selectedItems when check boxes are disabled
        if( !itemSelector.isActive )
            selectedItems.clear()
        else
            // Setting this field to true means disable it
            positionLock.isFirstIcon = true
    }
    // Either position lock or item selector can be turned on at a time
    LaunchedEffect( positionLock.isFirstIcon ) {
        if( !positionLock.isFirstIcon ) {
            // Open to move position
            itemSelector.isActive = false
            // Disable smart recommendation, it breaks the index
            isRecommendationEnabled = false
        }
    }

    val playNext = PlayNext {
        binder?.player?.addNext( getMediaItems(), appContext() )

        // Turn of selector clears the selected list
        itemSelector.isActive = false
    }
    val enqueue = Enqueue {
        binder?.player?.enqueue( getMediaItems(), context )

        // Turn of selector clears the selected list
        itemSelector.isActive = false
    }
    val addToFavorite = LikeSongs( ::getMediaItems )

    val addToPlaylist = PlaylistsMenu.init(
        navController,
        {
            if( it.playlist.name.startsWith(PIPED_PREFIX)
                && isPipedEnabled
                && pipedSession.token.isNotEmpty()
            )
                addToPipedPlaylist(
                    context = context,
                    coroutineScope = coroutineScope,
                    pipedSession = pipedSession.toApiSession(),
                    id = UUID.fromString(it.playlist.browseId),
                    videos = getMediaItems().map( MediaItem::mediaId )
                )

            getMediaItems()
        },
        { throwable, preview ->
            Timber.e( "Failed to add songs to playlist ${preview.playlist.name} on LocalPlaylistSongs" )
            throwable.printStackTrace()
        },
        {
            // Turn of selector clears the selected list
            itemSelector.isActive = false
        }
    )

    fun sync() {
        playlistPreview?.let { playlistPreview ->
            if (!playlistPreview.playlist.name.startsWith(
                    PIPED_PREFIX,
                    0,
                    true
                )
            ) {
                Database.asyncTransaction {
                    runBlocking(Dispatchers.IO) {
                        withContext(Dispatchers.IO) {
                            Innertube.playlistPage(
                                BrowseBody(
                                    browseId = playlistPreview.playlist.browseId
                                        ?: ""
                                )
                            )
                                ?.completed()
                        }
                    }?.getOrNull()?.let { remotePlaylist ->
                        Database.clearPlaylist(playlistId)

                        remotePlaylist.songsPage
                            ?.items
                            ?.map(Innertube.SongItem::asMediaItem)
                            ?.onEach(Database::insert)
                            ?.mapIndexed { position, mediaItem ->
                                SongPlaylistMap(
                                    songId = mediaItem.mediaId,
                                    playlistId = playlistId,
                                    position = position
                                )
                            }?.let(Database::insertSongPlaylistMaps)
                    }
                }
            } else {
                syncSongsInPipedPlaylist(
                    context = context,
                    coroutineScope = coroutineScope,
                    pipedSession = pipedSession.toApiSession(),
                    idPipedPlaylist = UUID.fromString(
                        playlistPreview.playlist.browseId
                    ),
                    playlistId = playlistPreview.playlist.id

                )
            }
        }
    }
    val syncComponent = Synchronize { sync() }
    val listenOnYT = ListenOnYouTube {
        val browseId = playlistPreview?.playlist?.browseId?.removePrefix( "VL" )

        binder?.player?.pause()
        uriHandler.openUri( "https://youtube.com/playlist?list=$browseId" )
    }

    fun openEditThumbnailPicker() {
        editThumbnailLauncher.launch("image/*")
    }
    val thumbnailPicker = ThumbnailPicker { openEditThumbnailPicker() }

    fun resetThumbnail() {
        if(thumbnailUrl.value == ""){
            SmartMessage(context.resources.getString(R.string.no_thumbnail_present), context = context)
            return
        }
        val thumbnailName = "thumbnail/playlist_${playlistPreview?.playlist?.id}"
        val retVal = deleteFileIfExists(context, thumbnailName)
        if(retVal == true){
            SmartMessage(context.resources.getString(R.string.removed_thumbnail), context = context)
            thumbnailUrl.value = ""
        } else {
            SmartMessage(context.resources.getString(R.string.failed_to_remove_thumbnail), context = context)
        }
    }
    val resetThumbnail = ResetThumbnail { resetThumbnail() }

    val locator = LocateComponent.init( lazyListState, ::getMediaItems )

    //<editor-fold defaultstate="collapsed" desc="Smart recommendation">
    val recommendationsNumber by rememberPreference( recommendationsNumberKey, RecommendationsNumber.`5` )
    var relatedSongs by rememberSaveable {
        // SongEntity before Int in case random position is equal
        mutableStateOf( emptyMap <SongEntity, Int>() )
    }

    LaunchedEffect( isRecommendationEnabled ) {
        if( !isRecommendationEnabled ) {
            // Clear the map when this feature is turned off
            items = items.toMutableList().apply {
                removeAll( relatedSongs.keys )
            }
            relatedSongs = emptyMap()
            return@LaunchedEffect
        }

        /*
            This process will be run before [items]
               most of the time.
            When it does, an exception will
               be thrown because [items] is not ready yet.
            To make sure that it is ready to use, a
               delay is set to suspend the thread.
        */
        while( items.isEmpty() )
            delay( 100L )

        val requestBody = NextBody( videoId =  items.random().song.id )
        Innertube.relatedSongs( requestBody )?.onSuccess { response ->
            val fetchedSongs = mutableMapOf<SongEntity, Int>()

            response?.songs
                ?.map { songItem ->

                    // Do NOT use [Utils#Innertube.SongItem.asSong]
                    // It doesn't have explicit prefix
                    val song = with( songItem ) {
                        val prefix = if( explicit ) EXPLICIT_PREFIX else ""

                        Song(
                            // Song's ID & title must not be "null". If they are,
                            // Something is wrong with Innertube.
                            id = "$prefix${info!!.endpoint!!.videoId!!}",
                            title = info!!.name!!,
                            artistsText = authors?.joinToString { author -> author.name ?: "" },
                            durationText = durationText,
                            thumbnailUrl = thumbnail?.url
                        )
                    }

                    SongEntity(
                        song = song,
                        // [albumTitle] is optional in this context,
                        // but it doesn't hurt to reduce nullable variables
                        albumTitle = songItem.album?.name
                    )
                }
                ?.forEach {
                    // Skip songs that are already in the playlist by comparing their IDs.
                    if( it.song.id in items.map{ e -> e.song.id }
                        || fetchedSongs.size >= recommendationsNumber.toInt()
                    ) return@forEach

                    val insertPosition = (0..items.size).random()
                    fetchedSongs[it] = insertPosition
                }

            relatedSongs = fetchedSongs

            // Enable position lock
            positionLock.isFirstIcon = true
        }
    }
    //</editor-fold>
    LaunchedEffect( sort.sortOrder, sort.sortBy ) {
        Database.songsPlaylist( playlistId, sort.sortBy, sort.sortOrder )
                .flowOn( Dispatchers.IO )
                .distinctUntilChanged()
                .collect { items = it }
    }
    LaunchedEffect( items, relatedSongs, search.input, parentalControlEnabled ) {
        items.toMutableList()
             .apply {
                 relatedSongs.forEach { (song, index) ->
                     add( index, song )
                 }
             }
             .distinctBy { it.song.id }
             .filter {
                 if( parentalControlEnabled )
                     !it.song.title.startsWith(EXPLICIT_PREFIX)
                 else
                     true
             }.filter {
                 // Without cleaning, user can search explicit songs with "e:"
                 // I kinda want this to be a feature, but it seems unnecessary
                 val containsName = it.song.cleanTitle().contains(search.input, true)
                 val containsArtist = it.song.artistsText?.contains(search.input, true) ?: false
                 val containsAlbum = it.albumTitle?.contains(search.input, true) ?: false

                 containsName || containsArtist || containsAlbum
             }.let { itemsOnDisplay = it }
    }
    LaunchedEffect(Unit) {
        Database.singlePlaylistPreview( playlistId )
                .flowOn( Dispatchers.IO )
                .distinctUntilChanged()
                .collect { playlistPreview = it }
    }
    LaunchedEffect( playlistPreview?.playlist?.name ) {
        renameDialog.playlistName = playlistPreview?.playlist?.name?.let { name ->
            if( name.startsWith( MONTHLY_PREFIX, true ) )
                getTitleMonthlyPlaylist(context, name.substringAfter(MONTHLY_PREFIX))
            else
                name.substringAfter( PINNED_PREFIX )
                    .substringAfter( PIPED_PREFIX )
        } ?: "Unknown"

        val thumbnailName = "thumbnail/playlist_${playlistId}"
        val presentThumbnailUrl: String? = checkFileExists(context, thumbnailName)
        if (presentThumbnailUrl != null) {
            thumbnailUrl.value = presentThumbnailUrl
        }
    }

    var autosync by rememberPreference(autosyncKey, false)

    val thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )


    val reorderingState = rememberReorderingState(
        lazyListState = lazyListState,
        key = items,
        onDragEnd = { fromIndex, toIndex ->
            //Log.d("mediaItem","reoder playlist $playlistId, from $fromIndex, to $toIndex")
            Database.asyncTransaction {
                move(playlistId, fromIndex, toIndex)
            }
        },
        extraItemCount = 1
    )

    renameDialog.Render()
    exportDialog.Render()
    deleteDialog.Render()
    (renumberDialog as Dialog).Render()
    downloadAllDialog.Render()
    deleteDownloadsDialog.Render()

    val playlistThumbnailSizeDp = Dimensions.thumbnails.playlist
    val playlistThumbnailSizePx = playlistThumbnailSizeDp.px

    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSizePx = thumbnailSizeDp.px

    val rippleIndication = ripple(bounded = false)

//    var nowPlayingItem by remember {
//        mutableStateOf(-1)
//    }

    val playlistNotMonthlyType =
        playlistPreview?.playlist?.name?.startsWith(MONTHLY_PREFIX, 0, true) == false
    val playlistNotPipedType =
        playlistPreview?.playlist?.name?.startsWith(PIPED_PREFIX, 0, true) == false
    val hapticFeedback = LocalHapticFeedback.current


    Box(
        modifier = Modifier
            .background(colorPalette().background0)
            //.fillMaxSize()
            .fillMaxHeight()
            .fillMaxWidth(
                if (NavigationBarPosition.Right.isCurrent())
                    Dimensions.contentWidthRightBar
                else
                    1f
            )
    ) {
        //LookaheadScope {
            LazyColumn(
                state = reorderingState.lazyListState,
                //contentPadding = LocalPlayerAwareWindowInsets.current
                //    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                //    .asPaddingValues(),
                modifier = Modifier
                    .background(colorPalette().background0)
                    .fillMaxSize()
            ) {
                item(
                    key = "header",
                    contentType = 0
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {

                        HeaderWithIcon(
                            title = cleanPrefix(playlistName.value),
                            iconId = R.drawable.playlist,
                            enabled = true,
                            showIcon = false,
                            modifier = Modifier
                                .padding(bottom = 8.dp),
                            onClick = {}
                        )

                    }

                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            //.background(colorPalette().background4)
                            .fillMaxSize(0.99F)
                            .background(
                                color = colorPalette().background1,
                                shape = thumbnailRoundness.shape()
                            )
                    ) {

                        playlistPreview?.let {
                            Playlist(
                                playlist = it,
                                thumbnailSizeDp = playlistThumbnailSizeDp,
                                thumbnailSizePx = playlistThumbnailSizePx,
                                alternative = true,
                                showName = false,
                                modifier = Modifier
                                    .padding(top = 14.dp),
                                disableScrollingText = disableScrollingText,
                                thumbnailUrl = if (thumbnailUrl.value == "") null else thumbnailUrl.value
                            )
                        }


                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier
                                //.fillMaxHeight()
                                .padding(end = 10.dp)
                                .fillMaxWidth(if (isLandscape) 0.90f else 0.80f)
                        ) {
                            Spacer(modifier = Modifier.height(10.dp))
                            IconInfo(
                                title = items.size.toString(),
                                icon = painterResource(R.drawable.musical_notes)
                            )
                            Spacer(modifier = Modifier.height(5.dp))

                            val totalDuration = items.sumOf { durationTextToMillis(it.song.durationText ?: "0:0") }
                            IconInfo(
                                title = formatAsTime( totalDuration ),
                                icon = painterResource(R.drawable.time)
                            )
                            if (isRecommendationEnabled) {
                                Spacer(modifier = Modifier.height(5.dp))
                                IconInfo(
                                    title = relatedSongs.keys.size.toString(),
                                    icon = painterResource(R.drawable.smart_shuffle)
                                )
                            }
                            Spacer(modifier = Modifier.height(30.dp))
                        }

                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            HeaderIconButton(
                                icon = R.drawable.smart_shuffle,
                                enabled = true,
                                color = if (isRecommendationEnabled) colorPalette().text else colorPalette().textDisabled,
                                onClick = {},
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            isRecommendationEnabled = !isRecommendationEnabled
                                        },
                                        onLongClick = {
                                            SmartMessage(
                                                context.resources.getString(R.string.info_smart_recommendation),
                                                context = context
                                            )
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            shuffle.ToolBarButton()
                            Spacer(modifier = Modifier.height(10.dp))
                            search.ToolBarButton()
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TabToolBar.Buttons(
                        mutableListOf<Button>().apply {
                            if (playlistNotMonthlyType)
                                this.add( pin )
                            if ( sort.sortBy == PlaylistSongSortBy.Position )
                                this.add( positionLock )

                            this.add( downloadAllDialog )
                            this.add( deleteDownloadsDialog )
                            this.add( itemSelector )
                            this.add( playNext )
                            this.add( enqueue )
                            this.add( addToFavorite )
                            this.add( addToPlaylist )
                            if( playlistPreview?.playlist?.browseId?.isNotBlank() == true )
                                this.add( syncComponent )
                            if( playlistPreview?.playlist?.browseId?.isNotBlank() == true )
                                this.add( listenOnYT )
                            this.add( renameDialog )
                            this.add( renumberDialog )
                            this.add( deleteDialog )
                            this.add( exportDialog )
                            this.add( thumbnailPicker )
                            this.add( resetThumbnail )
                        }
                    )

                    if (autosync && playlistPreview?.let { playlistPreview -> !playlistPreview.playlist.browseId.isNullOrBlank() } == true) {
                        sync()
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    /*        */
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .fillMaxWidth()
                    ) {

                        sort.ToolBarButton()

                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) { locator.ToolBarButton() }

                    }

                    Column { search.SearchBar( this ) }
                }

                itemsIndexed(
                    items = itemsOnDisplay.filter { it.song.id.isNotBlank() },
                    key = { _, song -> song.song.id },
                    contentType = { _, song -> song },
                ) { index, song ->

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .draggedItem(
                                reorderingState = reorderingState,
                                index = index
                            )
                    ) {

                        // Drag anchor
                        if ( !positionLock.isLocked() ) {
                            Box(
                                modifier = Modifier.padding( end = 16.dp ) // Accommodate horizontal padding of SongItem
                                    .size( 24.dp )
                                    .zIndex(2f)
                                    .align( Alignment.CenterEnd ),
                                contentAlignment = Alignment.Center
                            ) {

                                IconButton(
                                    icon = R.drawable.reorder,
                                    color = colorPalette().textDisabled,
                                    indication = rippleIndication,
                                    onClick = {},
                                    modifier = Modifier
                                        .reorder(
                                            reorderingState = reorderingState,
                                            index = index
                                        )
                                )
                            }
                        }

                        SwipeableQueueItem(
                            mediaItem = song.asMediaItem,
                            onSwipeToLeft = {
                                Database.asyncTransaction {
                                    deleteSongFromPlaylist(song.song.id, playlistId)
                                }


                                if (playlistPreview?.playlist?.name?.startsWith(PIPED_PREFIX) == true && isPipedEnabled && pipedSession.token.isNotEmpty()) {
                                    Timber.d("MediaItemMenu LocalPlaylistSongs onSwipeToLeft browseId ${playlistPreview!!.playlist.browseId}")
                                    removeFromPipedPlaylist(
                                        context = context,
                                        coroutineScope = coroutineScope,
                                        pipedSession = pipedSession.toApiSession(),
                                        id = UUID.fromString(playlistPreview?.playlist?.browseId),
                                        index
                                    )
                                }
                                coroutineScope.launch {
                                    SmartMessage(
                                        context.resources.getString(R.string.deleted) + " \"" + song.asMediaItem.mediaMetadata.title.toString() + " - " + song.asMediaItem.mediaMetadata.artist.toString() + "\" ",
                                        type = PopupType.Warning,
                                        context = context,
                                        durationLong = true
                                    )
                                }

                            },
                            onSwipeToRight = {
                                binder?.player?.addNext(song.asMediaItem)
                            },
                        ) {
                            SongItem(
                                song = song.song,
                                navController = navController,
                                isRecommended = song in relatedSongs,
                                modifier = Modifier
                                    .combinedClickable(
                                        onLongClick = {
                                            menuState.display {
                                                InPlaylistMediaItemMenu(
                                                    navController = navController,
                                                    playlist = playlistPreview,
                                                    playlistId = playlistId,
                                                    positionInPlaylist = index,
                                                    song = song.song,
                                                    onDismiss = menuState::hide,
                                                    disableScrollingText = disableScrollingText
                                                )
                                            }
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        onClick = {
                                            binder?.stopRadio()
                                            binder?.player?.forcePlayAtIndex(
                                                itemsOnDisplay.map( SongEntity::asMediaItem ),
                                                index
                                            )

                                            /*
                                                Due to the small size of checkboxes,
                                                we shouldn't disable [itemSelector]
                                             */

                                            search.onItemSelected()
                                        }
                                    )
                                    .background(color = colorPalette().background0),
                                trailingContent = {
                                    // It must watch for [selectedItems.size] for changes
                                    // Otherwise, state will stay the same
                                    val checkedState = remember( selectedItems.size ) {
                                        mutableStateOf( song in selectedItems )
                                    }

                                    if( itemSelector.isActive || !positionLock.isLocked() )
                                        // Create a fake box to store drag anchor and checkbox
                                        Box( Modifier.width( 24.dp ) ) {

                                            if( itemSelector.isActive )
                                                Checkbox(
                                                    checked = checkedState.value,
                                                    onCheckedChange = {
                                                        checkedState.value = it
                                                        if ( it )
                                                            selectedItems.add( song )
                                                        else
                                                            selectedItems.remove( song )
                                                    },
                                                    colors = CheckboxDefaults.colors(
                                                        checkedColor = colorPalette().accent,
                                                        uncheckedColor = colorPalette().text
                                                    ),
                                                    modifier = Modifier.scale( .7f )
                                                                       .size( 24.dp )
                                                                       .padding( all = 0.dp )
                                                )
                                        }
                                    else if( !itemSelector.isActive )
                                        checkedState.value = false
                                },
                                thumbnailOverlay = {
                                    if (sort.sortBy == PlaylistSongSortBy.PlayTime) {
                                        BasicText(
                                            text = song.song.formattedTotalPlayTime,
                                            style = typography().xxs.semiBold.center.color(
                                                colorPalette().onOverlay
                                            ),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    brush = Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color.Transparent,
                                                            colorPalette().overlay
                                                        )
                                                    ),
                                                    shape = thumbnailShape()
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .align(Alignment.BottomCenter)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                item(
                    key = "footer",
                    contentType = 0,
                ) {
                    Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
                }
            }

            FloatingActionsContainerWithScrollToTop(lazyListState = lazyListState)

            val showFloatingIcon by rememberPreference(showFloatingIconKey, false)
            if ( UiType.ViMusic.isCurrent() && showFloatingIcon )
                FloatingActionsContainerWithScrollToTop(
                    lazyListState = lazyListState,
                    iconId = R.drawable.shuffle,
                    visible = !reorderingState.isDragging,
                    onClick = {
                        getMediaItems().let { songs ->
                            if (songs.isNotEmpty()) {
                                binder?.stopRadio()
                                binder?.player
                                      ?.forcePlayFromBeginning( songs.shuffled() )
                            }
                        }
                    }
                )
    }
}
