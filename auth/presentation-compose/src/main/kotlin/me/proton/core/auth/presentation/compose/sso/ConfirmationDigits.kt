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
package me.proton.core.auth.presentation.compose.sso

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.proton.core.auth.presentation.compose.R
import me.proton.core.auth.presentation.compose.SMALL_SCREEN_HEIGHT
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm

@Composable
internal fun ConfirmationDigits(
    modifier: Modifier = Modifier,
    digits: List<Char>?,
    @StringRes titleText: Int = R.string.auth_login_confirmation_code,
    border: BorderStroke = BorderStroke(1.dp, ProtonTheme.colors.separatorNorm),
    digitStyle: TextStyle = ProtonTheme.typography.hero
) {
    Card(
        modifier = modifier,
        backgroundColor = Color.Transparent,
        contentColor = ProtonTheme.colors.textNorm,
        border = border,
        elevation = 0.dp
    ) {
        Box(
            modifier = modifier.padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(start = ProtonDimens.DefaultSpacing)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(id = titleText),
                    style = ProtonTheme.typography.body2Medium
                )
                Spacer(modifier = Modifier.size(ProtonDimens.SmallSpacing))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (digit in digits ?: listOf(' ', ' ', ' ', ' ')) {
                        ConfirmationCodeDigit(digit = digit, textStyle = digitStyle)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmationCodeDigit(
    digit: Char,
    textStyle: TextStyle = ProtonTheme.typography.hero
) {
    Card(
        modifier = Modifier
            .padding(ProtonDimens.ExtraSmallSpacing)
            .size(height = 52.dp, width = 41.dp),
        backgroundColor = ProtonTheme.colors.backgroundSecondary,
        contentColor = ProtonTheme.colors.textNorm,
        elevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                textAlign = TextAlign.Center,
                text = digit.toString(),
                style = textStyle
            )
        }
    }
}

@Composable
internal fun ConfirmationDigitTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    focusRequester: FocusRequester = remember { FocusRequester() },
    textStyle: TextStyle = TextStyle.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1
) {
    var focused: Boolean by rememberSaveable { mutableStateOf(false) }
    BasicTextField(
        value = value.padEnd(4),
        onValueChange = onValueChange,
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { focused = it.isFocused },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        singleLine = true,
        maxLines = maxLines,
        minLines = minLines,
    ) {
        ConfirmationDigits(
            digits = value.padEnd(4).toList(),
            border = when (focused) {
                true -> BorderStroke(1.dp, ProtonTheme.colors.interactionNorm)
                false -> BorderStroke(1.dp, ProtonTheme.colors.separatorNorm)
            },
            digitStyle = ProtonTheme.typography.defaultNorm
        )
    }
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ConfirmationDigitTextFieldPreview() {
    ProtonTheme {
        ConfirmationDigitTextField(
            value = "64S",
            onValueChange = {}
        )
    }
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun ConfirmationDigitsPreview() {
    ProtonTheme {
        ConfirmationDigits(digits = listOf('6', '4', 'S', '3'))
    }
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun ConfirmationDigitsLoadingPreview() {
    ProtonTheme {
        ConfirmationDigits(digits = null)
    }
}
