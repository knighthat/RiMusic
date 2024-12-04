package it.fast4x.rimusic.enums

import androidx.annotation.StringRes
import it.fast4x.rimusic.R

enum class PlayerBackgroundColors(
    @field:StringRes override val textId: Int
): TextView {

    CoverColorGradient( R.string.bg_colors_gradient_background_from_cover ),

    ThemeColorGradient( R.string.bg_colors_gradient_background_from_theme ),

    FluidThemeColorGradient( R.string.bg_colors_fluid_gradient_background_from_theme ),

    FluidCoverColorGradient( R.string.bg_colors_fluid_gradient_background_from_cover ),

    CoverColor( R.string.bg_colors_background_from_cover ),

    BlurredCoverColor( R.string.bg_colors_blurred_cover_background ),

    ThemeColor( R.string.bg_colors_background_from_theme );
}