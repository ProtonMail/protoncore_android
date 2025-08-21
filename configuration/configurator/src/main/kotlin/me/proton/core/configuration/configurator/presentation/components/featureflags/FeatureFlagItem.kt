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

package me.proton.core.configuration.configurator.presentation.components.featureflags

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import me.proton.core.compose.component.ProtonSettingsToggleItem
import me.proton.core.configuration.configurator.domain.CUSTOM_TYPE
import me.proton.core.configuration.configurator.domain.FeatureFlagsUseCase
import me.proton.core.configuration.configurator.presentation.viewModel.FeatureFlagsViewModel


@Composable
fun FeatureFlagItem(
    flag: FeatureFlagsUseCase.FeatureFlagEntity,
    featureFlagsViewModel: FeatureFlagsViewModel
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(flag.name) {
                detectTapGestures(onLongPress = { showMenu = true })
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProtonSettingsToggleItem(
                modifier = Modifier.weight(1f),
                name = flag.name,
                hint = flag.description,
                value = flag.effectiveValue,
                onToggle = { newValue ->
                    featureFlagsViewModel.toggleFeatureFlag(flag, newValue)
                }
            )
            if (flag.type == CUSTOM_TYPE) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(onClick = {
                            showMenu = false
                            featureFlagsViewModel.removeCustomFeatureFlag(flag.name)
                            featureFlagsViewModel.saveConfig()
                        }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}
