package player.frame

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.unit.IntSize
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skia.Bitmap
import rimusic.composeapp.generated.resources.Res
import rimusic.composeapp.generated.resources.app_icon

@Composable
fun FrameContainer(
    modifier: Modifier = Modifier,
    size: IntSize,
    bytes: ByteArray?,
) {
    val bitmap by remember(size) {
        derivedStateOf {
            if (size.width > 0 && size.height > 0) Bitmap().apply {
                allocN32Pixels(size.width, size.height, true)
            }
            else null
        }
    }
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        bitmap?.let { bitmap ->
            bytes?.let { bytes ->
                Image(
                    bitmap = bitmap.run {
                        installPixels(bytes)
                        asComposeImageBitmap()
                    },
                    contentDescription = "frame"
                )
            }
        } ?: Image(
            painter = painterResource(Res.drawable.app_icon),
            colorFilter = ColorFilter.tint(Color.Green.copy(alpha = 0.6f)),
            contentDescription = "Logo",
            modifier = Modifier.fillMaxSize(0.7f)
        )
            //CircularProgressIndicator()
    }
}