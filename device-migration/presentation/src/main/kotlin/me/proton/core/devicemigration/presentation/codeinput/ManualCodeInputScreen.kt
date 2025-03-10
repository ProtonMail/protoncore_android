/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.devicemigration.presentation.codeinput

import android.content.res.Resources
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.compose.component.ProtonOutlinedTextFieldWithError
import me.proton.core.compose.component.ProtonSnackbarHost
import me.proton.core.compose.component.ProtonSnackbarHostState
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.devicemigration.presentation.R
import me.proton.core.network.presentation.util.getUserMessage

@Composable
internal fun ManualCodeInputScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    viewModel: ManualCodeInputViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ManualCodeInputScreen(
        onNavigateBack = onNavigateBack,
        performAction = viewModel::perform,
        state = state,
        modifier = modifier
    )
}

@Composable
internal fun ManualCodeInputScreen(
    onNavigateBack: () -> Unit,
    performAction: (ManualCodeInputAction) -> Unit,
    state: ManualCodeInputState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { ProtonSnackbarHostState() }

    Scaffold(
        modifier = modifier,
        snackbarHost = { ProtonSnackbarHost(snackbarHostState) },
        topBar = { ManualCodeInputTopBar(onBackClicked = onNavigateBack) }
    ) { padding ->
        ManualCodeInputForm(
            performAction = performAction,
            state = state,
            modifier = Modifier.padding(padding)
        )
    }

    LaunchedEffect(state) {
        state.errorMessage(context.resources)?.let {
            snackbarHostState.showSnackbar(ProtonSnackbarType.ERROR, message = it)
        }
    }
}

@Composable
private fun ManualCodeInputTopBar(
    onBackClicked: () -> Unit,
) {
    ProtonTopAppBar(
        title = { Text(text = stringResource(R.string.manual_code_input_title)) },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(
                    painterResource(id = R.drawable.ic_proton_arrow_back),
                    stringResource(id = R.string.presentation_back)
                )
            }
        }
    )
}

@Composable
private fun ManualCodeInputForm(
    performAction: (ManualCodeInputAction) -> Unit,
    state: ManualCodeInputState,
    modifier: Modifier = Modifier
) {
    var code by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .padding(ProtonDimens.DefaultSpacing)
    ) {
        ProtonOutlinedTextFieldWithError(
            text = code,
            onValueChanged = { code = it },
            enabled = !state.isLoading(),
            errorText = state.errorTextForCodeInputField(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions { performAction(ManualCodeInputAction.Submit(code)) },
            label = { Text(text = stringResource(R.string.manual_code_input_code_label)) },
            singleLine = false,
            minLines = 3,
            maxLines = 5,
            trailingIcon = {
                IconButton(
                    enabled = !state.isLoading(),
                    onClick = { code = "" }
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_proton_cross),
                        stringResource(R.string.manual_code_input_clear_code)
                    )
                }
            }
        )

        ProtonSolidButton(
            onClick = { performAction(ManualCodeInputAction.Submit(code)) },
            loading = state.isLoading(),
            contained = false,
            modifier = Modifier
                .padding(top = ProtonDimens.DefaultSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight)
        ) {
            Text(text = stringResource(R.string.manual_code_input_submit))
        }
    }
}

private fun ManualCodeInputState.errorMessage(resources: Resources): String? =
    when (val s = this as? ManualCodeInputState.Error) {
        is ManualCodeInputState.Error.EmptyCode -> null
        is ManualCodeInputState.Error.Generic -> s.error.getUserMessage(resources)
        null -> null
    }

@Composable
private fun ManualCodeInputState.errorTextForCodeInputField(): String? = when (this as? ManualCodeInputState.Error) {
    is ManualCodeInputState.Error.EmptyCode -> stringResource(R.string.presentation_field_required)
    is ManualCodeInputState.Error.Generic -> null
    null -> null
}

@Composable
@Preview
private fun ManualCodeInputPreview() {
    ProtonTheme {
        ManualCodeInputScreen(
            onNavigateBack = {},
            performAction = {},
            state = ManualCodeInputState.Idle
        )
    }
}
