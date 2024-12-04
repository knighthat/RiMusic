package me.knighthat.enums

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.fast4x.rimusic.R
import it.fast4x.rimusic.ui.styling.px

enum class HomeItemSize (
    val size: Int,
    @field:StringRes override val textId: Int
): TextView, Drawable {

    SMALL( 104, R.string.small ),
    MEDIUM( 132,R.string.medium ),
    BIG( 162, R.string.big );

    override val iconId = R.drawable.arrow_forward

    val dp: Dp = this.size.dp
    val px: Int
        @Composable
        get() = this.dp.px
}