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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
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
import me.proton.core.configuration.configurator.presentation.components.featureflags.BackButton
import me.proton.core.configuration.configurator.presentation.components.shared.DropdownField
import me.proton.core.configuration.configurator.presentation.components.shared.UserEnvironmentText
import me.proton.core.configuration.configurator.presentation.viewModel.DriveUpdateUserViewModel

enum class DriveScenario(val code: Int, var text: String) {
    FullDataSet(1, "Drive - Full data"),
    FileAndFolder(2, "Drive - File and folder"),
    UnsignedContent(3, "Drive - Unsigned content"),
    SharedAndTrashedItems(4, "Drive - Shared and trashed items"),
    CollaborativeSharing(6, "Drive - Collaborative sharing"),
    Documents(8, "Drive - Documents"),
    AnonymousUpload(9, "Drive - Anonymous upload"),
    Album(10, "Drive - Album")
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun DriveUserUpdateScreen(
    navController: NavHostController, viewModel: DriveUpdateUserViewModel = hiltViewModel()
) {

    val isLoading by viewModel.isLoading.collectAsState()
    val isQuotaLoading by viewModel.isQuotaLoading.collectAsState()
    val selectedDomain by viewModel.selectedDomain.collectAsState()
    var hasPhotos by remember { mutableStateOf(false) }
    var withDevice by remember { mutableStateOf(false) }
    var selectedFixture by remember { mutableStateOf(DriveScenario.FullDataSet) } // Default value as an example
    val errorState by viewModel.errorState.collectAsState()
    val hostState = remember { ProtonSnackbarHostState() }
    val state by viewModel.response.collectAsState()
    val memoryMetric = listOf("KB", "MB", "GB", "TB")
    var amount by remember { mutableStateOf(TextFieldValue("")) }
    var selectedMemoryMetric by remember { mutableStateOf("MB") }

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
                title = { Text("Drive populate") },
                navigationIcon = { BackButton(navController) },
            )
        },
        content = { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                UserEnvironmentText(selectedDomain, viewModel.sharedData)

                ProtonSettingsHeader(
                    title = "Drive data",
                    modifier = Modifier
                        .fillMaxWidth()
                )
                DropdownField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ProtonDimens.DefaultSpacing),
                    label = "Select Drive scenario",
                    options = DriveScenario.entries.map { it.text },
                    selectedOption = selectedFixture.text,
                    onOptionSelected = { selectedText ->
                        selectedFixture = DriveScenario.entries.first { it.text == selectedText }
                    })
                ProtonSettingsToggleItem(
                    name = "Has photos",
                    value = hasPhotos,
                    onToggle = { isChecked ->
                        hasPhotos = isChecked
                    }
                )
                ProtonSettingsToggleItem(
                    name = "With device",
                    value = withDevice,
                    onToggle = { isChecked ->
                        withDevice = isChecked
                    }
                )
                ProtonSolidButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ProtonDimens.DefaultSpacing)
                        .padding(top = ProtonDimens.DefaultSpacing),
                    onClick = {
                        viewModel.drivePopulate(
                            scenario = selectedFixture.code,
                            hasPhotos = hasPhotos,
                            withDevice = withDevice
                        )
                    },
                    enabled = !isLoading && viewModel.sharedData.lastUsername.isNotEmpty(),
                    loading = isLoading,
                ) {
                    Text("Populate")
                }
                ProtonSettingsHeader(
                    title = "Control Drive quota",
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ProtonDimens.DefaultSpacing)
                ) {
                    ProtonOutlinedTextField(
                        modifier = Modifier.weight(3f),
                        value = amount,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        onValueChange = { newValue ->
                            amount = newValue
                        },
                        label = { Text(text = "Amount") },
                        singleLine = true
                    )
                    DropdownField(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = ProtonDimens.DefaultSpacing),
                        options = memoryMetric,
                        selectedOption = selectedMemoryMetric,
                        onOptionSelected = { selectedMemoryMetric = it }
                    )
                }
                ProtonSolidButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ProtonDimens.DefaultSpacing)
                        .padding(top = ProtonDimens.DefaultSpacing),
                    onClick = {
                        viewModel.driveQuotaSeedUsedSpace(
                            "${amount.text}$selectedMemoryMetric"
                        )
                    },
                    enabled = viewModel.sharedData.lastUserId.toInt() != 0 && amount.text.isNotEmpty(),
                    loading = isQuotaLoading,
                ) {
                    Text("Set quota")
                }
            }
        }
    )
}
