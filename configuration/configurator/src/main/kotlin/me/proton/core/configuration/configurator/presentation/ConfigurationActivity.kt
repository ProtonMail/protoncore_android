/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.configuration.configurator.presentation

import NavigationContent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.compose.component.NavigationTab
import me.proton.core.compose.component.ProtonBottomNavigation
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.configuration.configurator.R
import me.proton.core.presentation.ui.ProtonActivity

@AndroidEntryPoint
class ConfigurationActivity : ProtonActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val currentScreen = remember { mutableStateOf(Screen.Home) }

            val tabs = listOf(
                NavigationTab(
                    R.string.configuration_title, Icons.Filled.Home
                ), NavigationTab(
                    R.string.feature_flags_title, Icons.Filled.Settings
                ), NavigationTab(
                    R.string.quark_title, Icons.Filled.Create
                )
            )

            ProtonTheme {

                Scaffold(
                    bottomBar = {
                        ProtonBottomNavigation(
                            tabs = tabs,
                            onSelectedTabIndex = { index ->
                                currentScreen.value = when (index) {
                                    0 -> Screen.Home
                                    1 -> Screen.FeatureFlag
                                    2 -> Screen.Quark
                                    else -> Screen.Home
                                }
                            },
                            initialSelectedTabIndex = 0
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxHeight()
                            .padding(horizontal = ProtonDimens.DefaultSpacing)
                            .verticalScroll(rememberScrollState())
                    ) {
                        NavigationContent(currentScreen = currentScreen.value)
                    }
                }
            }
        }
    }
}

enum class Screen {
    Home,
    FeatureFlag,
    Quark,
}