package me.proton.core.configuration.configurator.presentation.components.featureflags

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import me.proton.core.compose.component.ProtonOutlinedTextField
import me.proton.core.compose.component.ProtonSettingsToggleItem
import me.proton.core.compose.component.ProtonSnackbarHostState
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.configuration.configurator.R
import me.proton.core.configuration.configurator.domain.CUSTOM_TYPE
import me.proton.core.configuration.configurator.presentation.components.configuration.bottomPad
import me.proton.core.configuration.configurator.presentation.viewModel.FeatureFlagsViewModel
import me.proton.core.util.kotlin.EMPTY_STRING

@Composable
fun CustomFeatureFlagsScreen(
    featureFlagsViewModel: FeatureFlagsViewModel,
    navController: NavController
) {
    val featureFlags by featureFlagsViewModel.featureFlags.collectAsStateWithLifecycle()
    val snackbarHostState = remember { ProtonSnackbarHostState() }

    LaunchedEffect("CustomFeatureFlagsScreen") {
        featureFlagsViewModel.errorFlow.collect {
            snackbarHostState.showSnackbar(
                type = ProtonSnackbarType.ERROR, message = it, actionLabel = "OK"
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Saves feature flags config on exit
            featureFlagsViewModel.saveConfig()
        }
    }

    Column {
        ProtonTopAppBar(
            title = { Text("Custom") },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            },
            actions = {
                IconButton(onClick = { navController.navigate("customFlags/create") }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        )
        Surface(
            modifier = Modifier
                .padding(vertical = ProtonDimens.SmallSpacing)
                .fillMaxWidth(),
            color = Color.White.copy(alpha = 0.1f)
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                for (flag in featureFlags.filter { it.type == CUSTOM_TYPE }
                    .sortedBy { it.name }) {
                    FeatureFlagItem(flag = flag, featureFlagsViewModel)
                }
            }
        }
    }
}

@Composable
fun CreateCustomFeatureFlagScreen(
    featureFlagsViewModel: FeatureFlagsViewModel,
    navController: NavController
) {
    val nameState = remember { mutableStateOf(TextFieldValue(EMPTY_STRING)) }
    val descriptionState = remember { mutableStateOf(TextFieldValue(EMPTY_STRING)) }
    val defaultOnState = remember { mutableStateOf(false) }
    val projectState = remember { mutableStateOf(TextFieldValue(EMPTY_STRING)) }

    DisposableEffect(Unit) {
        onDispose {
            featureFlagsViewModel.saveConfig()
        }
    }

    Column {
        ProtonTopAppBar(
            title = { Text("Create custom flag") },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        )
        Column {
            ProtonOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = nameState.value,
                label = { Text(text = "Title") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(autoCorrect = false),
                onValueChange = { newValue ->
                    nameState.value = newValue
                }
            )
            ProtonOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = descriptionState.value,
                label = { Text(text = "Description") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(autoCorrect = false),
                onValueChange = { newValue ->
                    descriptionState.value = newValue
                }
            )
            ProtonOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = projectState.value,
                label = { Text(text = "Project") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(autoCorrect = false),
                onValueChange = { newValue ->
                    projectState.value = newValue
                }
            )
            ProtonSettingsToggleItem(
                modifier = Modifier.fillMaxWidth(),
                name = "Default value",
                hint = "Default feature flag value after creation",
                value = defaultOnState.value,
                onToggle = {
                    defaultOnState.value = !defaultOnState.value
                }
            )

            ProtonSolidButton(
                modifier = Modifier
                    .bottomPad(ProtonDimens.SmallSpacing)
                    .padding(horizontal = ProtonDimens.DefaultSpacing),
                onClick = {
                    val name = nameState.value.text.trim()
                    if (name.isNotEmpty()) {
                        featureFlagsViewModel.addCustomFeatureFlag(
                            name = name,
                            description = descriptionState.value.text.trim().ifEmpty { null },
                            defaultValue = defaultOnState.value,
                            project = projectState.value.text.ifEmpty { CUSTOM_TYPE }
                        )
                        navController.navigateUp()
                    }
                }
            ) {
                Text(stringResource(id = R.string.configuration_button_create))
            }
        }
    }
}


