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

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.compose.R

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun BoxWithNotificationDot(
    modifier: Modifier = Modifier,
    notificationDotVisible: Boolean = false,
    content: @Composable () -> Unit
) {
    BadgedBox(
        badge = {
            if (notificationDotVisible) {
                Badge(
                    containerColor = ProtonTheme.colors.notificationError,
                    modifier = Modifier.size(ProtonDimens.NotificationDotIconSize)
                )
            }
        },
        modifier = modifier
    ) {
        content()
    }
}

@Composable
@Preview
private fun IconButtonWithDot() {
    IconButton(
        onClick = {}
    ) {
        BoxWithNotificationDot(
            notificationDotVisible = true
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_proton_hamburger),
                contentDescription = null,
            )
        }
    }
}

@Composable
@Preview
private fun IconButtonWithoutDot() {
    IconButton(
        onClick = {}
    ) {
        BoxWithNotificationDot(
            notificationDotVisible = false
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_proton_hamburger),
                contentDescription = null,
            )
        }
    }
}
