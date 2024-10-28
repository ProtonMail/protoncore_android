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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.tooling.preview.Preview
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.entity.AuthDevicePlatform
import me.proton.core.auth.presentation.compose.R
import me.proton.core.compose.component.DeferredCircularProgressIndicator
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.defaultSmallWeak

internal const val CONFIRMATION_CODE_FIELD_TAG = "CONFIRMATION_CODE_FIELD_TAG"

@Composable
internal fun AuthDeviceList(
    modifier: Modifier = Modifier,
    devices: List<AuthDeviceData>? = null,
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(top = ProtonDimens.MediumSpacing),
            text = stringResource(id = R.string.auth_login_devices_available),
            style = ProtonTypography.Default.defaultSmallWeak
        )
        AuthDeviceLazyColumn(devices = devices)
    }
}

@Composable
internal fun AuthDeviceLazyColumn(
    modifier: Modifier = Modifier,
    devices: List<AuthDeviceData>? = null,
    onItemClicked: ((AuthDeviceData) -> Unit)? = null
) {
    LazyColumn(modifier = modifier) {
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
            items(devices, { device -> device.deviceId.id }) { device ->
                AuthDeviceListItem(
                    modifier = Modifier.clickable(enabled = onItemClicked != null) {
                        onItemClicked?.invoke(device)
                    },
                    device = device
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun AuthDeviceListItem(
    device: AuthDeviceData?,
    modifier: Modifier = Modifier,
    iconVisible: Boolean = true,
    nameVisible: Boolean = true,
    clientNameVisible: Boolean = true,
    lastActivityVisible: Boolean = true,
    trailing: @Composable (() -> Unit)? = null,
) {
    ListItem(
        modifier = modifier,
        icon = {
            if (iconVisible) {
                Icon(
                    modifier = Modifier.size(ProtonDimens.DefaultIconSize),
                    painter = when (device?.platform) {
                        null,
                        AuthDevicePlatform.Android,
                        AuthDevicePlatform.IOS -> painterResource(id = R.drawable.ic_proton_mobile)

                        else -> painterResource(id = R.drawable.ic_proton_tv)
                    },
                    contentDescription = null
                )
            }
        },
        text = {
            if (nameVisible) {
                Text(
                    text = device?.name ?: "",
                    style = ProtonTypography.Default.defaultNorm
                )
            }
        },
        secondaryText = {
            Column {
                if (clientNameVisible) {
                    Text(
                        text = device?.localizedClientName ?: "",
                        style = ProtonTypography.Default.defaultSmallWeak
                    )
                }
                if (lastActivityVisible) {
                    Text(
                        text = device?.lastActivityReadable ?: stringResource(R.string.auth_login_not_available),
                        style = ProtonTypography.Default.defaultSmallWeak
                    )
                }
            }
        },
        trailing = { trailing?.invoke() }
    )
}

@Composable
internal fun TrailingIcon(expanded: Boolean) {
    Icon(
        painter = when (expanded) {
            false -> painterResource(R.drawable.ic_proton_chevron_down)
            true -> painterResource(R.drawable.ic_proton_chevron_up)
        },
        contentDescription = null
    )
}


@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
public fun AvailableDeviceListPreview() {
    ProtonTheme {
        AuthDeviceList(
            modifier = Modifier.padding(ProtonDimens.SmallSpacing),
            devices = listOf(
                AuthDeviceData(
                    deviceId = AuthDeviceId("1"),
                    name = "Google Pixel 8",
                    localizedClientName = "Proton Pass for Android",
                    platform = AuthDevicePlatform.Android,
                    lastActivityTime = 0,
                    lastActivityReadable = "Last used 24 minutes ago"
                ),
                AuthDeviceData(
                    deviceId = AuthDeviceId("2"),
                    name = "Chrome, Linux",
                    localizedClientName = "Proton Pass for Web",
                    platform = AuthDevicePlatform.Web,
                    lastActivityTime = 0,
                    lastActivityReadable = "Last used 4 minutes ago"
                )
            )
        )
    }
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
public fun AvailableDeviceListItemPreview() {
    ProtonTheme {
        AuthDeviceListItem(
            modifier = Modifier.padding(ProtonDimens.SmallSpacing),
            device = AuthDeviceData(
                deviceId = AuthDeviceId(""),
                name = "Google Pixel 8",
                localizedClientName = "Proton Pass for Android",
                platform = AuthDevicePlatform.Android,
                lastActivityTime = 0,
                lastActivityReadable = "Last used 24 minutes ago"
            )
        )
    }
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
public fun AvailableDeviceListItemWithTrailingPreview() {
    ProtonTheme {
        AuthDeviceListItem(
            modifier = Modifier.padding(ProtonDimens.SmallSpacing),
            device = AuthDeviceData(
                deviceId = AuthDeviceId(""),
                name = "Google Pixel 8",
                localizedClientName = "Proton Pass for Android",
                platform = AuthDevicePlatform.Android,
                lastActivityTime = 0,
                lastActivityReadable = "Last used 24 minutes ago"
            ),
            trailing = { TrailingIcon(expanded = false) }
        )
    }
}
