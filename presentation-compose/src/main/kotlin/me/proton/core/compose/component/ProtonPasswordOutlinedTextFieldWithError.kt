/*
 * Copyright (c) 2024 Proton Technologies AG
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

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonDimens.DefaultSpacing
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionNorm
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.presentation.compose.R

private const val MaxLines = 2

@Composable
@Suppress("LongParameterList")
fun ProtonPasswordOutlinedTextFieldWithError(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    passwordVisible: Boolean = false,
    errorText: String? = null,
    requestFocus: () -> Boolean = { false },
    onFocusChanged: (Boolean) -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Password
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    label: (@Composable () -> Unit)? = null,
    maxLines: Int = MaxLines,
    placeholder: (@Composable () -> Unit)? = null,
    errorContent: (@Composable (errorText: String?) -> Unit) = { msg ->
        ProtonTextFieldError(
            msg,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = ProtonDimens.ExtraSmallSpacing),
            maxLines = maxLines
        )
    },
    singleLine: Boolean = false,
    onValueChanged: (String) -> Unit,
    readOnly: Boolean = false,
) {
    var passwordVisualTransformationEnabled by remember(passwordVisible) {
        mutableStateOf(passwordVisible)
    }
    val focusRequester = remember { FocusRequester() }
    var textFieldLoaded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        OutlinedTextField(
            value = text,
            onValueChange = onValueChanged,
            colors = TextFieldDefaults.protonOutlineTextFieldColors(),
            enabled = enabled,
            readOnly = readOnly,
            isError = errorText != null,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            label = label,
            trailingIcon = {
                val icon = when (passwordVisualTransformationEnabled) {
                    true -> R.drawable.ic_proton_eye_slash
                    false -> R.drawable.ic_proton_eye
                }
                IconButton(
                    onClick = { passwordVisualTransformationEnabled = !passwordVisualTransformationEnabled },
                    content = { Icon(painterResource(icon), null) }
                )
            },
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
            visualTransformation = when (passwordVisualTransformationEnabled) {
                true -> VisualTransformation.None
                else -> PasswordVisualTransformation()
            }
        )
        errorContent(errorText)
    }
}

@Preview(name = "Light mode")
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ProtonPasswordOutlinedTextFieldWithErrorPreview() {
    ProtonTheme {
        ProtonPasswordOutlinedTextFieldWithError(
            text = "password",
            onValueChanged = {},
            enabled = true,
            label = { Text(text = "Password") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = DefaultSpacing)
        )
    }
}

@Preview(name = "Light mode")
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ProtonPasswordOutlinedTextFieldWithErrorPreviewPasswordVisible() {
    ProtonTheme {
        ProtonPasswordOutlinedTextFieldWithError(
            text = "password",
            onValueChanged = {},
            enabled = true,
            passwordVisible = true,
            label = { Text(text = "Password") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = DefaultSpacing)
        )
    }
}
