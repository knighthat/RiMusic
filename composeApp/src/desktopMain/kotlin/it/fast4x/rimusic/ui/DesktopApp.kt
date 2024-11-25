package it.fast4x.rimusic.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import it.fast4x.rimusic.ui.screens.ArtistsScreen
import org.jetbrains.compose.resources.painterResource
import rimusic.composeapp.generated.resources.Res
import rimusic.composeapp.generated.resources.app_icon
import rimusic.composeapp.generated.resources.app_logo_text


@Composable
fun DesktopApp(
    navController: NavHostController = rememberNavController()
) {

    //MusicDatabaseDesktop.getAllSongs()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = backStackEntry?.destination?.route ?: "artists"

    Scaffold(
        topBar = {
            DesktopTopAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        },
        bottomBar = {
            DesktopBottomAppBar()
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = "quickpics",
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            composable(route = "artists") {
                VerticalLayout(
                    navController = navController
                ) {
                    ArtistsScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }

            composable(route = "quickpics") {
                VerticalLayout(
                    navController = navController
                ) {
                    /*
                    QuickPicsScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                     */
                }
            }



        }

    }


}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DesktopTopAppBar(
    currentScreen: String = "home",
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(Res.drawable.app_icon),
                    colorFilter = ColorFilter.tint(Color.White),
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {}
                        )
                )
                Image(
                    painter = painterResource(Res.drawable.app_logo_text),
                    colorFilter = ColorFilter.tint( Color.White
                        /*
                        when (colorPaletteMode) {
                            ColorPaletteMode.Light, ColorPaletteMode.System -> colorPalette.text
                            else -> Color.White
                        }

                         */
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clickable {}
                )
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            /*
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }

             */
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DesktopBottomAppBar(
    modifier: Modifier = Modifier
) {
    BottomAppBar {

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(Res.drawable.app_icon),
                colorFilter = ColorFilter.tint(Color.White),
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
            )
            Image(
                painter = painterResource(Res.drawable.app_logo_text),
                colorFilter = ColorFilter.tint(
                    Color.White
                    /*
                        when (colorPaletteMode) {
                            ColorPaletteMode.Light, ColorPaletteMode.System -> colorPalette.text
                            else -> Color.White
                        }

                         */
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
            )
        }
    }
}

@Composable
fun VerticalLayout(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp),
    ) {
        Text("Menu")
        VerticalMenuBuilder(navController)
    }
    Column(
        modifier = modifier
            .fillMaxSize(),
    ) {
        content()
    }
}

@Composable
fun VerticalMenuBuilder(navController: NavHostController) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 16.dp)
    ) {
        Text("Quick Pics",
            modifier = Modifier
                .padding(16.dp)
                .clickable {
                    navController.navigate("quickpics")
                }
        )
        Text("Artists",
            modifier = Modifier
                .padding(16.dp)
                .clickable {
                    navController.navigate("artists")
                }
        )
    }
}