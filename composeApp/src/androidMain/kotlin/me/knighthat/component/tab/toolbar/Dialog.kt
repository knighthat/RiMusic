package me.knighthat.component.tab.toolbar

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

interface Dialog {

    companion object {

        const val OUTER_PADDING = 10
        const val INNER_PADDING = 10
        const val HEIGHT = 190
        const val TITLE_VERTICAL_PADDING = 8
        const val TITLE_HORIZONTAL_PADDING = 24
        val SHAPE = RoundedCornerShape( 8.dp )
    }

    var isActive: Boolean
    @get:Composable
    val dialogTitle: String

    @Composable
    fun Render()

    /**
     * What happens when user taps on icon.
     *
     * By default, this action enables dialog
     * by assigning `true` to [isActive]
     */
    fun onShortClick() { isActive = true }
}