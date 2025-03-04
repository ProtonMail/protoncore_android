package me.proton.core.configuration.configurator.presentation.components.configuration

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.proton.core.compose.component.ProtonOutlinedTextField
import me.proton.core.compose.component.ProtonSettingsToggleItem
import me.proton.core.compose.component.ProtonSnackbarHost
import me.proton.core.compose.component.ProtonSnackbarHostState
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.configuration.configurator.R
import me.proton.core.configuration.configurator.domain.ConfigurationUseCase
import me.proton.core.configuration.configurator.presentation.Screen
import me.proton.core.configuration.configurator.presentation.components.featureflags.FeatureFlagsScreen
import me.proton.core.configuration.configurator.presentation.components.quark.QuarkMainScreen
import me.proton.core.configuration.configurator.presentation.components.shared.ProtonSearchableOutlinedTextField
import me.proton.core.configuration.configurator.presentation.viewModel.ConfigurationScreenViewModel
import me.proton.core.util.kotlin.EMPTY_STRING
import me.proton.core.presentation.R.drawable as CoreDrawable

@Composable
fun NavigationContent(currentScreen: Screen) {
    val snackbarHostState = remember { ProtonSnackbarHostState() }

    when (currentScreen) {
        Screen.Home -> ConfigurationScreen(
            snackbarHostState = snackbarHostState,
            title = stringResource(id = R.string.configuration_title_network_configuration)
        )

        Screen.FeatureFlag -> FeatureFlagsScreen()
        Screen.Quark -> QuarkMainScreen()
    }
}

@Composable
fun ConfigurationScreen(
    configViewModel: ConfigurationScreenViewModel = hiltViewModel(),
    snackbarHostState: ProtonSnackbarHostState,
    title: String,
) {
    val configurationState by configViewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val hostState = remember { ProtonSnackbarHostState() }
    Scaffold(
        snackbarHost = { ProtonSnackbarHost(hostState) },
        topBar = {
            ProtonTopAppBar(title = { Text(title) })
        },
        content = { paddingValues ->
            ConfigSettingsScreen(
                modifier = Modifier.padding(paddingValues),
                configFieldSet = configurationState.configFieldSet,
                onConfigurationFieldUpdate = { key, newValue ->
                    configViewModel.perform(
                        ConfigurationScreenViewModel.Action.UpdateConfigField(
                            key,
                            newValue
                        )
                    )
                },
                onAdvanceSetting = {
                    configViewModel.perform(ConfigurationScreenViewModel.Action.SetDefaultConfigFields)
                },
                onConfigurationSave = {
                    configViewModel.perform(ConfigurationScreenViewModel.Action.SaveConfig)
                    scope.launch {
                        hostState.showSnackbar(
                            type = ProtonSnackbarType.SUCCESS,
                            message = "Configuration saved",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                onConfigurationFieldFetch = {
                    configViewModel.perform(ConfigurationScreenViewModel.Action.FetchConfigField(it))
                })
        }
    )

    LaunchedEffect(Unit) {
        configViewModel.errorFlow.collect {
            snackbarHostState.showSnackbar(
                type = ProtonSnackbarType.ERROR, message = it, actionLabel = "OK"
            )
        }
    }
}

@Composable
private fun ConfigSettingsScreen(
    modifier: Modifier = Modifier,
    configFieldSet: Set<ConfigurationUseCase.ConfigField>,
    onConfigurationFieldUpdate: (String, Any) -> Unit,
    onAdvanceSetting: () -> Unit,
    onConfigurationFieldFetch: (String) -> Unit,
    onConfigurationSave: () -> Unit,
) {
    Column(modifier = modifier) {
        ConfigurationFields(
            configFields = configFieldSet,
            onFieldUpdate = onConfigurationFieldUpdate,
            onConfigurationFieldFetch = onConfigurationFieldFetch,
            onAdvanceSetting = onAdvanceSetting
        )

        SaveConfigurationButton(onClick = {
            onConfigurationSave()
        })
    }
}

@Composable
private fun ConfigurationFields(
    configFields: Set<ConfigurationUseCase.ConfigField>,
    onFieldUpdate: (String, Any) -> Unit,
    onConfigurationFieldFetch: (String) -> Unit,
    onAdvanceSetting: () -> Unit
) {
    configFields.forEach { configField ->
        val fetchAction =
            configField.fetcher?.let { { onConfigurationFieldFetch(configField.name) } }
        val showingSearchView = remember { mutableStateOf(false) }
        if (configField.isSearchable) {
            var selectedResult by remember { mutableStateOf("") }
            val onResultSelected: (String) -> Unit = { result ->
                selectedResult = result
                onFieldUpdate(configField.name, handleDomain(result)) // Update immediately
                showingSearchView.value = false
                onAdvanceSetting()
            }
            val onDismissRequest: () -> Unit = {
                showingSearchView.value = false // Action to take on dismissing the search
            }

            val domains = LocalContext.current.resources.getStringArray(R.array.domains)

            if (showingSearchView.value) {
                ProtonSearchableOutlinedTextField(
                    name = "search",
                    value = "",
                    searchData = domains.toMutableList(),
                    onResultSelected = onResultSelected,
                    onCancelIconClick = onDismissRequest,
                )
            } else {
                ConfigurationTextField(
                    configField = configField,
                    onValueChange = {
                        showingSearchView.value = true
                    },
                    fetchAction = fetchAction,
                    showingSearchView
                )
            }
        } else {
            when (configField.value) {
                is String -> ConfigurationTextField(
                    configField = configField,
                    onValueChange = { newValue ->
                        onFieldUpdate(configField.name, newValue)
                    },
                    fetchAction = fetchAction,
                    showingSearchView = showingSearchView
                )

                is Boolean -> ProtonSettingsToggleItem(
                    name = configField.name,
                    value = configField.value,
                    onToggle = { newValue ->
                        onFieldUpdate(configField.name, newValue)
                    })

                null -> Unit
                else -> error("Unsupported configuration field type for key ${configField.name}")
            }
        }
    }
}

@Composable
fun ConfigurationTextField(
    configField: ConfigurationUseCase.ConfigField,
    onValueChange: (String) -> Unit,
    fetchAction: (() -> Unit)? = null,
    showingSearchView: MutableState<Boolean>
) {
    val initialValue = configField.value?.toString() ?: EMPTY_STRING
    val initialTextFieldValue = remember(initialValue) { TextFieldValue(initialValue) }
    var textFieldValue by remember { mutableStateOf(initialTextFieldValue) }

    LaunchedEffect(initialValue) {
        if (textFieldValue.text != initialValue) textFieldValue =
            TextFieldValue(text = initialValue)
    }

    ProtonOutlinedTextField(modifier = Modifier.fillMaxWidth(),
        value = textFieldValue,
        onValueChange = { newValue ->
            textFieldValue = newValue
            onValueChange(newValue.text)
        },
        label = { Text(text = configField.name) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(autoCorrect = false),
        trailingIcon = {
            if (configField.isSearchable) {
                ConfigActionButton(
                    drawableId = CoreDrawable.abc_ic_search_api_material,
                    onClick = { showingSearchView.value = true })
            } else {
                fetchAction?.let {
                    ConfigActionButton(onClick = fetchAction)
                }
            }
        })
}


@Composable
private fun SaveConfigurationButton(onClick: () -> Unit) {
    Column(
        modifier = Modifier.bottomPad(ProtonDimens.DefaultSpacing),
        horizontalAlignment = Alignment.End
    ) {
        ProtonSolidButton(
            modifier = Modifier
                .bottomPad(ProtonDimens.SmallSpacing)
                .padding(horizontal = ProtonDimens.DefaultSpacing),
            onClick = onClick
        ) {
            Text(stringResource(id = R.string.configuration_button_apply))
        }
    }
}

@Composable
internal fun ConfigActionButton(
    enabled: Boolean = true,
    @DrawableRes drawableId: Int = CoreDrawable.ic_proton_arrow_down_circle,
    onClick: () -> Unit = { },
) = IconButton(onClick = onClick, enabled = enabled) {
    Icon(
        painter = painterResource(id = drawableId),
        tint = ProtonTheme.colors.iconWeak,
        contentDescription = "Configuration Field Action Icon",
        modifier = Modifier.padding(horizontal = ProtonDimens.SmallSpacing),
    )
}

internal fun Modifier.bottomPad(bottomPadding: Dp) = fillMaxWidth().padding(bottom = bottomPadding)

sealed class Domain(val rawValue: String) {
    data object Black : Domain("proton.black")
    class Custom(name: String) : Domain("$name.proton.black")
    data object Production : Domain("proton.me")
}

fun handleDomain(stringValue: String): String {
    val domain = when (stringValue) {
        "black" -> Domain.Black
        "production" -> Domain.Production
        else -> Domain.Custom(name = stringValue)
    }

    return domain.rawValue
}
