package player.frame

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import it.fast4x.rimusic.ui.bars.DefaultBottomBar
import player.PlayerController

@Composable
fun FramePlayer(
    modifier: Modifier = Modifier,
    url: String,
    size: IntSize,
    bytes: ByteArray?,
    controller: PlayerController,
    showControls: Boolean = true,
    showFrame: Boolean = true
) {
    //if (url.isEmpty()) return

    DisposableEffect(url) {
        controller.load(url)
        controller.play()
        onDispose { controller.dispose() }
    }
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (showFrame)
            FrameContainer(Modifier, size, bytes)
        if (showControls)
            //DefaultControls(Modifier, controller)
            DefaultBottomBar(Modifier, controller)
    }
}