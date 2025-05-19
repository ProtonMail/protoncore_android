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

package me.proton.core.devicemigration.presentation.success

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.compose.component.ProtonCenteredProgress
import me.proton.core.compose.component.ProtonSnackbarHost
import me.proton.core.compose.component.ProtonSnackbarHostState
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.LocalTypography
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.devicemigration.presentation.R

/**
 * The screen that is presented on the origin device (the one with active login session),
 * after the device migration is completed (i.e. when the target device is logged in).
 */
@Composable
internal fun OriginSuccessScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OriginSuccessViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    OriginSuccessScreen(
        state = state,
        modifier = modifier,
        onClose = onClose,
    )
}

@Composable
internal fun OriginSuccessScreen(
    state: OriginSuccessState,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
) {
    val snackbarHostState = remember { ProtonSnackbarHostState() }
    Scaffold(
        modifier = modifier,
        snackbarHost = { ProtonSnackbarHost(snackbarHostState) },
        topBar = { OriginSuccessTopBar(onClose = onClose) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (state) {
                is OriginSuccessState.Loading -> ProtonCenteredProgress()
                is OriginSuccessState.Idle -> OriginSuccessContent(
                    state = state,
                    onClose = onClose,
                    modifier = Modifier.padding(ProtonDimens.DefaultSpacing)
                )

                is OriginSuccessState.Error.Unknown -> Unit
            }
        }
    }

    LaunchedEffect(state) {
        if (state is OriginSuccessState.Error.Unknown) {
            snackbarHostState.showSnackbar(
                type = ProtonSnackbarType.ERROR,
                message = state.message,
                duration = SnackbarDuration.Indefinite
            )
        }
    }
}

@Composable
private fun OriginSuccessTopBar(
    onClose: () -> Unit
) {
    ProtonTopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    painterResource(id = R.drawable.ic_proton_close),
                    contentDescription = stringResource(id = R.string.presentation_close)
                )
            }
        },
    )
}

@Composable
private fun OriginSuccessContent(
    state: OriginSuccessState.Idle,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(ProtonDimens.DefaultSpacing)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.origin_success_signed_in),
            style = LocalTypography.current.headline,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = ProtonDimens.MediumSpacing)
        )

        Text(
            text = state.email,
            textAlign = TextAlign.Center,
            style = LocalTypography.current.body2Regular,
            color = LocalColors.current.textWeak,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = ProtonDimens.SmallSpacing)
        )

        Image(
            painter = painterResource(id = R.drawable.edm_origin_success),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = ProtonDimens.MediumSpacing)
        )

        Spacer(modifier = Modifier.weight(1.0f))

        ProtonSolidButton(
            onClick = onClose,
            contained = false,
            modifier = Modifier
                .padding(top = ProtonDimens.DefaultSpacing)
                .heightIn(min = ProtonDimens.DefaultButtonMinHeight)
        ) {
            Text(text = stringResource(R.string.origin_success_ack))
        }
    }
}

@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun OriginSuccessScreenPreview() {
    ProtonTheme {
        OriginSuccessScreen(
            state = OriginSuccessState.Idle("test@example.test")
        )
    }
}
