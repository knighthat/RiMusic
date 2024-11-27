package me.knighthat.enums

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

interface Drawable {

    @get:DrawableRes
    val iconId: Int

    val icon: Painter
        @Composable
        get() = painterResource( this.iconId )
}