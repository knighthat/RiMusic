package me.knighthat.component.header

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.extensions.games.pacman.Pacman
import it.fast4x.rimusic.ui.styling.favoritesIcon
import me.knighthat.button.Button
import me.knighthat.colorPalette

class AppHeader(
    val navController: NavController
) {

    companion object {

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun colors(): TopAppBarColors = TopAppBarColors(
            containerColor = colorPalette().background0,
            titleContentColor = colorPalette().text,
            scrolledContainerColor = colorPalette().background0,
            navigationIconContentColor = colorPalette().background0,
            actionIconContentColor = colorPalette().text
        )
    }

    @Composable
    private fun BackButton() {
        if ( NavRoutes.home.isNotHere( navController ) )
            androidx.compose.material3.IconButton(
                onClick = { NavRoutes.back( navController ) }
            ) {
                Button(
                    R.drawable.chevron_back,
                    colorPalette().favoritesIcon,
                    0.dp,
                    24.dp
                ).Draw()
            }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Draw() {
        val showGames by remember { mutableStateOf(false) }
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        val context = LocalContext.current

        if (showGames) Pacman()

        TopAppBar(
            title = { AppTitle( navController, context ) },
            actions = { ActionBar( navController ) },
            navigationIcon = { BackButton() },
            scrollBehavior = scrollBehavior,
            colors = colors()
        )
    }
}