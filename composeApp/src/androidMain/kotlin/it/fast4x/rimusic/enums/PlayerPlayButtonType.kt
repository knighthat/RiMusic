package it.fast4x.rimusic.enums

import androidx.annotation.StringRes
import it.fast4x.rimusic.R
import me.knighthat.enums.TextView

enum class PlayerPlayButtonType(
    val width: Int,
    val height: Int,
    @field:StringRes override val textId: Int
): TextView {

    Disabled( 60, 60, R.string.vt_disabled ),

    Default( 60, 60, R.string._default ),

    Rectangular( 110, 70, R.string.rectangular ),

    CircularRibbed( 100, 100, R.string.circular_ribbed ),

    Square( 80, 80, R.string.square );
}