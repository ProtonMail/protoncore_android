package me.proton.core.configuration.configurator.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.compose.component.ProtonOutlinedTextField
import me.proton.core.compose.component.ProtonSnackbarHostState
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.configuration.configurator.R
import me.proton.core.configuration.configurator.domain.ConfigurationUseCase
import me.proton.core.configuration.configurator.presentation.viewModel.ConfigurationScreenViewModel
import me.proton.core.util.kotlin.EMPTY_STRING
import me.proton.core.presentation.R.drawable as CoreDrawable

@Composable
fun ConfigurationScreen(
    configViewModel: ConfigurationScreenViewModel = hiltViewModel(),
    snackbarHostState: ProtonSnackbarHostState,
    title: String,
) {
    val configurationState by configViewModel.state.collectAsStateWithLifecycle()

    ConfigSettingsScreen(
        configFieldSet = configurationState.configFieldSet,
        isAdvancedExpanded = configurationState.isAdvanced,
        title = title,
        onConfigurationFieldUpdate = { key, newValue ->
            configViewModel.perform(ConfigurationScreenViewModel.Action.UpdateConfigField(key, newValue))
        },
        onAdvanceSetting = {
            configViewModel.perform(ConfigurationScreenViewModel.Action.SetDefaultConfigFields)
        },
        onConfigurationSave = {
            configViewModel.perform(ConfigurationScreenViewModel.Action.SaveConfig(it))
        },
        onAdvancedExpanded = {
            configViewModel.perform(ConfigurationScreenViewModel.Action.SetAdvanced(it))
        },
        onConfigurationFieldFetch = {
            configViewModel.perform(ConfigurationScreenViewModel.Action.FetchConfigField(it))
        })

    LaunchedEffect(Unit) {
        configViewModel.errorFlow.collect {
            snackbarHostState.showSnackbar(
                type = ProtonSnackbarType.ERROR,
                message = it,
                actionLabel = "OK"
            )
        }
    }
}


@Composable
private fun ConfigSettingsScreen(
    configFieldSet: Set<ConfigurationUseCase.ConfigField>,
    title: String,
    onConfigurationFieldUpdate: (String, Any) -> Unit,
    onAdvanceSetting: () -> Unit,
    onConfigurationFieldFetch: (String) -> Unit,
    onConfigurationSave: (Boolean) -> Unit,
    isAdvancedExpanded: Boolean,
    onAdvancedExpanded: (Boolean) -> Unit
) {
    Column {
        ProtonTopAppBar(title = { Text(title) })

        ExpandableHeader(isExpanded = isAdvancedExpanded) {
            onAdvancedExpanded(it)
        }

        ConfigurationFields(
            configFields = configFieldSet,
            onFieldUpdate = onConfigurationFieldUpdate,
            onConfigurationFieldFetch = onConfigurationFieldFetch
        )

        AdvancedOptionsColumn(
            isAdvancedExpanded = isAdvancedExpanded,
            onClick = onAdvanceSetting
        )

        SaveConfigurationButton(
            onClick = {
                onConfigurationSave(isAdvancedExpanded)
            }
        )
    }
}

@Composable
private fun ConfigurationFields(
    configFields: Set<ConfigurationUseCase.ConfigField>,
    onFieldUpdate: (String, Any) -> Unit,
    onConfigurationFieldFetch: (String) -> Unit,
) {
    configFields.forEach { configField ->
        val fetchAction = configField.fetcher?.let { { onConfigurationFieldFetch(configField.name) } }
        if (configField.isSearchable) {
            var showingSearchView by remember { mutableStateOf(true) }
            var selectedResult by remember { mutableStateOf("") }
            val onResultSelected: (String) -> Unit = { result ->
                selectedResult = result
                onFieldUpdate(configField.name, handleDomain(result)) // Update immediately
                showingSearchView = false // Hide the SearchView after a result is selected
            }
            val onDismissRequest: () -> Unit = {
                showingSearchView = false // Action to take on dismissing the search
            }

            val domains = LocalContext.current.resources.getStringArray(R.array.domains)

            if (showingSearchView) {
                SearchableConfigurationTextField(
                    searchData = domains.toMutableList(),
                    onResultSelected = onResultSelected,
                    onDismissRequest = onDismissRequest
                )
            } else {
                ConfigurationTextField(
                    configField = configField,
                    onValueChange = {
                        showingSearchView = true
                    },
                    fetchAction = fetchAction
                )
            }
        } else {
            when (configField.value) {
                is String -> ConfigurationTextField(
                    configField = configField,
                    onValueChange = { newValue ->
                        onFieldUpdate(configField.name, newValue)
                    },
                    fetchAction = fetchAction
                )

                is Boolean -> ConfigurationCheckbox(
                    configField = configField,
                    onCheckChanged = { newValue ->
                        onFieldUpdate(configField.name, newValue)
                    }
                )

                null -> Unit
                else -> error("Unsupported configuration field type for key ${configField.name}")
            }
        }
    }
}

@Composable
private fun ConfigurationTextField(
    configField: ConfigurationUseCase.ConfigField,
    onValueChange: (String) -> Unit,
    fetchAction: (() -> Unit)? = null,
) {
    val initialValue = configField.value?.toString() ?: EMPTY_STRING
    val initialTextFieldValue = remember(initialValue) { TextFieldValue(initialValue) }
    var textFieldValue by remember { mutableStateOf(initialTextFieldValue) }

    LaunchedEffect(initialValue) {
        if (textFieldValue.text != initialValue)
            textFieldValue = TextFieldValue(text = initialValue)
    }

    ProtonOutlinedTextField(
        modifier = Modifier.bottomPad(8.dp),
        value = textFieldValue,
        onValueChange = { newValue ->
            textFieldValue = newValue
            onValueChange(newValue.text)
        },
        label = { Text(text = configField.name) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(autoCorrect = false),
        trailingIcon = {
            fetchAction?.let {
                ConfigActionButton(onClick = fetchAction)
            }
        }
    )
}

@Composable
private fun ConfigurationCheckbox(
    configField: ConfigurationUseCase.ConfigField,
    onCheckChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .bottomPad(8.dp)
            .clickable {
                onCheckChanged(
                    !configField.value
                        .toString()
                        .toBoolean()
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = configField.value.toString().toBoolean(),
            onCheckedChange = onCheckChanged,
        )
        Text(text = configField.name.toSpacedWords())
    }
}

@Composable
private fun ExpandableHeader(
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = ProtonTheme.colors.floatyText)
            .clickable { onExpandChange(!isExpanded) }
    ) {
        Text(
            text = stringResource(id = R.string.configuration_text_advanced),
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(color = ProtonTheme.colors.floatyText, shape = MaterialTheme.shapes.small)
        )
        Icon(
            painter = painterResource(id = isExpanded.drawable),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp),
            tint = ProtonTheme.colors.iconNorm,
            contentDescription = null
        )
    }
}

@Composable
private fun AdvancedOptionsColumn(
    isAdvancedExpanded: Boolean,
    onClick: () -> Unit,
) {
    if (isAdvancedExpanded) {
        Column(modifier = Modifier.bottomPad(16.dp), horizontalAlignment = Alignment.End) {
            ProtonSolidButton(
                modifier = Modifier.bottomPad(8.dp),
                onClick = onClick
            ) {
                Text(stringResource(id = R.string.configuration_set_defaults))
            }
        }
    }
}

@Composable
private fun SaveConfigurationButton(onClick: () -> Unit) {
    Column(modifier = Modifier.bottomPad(16.dp), horizontalAlignment = Alignment.End) {
        ProtonSolidButton(
            modifier = Modifier.bottomPad(8.dp),
            onClick = onClick
        ) {
            Text(stringResource(id = R.string.configuration_button_apply))
        }
    }
}

@Composable
private fun ConfigActionButton(
    @DrawableRes drawableId: Int = CoreDrawable.ic_proton_arrow_down_circle,
    onClick: () -> Unit = { },
) =
    IconButton(onClick) {
        Icon(
            painter = painterResource(id = drawableId),
            tint = ProtonTheme.colors.iconNorm,
            contentDescription = "Configuration Field Action Icon"
        )
    }

private fun Modifier.bottomPad(bottomPadding: Dp) = fillMaxWidth().padding(bottom = bottomPadding)

private
val Boolean.drawable: Int
    @DrawableRes get() = if (this) R.drawable.ic_proton_arrow_up else R.drawable.ic_proton_arrow_down

fun String.toSpacedWords(): String = replace("(?<=\\p{Lower})(?=[A-Z])".toRegex(), " ").capitalize()


sealed class Domain(val rawValue: String) {
    object Black : Domain("proton.black")
    class Custom(name: String) : Domain("$name.proton.black")
    object Production : Domain("proton.me")
}

fun handleDomain(stringValue: String): String {
    val domain = when (stringValue) {
        "black" -> Domain.Black
        "production" -> Domain.Production
        else -> Domain.Custom(name = stringValue)
    }

    return domain.rawValue
}