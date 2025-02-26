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
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import me.proton.core.compose.component.ProtonOutlinedTextField
import me.proton.core.compose.component.ProtonSettingsHeader
import me.proton.core.compose.component.ProtonSettingsItem
import me.proton.core.compose.component.ProtonSnackbarHost
import me.proton.core.compose.component.ProtonSnackbarHostState
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.configuration.configurator.featureflag.entity.BackButton
import me.proton.core.configuration.configurator.presentation.components.shared.ProtonSearchableOutlinedTextField
import me.proton.core.configuration.configurator.presentation.components.shared.UserEnvironmentText
import me.proton.core.configuration.configurator.presentation.viewModel.UpdateUserViewModel

@Composable
fun UpdateUserScreen(
    navController: NavHostController,
    viewModel: UpdateUserViewModel = hiltViewModel()
) {

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedDomain by viewModel.selectedDomain.collectAsState()
    var password by remember { mutableStateOf(TextFieldValue(viewModel.lastUserData.lastPassword)) }
    val userNames by viewModel.userNames.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    var expandDropDownState by remember { mutableStateOf(false) }
    var rememberedUserName by remember { mutableStateOf(viewModel.sharedData.lastUsername) }
    var rememberedUserPassword by remember { mutableStateOf("") }
    val hostState = remember { ProtonSnackbarHostState() }
    val state by viewModel.response.collectAsState()
    val scope = rememberCoroutineScope()
    val onResultSelected: (String) -> Unit = { result ->
        expandDropDownState = false
        viewModel.sharedData.setUser(result)
        rememberedUserName = result
    }
    val onDismissRequest: () -> Unit = {
        viewModel.sharedData.clean()
        expandDropDownState = false
    }

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
                title = { Text("Update User") },
                navigationIcon = { BackButton(navController) },
            )
        },
        content = { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                UserEnvironmentText(selectedDomain, viewModel.sharedData)
                ProtonSettingsHeader(
                    title = "User selection",
                    modifier = Modifier
                        .fillMaxWidth()
                )

                ProtonSearchableOutlinedTextField(
                    name = "Username",
                    expandedState = expandDropDownState,
                    value = viewModel.lastUserData.lastUsername,
                    searchData = userNames.toMutableList(),
                    onResultSelected = onResultSelected,
                    onSearchIconClick = {
                        viewModel.fetchUsers()
                    },
                    onCancelIconClick = onDismissRequest,
                    onValueChange = {
                        rememberedUserName = it.text
                    },
                    loading = isLoading
                )

                ProtonOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = password,
                    onValueChange = { newValue ->
                        password = newValue
                        viewModel.lastUserData.lastPassword = password.text
                        rememberedUserPassword = password.text
                    },
                    label = { Text(text = "Password") },
                    singleLine = true
                )

                ProtonSettingsHeader(
                    title = "User update domains",
                    modifier = Modifier
                        .fillMaxWidth()
                )

                ProtonSettingsItem(
                    modifier = Modifier.fillMaxWidth(),
                    name = "Account management",
                    hint = "Control Account settings",
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()

                        if (viewModel.lastUserData.lastUsername.isEmpty()
                            && viewModel.lastUserData.lastPassword.isEmpty()
                        ) {
                            scope.launch {
                                hostState.showSnackbar(
                                    ProtonSnackbarType.WARNING,
                                    "Some account commands require userId. " +
                                            "Create new user or fetch users from selected " +
                                            "environment to proceed.",
                                    duration = SnackbarDuration.Long
                                )
                            }
                        } else {
                            navController.navigate("userAccountUpdate")
                        }
                    }
                )

                ProtonSettingsItem(
                    modifier = Modifier.fillMaxWidth(),
                    name = "Drive management",
                    hint = "Seed Drive content",
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()

                        if (viewModel.lastUserData.lastUsername.isEmpty()
                            || viewModel.lastUserData.lastPassword.isEmpty()
                        ) {
                            scope.launch {
                                hostState.showSnackbar(
                                    ProtonSnackbarType.WARNING,
                                    "Provide both username and password to use " +
                                            "Drive populate. Create new user or provide manually.",
                                    duration = SnackbarDuration.Long
                                )
                            }
                        } else {
                            navController.navigate("drive")
                        }
                    }
                )

                ProtonSolidButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.deleteUser()
                    },
                    loading = isLoading,
                    enabled = !isLoading && rememberedUserName.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)

                ) {
                    Text("Delete user", color = MaterialTheme.colors.onError)
                }
            }
        }
    )
}
