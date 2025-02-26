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


import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import me.proton.core.compose.component.ProtonOutlinedTextField
import me.proton.core.compose.component.ProtonSettingsHeader
import me.proton.core.compose.component.ProtonSettingsToggleItem
import me.proton.core.compose.component.ProtonSnackbarHost
import me.proton.core.compose.component.ProtonSnackbarHostState
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.configuration.configurator.R
import me.proton.core.configuration.configurator.featureflag.entity.BackButton
import me.proton.core.configuration.configurator.presentation.components.shared.DropdownField
import me.proton.core.configuration.configurator.presentation.components.shared.ProtonSearchableOutlinedTextField
import me.proton.core.configuration.configurator.presentation.components.shared.UserEnvironmentText
import me.proton.core.configuration.configurator.presentation.viewModel.CreateUserViewModel
import me.proton.core.test.quark.data.Plan

@Composable
fun QuarkCreateUserScreen(
    navController: NavHostController,
    viewModel: CreateUserViewModel = hiltViewModel()
) {

    val isLoading by viewModel.isLoading.collectAsState()
    val selectedDomain by viewModel.selectedDomain.collectAsState()
    var username by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var selectedPlan by remember { mutableStateOf("mail2022") } // Default value as an example
    var selectedKey by remember { mutableStateOf("Curve25519") }
    val userKeys = listOf("Curve25519", "RSA1024", "RSA2048", "RSA4096")
    val plans = LocalContext.current.resources.getStringArray(R.array.plans)
    var isEarlyAccessEnabled by remember { mutableStateOf(true) }
    val createUserResponse by viewModel.userResponse.collectAsState()
    val createUserError by viewModel.errorState.collectAsState()
    val hostState = remember { ProtonSnackbarHostState() }

    LaunchedEffect(createUserError) {
        createUserError?.let { error ->
            hostState.showSnackbar(
                type = ProtonSnackbarType.ERROR,
                message = error,
                duration = SnackbarDuration.Short
            )
        }
    }

    LaunchedEffect(createUserResponse) {
        createUserResponse?.let { response ->
            hostState.showSnackbar(
                type = ProtonSnackbarType.SUCCESS,
                message = response,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { ProtonSnackbarHost(hostState) },
        topBar = {
            ProtonTopAppBar(
                title = { Text("Create User") },
                navigationIcon = { BackButton(navController) })
        },
        content = { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {

                UserEnvironmentText(selectedDomain, viewModel.sharedData)

                ProtonSettingsHeader(
                    title = "User credentials",
                    modifier = Modifier
                        .fillMaxWidth()
                )
                ProtonOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = username,
                    onValueChange = { newValue ->
                        username = newValue
                    },
                    label = { Text(text = "Username") },
                    singleLine = true,
                )

                ProtonOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = password,
                    onValueChange = { newValue ->
                        password = newValue
                    },
                    label = { Text(text = "Password") },
                    singleLine = true,
                )

                ProtonSettingsHeader(
                    title = "Subscription",
                    modifier = Modifier
                        .fillMaxWidth()
                )

                ProtonSearchableOutlinedTextField(
                    "Select plan",
                    false,
                    value = "free",
                    searchData = plans.toMutableList(),
                    onResultSelected = { selectedPlan = it },
                    onCancelIconClick = { }
                )

                ProtonSettingsHeader(
                    title = "Select Key",
                    modifier = Modifier
                        .fillMaxWidth()
                )

                DropdownField(
                    options = userKeys,
                    selectedOption = selectedKey,
                    onOptionSelected = { selectedKey = it }
                )

                ProtonSettingsHeader(
                    title = "Beta",
                    modifier = Modifier
                        .fillMaxWidth()
                )

                ProtonSettingsToggleItem(
                    name = "Enable early access",
                    value = isEarlyAccessEnabled,
                    onToggle = { isChecked ->
                        isEarlyAccessEnabled = isChecked
                    }
                )

                ProtonSolidButton(
                    onClick = {
                        viewModel.createUser(
                            username.text,
                            password.text,
                            plan = Plan.fromString(selectedPlan),
                            isEnableEarlyAccess = isEarlyAccessEnabled,
                        )
                    },
                    loading = isLoading,
                    enabled = username.text.isNotEmpty() && password.text.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = ProtonDimens.SmallSpacing)
                ) {
                    Text("Create User")
                }
            }
        }
    )
}
