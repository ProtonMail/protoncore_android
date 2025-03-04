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

package me.proton.core.configuration.configurator.featureflag.entity

import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.configuration.configurator.BuildConfig
import me.proton.core.configuration.configurator.R
import me.proton.core.configuration.configurator.presentation.viewModel.FeatureFlagsViewModel

@Composable
fun FeatureFlagsScreen(
    featureFlagsViewModel: FeatureFlagsViewModel,
    project: String,
    navController: NavController
) {
    val context = LocalContext.current
    LaunchedEffect(project) {
        featureFlagsViewModel.loadFeatureFlagsByProject(project.lowercase())
    }
    val featureFlags = featureFlagsViewModel.featureFlags.collectAsState()
    Column {
        ProtonTopAppBar(
            title = { Text(project) },
            navigationIcon = { BackButton(navController) }
        )
        Surface(
            modifier = Modifier
                .padding(vertical = ProtonDimens.SmallSpacing)
                .fillMaxWidth(),
            color = Color.LightGray.copy(alpha = 0.1f)
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                ProtonSolidButton(
                    onClick = {
                        val intent =
                            Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.UNLEASH_URL))
                        context.startActivity(intent)
                    }
                ) {
                    Text(stringResource(id = R.string.feature_flags_manage))
                }
                FeatureFlagList(featureFlags.value)
            }
        }
    }
}

@Composable
fun BackButton(navController: NavController) {
    IconButton(onClick = { navController.navigateUp() }) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }
}
