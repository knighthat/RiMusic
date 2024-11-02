package it.fast4x.rimusic.ui.screens.home

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import it.fast4x.compose.persist.PersistMapCleanup
import it.fast4x.compose.routing.RouteHandler
import it.fast4x.compose.routing.defaultStacking
import it.fast4x.compose.routing.defaultStill
import it.fast4x.compose.routing.defaultUnstacking
import it.fast4x.compose.routing.isStacking
import it.fast4x.compose.routing.isUnknown
import it.fast4x.compose.routing.isUnstacking
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.CheckUpdateState
import it.fast4x.rimusic.enums.HomeScreenTabs
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.models.toUiMood
import it.fast4x.rimusic.ui.components.themed.ConfirmationDialog
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.screens.globalRoutes
import it.fast4x.rimusic.ui.screens.searchResultRoute
import it.fast4x.rimusic.ui.screens.searchRoute
import it.fast4x.rimusic.utils.CheckAvailableNewVersion
import it.fast4x.rimusic.utils.checkUpdateStateKey
import it.fast4x.rimusic.utils.enableQuickPicksPageKey
import it.fast4x.rimusic.utils.getEnum
import it.fast4x.rimusic.utils.homeScreenTabIndexKey
import it.fast4x.rimusic.utils.indexNavigationTabKey
import it.fast4x.rimusic.utils.preferences
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.showSearchTabKey
import it.fast4x.rimusic.utils.showStatsInNavbarKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.knighthat.Skeleton
import kotlin.system.exitProcess


@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalMaterialApi
@ExperimentalTextApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun HomeScreen(
    navController: NavController,
    onPlaylistUrl: (String) -> Unit,
    miniPlayer: @Composable () -> Unit = {},
    openTabFromShortcut: Int
) {
    var showNewversionDialog by remember {
        mutableStateOf(true)
    }

    var checkUpdateState by rememberPreference(checkUpdateStateKey, CheckUpdateState.Disabled)

    val saveableStateHolder = rememberSaveableStateHolder()

    val preferences = LocalContext.current.preferences
    val showSearchTab by rememberPreference(showSearchTabKey, false)
    val showStatsInNavbar by rememberPreference(showStatsInNavbarKey, false)
    val enableQuickPicksPage by rememberPreference(enableQuickPicksPageKey, true)

    PersistMapCleanup("home/")

    RouteHandler(
        listenToGlobalEmitter = true,
        transitionSpec = {
            when {
                isStacking -> defaultStacking
                isUnstacking -> defaultUnstacking
                isUnknown -> when {
                    initialState.route == searchRoute && targetState.route == searchResultRoute -> defaultStacking
                    initialState.route == searchResultRoute && targetState.route == searchRoute -> defaultUnstacking
                    else -> defaultStill
                }

                else -> defaultStill
            }
        }
    ) {
        globalRoutes()

        host {

            val openTabFromShortcut1 by remember{ mutableIntStateOf(openTabFromShortcut) }

            var (tabIndex, onTabChanged) =
                when (openTabFromShortcut1) {
                    -1 -> when (preferences.getEnum(indexNavigationTabKey, HomeScreenTabs.Default)) {
                            HomeScreenTabs.Default -> rememberPreference(homeScreenTabIndexKey,
                            HomeScreenTabs.QuickPics.index)
                          else -> remember {
                                mutableIntStateOf(preferences.getEnum(indexNavigationTabKey, HomeScreenTabs.QuickPics).index)
                          }
                        }
                    else -> remember { mutableIntStateOf(openTabFromShortcut1) }
                }

            if (tabIndex == -2) navController.navigate(NavRoutes.search.name)


            if (!enableQuickPicksPage && tabIndex==0) tabIndex = 1

            Skeleton(
                navController,
                tabIndex,
                onTabChanged,
                miniPlayer,
                navBarContent = { Item ->
                    if (enableQuickPicksPage)
                        Item(0, stringResource(R.string.quick_picks), R.drawable.sparkles)
                    Item(1, stringResource(R.string.songs), R.drawable.musical_notes)
                    Item(2, stringResource(R.string.artists), R.drawable.artists)
                    Item(3, stringResource(R.string.albums), R.drawable.album)
                    Item(4, stringResource(R.string.playlists), R.drawable.library)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> QuickPicksModern(
                            onAlbumClick = {
                                navController.navigate(route = "${NavRoutes.album.name}/$it")
                            },
                            onArtistClick = {
                                navController.navigate(route = "${NavRoutes.artist.name}/$it")
                            },
                            onPlaylistClick = {
                                navController.navigate(route = "${NavRoutes.playlist.name}/$it")
                            },
                            onSearchClick = {
                                navController.navigate(NavRoutes.search.name)
                            },
                            onMoodClick = { mood ->
                                navController.currentBackStackEntry?.savedStateHandle?.set("mood", mood.toUiMood())
                                navController.navigate(NavRoutes.mood.name)
                            },
                            onSettingsClick = {
                                navController.navigate(NavRoutes.settings.name)
                            },
                            navController = navController

                        )

                        1 -> HomeSongsModern(
                            navController = navController,
                            onSearchClick = {
                                //searchRoute("")
                                navController.navigate(NavRoutes.search.name)
                            },
                            onSettingsClick = {
                                //settingsRoute()
                                navController.navigate(NavRoutes.settings.name)
                            }
                        )

                        2 -> HomeArtistsModern(
                            onArtistClick = {
                                navController.navigate(route = "${NavRoutes.artist.name}/${it.id}")
                            },
                            onSearchClick = {
                                //searchRoute("")
                                navController.navigate(NavRoutes.search.name)
                            },
                            onSettingsClick = {
                                //settingsRoute()
                                navController.navigate(NavRoutes.settings.name)
                            }
                        )

                        3 -> HomeAlbumsModern(
                            onAlbumClick = {
                                //albumRoute(it.id)
                                navController.navigate(route = "${NavRoutes.album.name}/${it.id}")
                            },
                            onSearchClick = {
                                //searchRoute("")
                                navController.navigate(NavRoutes.search.name)
                            },
                            onSettingsClick = {
                                //settingsRoute()
                                navController.navigate(NavRoutes.settings.name)
                            }
                        )

                        4 -> HomeLibraryModern(
                            onPlaylistClick = {
                                //localPlaylistRoute(it.id)
                                navController.navigate(route = "${NavRoutes.localPlaylist.name}/${it.id}")
                            },
                            onSearchClick = {
                                //searchRoute("")
                                navController.navigate(NavRoutes.search.name)
                            },
                            onSettingsClick = {
                                //settingsRoute()
                                navController.navigate(NavRoutes.settings.name)
                            }

                        )
                    }
                }
            }
        }
    }

    if (showNewversionDialog && checkUpdateState == CheckUpdateState.Enabled)
        CheckAvailableNewVersion(
            onDismiss = { showNewversionDialog = false },
            updateAvailable = {}
        )

    if (checkUpdateState == CheckUpdateState.Ask)
        ConfirmationDialog(
            text = stringResource(R.string.check_at_github_for_updates) + "\n\n" +
                    stringResource(R.string.when_an_update_is_available_you_will_be_asked_if_you_want_to_install_info) + "\n\n" +
                    stringResource(R.string.but_these_updates_would_not_go_through) + "\n\n" +
                    stringResource(R.string.you_can_still_turn_it_on_or_off_from_the_settings),
            confirmText = stringResource(R.string.enable),
            cancelText = stringResource(R.string.don_t_enable),
            cancelBackgroundPrimary = true,
            onCancel = { checkUpdateState = CheckUpdateState.Disabled },
            onDismiss = { checkUpdateState = CheckUpdateState.Disabled },
            onConfirm = { checkUpdateState = CheckUpdateState.Enabled },
        )

    // Exit app when user uses back
    val context = LocalContext.current
    var confirmCount by remember { mutableIntStateOf( 0 ) }
    BackHandler {
        // Prevent this from being applied when user is not on HomeScreen
        if( NavRoutes.home.isNotHere( navController ) )  {
            NavRoutes.back( navController )
            return@BackHandler
        }

        if( confirmCount == 0 ) {
            SmartMessage(
                // TODO: add this string to xml
                "Press once again to exit",
                context = context
            )
            confirmCount++

            // Reset confirmCount after 5s
            CoroutineScope( Dispatchers.Default ).launch {
                delay( 5000L )
                confirmCount = 0
            }
        } else {
            val activity = context as? Activity
            activity?.finishAffinity()
            // Close app with exit 0 notify that no problem occurred
            exitProcess( 0 )
        }
    }
}
