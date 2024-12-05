package it.fast4x.rimusic.enums

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import it.fast4x.rimusic.R

enum class ExoPlayerDiskCacheMaxSize(
    val megabytes: Int
): TextView {

    `Disabled`( 1 ),
    
    `32MB`( 32 ),
    
    `512MB`( 512 ),
    
    `1GB`( 1024 ),
    
    `2GB`( 2048 ),
    
    `4GB`( 4096 ),
    
    `8GB`( 8192 ),
    
    Unlimited( 0 ),
    
    Custom( 1_000_000 );        // YES! This is a valid format in Kotlin

    val bytes: Long = this.megabytes * 1000L * 1000

    override val text: String
        @Composable
        get() = when ( this ) {
            Disabled -> stringResource( R.string.turn_off )
            Unlimited -> stringResource( R.string.unlimited )
            Custom -> stringResource( R.string.custom )
            else -> this.name
        }
}
