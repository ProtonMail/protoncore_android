/*
 * Copyright (c) 2024 Proton AG
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

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.R

@Composable
fun ProtonCloseButton(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {}
) {
    IconButton(
        modifier = modifier,
        onClick = onCloseClicked
    ) {
        Icon(
            painterResource(id = R.drawable.ic_proton_close),
            contentDescription = stringResource(id = R.string.presentation_close)
        )
    }
}

@Preview
@Composable
fun ProtonCloseButtonPreview() {
    ProtonTheme {
        ProtonCloseButton()
    }
}
