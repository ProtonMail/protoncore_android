package me.proton.core.configuration.configurator.presentation.components

import android.widget.Toast
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
import androidx.compose.runtime.collectAsState
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
import me.proton.core.compose.component.ProtonOutlinedTextField
import me.proton.core.compose.component.ProtonSnackbarHostState
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.configuration.configurator.R
import me.proton.core.configuration.configurator.presentation.viewModel.ConfigurationScreenViewModel
import me.proton.core.util.kotlin.EMPTY_STRING
import me.proton.core.presentation.R.drawable as CoreDrawable

typealias FieldActionMap = Map<String, (suspend () -> Any)?>

@Composable
fun <T : Any> ConfigurationScreen(
    configViewModel: ConfigurationScreenViewModel<T>,
    advancedFields: FieldActionMap,
    basicFields: FieldActionMap,
    preservedFields: Set<String>,
    snackbarHostState: ProtonSnackbarHostState,
    title: String
) {
    var isAdvancedExpanded by remember { mutableStateOf(false) }

    Column {
        ProtonTopAppBar(title = { Text(title) })

        ExpandableHeader(isExpanded = isAdvancedExpanded, onExpandChange = { isAdvancedExpanded = it })

        val configFields = if (isAdvancedExpanded) advancedFields else basicFields
        ConfigurationFields(configViewModel, configFields)

        AdvancedOptionsColumn(isAdvancedExpanded, preservedFields, configViewModel)

        SaveConfigurationButton(configFields.keys, configViewModel)
    }

    ObserveEvents(configViewModel, snackbarHostState)
}

@Composable
private fun ExpandableHeader(isExpanded: Boolean, onExpandChange: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandChange(!isExpanded) }
    ) {
        Text(
            text = stringResource(id = R.string.configuration_text_advanced),
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(color = ProtonTheme.colors.floatyText, shape = MaterialTheme.shapes.small)
        )
        Icon(
            painter = painterResource(id = if (isExpanded) R.drawable.ic_proton_arrow_up else R.drawable.ic_proton_arrow_down),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp),
            tint = ProtonTheme.colors.iconNorm,
            contentDescription = null
        )
    }
}

@Composable
private fun <T : Any> ConfigurationFields(configViewModel: ConfigurationScreenViewModel<T>, configFields: FieldActionMap) {
    configFields.forEach { (key, value) ->
        when (configViewModel.configFieldMap[key]) {
            is String -> ConfigurationTextField(configViewModel, key, value)
            is Boolean -> ConfigurationCheckbox(configViewModel, key)
            else -> {}
        }
    }
}

@Composable
private fun <T : Any> AdvancedOptionsColumn(
    isAdvancedExpanded: Boolean,
    preservedFields: Set<String>,
    configViewModel: ConfigurationScreenViewModel<T>,
) {
    if (isAdvancedExpanded) {
        Column(modifier = Modifier.bottomPad(16.dp), horizontalAlignment = Alignment.End) {
            ProtonSolidButton(
                modifier = Modifier.bottomPad(8.dp),
                onClick = { configViewModel.setDefaultConfigurationFields(preservedFields) }
            ) {
                Text(stringResource(id = R.string.configuration_restore_confirmation))
            }
        }
    }
}

@Composable
private fun SaveConfigurationButton(keys: Set<String>, configViewModel: ConfigurationScreenViewModel<*>) {
    Column(modifier = Modifier.bottomPad(16.dp), horizontalAlignment = Alignment.End) {
        ProtonSolidButton(
            modifier = Modifier.bottomPad(8.dp),
            onClick = { configViewModel.saveConfiguration(keys) }
        ) {
            Text(stringResource(id = R.string.configuration_button_apply))
        }
    }
}


@Composable
private fun <T : Any> ConfigurationTextField(
    configViewModel: ConfigurationScreenViewModel<T>,
    configPropertyKey: String,
    trailingAction: (suspend () -> Any)? = null
) {
    val fieldValue by configViewModel.observeField(configPropertyKey, EMPTY_STRING).collectAsState()
    var textState by remember { mutableStateOf(TextFieldValue(fieldValue)) }

    if (fieldValue != textState.text) {
        textState = TextFieldValue(fieldValue)
    }

    ProtonOutlinedTextField(
        modifier = Modifier.bottomPad(8.dp),
        value = textState,
        onValueChange = { newValue ->
            textState = newValue
            configViewModel.updateConfigField(configPropertyKey, newValue.text)
        },
        label = { Text(text = configPropertyKey) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(autoCorrect = false),
        trailingIcon = {
            if (trailingAction != null) {
                ConfigActionButton(onClick = {
                    configViewModel.fetchConfigField(configPropertyKey, trailingAction)
                })
            }
        }
    )
}

@Composable
private fun <T : Any> ConfigurationCheckbox(
    configViewModel: ConfigurationScreenViewModel<T>,
    configPropertyKey: String
) {
    var checkboxState by remember { mutableStateOf(configViewModel.configFieldMap[configPropertyKey] as Boolean) }

    Row(
        modifier = Modifier
            .bottomPad(8.dp)
            .clickable {
                checkboxState = !checkboxState
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checkboxState,
            onCheckedChange = { isChecked ->
                checkboxState = isChecked
                configViewModel.updateConfigField(configPropertyKey, isChecked)
            }
        )
        Text(
            text = configPropertyKey.toSpacedWords(),
            modifier = Modifier.bottomPad(8.dp)
        )
    }
}

@Composable
private fun ConfigActionButton(
    onClick: () -> Unit,
    @DrawableRes drawableId: Int = CoreDrawable.ic_proton_arrow_down_circle,
) =
    IconButton(onClick) {
        Icon(
            painter = painterResource(id = drawableId),
            tint = ProtonTheme.colors.iconNorm,
            contentDescription = "Configuration Field Action Icon"
        )
    }

@Composable
private fun <T : Any> ObserveEvents(
    configurationScreenViewModel: ConfigurationScreenViewModel<T>,
    snackbarHostState: ProtonSnackbarHostState
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        configurationScreenViewModel.errorEvent.collect { throwable ->
            snackbarHostState.showSnackbar(
                type = ProtonSnackbarType.ERROR,
                message = throwable.message ?: "Unknown error",
                actionLabel = "OK"
            )
        }
    }

    LaunchedEffect(Unit) {
        configurationScreenViewModel.infoEvent.collect { info ->
            Toast.makeText(context, info, Toast.LENGTH_SHORT).show()
        }
    }
}

private fun Modifier.bottomPad(bottomPadding: Dp) = fillMaxWidth().padding(bottom = bottomPadding)

private fun String.toSpacedWords(): String = replace("(?<=\\p{Lower})(?=[A-Z])".toRegex(), " ").capitalize()

