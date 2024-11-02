package me.knighthat.component.nav

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.ui.styling.favoritesIcon
import me.knighthat.button.Button
import me.knighthat.colorPalette

@SuppressLint("ComposableNaming")
abstract class AbstractNavigationBar(
    val navController: NavController,
    val modifier: Modifier = Modifier
) {
    internal val buttonList: MutableList<@Composable () -> Unit> = mutableListOf()

    @ReadOnlyComposable
    @Composable
    internal open fun BackButton(): NavigationButton {
        val button = NavigationButton( navController, R.drawable.chevron_back, colorPalette().favoritesIcon )
        button.clickEvent { NavRoutes.back( navController ) }
        return button
    }

    @ReadOnlyComposable
    @Composable
    internal open fun SettingsButton(): NavigationButton {
        return NavigationButton(
            navController,
            R.drawable.settings,
            colorPalette().favoritesIcon,
            NavRoutes.settings.name
        )
    }

    @ReadOnlyComposable
    @Composable
    internal open fun StatsButton(): NavigationButton {
        return NavigationButton(
            navController,
            R.drawable.stats_chart,
            colorPalette().textSecondary,
            NavRoutes.statistics.name
        )
    }

    @ReadOnlyComposable
    @Composable
    internal open fun SearchButton(): NavigationButton {
        return NavigationButton(
            navController,
            R.drawable.search,
            colorPalette().textSecondary,
            NavRoutes.search.name
        )
    }

    @Composable
    abstract fun add( buttons: @Composable (@Composable (Int, String, Int) -> Unit) -> Unit )

    @Composable
    abstract fun Draw()

    @Composable
    fun buttonList(): MutableList<@Composable () -> Unit> = remember { buttonList }
}

internal class NavigationButton(
    val navController: NavController,
    iconId: Int,
    color: Color,
    val destination: String = "",
    padding: Dp = 0.dp,
    size: Dp = 0.dp,
    modifier: Modifier = Modifier
): Button( iconId, color, padding, size, modifier ) {

    fun clickEvent( event: (NavigationButton) -> Unit ) {
        modifier = modifier.clickable { event(this@NavigationButton) }
    }

    @Composable
    override fun Draw() {
        if( destination.isNotBlank() )
            clickEvent { navController.navigate( destination ) }

        super.Draw()
    }
}