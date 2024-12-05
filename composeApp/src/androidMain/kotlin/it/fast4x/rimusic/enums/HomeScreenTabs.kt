package it.fast4x.rimusic.enums

import androidx.annotation.StringRes
import it.fast4x.rimusic.R
import me.knighthat.enums.TextView

enum class HomeScreenTabs(
    val index: Int,
    @field:StringRes override val textId: Int
): TextView {

    Default( 100, R.string._default ),

    QuickPics( 0, R.string.quick_picks ),

    Songs( 1, R.string.songs ),

    Artists( 2, R.string.albums ),

    Albums( 3, R.string.artists ),

    Playlists( 4, R.string.playlists ),

    Search( 5, R.string.search );
}