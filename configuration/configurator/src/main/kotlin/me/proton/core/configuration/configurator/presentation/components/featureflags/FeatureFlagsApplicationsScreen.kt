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

package me.proton.core.configuration.configurator.presentation.components.featureflags;

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import me.proton.core.compose.component.ProtonSettingsHeader
import me.proton.core.compose.component.ProtonSettingsItem
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.defaultSmallNorm
import me.proton.core.configuration.configurator.R

enum class ConfigType(val displayName: String) {
    Account("Account"),
    Calendar("Calendar"),
    Common("Common"),
    Custom("Custom feature flags"),
    Drive("Drive"),
    Mail("Mail"),
    Pass("Pass"),
    Payments("Payments"),
    VPN("VPN"),
    Wallet("Wallet")
}

@Composable
fun FeatureFlagsApplicationsScreen(navController: NavController, canNavigateBack: Boolean = false) {
    Column {
        ProtonTopAppBar(
            title = { Text(stringResource(R.string.feature_flags_title)) },
            navigationIcon = if (canNavigateBack) {
                { BackButton(navController) }
            } else {
                null
            }
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                ProtonSettingsHeader(
                    title = "Apps"
                )
                ConfigType.entries.forEach { config ->
                    ProtonSettingsItem(
                        name = config.displayName,
                        hint = "${config.displayName} feature flags overview",
                        onClick = { navigateWithConfig(navController, config) },
                    )
                }
            }
        }
    }
}

private fun navigateWithConfig(navController: NavController, config: ConfigType) {
    // Navigate to the specific screen with the configuration type as a parameter
    if (config == ConfigType.Custom) {
        navController.navigate("customFlags")
    } else {
        navController.navigate("featureFlags/${config.name}")
    }
}
