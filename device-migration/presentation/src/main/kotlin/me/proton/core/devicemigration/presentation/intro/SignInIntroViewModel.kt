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

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import me.proton.core.biometric.data.StrongAuthenticatorsResolver
import me.proton.core.biometric.domain.BiometricAuthErrorCode
import me.proton.core.biometric.domain.BiometricAuthResult
import me.proton.core.biometric.domain.CheckBiometricAuthAvailability
import me.proton.core.compose.effect.Effect
import me.proton.core.compose.viewmodel.BaseViewModel
import me.proton.core.devicemigration.domain.usecase.DecodeEdmCode
import me.proton.core.devicemigration.domain.usecase.PushEdmSessionFork
import me.proton.core.devicemigration.presentation.DeviceMigrationRoutes.Arg.getUserId
import me.proton.core.devicemigration.presentation.R
import me.proton.core.devicemigration.presentation.qr.QrScanOutput
import me.proton.core.domain.entity.UserId
import me.proton.core.network.presentation.util.getUserMessageOrDefault
import javax.inject.Inject

@HiltViewModel
internal class SignInIntroViewModel @Inject constructor(
    private val checkBiometricAuthAvailability: CheckBiometricAuthAvailability,
    @ApplicationContext private val context: Context,
    private val decodeEdmCode: DecodeEdmCode,
    private val pushEdmSessionFork: PushEdmSessionFork,
    savedStateHandle: SavedStateHandle,
    private val strongAuthenticatorsResolver: StrongAuthenticatorsResolver,
) : BaseViewModel<SignInIntroAction, SignInIntroStateHolder>(
    initialAction = SignInIntroAction.Load,
    initialState = SignInIntroStateHolder(state = SignInIntroState.Loading)
) {
    private val userId: UserId by lazy { savedStateHandle.getUserId() }

    override fun onAction(action: SignInIntroAction): Flow<SignInIntroStateHolder> = when (action) {
        is SignInIntroAction.ConsumeEvent -> onConsumeEvent(action.event)
        is SignInIntroAction.Load -> onLoad()
        is SignInIntroAction.OnBiometricAuthResult -> onBiometricAuthResult(action.result)
        is SignInIntroAction.OnQrScanResult -> onQrScanResult(action.result)
        is SignInIntroAction.Start -> onStart()
    }

    override suspend fun FlowCollector<SignInIntroStateHolder>.onError(throwable: Throwable) {
        emit(idleWithEffect(SignInIntroEvent.ErrorMessage(throwable.getUserMessageOrDefault(context.resources))))
    }

    private fun onConsumeEvent(event: SignInIntroEvent) = flow {
        if (state.value.effect?.peek() == event) {
            emit(SignInIntroStateHolder(state = state.value.state))
        }
    }

    private fun onLoad() = flow {
        emit(SignInIntroStateHolder(state = SignInIntroState.Idle))
    }

    private fun onStart() = flow {
        when {
            shouldStartBiometricsCheck() ->
                emit(idleWithEffect(SignInIntroEvent.LaunchBiometricsCheck(strongAuthenticatorsResolver)))

            else ->
                emit(idleWithEffect(SignInIntroEvent.LaunchQrScanner))
        }
    }

    private fun onBiometricAuthResult(result: BiometricAuthResult) = flow {
        val stateHolder = when (result) {
            is BiometricAuthResult.AuthError -> {
                if (result.code.shouldDisplayErrorMessage) {
                    idleWithEffect(SignInIntroEvent.ErrorMessage(result.message.toString()))
                } else {
                    SignInIntroStateHolder(state = SignInIntroState.Idle)
                }
            }

            is BiometricAuthResult.Success -> idleWithEffect(SignInIntroEvent.LaunchQrScanner)
        }
        emit(stateHolder)
    }

    private fun onQrScanResult(result: QrScanOutput<String>) = flow {
        when (result) {
            is QrScanOutput.Cancelled -> emit(SignInIntroStateHolder(state = SignInIntroState.Idle))
            is QrScanOutput.ManualInputRequested -> emit(idleWithEffect(SignInIntroEvent.LaunchManualCodeInput))
            is QrScanOutput.Success -> emitAll(submitCode(result.contents))
        }
    }

    private fun submitCode(code: String) = flow {
        emit(SignInIntroStateHolder(state = SignInIntroState.Verifying))

        val edmParams = decodeEdmCode(code)

        if (edmParams == null) {
            val msg = context.getString(R.string.emd_code_not_recognized)
            emit(idleWithEffect(SignInIntroEvent.ErrorMessage(msg)))
        } else {
            pushEdmSessionFork(userId = userId, params = edmParams)
            emit(stateWithEffect(SignInIntroState.SignedInSuccessfully, SignInIntroEvent.SignedInSuccessfully))
        }
    }

    private fun consumableEffect(event: SignInIntroEvent): Effect<SignInIntroEvent> =
        Effect.of(event) { perform(SignInIntroAction.ConsumeEvent(event)) }

    private fun idleWithEffect(event: SignInIntroEvent): SignInIntroStateHolder =
        stateWithEffect(SignInIntroState.Idle, event)

    private fun stateWithEffect(state: SignInIntroState, event: SignInIntroEvent): SignInIntroStateHolder =
        SignInIntroStateHolder(consumableEffect(event), state)

    private fun shouldStartBiometricsCheck() =
        checkBiometricAuthAvailability(authenticatorsResolver = strongAuthenticatorsResolver).canAttemptBiometricAuth()
}

private val BiometricAuthErrorCode.shouldDisplayErrorMessage: Boolean
    get() = when (this) {
        BiometricAuthErrorCode.UserCanceled,
        BiometricAuthErrorCode.NegativeButton -> false

        else -> true
    }
