package it.fast4x.rimusic.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import rimusic.composeapp.generated.resources.Res
import rimusic.composeapp.generated.resources.arrow_forward

@Composable
fun Title2Actions(
    title: String,
    modifier: Modifier = Modifier,
    icon1: DrawableResource? = Res.drawable.arrow_forward,
    icon2: DrawableResource? = Res.drawable.arrow_forward,
    onClick1: (() -> Unit)? = null,
    onClick2: (() -> Unit)? = null,
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            .clickable(enabled = onClick1 != null) {
                onClick1?.invoke()
            }
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = Color.White

        )
        if (onClick2 != null) {
            Image(
                painter = painterResource(icon2 ?: Res.drawable.arrow_forward),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier
                    .clickable {
                        onClick2.invoke()
                    }
                    .padding(end = 12.dp)
                    .size(20.dp)
            )
        }

        if (onClick1 != null) {
            Image(
                painter = painterResource(icon1 ?: Res.drawable.arrow_forward),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier
                    .clickable {
                    onClick1.invoke()
                }
            )
        }

    }
}