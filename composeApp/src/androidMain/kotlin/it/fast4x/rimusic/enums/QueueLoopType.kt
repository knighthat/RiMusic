package it.fast4x.rimusic.enums

import androidx.annotation.DrawableRes
import androidx.media3.common.Player
import it.fast4x.rimusic.R
import me.knighthat.enums.Drawable

enum class QueueLoopType(
    @field:DrawableRes override val iconId: Int
): Drawable {

    Default( R.drawable.repeat ),
    RepeatOne( R.drawable.repeatone ),
    RepeatAll( R.drawable.infinite );

    val type: Int
        get() = when (this) {
        Default -> Player.REPEAT_MODE_OFF
        RepeatOne -> Player.REPEAT_MODE_ONE
        RepeatAll -> Player.REPEAT_MODE_ALL
    }

    companion object {
        @JvmStatic
        fun from(value: Int): QueueLoopType {
            return when (value) {
                Player.REPEAT_MODE_OFF -> Default
                Player.REPEAT_MODE_ONE -> RepeatOne
                Player.REPEAT_MODE_ALL -> RepeatAll
                else -> Default
            }
        }
    }
}