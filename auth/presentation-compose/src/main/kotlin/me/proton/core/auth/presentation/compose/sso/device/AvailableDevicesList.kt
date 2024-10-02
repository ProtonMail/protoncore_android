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

package me.proton.core.auth.presentation.compose.sso.device

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import me.proton.core.auth.presentation.compose.R
import me.proton.core.compose.component.DeferredCircularProgressIndicator
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.defaultSmallWeak

internal const val CONFIRMATION_CODE_FIELD_TAG = "CONFIRMATION_CODE_FIELD_TAG"

@Composable
internal fun AvailableDevicesList(
    modifier: Modifier = Modifier,
    devices: List<AvailableDeviceUIModel>?
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(top = ProtonDimens.MediumSpacing),
            text = stringResource(id = R.string.auth_login_devices_available),
            style = ProtonTypography.Default.defaultSmallWeak
        )
        LazyColumn {
            if (devices == null) {
                item {
                    DeferredCircularProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else if (devices.isEmpty()) {
                item {
                    Text(
                        modifier = Modifier
                            .padding(top = ProtonDimens.MediumSpacing),
                        text = stringResource(id = R.string.auth_login_no_devices_available),
                        style = ProtonTypography.Default.defaultSmallWeak
                    )
                }
            } else {
                items(devices, { device -> device.id }) { device ->
                    AvailableDeviceRow(device = device)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun AvailableDeviceRow(
    device: AvailableDeviceUIModel
) {
    ListItem(
        icon = {
            val iconPainterResource = when (device.clientType) {
                ClientType.Web -> painterResource(id = R.drawable.ic_proton_tv)
                ClientType.Android,
                ClientType.iOS -> painterResource(id = R.drawable.ic_proton_mobile)
            }
            Icon(
                painter = iconPainterResource,
                contentDescription = null
            )
        },
        text = {
            Text(
                text = device.authDeviceName,
                style = ProtonTypography.Default.defaultNorm
            )
        },
        secondaryText = {
            Column {
                Text(
                    text = device.localizedClientName,
                    style = ProtonTypography.Default.defaultSmallWeak
                )
                Text(
                    text = stringResource(
                        id = R.string.auth_login_device_last_used,
                        device.lastActivityReadable ?: stringResource(R.string.auth_login_not_available)
                    ),
                    style = ProtonTypography.Default.defaultSmallWeak
                )
            }
        }
    )
}
