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

package me.proton.core.configuration.configurator.presentation.components;


import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.configuration.configurator.featureflag.entity.BackButton
import me.proton.core.configuration.configurator.presentation.viewModel.DriveUpdateUserViewModel

enum class DriveScenario(val code: Int, var text: String) {
    FullDataSet(1, "Drive - Full data"), FileAndFolder(2, "Drive - File and folder"), UnsignedContent(
        3, "Drive - Unsigned content"
    ),
    SharedAndTrashedItems(4, "Drive - Shared and trashed items"), Documents(8, "Drive - Documents")
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun DriveUserUpdateScreen(
    navController: NavHostController, viewModel: DriveUpdateUserViewModel = hiltViewModel()
) {

    val isLoading by viewModel.isLoading.collectAsState()
    val selectedDomain by viewModel.selectedDomain.collectAsState()
    var hasPhotos by remember { mutableStateOf(false) }
    var withDevice by remember { mutableStateOf(false) }
    var selectedFixture by remember { mutableStateOf(DriveScenario.FullDataSet) } // Default value as an example
    val response by viewModel.response.collectAsState()
    val errorState by viewModel.errorState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ProtonDimens.DefaultSpacing)
    ) {

        ProtonTopAppBar(title = { Text("Populate user with Drive fixture") },
            navigationIcon = { BackButton(navController) })
        Text(text = "Selected Domain: $selectedDomain")

        Spacer(modifier = Modifier.height(ProtonDimens.DefaultSpacing))

        DropdownField(label = "Select fixture",
            options = DriveScenario.entries.map { it.text },
            selectedOption = selectedFixture.text,
            onOptionSelected = { selectedText ->
                selectedFixture = DriveScenario.entries.first { it.text == selectedText }
            })

        Spacer(modifier = Modifier.height(ProtonDimens.SmallSpacing))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = hasPhotos, onCheckedChange = { hasPhotos = it })
            Text("Has photos")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = withDevice, onCheckedChange = { withDevice = it })
            Text("With device")
        }

        Spacer(modifier = Modifier.height(ProtonDimens.DefaultSpacing))

        ProtonSolidButton(
            onClick = {
                viewModel.drivePopulate(
                    scenario = selectedFixture.code, hasPhotos = hasPhotos, withDevice = withDevice
                )
            },
            enabled = !isLoading,
            loading = isLoading,

            ) {
            Text("Apply")
        }

        if (!errorState.isNullOrBlank()) {
            Text(
                text = errorState.toString(),
                color = MaterialTheme.colors.error,
                modifier = Modifier.padding(top = ProtonDimens.SmallSpacing)
            )
        }

        response?.let { response ->
            Text(
                text = response, modifier = Modifier.padding(top = ProtonDimens.SmallSpacing)
            )
        }
    }
}
