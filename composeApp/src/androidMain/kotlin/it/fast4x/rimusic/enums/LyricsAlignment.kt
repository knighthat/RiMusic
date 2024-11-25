package it.fast4x.rimusic.enums

import androidx.compose.ui.text.style.TextAlign

enum class LyricsAlignment {
    Left,
    Center,
    Right;

    val selected: TextAlign
        get() = when (this) {
            Left -> TextAlign.Start
            Center -> TextAlign.Center
            Right -> TextAlign.End
        }
}