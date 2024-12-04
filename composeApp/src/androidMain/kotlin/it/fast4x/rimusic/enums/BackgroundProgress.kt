package it.fast4x.rimusic.enums;

import androidx.annotation.StringRes
import it.fast4x.rimusic.R

enum class BackgroundProgress(
    @field:StringRes override val textId: Int
): TextView {

    Player( R.string.player ),

    MiniPlayer( R.string.minimized_player ),

    Both( R.string.both ),

    Disabled( R.string.vt_disabled );
}
