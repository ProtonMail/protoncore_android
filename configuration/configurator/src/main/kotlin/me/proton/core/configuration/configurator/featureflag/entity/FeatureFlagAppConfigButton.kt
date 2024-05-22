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

package me.proton.core.configuration.configurator.featureflag.entity;

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.configuration.configurator.R
import java.util.regex.Pattern

enum class ConfigType(val displayName: String) {
    Calendar("Calendar"),
    Drive("Drive"),
    Mail("Mail"),
    Pass("Pass"),
    VPN("VPN"),
    Wallet("Wallet"),
    Account("Account"),
    Payments("Payments"),
    Common("Common")
}

@Composable
fun FeatureFlagAppConfigButton(navController: NavController, canNavigateBack: Boolean = false) {
    Column {
        ProtonTopAppBar(
            title = { Text(stringResource(R.string.feature_flags_title)) },
            navigationIcon = if (canNavigateBack) {
                { BackButton(navController) }
            } else {
                null
            }
        )
        Spacer(modifier = Modifier.height(ProtonDimens.SmallSpacing))
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(ProtonDimens.DefaultSpacing)) {
                ConfigType.entries.forEach { config ->
                    Button(
                        onClick = { navigateWithConfig(navController, config) },
                        modifier = Modifier.padding(vertical = ProtonDimens.SmallSpacing)
                    ) {
                        Text(text = config.displayName)
                    }
                }
            }
        }
    }
}

private fun navigateWithConfig(navController: NavController, config: ConfigType) {
    // Navigate to the specific screen with the configuration type as a parameter
    navController.navigate("featureFlags/${config.name}")
}
