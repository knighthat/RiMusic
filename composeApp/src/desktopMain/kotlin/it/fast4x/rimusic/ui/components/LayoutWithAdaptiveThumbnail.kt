package it.fast4x.rimusic.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import it.fast4x.rimusic.enums.ThumbnailRoundness
import org.jetbrains.compose.resources.painterResource
import rimusic.composeapp.generated.resources.Res
import rimusic.composeapp.generated.resources.loader

@Composable
inline fun LayoutWithAdaptiveThumbnail(
    thumbnailContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    content()
}

fun adaptiveThumbnailContent(
    isLoading: Boolean,
    url: String?,
    shape: Shape? = null,
    showIcon: Boolean = false,
    onOtherVersionAvailable: (() -> Unit)? = {},
    onClick: (() -> Unit)? = {}
): @Composable () -> Unit = {
    BoxWithConstraints(contentAlignment = Alignment.Center) {


        val modifier = Modifier
            //.padding(all = 16.dp)
            //.padding(horizontal = playerThumbnailSize.size.dp)
            .padding(top = 16.dp)
            .clip(shape ?: ThumbnailRoundness.Medium.shape())
            .clickable {
                if (onClick != null) {
                    onClick()
                }
            }

        if (isLoading) {
            Image(
                painter = painterResource(Res.drawable.loader),
                colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.6f)),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
            )
        } else {
            AsyncImage(
                model = url, //.thumbnail(thumbnailSize),
                contentDescription = null,
                modifier = modifier
            )
            /*
            if(showIcon)
                onOtherVersionAvailable?.let {
                    Box(
                        modifier = modifier
                            .align(Alignment.BottomEnd)
                            .fillMaxWidth(0.2f)
                    ) {
                        HeaderIconButton(
                            icon = R.drawable.alternative_version,
                            color = colorPalette().text,
                            onClick = {
                                onOtherVersionAvailable()
                            },
                            modifier = Modifier.size(35.dp)
                        )
                    }
                }
             */

        }
    }
}
