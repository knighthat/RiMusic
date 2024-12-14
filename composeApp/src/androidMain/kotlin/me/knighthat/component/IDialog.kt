package me.knighthat.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import it.fast4x.rimusic.R
import it.fast4x.rimusic.ui.components.themed.DialogTextButton
import it.fast4x.rimusic.utils.semiBold
import me.knighthat.colorPalette
import me.knighthat.component.tab.toolbar.Dialog
import me.knighthat.typography

interface IDialog: Dialog {

    companion object {

        val defaultTextColors: TextFieldColors
            @Composable
            get() = TextFieldDefaults.textFieldColors(
                placeholderColor = colorPalette().textDisabled,
                cursorColor = colorPalette().text,
                textColor = colorPalette().text,
                backgroundColor = colorPalette().background1,
                focusedIndicatorColor = colorPalette().accent,
                unfocusedIndicatorColor = colorPalette().textDisabled
            )

        @Composable
        fun Default(
            title: String,
            onDismissRequest: () -> Unit,
            onConfirm: () -> Unit,
            inputArea: @Composable ColumnScope.() -> Unit,
            modifier: Modifier = Modifier
        ): Unit = Dialog( onDismissRequest = onDismissRequest ) {
            Column(
                modifier.padding( all = Dialog.OUTER_PADDING.dp )
                        .background(
                            color = colorPalette().background1,
                            shape = Dialog.SHAPE
                        )
                        .padding( vertical = Dialog.INNER_PADDING.dp )
                        .requiredHeight( Dialog.HEIGHT.dp )
            ) {
                // Dialog's title
                BasicText(
                    text = title,
                    style = typography().s.semiBold,
                    modifier = Modifier.padding(
                        vertical = Dialog.TITLE_VERTICAL_PADDING.dp,
                        horizontal = Dialog.TITLE_HORIZONTAL_PADDING.dp
                    )
                )

                // Input box
                inputArea()

                // Buttons
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // Cancel button
                    DialogTextButton(
                        text = stringResource( R.string.cancel ),
                        onClick = onDismissRequest,
                    )

                    // Confirm button
                    DialogTextButton(
                        text = stringResource( R.string.confirm ),
                        onClick = onConfirm,
                        primary = true
                    )
                }
            }
        }
    }

    val placeholder: String
        @Composable
        get() = ""

    /**
     * Triggered when user interacts with back button
     * or with something outside of this menu's scope.
     *
     * By default, this will turn off the dialog
     */
    fun onDismiss() { isActive = false }
}