package it.fast4x.rimusic.enums

import androidx.annotation.StringRes
import it.fast4x.rimusic.R

enum class AlbumsType(
    @field:StringRes override val textId: Int
): TextView {

    Favorites( R.string.favorites ),
    Library( R.string.library );
}