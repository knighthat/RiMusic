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
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import rimusic.composeapp.generated.resources.Res
import rimusic.composeapp.generated.resources.arrow_forward

@Composable
fun Title(
    title: String,
    modifier: Modifier = Modifier,
    icon: DrawableResource = Res.drawable.arrow_forward,
    onClick: (() -> Unit)? = null,
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            .clickable(enabled = onClick != null) {
                onClick?.invoke()
            }
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = typography.titleMedium.fontSize,
                fontWeight = typography.titleMedium.fontWeight,
                color = Color.White,
                textAlign = TextAlign.Start
            ),
            modifier = Modifier.weight(1f)

        )

        if (onClick != null) {
            Image(
                painter = org.jetbrains.compose.resources.painterResource(icon),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier
                    .clickable {}
                    .padding(end = 12.dp)
                    .size(20.dp)
            )
        }
    }
}