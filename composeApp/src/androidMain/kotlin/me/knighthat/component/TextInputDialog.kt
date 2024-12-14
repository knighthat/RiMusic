package me.knighthat.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import it.fast4x.rimusic.R
import it.fast4x.rimusic.utils.medium
import me.knighthat.typography

interface TextInputDialog: IDialog {

    val allowEmpty: Boolean

    var value: String

    /**
     * What happens when user hits "Confirm" button
     */
    fun onSet( newValue: String )

    @Composable
    override fun Render() {
        if( !isActive ) return

        var textField by remember { mutableStateOf(
            TextFieldValue( this.value )
        ) }
        var isError by rememberSaveable { mutableStateOf( false ) }
        var errorMessageId by rememberSaveable {
            mutableIntStateOf( R.string.value_cannot_be_empty )
        }

        IDialog.Default(
            title = dialogTitle,
            onDismissRequest = ::onDismiss,
            onConfirm = {
                if( textField.text.isEmpty() && !allowEmpty ) {
                    errorMessageId = R.string.value_cannot_be_empty
                    isError = true

                    return@Default
                }

                isError = false
                onSet( this.value )
            },
            inputArea = {
                // Text input for users
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = textField,
                        singleLine = true,
                        placeholder = { Text( placeholder ) },
                        colors = IDialog.defaultTextColors,
                        keyboardOptions = KeyboardOptions( keyboardType = KeyboardType.Text ),
                        modifier = Modifier.fillMaxWidth(.9f ),
                        isError = isError,
                        onValueChange = {
                            // Disable error message once user starts typing
                            if( isError && it.text.isNotEmpty() )
                                isError = false

                            textField = it
                            this@TextInputDialog.value = it.text
                        }
                    )
                }

                // [Row] is specifically placed before [AnimatedVisibility]
                // to ensure space between input area and buttons remains unchanged.
                Row(
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth().weight( 1f )
                ) {
                    AnimatedVisibility( isError ) {
                        BasicText(
                            text = stringResource( errorMessageId ),
                            style = typography().xs.medium.copy(
                                color = Color.Red
                            ),
                            modifier = Modifier.padding(
                                vertical = 8.dp,
                                horizontal = 24.dp
                            )
                        )
                    }
                }
            }
        )
    }
}