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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import me.proton.core.compose.component.ProtonSettingsHeader
import me.proton.core.compose.component.ProtonSnackbarHost
import me.proton.core.compose.component.ProtonSnackbarHostState
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.configuration.configurator.featureflag.entity.BackButton
import me.proton.core.configuration.configurator.presentation.components.shared.DropdownField
import me.proton.core.configuration.configurator.presentation.components.shared.UserEnvironmentText
import me.proton.core.configuration.configurator.presentation.viewModel.EnvironmentManagementViewModel

enum class FingerprintResponse(val response: String) {
    OK("{\\\"code\\\": 1000, \\\"result\\\": \\\"ok\\\", \\\"message\\\":\\\"{\\\\\\\"VerifyMethods\\\\\\\":[\\\\\\\"captcha\\\\\\\", \\\\\\\"email\\\\\\\", \\\\\\\"sms\\\\\\\"]}\\\"}"), WHITELIST(
        "{\\\"code\\\": 1001, \\\"result\\\": \\\"whitelist\\\"}"
    ),
    CAPTCHA("{\\\"code\\\": 2000, \\\"result\\\": \\\"captcha\\\"}"), PROOF_OF_WORK("{\\\"code\\\": 2004, \\\"result\\\": \\\"pow\\\"}"), BLOCK(
        "{\\\"code\\\": 3000, \\\"result\\\": \\\"block\\\", \\\"message\\\":\\\"Any error message you want\\\"}"
    ),
    EVIL("{\\\"code\\\": 3002, \\\"result\\\": \\\"evil\\\"}"), OWNERSHIP_VERIFICATION("{\\\"code\\\": 2001, \\\"result\\\": \\\"verify\\\"}"), CAPTCHA_AND_VERIFY(
        "{\\\"code\\\": 2003, \\\"result\\\": \\\"captcha+verify\\\"}"
    ),
    USER_FACING_ERROR_MESSAGE("{\\\"code\\\": 2000, \\\"result\\\": \\\"captcha\\\", \\\"message\\\":\\\"Please, solve CAPTCHA before continuing\\\"}"), DEVICE_LOCATION_ISP(
        "{\\\"code\\\": 2000, \\\"result\\\": \\\"captcha\\\", \\\"user_device\\\":\\\"Mac OS X, MacBook Pro 13inch 2020 M1\\\", \\\"user_location\\\":\\\"Geneva, Switzerland\\\", \\\"user_internet_provider\\\":\\\"AT&T\\\"}"
    )
}

enum class ProtonCaptchaVerifyResponse(val response: String) {
    PASS("{\\\"status\\\": \\\"pass\\\"}"), FAIL("{\\\"status\\\": \\\"fail\\\"}")
}

@Composable
fun QuarkEnvironmentManagementScreen(
    navController: NavHostController, viewModel: EnvironmentManagementViewModel = hiltViewModel()
) {

    val isSystemEnvLoading by viewModel.isSystemEnvLoading.collectAsState()
    val isUnbanLoading by viewModel.isUnbanLoading.collectAsState()
    val selectedDomain by viewModel.selectedDomain.collectAsState()
    val unbanResponse by viewModel.unbanResponse.collectAsState()
    val systemEnvResponse by viewModel.systemEnvResponse.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    var selectedFingerprintResponse by remember { mutableStateOf(FingerprintResponse.OK) }
    var selectedCaptchaResponse by remember { mutableStateOf(ProtonCaptchaVerifyResponse.PASS) }
    var selectedDevMode by remember { mutableStateOf(true) }
    val hostState = remember { ProtonSnackbarHostState() }

    LaunchedEffect(errorState) {
        errorState?.let { error ->
            hostState.showSnackbar(
                type = ProtonSnackbarType.ERROR,
                message = error,
                duration = SnackbarDuration.Long
            )
        }
    }

    LaunchedEffect(systemEnvResponse) {
        systemEnvResponse?.let { response ->
            hostState.showSnackbar(
                type = ProtonSnackbarType.SUCCESS,
                message = response,
                duration = SnackbarDuration.Long
            )
        }
    }

    LaunchedEffect(unbanResponse) {
        unbanResponse?.let { response ->
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
                title = { Text("Environment management") },
                navigationIcon = { BackButton(navController) },
            )
        },
        content = { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {

                UserEnvironmentText(selectedDomain, viewModel.sharedData)

                ProtonSettingsHeader(
                    title = "Environment variables",
                    modifier = Modifier
                        .fillMaxWidth()
                )

                DropdownField(label = "FINGERPRINT_RESPONSE",
                    options = FingerprintResponse.entries.map { it.name },
                    selectedOption = selectedFingerprintResponse.name,
                    onOptionSelected = { selectedFingerprintResponse = FingerprintResponse.valueOf(it)})

                DropdownField(label = "FINGERPRINT_DEV",
                    options = listOf("True", "False"),
                    selectedOption = if (selectedDevMode) "True" else "False",
                    onOptionSelected = { selectedDevMode = it == "True" })

                DropdownField(label = "PROTON_CAPTCHA_VERIFY_RESPONSE",
                    options = ProtonCaptchaVerifyResponse.entries.map { it.name },
                    selectedOption = selectedCaptchaResponse.name,
                    onOptionSelected = {
                        selectedCaptchaResponse = ProtonCaptchaVerifyResponse.valueOf(it)
                    })

                ProtonSolidButton(
                    onClick = {
                        viewModel.systemEnvVariableAsJson(
                            "FINGERPRINT_DEV",
                            selectedDevMode.toString()
                        )
                        viewModel.systemEnvVariableAsJson(
                            "FINGERPRINT_RESPONSE",
                            selectedFingerprintResponse.response
                        )
                        viewModel.systemEnvVariableAsJson(
                            "PROTON_CAPTCHA_VERIFY_RESPONSE",
                            selectedCaptchaResponse.response
                        )
                    },
                    loading = isSystemEnvLoading,
                    enabled = !isSystemEnvLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = ProtonDimens.SmallSpacing)
                ) {
                    Text("Save system env changes")
                }

                ProtonSettingsHeader(
                    title = "Jails",
                    modifier = Modifier
                        .fillMaxWidth()
                )

                ProtonSolidButton(
                    onClick = {
                        viewModel.unban()
                    },
                    loading = isUnbanLoading,
                    enabled = !isUnbanLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = ProtonDimens.SmallSpacing)
                ) {
                    Text("Unban users")
                }
            }
        }
    )
}
//
//@Composable
//fun DropdownField(
//    label: String, selectedValue: String, items: List<String>, onItemSelected: (String) -> Unit
//) {
//    var expanded by remember { mutableStateOf(false) }
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = ProtonDimens.SmallSpacing)
//    ) {
//        Column {
//            Text(label)
//            OutlinedButton(
//                onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()
//            ) {
//                Text(selectedValue)
//                Icon(
//                    imageVector = Icons.Filled.ArrowDropDown, contentDescription = null
//                )
//            }
//            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
//                items.forEach { item ->
//                    DropdownMenuItem(onClick = {
//                        onItemSelected(item)
//                        expanded = false
//                    }) {
//                        Text(text = item)
//                    }
//                }
//            }
//        }
//    }
//}