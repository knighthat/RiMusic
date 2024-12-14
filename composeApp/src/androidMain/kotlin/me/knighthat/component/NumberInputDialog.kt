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

interface NumberInputDialog: IDialog {

    companion object {
        @JvmField
        val ONLY_NUMERICAL_CHARS_REGEX = Regex("^[+-]?(\\d*\\.)?\\d+\$")
    }

    val minValue: Double
        get() = Double.MIN_VALUE
    val maxValue: Double
        get() = Double.MAX_VALUE

    var value: Double

    /**
     * What happens when user hits "Confirm" button
     */
    fun onSet( newValue: Double )

    @Composable
    override fun Render() {
        if(!isActive) return

        var textField by remember {
            mutableStateOf( TextFieldValue(this.value.toString()) )
        }
        var isError by rememberSaveable { mutableStateOf(false) }
        var errorMessageId by rememberSaveable {
            mutableIntStateOf(R.string.value_cannot_be_empty)
        }

        IDialog.Default(
            title = dialogTitle,
            onDismissRequest = ::onDismiss,
            onConfirm = {
                if( textField.text.isEmpty() ) {
                    errorMessageId = R.string.value_cannot_be_empty
                    isError = true

                    return@Default
                }
                
                if( !textField.text.matches(ONLY_NUMERICAL_CHARS_REGEX) ) {
                    errorMessageId = R.string.not_a_number
                    isError = true

                    return@Default
                }

                val inputValue = textField.text.toDouble()

                if(inputValue < minValue || inputValue > maxValue) {
                    errorMessageId = R.string.number_out_of_range
                    isError = true

                    return@Default
                }

                isError = false
                onSet(inputValue)
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
                        placeholder = { Text(placeholder) },
                        colors = IDialog.defaultTextColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(.9f),
                        isError = isError,
                        onValueChange = {
                            // Disable error message once user starts typing
                            if( isError && it.text.isNotEmpty() )
                                isError = false

                            textField = it

                            if( textField.text.matches(ONLY_NUMERICAL_CHARS_REGEX) )
                                this@NumberInputDialog.value = it.text.toDouble()
                        }
                    )
                }

                // [Row] is specifically placed before [AnimatedVisibility]
                // to ensure space between input area and buttons remains unchanged.
                Row(
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    AnimatedVisibility(isError) {
                        BasicText(
                            text = stringResource( errorMessageId, minValue, maxValue ),
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