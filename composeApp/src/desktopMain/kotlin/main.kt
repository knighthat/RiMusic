import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import it.fast4x.rimusic.getAsyncImageLoader
import it.fast4x.rimusic.ui.ThreeColumnsApp
import it.fast4x.rimusic.ui.theme.DesktopTheme
import org.jetbrains.compose.resources.painterResource
import rimusic.composeapp.generated.resources.Res
import rimusic.composeapp.generated.resources.app_icon


@OptIn(ExperimentalCoilApi::class)
fun main() = application {
    //val main = Dispatchers.Main
    setSingletonImageLoaderFactory { context ->
        getAsyncImageLoader(context)
    }
    Window(
       icon = painterResource(Res.drawable.app_icon),
        onCloseRequest = ::exitApplication,
        state = WindowState(
            placement = WindowPlacement.Maximized,
        ),
        title = "RiMusic MP",
    ) {
        //App(MusicDatabaseDesktop)
        DesktopTheme {
            ThreeColumnsApp()
        }

    }
}