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

package me.proton.core.auth.presentation.compose.confirmationcode

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.proton.core.auth.presentation.compose.R
import me.proton.core.auth.presentation.compose.SMALL_SCREEN_HEIGHT
import me.proton.core.compose.component.DeferredCircularProgressIndicator
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme

@Composable
internal fun ConfirmationDigits(
    modifier: Modifier = Modifier,
    digits: List<Char>?,
    @StringRes titleText: Int = R.string.auth_login_confirmation_code
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ProtonDimens.SmallSpacing))
            .border(BorderStroke(1.dp, ProtonTheme.colors.separatorNorm))
            .padding(ProtonDimens.DefaultSpacing)
    ) {
        Column(
            modifier = Modifier
                .padding(start = ProtonDimens.DefaultSpacing)
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(id = titleText),
                style = ProtonTheme.typography.body2Regular
            )
            if (digits == null) {
                DeferredCircularProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (digit in digits) {
                        ConfirmationCodeDigit(digit = digit)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmationCodeDigit(
    digit: Char
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(ProtonDimens.ExtraSmallSpacing)
            .size(height = 52.dp, width = 41.dp)
            .clip(RoundedCornerShape(ProtonDimens.SmallSpacing))
            .background(ProtonTheme.colors.backgroundSecondary)
    ) {
        Text(
            text = digit.toString(),
            style = ProtonTheme.typography.hero
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
