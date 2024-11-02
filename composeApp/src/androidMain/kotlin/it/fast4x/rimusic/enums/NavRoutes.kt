package it.fast4x.rimusic.enums

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController

enum class NavRoutes {
    home,
    album,
    artist,
    builtInPlaylist,
    games,
    gamePacman,
    gameSnake,
    history,
    localPlaylist,
    mood,
    onDevice,
    player,
    playlist,
    queue,
    search,
    searchResults,
    settings,
    statistics,
    newAlbums,
    moodsPage,
    podcast;

    companion object {

        fun current( navController: NavController ) = navController.currentBackStackEntry?.destination?.route

        fun back( navController: NavController ) {
            if ( navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED )
                navController.popBackStack()
        }
    }

    fun isHere( navController: NavController ) = current( navController ) == this.name

    fun isNotHere( navController: NavController ) = !isHere( navController )
}