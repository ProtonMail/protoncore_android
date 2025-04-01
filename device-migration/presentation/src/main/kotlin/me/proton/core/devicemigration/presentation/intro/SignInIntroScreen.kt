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

package me.proton.core.devicemigration.presentation.intro

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.biometric.presentation.rememberBiometricLauncher
import me.proton.core.compose.component.ProtonBackButton
import me.proton.core.compose.component.ProtonSnackbarHost
import me.proton.core.compose.component.ProtonSnackbarHostState
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.effect.Effect
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.LocalTypography
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.util.annotatedStringResource
import me.proton.core.devicemigration.presentation.R
import me.proton.core.devicemigration.presentation.qr.QrScanEncoding
import me.proton.core.devicemigration.presentation.qr.rememberQrScanLauncher

@Composable
internal fun SignInIntroScreen(
    modifier: Modifier = Modifier,
    onManualCodeInput: () -> Unit,
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: SignInIntroViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SignInIntroScreen(
        state = state.state,
        effect = state.effect,
        modifier = modifier,
        onBiometricAuthResult = viewModel::perform,
        onManualCodeInput = onManualCodeInput,
        onNavigateBack = onNavigateBack,
        onStart = { viewModel.perform(SignInIntroAction.Start) },
        onQrScanResult = { viewModel.perform(it) },
        onSuccess = onSuccess
    )
}

@Composable
@Suppress("LongParameterList")
internal fun SignInIntroScreen(
    state: SignInIntroState,
    effect: Effect<SignInIntroEvent>?,
    modifier: Modifier = Modifier,
    onBiometricAuthResult: (SignInIntroAction.OnBiometricAuthResult) -> Unit = {},
    onManualCodeInput: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onStart: () -> Unit = {},
    onQrScanResult: (SignInIntroAction.OnQrScanResult) -> Unit = {},
    onSuccess: () -> Unit = {}
) {
    val snackbarHostState = remember { ProtonSnackbarHostState() }

    SignInIntroEvents(
        effect = effect,
        onBiometricAuthResult = onBiometricAuthResult,
        onManualCodeInput = onManualCodeInput,
        onQrScanResult = onQrScanResult,
        onSuccess = onSuccess,
        snackbarHostState = snackbarHostState
    )

    Scaffold(
        modifier = modifier,
        snackbarHost = { ProtonSnackbarHost(snackbarHostState) },
        topBar = {
            ProtonTopAppBar(
                title = { Text(text = stringResource(R.string.intro_origin_sign_in_title)) },
                navigationIcon = { ProtonBackButton(onBack = onNavigateBack) }
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.padding(padding)
        ) {
            if (state is SignInIntroState.Verifying) {
                SignInIntroVerifying()
            } else {
                SignInIntroContent(
                    isInteractionDisabled = state.shouldDisableInteraction(),
                    onStart = onStart,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun SignInIntroEvents(
    effect: Effect<SignInIntroEvent>?,
    onBiometricAuthResult: (SignInIntroAction.OnBiometricAuthResult) -> Unit,
    onManualCodeInput: () -> Unit,
    onQrScanResult: (SignInIntroAction.OnQrScanResult) -> Unit,
    onSuccess: () -> Unit,
    snackbarHostState: ProtonSnackbarHostState
) {
    val biometricsLauncher = rememberBiometricLauncher { result ->
        onBiometricAuthResult(SignInIntroAction.OnBiometricAuthResult(result))
    }
    val biometricsTitle = stringResource(R.string.intro_origin_biometrics_title)
    val biometricsSubtitle = stringResource(R.string.intro_origin_biometrics_subtitle)
    val biometricsCancelButton = stringResource(R.string.presentation_alert_cancel)

    val qrScanLauncher = rememberQrScanLauncher(QrScanEncoding.default) { result ->
        onQrScanResult(SignInIntroAction.OnQrScanResult(result))
    }

    LaunchedEffect(effect) {
        effect?.consume { event ->
            when (event) {
                is SignInIntroEvent.ErrorMessage -> snackbarHostState.showSnackbar(
                    ProtonSnackbarType.ERROR,
                    message = event.message,
                    duration = SnackbarDuration.Long
                )

                is SignInIntroEvent.LaunchBiometricsCheck -> biometricsLauncher.launch(
                    title = biometricsTitle,
                    subtitle = biometricsSubtitle,
                    cancelButton = biometricsCancelButton,
                    authenticatorsResolver = event.resolver
                )

                is SignInIntroEvent.LaunchManualCodeInput -> onManualCodeInput()
                is SignInIntroEvent.LaunchQrScanner -> qrScanLauncher.launch()
                is SignInIntroEvent.SignedInSuccessfully -> onSuccess()
            }
        }
    }
}

@Composable
private fun SignInIntroContent(
    isInteractionDisabled: Boolean,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(ProtonDimens.MediumSpacing)
            .verticalScroll(rememberScrollState())
    ) {
        Image(
            painter = painterResource(R.drawable.edm_intro_qr_scan_icon),
            contentDescription = null,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = stringResource(R.string.intro_origin_sign_in_subtitle),
            style = LocalTypography.current.headline,
            color = LocalColors.current.textNorm,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = ProtonDimens.LargerSpacing)
        )

        Text(
            text = stringResource(R.string.intro_origin_sign_in_hint_1),
            textAlign = TextAlign.Center,
            style = LocalTypography.current.body2Regular,
            color = LocalColors.current.textWeak,
            modifier = Modifier
                .padding(top = ProtonDimens.DefaultSpacing)
        )

        Text(
            text = annotatedStringResource(R.string.intro_origin_sign_in_hint_2),
            textAlign = TextAlign.Center,
            style = LocalTypography.current.body2Regular,
            color = LocalColors.current.textWeak,
            modifier = Modifier
                .padding(top = ProtonDimens.DefaultSpacing)
        )

        Spacer(modifier = Modifier.weight(1.0f))

        TipBox(
            text = R.string.intro_origin_sign_in_tip_1,
            modifier = Modifier.padding(top = ProtonDimens.DefaultSpacing)
        )
        TipBox(
            text = R.string.intro_origin_sign_in_tip_2,
            modifier = Modifier.padding(top = ProtonDimens.SmallSpacing)
        )

        ProtonSolidButton(
            onClick = onStart,
            modifier = Modifier
                .padding(top = ProtonDimens.MediumSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight),
            contained = false,
            loading = isInteractionDisabled,
        ) {
            Text(text = stringResource(R.string.intro_origin_sign_in_begin))
        }
    }
}

@Composable
private fun SignInIntroVerifying(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.edm_code_verifying),
            style = LocalTypography.current.headline,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(ProtonDimens.MediumSpacing)
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = ProtonDimens.LargerSpacing)
        ) {
            Image(
                painter = painterResource(R.drawable.edm_qr_square),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize(0.85f)
                    .align(Alignment.Center)
            )
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
@Suppress("LongParameterList")
private fun TipBox(
    @StringRes text: Int,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int = R.drawable.ic_proton_lightbulb,
    bgColor: Color = LocalColors.current.backgroundSecondary,
    textColor: Color = LocalColors.current.textWeak,
    textStyle: TextStyle = LocalTypography.current.body2Regular
) {
    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(ProtonDimens.ExtraLargeCornerRadius))
            .padding(ProtonDimens.DefaultSpacing),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = textColor,
        )
        Text(
            text = stringResource(text),
            modifier = Modifier.padding(start = ProtonDimens.SmallSpacing),
            color = textColor,
            style = textStyle
        )
    }
}

@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun SignInIntroScreenPreview() {
    ProtonTheme {
        SignInIntroScreen(
            state = SignInIntroState.Idle,
            effect = null
        )
    }
}

@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun SignInIntroVerifyingScreenPreview() {
    ProtonTheme {
        SignInIntroScreen(
            state = SignInIntroState.Verifying,
            effect = null
        )
    }
}
