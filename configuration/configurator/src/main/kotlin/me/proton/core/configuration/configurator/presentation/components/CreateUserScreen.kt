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


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.configuration.configurator.R
import me.proton.core.configuration.configurator.domain.ConfigurationUseCase
import me.proton.core.configuration.configurator.presentation.viewModel.CreateUserViewModel
import me.proton.core.test.quark.data.Plan

@Composable
fun CreateUserScreen(viewModel: CreateUserViewModel = hiltViewModel()) {
    var username by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var selectedPlan by remember { mutableStateOf("mail2022") } // Default value as an example
    var selectedKey by remember { mutableStateOf("Curve25519") }
    val userKeys = listOf("Curve25519")
    val plans = LocalContext.current.resources.getStringArray(R.array.plans)
    var isEarlyAccessEnabled by remember { mutableStateOf(true) }
    val state by viewModel.userResponse.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    val selectedDomain by viewModel.selectedDomain.collectAsState()

    Column(modifier = Modifier.padding(ProtonDimens.DefaultSpacing)) {
        ProtonTopAppBar(title = { Text("Create User") })
        Text(text = "Selected Domain: $selectedDomain")
        ConfigurationTextField(
            configField = ConfigurationUseCase.ConfigField(name = "Username", value = username.text),
            onValueChange = { newValue -> username = TextFieldValue(newValue) }
        )

        ConfigurationTextField(
            configField = ConfigurationUseCase.ConfigField(name = "Password", value = password.text),
            onValueChange = { newValue -> password = TextFieldValue(newValue) }
        )

        DropdownField(
            label = "Select Plan",
            options = plans.toMutableList(),
            selectedOption = selectedPlan,
            onOptionSelected = { selectedPlan = it }
        )

        DropdownField(
            label = "Select Key",
            options = userKeys,
            selectedOption = selectedKey,
            onOptionSelected = { selectedKey = it }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable early access", modifier = Modifier.weight(1f))
            Switch(
                checked = isEarlyAccessEnabled,
                onCheckedChange = { isChecked ->
                    isEarlyAccessEnabled = isChecked
                }
            )
        }
        DropdownField(
            label = "Select Key",
            options = userKeys,
            selectedOption = selectedKey,
            onOptionSelected = { selectedKey = it }
        )

        ProtonSolidButton(
            onClick = {
                viewModel.createUser(
                    username.text,
                    password.text,
                    plan = Plan.fromString(selectedPlan),
                    isEnableEarlyAccess = isEarlyAccessEnabled
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Create User")
        }

        // Handle error state
        if (!errorState.isNullOrBlank()) {
            Text(
                text = errorState.toString(),
                color = MaterialTheme.colors.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Display user data if available
        state?.let { response ->
            Text(
                text = "User ID: ${response.userId}\nName: ${response.name}\nEmail: ${response.email ?: "Not provided"}\nRecovery Phone: ${response.recoveryPhone}",
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Column {
            Text(text = label, style = MaterialTheme.typography.caption)
            Text(
                text = selectedOption,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    onOptionSelected(option)
                    expanded = false
                }) {
                    Text(option)
                }
            }
        }
    }
}


@Composable
fun NewlyRegisteredUserView(username: String, password: String) {
    TODO("Not yet implemented")
}
