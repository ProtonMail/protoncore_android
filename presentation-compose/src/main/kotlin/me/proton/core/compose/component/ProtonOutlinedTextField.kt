/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

import androidx.compose.foundation.layout.Column
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.caption
import me.proton.core.compose.theme.default

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
fun OutlinedTextFieldWithError(
    text: String,
    modifier: Modifier = Modifier,
    selection: IntRange = IntRange(text.length, text.length),
    errorText: String? = null,
    focusRequester: FocusRequester = remember { FocusRequester() },
    maxLines: Int = MaxLines,
    onValueChanged: (String) -> Unit,
) {
    val textFieldValue = remember(text, selection) {
        mutableStateOf(
            TextFieldValue(
                text = text,
                selection = TextRange(selection.first, selection.last)
            )
        )
    }
    OutlinedTextFieldWithError(
        textFieldValue = textFieldValue,
        modifier = modifier,
        errorText = errorText,
        focusRequester = focusRequester,
        maxLines = maxLines,
    ) { textField ->
        textFieldValue.value = textField
        onValueChanged(textField.text)
    }
}

@Composable
fun OutlinedTextFieldWithError(
    textFieldValue: MutableState<TextFieldValue>,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    focusRequester: FocusRequester = remember { FocusRequester() },
    maxLines: Int = MaxLines,
    onValueChanged: (TextFieldValue) -> Unit,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = textFieldValue.value,
            onValueChange = onValueChanged,
            maxLines = maxLines,
            modifier = Modifier.focusRequester(focusRequester),
            isError = errorText != null,
            textStyle = ProtonTheme.typography.default,
            colors = TextFieldDefaults.protonOutlineTextFieldColors()
        )
        Text(
            text = errorText ?: "",
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            style = ProtonTheme.typography.caption,
            color = ProtonTheme.colors.notificationError
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
    OutlinedTextFieldWithError(text = "Some text", onValueChanged = {}, errorText = "Validation failed!")
}

private const val MaxLines = 2
