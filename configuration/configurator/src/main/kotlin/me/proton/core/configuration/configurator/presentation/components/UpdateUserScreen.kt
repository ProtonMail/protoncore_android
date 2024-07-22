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


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import me.proton.core.compose.component.ProtonOutlinedTextField
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.configuration.configurator.featureflag.entity.BackButton
import me.proton.core.configuration.configurator.presentation.viewModel.UpdateUserViewModel

@Composable
fun UpdateUserScreen(navController: NavHostController, viewModel: UpdateUserViewModel = hiltViewModel()) {

    val isLoading by viewModel.isLoading.collectAsState()
    val selectedDomain by viewModel.selectedDomain.collectAsState()
    var password by remember { mutableStateOf(TextFieldValue(viewModel.lastUserData.lastPassword)) }
    val response by viewModel.response.collectAsState()
    val userNames by viewModel.userNames.collectAsState()
    var showingSearchView by remember { mutableStateOf(false) }

    val onResultSelected: (String) -> Unit = { result ->
        showingSearchView = false
        viewModel.lastUserData.lastUsername = result
    }
    val onDismissRequest: () -> Unit = {
        showingSearchView = false
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        ProtonTopAppBar(title = { Text("Update User") }, navigationIcon = { BackButton(navController) })
        Text(text = "Selected Domain: $selectedDomain")

        if (showingSearchView) {
            SearchableConfigurationTextField(
                searchData = userNames.toMutableList(),
                onResultSelected = onResultSelected,
                onDismissRequest = onDismissRequest
            )
        } else {
            ProtonOutlinedTextField(
                value = TextFieldValue(viewModel.lastUserData.lastUsername),
                onValueChange = {
                    showingSearchView = true
                },
                label = { Text(text = "Username") },
                singleLine = true,
            )
        }
        ProtonSolidButton(
            onClick = {
                viewModel.fetchUsers()

                showingSearchView = true
            }, loading = isLoading, enabled = !isLoading
        ) {
            Text("Fetch Users")
        }
        ProtonOutlinedTextField(
            value = password,
            onValueChange = { newValue ->
                password = newValue
                viewModel.lastUserData.lastPassword = password.text
            },
            label = { Text(text = "Password") },
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(ProtonDimens.DefaultSpacing))

        Text("Select section to update")

        Button(
            onClick = {
                if (viewModel.lastUserData.isNotEmpty()) {
                    navController.navigate("drive")
                }
            }, enabled = viewModel.lastUserData.isNotEmpty()
        ) {
            Text("Navigate to Drive")
        }

        ProtonSolidButton(
            onClick = {
                viewModel.deleteUser()
            },
            loading = isLoading,
            enabled = !isLoading && viewModel.lastUserData.lastUsername.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)

        ) {
            Text("Delete user", color = MaterialTheme.colors.onError)
        }

        response?.let { response ->
            Text(
                text = response, modifier = Modifier.padding(top = ProtonDimens.SmallSpacing)
            )
        }
    }


}


