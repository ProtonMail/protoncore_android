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

package me.proton.core.configuration.configurator.presentation.components.quark;


import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import me.proton.core.compose.component.ProtonSettingsHeader
import me.proton.core.compose.component.ProtonSettingsItem
import me.proton.core.compose.component.ProtonSettingsToggleItem
import me.proton.core.compose.component.ProtonSnackbarHost
import me.proton.core.compose.component.ProtonSnackbarHostState
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.configuration.configurator.featureflag.entity.BackButton
import me.proton.core.configuration.configurator.presentation.components.shared.DropdownField
import me.proton.core.configuration.configurator.presentation.components.shared.UserEnvironmentText
import me.proton.core.configuration.configurator.presentation.viewModel.AccountUpdateUserViewModel
import me.proton.core.configuration.configurator.presentation.viewModel.DriveUpdateUserViewModel


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun AccountUserUpdateScreen(
    navController: NavHostController, viewModel: AccountUpdateUserViewModel = hiltViewModel()
) {

    val isSessionLoading by viewModel.isSessionLoading.collectAsState()
    val isResetLoading by viewModel.isResetLoading.collectAsState()
    val selectedDomain by viewModel.selectedDomain.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    val hostState = remember { ProtonSnackbarHostState() }
    val state by viewModel.response.collectAsState()
    var shouldExpireAccessTokens by remember { mutableStateOf(false) }
    var shouldExpireRefreshTokens by remember { mutableStateOf(false) }

    LaunchedEffect(errorState) {
        errorState?.let { error ->
            hostState.showSnackbar(
                type = ProtonSnackbarType.ERROR,
                message = error,
                duration = SnackbarDuration.Long
            )
        }
    }

    LaunchedEffect(state) {
        state?.let { response ->
            hostState.showSnackbar(
                type = ProtonSnackbarType.SUCCESS,
                message = response,
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        snackbarHost = { ProtonSnackbarHost(hostState) },
        topBar = {
            ProtonTopAppBar(
                title = { Text("Account management") },
                navigationIcon = { BackButton(navController) },
            )
        },
        content = { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                UserEnvironmentText(selectedDomain, viewModel.sharedData)

                ProtonSettingsHeader(
                    title = "Session management",
                    modifier = Modifier
                        .fillMaxWidth()
                )
                ProtonSettingsToggleItem(
                    name = "Expire all access tokens",
                    hint = "Expire all user sessions access tokens",
                    value = shouldExpireAccessTokens || shouldExpireRefreshTokens,
                    onToggle = { isChecked ->
                        shouldExpireAccessTokens = isChecked
                    }
                )
                ProtonSettingsToggleItem(
                    name = "Expire all refresh tokens",
                    hint = "Expire all user sessions access/refresh tokens",
                    value = shouldExpireRefreshTokens,
                    onToggle = { isChecked ->
                        shouldExpireAccessTokens = isChecked
                        shouldExpireRefreshTokens = isChecked
                    }
                )
                ProtonSolidButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.expireSession(shouldExpireRefreshTokens)
                    },
                    enabled = shouldExpireRefreshTokens || shouldExpireAccessTokens,
                    loading = isSessionLoading,
                ) {
                    Text("Apply")
                }

                ProtonSettingsHeader(
                    title = "Reset user",
                    modifier = Modifier
                        .fillMaxWidth()
                )
                ProtonSolidButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.userReset()
                    },
                    enabled = viewModel.sharedData.lastUserId.toInt() != 0,
                    loading = isResetLoading,
                ) {
                    Text("Apply")
                }
            }
        }
    )
}
