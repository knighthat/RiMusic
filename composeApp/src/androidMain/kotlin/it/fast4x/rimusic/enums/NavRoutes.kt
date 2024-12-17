package it.fast4x.rimusic.enums

import androidx.navigation.NavController

enum class NavRoutes {
    home,
    album,
    artist,
    games,
    gamePacman,
    gameSnake,
    history,
    localPlaylist,
    mood,
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
    }

    fun isHere( navController: NavController ) = current( navController )?.startsWith( this.name ) ?: false

    fun isNotHere( navController: NavController ) = !isHere( navController )
}