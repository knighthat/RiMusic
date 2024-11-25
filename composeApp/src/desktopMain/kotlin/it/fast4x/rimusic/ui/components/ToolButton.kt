package it.fast4x.rimusic.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun ToolButton(
    icon: DrawableResource,
    onAction: () -> Unit,
    enabled: Boolean = true,
    size: Dp = 28.dp
) =
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clickable{
                onAction()
            }
            .sizeIn(size+12.dp, size+12.dp)
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(size/2))
    ){
        Image(
            painter = painterResource(icon),
            colorFilter = ColorFilter.tint(if (enabled) Color.White else Color.Gray),
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .padding(horizontal = 4.dp)

        )
    }
