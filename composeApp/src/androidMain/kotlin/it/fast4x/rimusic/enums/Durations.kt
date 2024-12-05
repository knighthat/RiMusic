package it.fast4x.rimusic.enums

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import it.fast4x.rimusic.R
import me.knighthat.enums.TextView

enum class DurationInMinutes(
    val asMinutes: Int
): TextView {

    Disabled( 0 ),

    `3`( 3 ),

    `5`( 5 ),

    `10`( 10 ),

    `15`( 15 ),

    `20`( 20 ),

    `25`( 25 ),

    `30`( 30 ),

    `60`( 60 );

    val asMillis: Long = this.asMinutes *  3_600_000L

    override val text: String
        @Composable
        get() = when( this ) {
            Disabled -> stringResource( R.string.vt_disabled )
            else -> "${this.name}m"
        }
}

enum class DurationInMilliseconds(
    val asMillis: Long
): TextView {
    Disabled( 0L ),
    `100ms`( 100L ),
    `200ms`( 200L ),
    `300ms`( 300L ),
    `400ms`( 400L ),
    `500ms`( 500L ),
    `600ms`( 600L ),
    `700ms`( 700L ),
    `800ms`( 800L ),
    `900ms`( 900L ),
    `1000ms`( 1000L );

    override val text: String
        @Composable
        get() = when( this )  {
            Disabled -> stringResource( R.string.vt_disabled )
            else -> this.name
        }
}
