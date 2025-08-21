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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import me.proton.core.compose.component.ProtonSnackbarHostState
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultStrongNorm
import me.proton.core.configuration.configurator.presentation.viewModel.FeatureFlagsViewModel

@Composable
fun FeatureFlagsScreen(
    featureFlagsViewModel: FeatureFlagsViewModel,
    project: String,
    navController: NavController
) {
    val featureFlags by featureFlagsViewModel.featureFlags.collectAsStateWithLifecycle()
    val snackbarHostState = remember { ProtonSnackbarHostState() }

    LaunchedEffect(project) {
        featureFlagsViewModel.errorFlow.collect {
            snackbarHostState.showSnackbar(
                type = ProtonSnackbarType.ERROR, message = it, actionLabel = "OK"
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Saves feature flags config on exit
            featureFlagsViewModel.saveConfig()
        }
    }

    Column {
        ProtonTopAppBar(
            title = { Text(project) },
            navigationIcon = { BackButton(navController) },
            actions = {
                ProtonTextButton(
                    onClick = { featureFlagsViewModel.resetToUnleashState() }
                ) {
                    Text(
                        text = "Reset",
                        color = ProtonTheme.colors.textAccent,
                        style = ProtonTheme.typography.defaultStrongNorm
                    )
                }
            }
        )
        Surface(
            modifier = Modifier
                .padding(vertical = ProtonDimens.SmallSpacing)
                .fillMaxWidth(),
            color = Color.White.copy(alpha = 0.1f)
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                for (flag in featureFlags.filter { it.project == project.lowercase() }
                    .sortedBy { it.name }) {
                    FeatureFlagItem(flag = flag, featureFlagsViewModel)
                }
            }
        }
    }
}

@Composable
fun BackButton(navController: NavController) {
    IconButton(onClick = {
        navController.navigateUp()
    }) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }
}
