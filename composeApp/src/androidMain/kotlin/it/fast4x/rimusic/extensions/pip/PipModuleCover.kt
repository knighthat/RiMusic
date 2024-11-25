package it.fast4x.rimusic.extensions.pip

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
@OptIn(UnstableApi::class)
fun PipModuleCover(
    url: String,
    modifier: Modifier = Modifier
){
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .build(),
        contentDescription = "",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .fillMaxSize()

    )
}