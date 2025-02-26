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

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.proton.core.configuration.configurator.featureflag.entity.FeatureFlagAppConfigButton
import me.proton.core.configuration.configurator.featureflag.entity.FeatureFlagsScreen
import me.proton.core.configuration.configurator.presentation.viewModel.FeatureFlagsViewModel

@Composable
fun FeatureFlagsScreen(viewModel: FeatureFlagsViewModel = hiltViewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            FeatureFlagAppConfigButton(navController)
        }
        composable("featureFlags/{configType}") { backStackEntry ->
            val project = backStackEntry.arguments?.getString("configType") ?: "Default"
            FeatureFlagsScreen(viewModel, project, navController)
        }
    }
}
