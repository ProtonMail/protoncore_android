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

package me.proton.core.devicemigration.presentation.signin

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.core.graphics.createBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.compose.component.ProtonBackButton
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.effect.Effect
import me.proton.core.compose.theme.LocalTypography
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.util.annotatedStringResource
import me.proton.core.compose.viewmodel.hiltViewModelOrNull
import me.proton.core.devicemigration.presentation.R
import me.proton.core.domain.entity.UserId

/**
 * Sign-in on target device.
 */
@Composable
internal fun SignInScreen(
    onBackToSignIn: () -> Unit,
    onNavigateBack: () -> Unit,
    onSuccess: (userId: UserId) -> Unit,
    onSuccessAndPasswordChange: (userId: UserId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel? = hiltViewModelOrNull<SignInViewModel>()
) {
    val state by viewModel?.state?.collectAsStateWithLifecycle()
        ?: remember { derivedStateOf { SignInState.Loading } }
    SignInScreen(
        state = state,
        modifier = modifier,
        onBackToSignIn = onBackToSignIn,
        onNavigateBack = onNavigateBack,
        onSuccess = onSuccess,
        onSuccessAndPasswordChange = onSuccessAndPasswordChange,
    )
}

@Composable
internal fun SignInScreen(
    state: SignInState,
    modifier: Modifier = Modifier,
    onBackToSignIn: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onSuccess: (userId: UserId) -> Unit = {},
    onSuccessAndPasswordChange: (userId: UserId) -> Unit = {},
) {
    val title = when (state) {
        is SignInState.Failure -> ""
        else -> stringResource(R.string.target_sign_in_title)
    }
    Scaffold(
        modifier = modifier,
        topBar = { SignInTopBar(onBackClicked = onNavigateBack, title = title) }
    ) { padding ->
        Box(modifier = modifier.padding(padding)) {
            if (state is SignInState.Failure) {
                SignInErrorContent(state, onBackToSignIn = onBackToSignIn)
            } else {
                SignInContent(state)
            }
        }
    }

    SignInEffects(
        effect = state.effect,
        onSuccess = onSuccess,
        onSuccessAndPasswordChange = onSuccessAndPasswordChange,
    )
}

@Composable
private fun SignInTopBar(
    onBackClicked: () -> Unit,
    title: String
) {
    ProtonTopAppBar(
        title = { Text(text = title) },
        navigationIcon = { ProtonBackButton(onBack = onBackClicked) }
    )
}

@Composable
private fun SignInContent(
    state: SignInState,
    modifier: Modifier = Modifier
) {
    val qrBitmapSize = remember { 200.dp }
    val qrBoxSize = remember { qrBitmapSize + 2 * ProtonDimens.LargeSpacing }
    val emptyBitmap = remember { Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) }
    var qrBitmap: Bitmap by remember { mutableStateOf(emptyBitmap) }

    LaunchedEffect(state, qrBitmapSize) {
        qrBitmap = when (state) {
            is SignInState.Idle -> state.generateBitmap(state.qrCode, qrBitmapSize)
            else -> emptyBitmap
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ProtonDimens.DefaultSpacing)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = ProtonDimens.MediumSpacing)
                .background(
                    Color.White,
                    shape = ProtonTheme.shapes.medium
                )
                .align(Alignment.CenterHorizontally)
                .size(qrBoxSize)
        ) {
            when (state) {
                is SignInState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                is SignInState.Idle -> Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = state.qrCode,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(qrBitmapSize)
                )

                is SignInState.Failure -> Image(
                    painter = painterResource(R.drawable.ic_proton_cross_big),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center),
                    colorFilter = ColorFilter.tint(Color.Black)
                )

                is SignInState.SuccessfullySignedIn -> Image(
                    painter = painterResource(R.drawable.ic_proton_checkmark),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center),
                    colorFilter = ColorFilter.tint(Color.Black)
                )
            }
        }

        Text(
            text = stringResource(R.string.target_sign_in_scan_code),
            modifier = Modifier.padding(top = ProtonDimens.MediumSpacing),
            style = ProtonTheme.typography.body2Regular,
        )
        Text(
            text = annotatedStringResource(R.string.target_sign_in_instructions),
            modifier = Modifier.padding(top = ProtonDimens.MediumSpacing),
            style = ProtonTheme.typography.body2Regular,
        )
    }
}

@Composable
private fun SignInErrorContent(
    state: SignInState.Failure,
    onBackToSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ProtonDimens.DefaultSpacing)
    ) {
        Image(
            painter = painterResource(R.drawable.edm_target_error_icon),
            contentDescription = null,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = stringResource(R.string.target_sign_in_error_title),
            style = LocalTypography.current.headline,
            modifier = Modifier
                .padding(top = ProtonDimens.MediumSpacing)
                .align(Alignment.CenterHorizontally)
        )

        Text(
            text = state.message,
            style = LocalTypography.current.body2Regular,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = ProtonDimens.MediumSpacing)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.weight(1.0f))

        if (state.onRetry != null) {
            ProtonSolidButton(
                onClick = state.onRetry,
                contained = false,
                modifier = Modifier.heightIn(min = ProtonDimens.DefaultButtonMinHeight)
            ) {
                Text(text = stringResource(R.string.target_sign_in_error_new_qr))
            }
        }

        ProtonTextButton(
            onClick = onBackToSignIn,
            contained = false,
            modifier = Modifier
                .padding(top = ProtonDimens.SmallSpacing)
                .heightIn(min = ProtonDimens.DefaultButtonMinHeight)
        ) {
            Text(text = stringResource(R.string.target_sign_in_error_back_to_signin))
        }
    }
}

@Composable
private fun SignInEffects(
    effect: Effect<SignInEvent>?,
    onSuccess: (userId: UserId) -> Unit,
    onSuccessAndPasswordChange: (userId: UserId) -> Unit = {},
) {
    LaunchedEffect(effect) {
        effect?.consume { event ->
            when (event) {
                is SignInEvent.SignedIn -> onSuccess(event.userId)
                is SignInEvent.SignedInAndPasswordChange -> onSuccessAndPasswordChange(event.userId)
            }
        }
    }
}

@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun SignInScreenPreview() {
    ProtonTheme {
        SignInScreen(
            state = SignInState.Idle(
                "qr-code",
                generateBitmap = { _, size ->
                    createBitmap(
                        size.value.toInt(),
                        size.value.toInt(),
                        Bitmap.Config.ARGB_8888
                    )
                })
        )
    }
}

@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun SignInScreenErrorPreview() {
    ProtonTheme {
        SignInScreen(
            state = SignInState.Failure(message = "Error", onRetry = {})
        )
    }
}
