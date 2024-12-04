package it.fast4x.rimusic.enums

import androidx.annotation.StringRes
import it.fast4x.rimusic.R

enum class PlayEventsType(
    @field:StringRes override val textId: Int
): TextView {

    MostPlayed( R.string.by_most_played_song ),
    LastPlayed( R.string.by_last_played_song ),
    CasualPlayed( R.string.by_casual_played_song );
}