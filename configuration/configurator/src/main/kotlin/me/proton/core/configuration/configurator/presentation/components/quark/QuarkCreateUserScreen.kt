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


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
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
import me.proton.core.configuration.configurator.presentation.components.configuration.ConfigActionButton
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

    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedDomain by viewModel.selectedDomain.collectAsState()
    var username by remember { mutableStateOf(TextFieldValue("")) }
    var coupon by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var selectedPlan by remember { mutableStateOf("free") }
    var selectedKey by remember { mutableStateOf("Curve25519") }
    val userKeys = listOf("Curve25519", "RSA1024", "RSA2048", "RSA4096")

    val currency = listOf("CHF", "USD", "EUR", "CAD", "AUD", "GBP", "BRL")
    var selectedCurrency by remember { mutableStateOf("USD") }
    val cycle = listOf("1", "12", "24")
    var selectedCycle by remember { mutableStateOf("1") }

    val plans = LocalContext.current.resources.getStringArray(R.array.plans)
    var isEarlyAccessEnabled by remember { mutableStateOf(true) }
    var useMailFixture by remember { mutableStateOf(false) }
    val createUserResponse by viewModel.userResponse.collectAsState()
    val createUserError by viewModel.errorState.collectAsState()
    val hostState = remember { ProtonSnackbarHostState() }
    val fixtures = LocalContext.current.resources.getStringArray(R.array.mail_scenarios).asList()
    var selectedFixture by remember { mutableStateOf(fixtures[0]) }

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
            Column(modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
            ) {
                UserEnvironmentText(selectedDomain, viewModel.sharedData)
                ProtonOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = username,
                    onValueChange = { newValue ->
                        username = newValue
                    },
                    label = { Text(text = "Username") },
                    singleLine = true,
                    trailingIcon = {
                        ConfigActionButton(
                            enabled = !useMailFixture,
                            drawableId = R.drawable.ic_proton_squares,
                            onClick = {
                                copyToClipboard(context, username.text)
                            }
                        )
                    },
                    enabled = !useMailFixture
                )
                ProtonOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = password,
                    onValueChange = { newValue ->
                        password = newValue
                    },
                    label = { Text(text = "Password") },
                    singleLine = true,
                    trailingIcon = {
                        ConfigActionButton(
                            enabled = !useMailFixture,
                            drawableId = R.drawable.ic_proton_squares,
                            onClick = {
                                copyToClipboard(context, password.text)
                            }
                        )
                    },
                    enabled = !useMailFixture
                )
                ProtonSearchableOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    name = "Select plan",
                    value = selectedPlan,
                    searchData = plans.toMutableList(),
                    onResultSelected = {
                        selectedPlan = it
                        viewModel.sharedData.lastPlan = selectedPlan
                        if (it == "free") {
                            selectedCurrency = "USD"
                            selectedCycle = "1"
                        }
                    },
                    onCancelIconClick = { },
                    enabled = !useMailFixture,
                )

                ProtonOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = coupon,
                    onValueChange = { newValue ->
                        coupon = newValue
                    },
                    label = { Text(text = "Coupon") },
                    singleLine = true,
                    enabled = !useMailFixture && (selectedPlan != "free")
                )

                DisabledContainer(enabled = !useMailFixture) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            DropdownField(
                                label = "Currency",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = ProtonDimens.DefaultSpacing)
                                    .weight(1f)
                                    .clickable { !useMailFixture },
                                options = currency,
                                selectedOption = selectedCurrency,
                                enabled = !useMailFixture && (selectedPlan != "free"),
                                onOptionSelected = { selectedCurrency = it }
                            )
                            DropdownField(
                                label = "Payment cycle",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = ProtonDimens.DefaultSpacing)
                                    .weight(1f)
                                    .clickable { !useMailFixture },
                                options = cycle,
                                selectedOption = selectedCycle,
                                enabled = !useMailFixture && (selectedPlan != "free"),
                                onOptionSelected = { selectedCycle = it }
                            )
                        }

                        DropdownField(
                            label = "Select Key",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = ProtonDimens.DefaultSpacing)
                                .clickable { !useMailFixture },
                            options = userKeys,
                            selectedOption = selectedKey,
                            enabled = !useMailFixture,
                            onOptionSelected = { selectedKey = it }
                        )
                        ProtonSettingsToggleItem(
                            modifier = Modifier.alpha(if (!useMailFixture) 1f else 0.5f),
                            name = "Beta features",
                            hint = "Enable early access",
                            value = isEarlyAccessEnabled,
                            onToggle = { isChecked ->
                                isEarlyAccessEnabled = isChecked && !useMailFixture
                            }
                        )
                    }
                }

                Divider(modifier = Modifier.padding(ProtonDimens.DefaultSpacing))

                ProtonSettingsToggleItem(
                    name = "Use mail Fixture",
                    hint = "Enable to seed user from Mail fixture",
                    value = useMailFixture,
                    onToggle = { isChecked ->
                        useMailFixture = isChecked
                        isEarlyAccessEnabled = !isChecked
                        viewModel.sharedData.clean()
                        username = TextFieldValue("")
                        password = TextFieldValue("")
                    }
                )
                DisabledContainer(enabled = useMailFixture) {
                    DropdownField(
                        enabled = useMailFixture,
                        label = "Select fixture",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = ProtonDimens.DefaultSpacing),
                        options = fixtures,
                        selectedOption = selectedFixture,
                        onOptionSelected = { selectedFixture = it }
                    )
                }
                ProtonSolidButton(
                    onClick = {
                        if (useMailFixture) {
                            viewModel.loadFixture(
                                selectedFixture
                            )
                        } else {
                            viewModel.createUser(
                                username = username.text,
                                password = password.text,
                                plan = Plan.fromString(selectedPlan),
                                cycle = selectedCycle,
                                currency = selectedCurrency,
                                isEnableEarlyAccess = isEarlyAccessEnabled,
                            )
                        }
                    },
                    loading = isLoading,
                    enabled = (username.text.isNotEmpty() && password.text.isNotEmpty()) || useMailFixture,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = ProtonDimens.SmallSpacing)
                        .padding(horizontal = ProtonDimens.DefaultSpacing)
                ) {
                    Text("Create User")
                }
            }
        }
    )
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied text", text)
    clipboard.setPrimaryClip(clip)
}

@Composable
fun DisabledContainer(enabled: Boolean, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(enabled) {
                if (!enabled) detectTapGestures { }
            }
    ) {
        content()
    }
}
