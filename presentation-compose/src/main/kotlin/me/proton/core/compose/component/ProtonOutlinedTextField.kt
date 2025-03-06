/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.proton.core.compose.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionNorm
import me.proton.core.compose.theme.defaultNorm

const val PROTON_OUTLINED_TEXT_INPUT_TAG = "PROTON_OUTLINED_TEXT_INPUT_TAG"
private const val MinLines = 1
private const val MaxLines = 2

@Composable
fun TextFieldDefaults.protonOutlineTextFieldColors(): TextFieldColors =
    outlinedTextFieldColors(
        textColor = ProtonTheme.colors.textNorm,
        backgroundColor = ProtonTheme.colors.backgroundSecondary,

        focusedLabelColor = ProtonTheme.colors.textNorm,
        focusedBorderColor = ProtonTheme.colors.brandNorm,

        unfocusedLabelColor = ProtonTheme.colors.textHint,
        unfocusedBorderColor = ProtonTheme.colors.backgroundSecondary,

        disabledLabelColor = ProtonTheme.colors.textDisabled,
        disabledBorderColor = ProtonTheme.colors.backgroundSecondary,

        errorLabelColor = ProtonTheme.colors.notificationError,
        errorBorderColor = ProtonTheme.colors.notificationError,
    )

@Composable
fun ProtonOutlinedTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = ProtonTheme.shapes.medium,
    colors: TextFieldColors = TextFieldDefaults.protonOutlineTextFieldColors(),
) = OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    textStyle = textStyle,
    label = label,
    placeholder = placeholder,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    isError = isError,
    visualTransformation = visualTransformation,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    singleLine = singleLine,
    maxLines = maxLines,
    interactionSource = interactionSource,
    shape = shape,
    colors = colors,
)

@Composable
fun ProtonOutlinedTextFieldWithError(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    errorText: String? = null,
    helpText: String? = null,
    requestFocus: () -> Boolean = { false },
    onFocusChanged: (Boolean) -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    label: (@Composable () -> Unit)? = null,
    minLines: Int = MinLines,
    maxLines: Int = MaxLines,
    placeholder: (@Composable () -> Unit)? = null,
    singleLine: Boolean = false,
    onValueChanged: (String) -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val focusRequester = remember { FocusRequester() }
    var textFieldLoaded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        OutlinedTextField(
            value = text,
            onValueChange = onValueChanged,
            colors = TextFieldDefaults.protonOutlineTextFieldColors(),
            enabled = enabled,
            isError = errorText != null,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            label = label,
            minLines = minLines,
            maxLines = maxLines,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onGloballyPositioned {
                    if (!textFieldLoaded) {
                        if (requestFocus()) {
                            focusRequester.requestFocus()
                        }
                        textFieldLoaded = true
                    }
                }
                .onFocusChanged { onFocusChanged(it.isFocused) }
                .testTag(PROTON_OUTLINED_TEXT_INPUT_TAG),
            placeholder = placeholder,
            singleLine = singleLine,
            textStyle = ProtonTheme.typography.defaultNorm,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation
        )
        Text(
            text = helpText ?: errorText ?: "",
            maxLines = maxLines,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = ProtonDimens.ExtraSmallSpacing),
            overflow = TextOverflow.Ellipsis,
            style = ProtonTheme.typography.captionNorm,
            color = if (helpText != null) {
                ProtonTheme.colors.textWeak
            } else {
                ProtonTheme.colors.notificationError
            }
        )
    }
}

@Preview
@Composable
fun PreviewOutlinedTextFieldWithProtonColors() {
    OutlinedTextField(
        value = "Some text",
        onValueChange = {},
        colors = TextFieldDefaults.protonOutlineTextFieldColors()
    )
}

@Preview
@Composable
fun PreviewOutlinedTextFieldWithError() {
    ProtonOutlinedTextFieldWithError(
        text = "Some text",
        onValueChanged = {},
        errorText = "Validation failed!"
    )
}

@Preview
@Composable
fun PreviewOutlinedTextFieldWithHelp() {
    ProtonOutlinedTextFieldWithError(
        text = "Some text",
        onValueChanged = {},
        helpText = "Field info"
    )
}
