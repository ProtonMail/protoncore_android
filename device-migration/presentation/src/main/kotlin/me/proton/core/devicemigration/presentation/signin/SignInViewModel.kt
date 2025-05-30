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

import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.entity.EncryptedAuthSecret
import me.proton.core.auth.domain.usecase.CreateLoginSessionFromFork
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.compose.effect.Effect
import me.proton.core.compose.viewmodel.BaseViewModel
import me.proton.core.devicemigration.domain.usecase.ObserveEdmCode
import me.proton.core.devicemigration.domain.usecase.PullEdmSessionFork
import me.proton.core.devicemigration.presentation.BuildConfig
import me.proton.core.devicemigration.presentation.R
import me.proton.core.devicemigration.presentation.qr.QrBitmapGenerator
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.isRetryable
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.EdmForkGetTotal
import me.proton.core.observability.domain.metrics.EdmForkPullTotal
import me.proton.core.observability.domain.metrics.EdmPostLoginTotal
import me.proton.core.observability.domain.metrics.EdmPostLoginTotal.PostLoginStatus
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.catchWhen
import me.proton.core.util.kotlin.coroutine.flowWithResultContext
import javax.inject.Inject

@HiltViewModel
internal class SignInViewModel @Inject constructor(
    private val accountType: AccountType,
    @ApplicationContext private val context: Context,
    private val createLoginSessionFromFork: CreateLoginSessionFromFork,
    override val observabilityManager: ObservabilityManager,
    private val observeEdmCode: ObserveEdmCode,
    private val postLoginAccountSetup: PostLoginAccountSetup,
    private val pullEdmSessionFork: PullEdmSessionFork,
    private val qrBitmapGenerator: QrBitmapGenerator
) : BaseViewModel<SignInAction, SignInState>(
    initialAction = SignInAction.Load(),
    initialState = SignInState.Loading
), ObservabilityContext {
    override fun onAction(action: SignInAction): Flow<SignInState> = when (action) {
        is SignInAction.Load -> onLoad()
        is SignInAction.SessionForkPulled -> onSessionForkPulled(action)
    }

    override suspend fun FlowCollector<SignInState>.onError(throwable: Throwable) {
        emit(stateWithUnrecoverableError(onRetry = { perform(SignInAction.Load()) }))
    }

    private fun onLoad(): Flow<SignInState> = flowWithResultContext {
        onResultEnqueueObservability("getSessionForks") { EdmForkGetTotal(this) }
        onResultEnqueueObservability("getForkedSession") { EdmForkPullTotal(this) }

        observeEdmCode(sessionId = null).catchWhen({ (it as? ApiException)?.isRetryable() == true }) {
            emit(SignInState.QrLoadFailure(onRetry = { perform(SignInAction.Load()) }))
        }.flatMapLatest { edmCodeResult ->
            pullEdmSessionFork(
                edmCodeResult.edmParams.encryptionKey,
                edmCodeResult.selector
            ).map { pullResult ->
                Pair(pullResult, edmCodeResult.qrCodeContent)
            }
        }.map { (pullResult, qrCodeContent) ->
            if (BuildConfig.DEBUG) {
                CoreLogger.d("GenerateEdmCode", "QR code: $qrCodeContent")
            }
            when (pullResult) {
                is PullEdmSessionFork.Result.Awaiting,
                is PullEdmSessionFork.Result.Loading,
                is PullEdmSessionFork.Result.NoConnection -> SignInState.Idle(
                    errorMessage = if (pullResult is PullEdmSessionFork.Result.NoConnection) {
                        context.getString(R.string.target_sign_in_no_connection)
                    } else null,
                    qrCode = qrCodeContent,
                    generateBitmap = qrBitmapGenerator::invoke
                )

                is PullEdmSessionFork.Result.Success -> {
                    perform(SignInAction.SessionForkPulled(pullResult.passphrase, pullResult.session))
                    SignInState.Loading
                }

                is PullEdmSessionFork.Result.UnrecoverableError ->
                    stateWithUnrecoverableError(onRetry = { perform(SignInAction.Load()) })
            }
        }.onStart {
            emit(SignInState.Loading)
        }.let {
            emitAll(it)
        }
    }

    private fun onSessionForkPulled(action: SignInAction.SessionForkPulled) = flow {
        emit(SignInState.Loading)

        createLoginSessionFromFork(accountType, action.passphrase, action.session)

        val authSecret = action.passphrase?.let { EncryptedAuthSecret.Passphrase(it) } ?: EncryptedAuthSecret.Absent
        val result = postLoginAccountSetup(
            userId = action.session.userId,
            encryptedAuthSecret = authSecret,
            requiredAccountType = accountType,
            isSecondFactorNeeded = false,
            isTwoPassModeNeeded = false,
            temporaryPassword = false,
        ).also {
            enqueueObservability(EdmPostLoginTotal(it.toObservabilityStatus()))
        }

        val state = when (result) {
            // Most `Result.Need.*` are handled separately by `AccountManagerObserver`.
            is PostLoginAccountSetup.Result.Need.ChooseUsername,
            is PostLoginAccountSetup.Result.Need.DeviceSecret,
            is PostLoginAccountSetup.Result.Need.SecondFactor,
            is PostLoginAccountSetup.Result.Need.TwoPassMode,
            is PostLoginAccountSetup.Result.AccountReady ->
                SignInState.SuccessfullySignedIn(Effect.of(SignInEvent.SignedIn(action.session.userId)))

            is PostLoginAccountSetup.Result.Need.ChangePassword -> SignInState.SuccessfullySignedIn(
                Effect.of(SignInEvent.SignedInAndPasswordChange(action.session.userId))
            )

            is PostLoginAccountSetup.Result.Error.UnlockPrimaryKeyError -> stateWithUnrecoverableError(
                message = context.getString(R.string.target_sign_in_passphrase_error),
                onRetry = null
            )

            is PostLoginAccountSetup.Result.Error.UserCheckError -> stateWithUnrecoverableError(
                message = result.error.localizedMessage,
                onRetry = null
            )
        }

        emit(state)
    }

    private fun stateWithUnrecoverableError(
        message: String = context.getString(R.string.target_sign_in_retryable_error),
        onRetry: (() -> Unit)?
    ) = SignInState.Failure(
        message = message,
        onRetry = onRetry
    )
}

private fun PostLoginAccountSetup.Result.toObservabilityStatus() = when (this) {
    is PostLoginAccountSetup.Result.AccountReady -> PostLoginStatus.accountReady
    is PostLoginAccountSetup.Result.Error.UnlockPrimaryKeyError -> PostLoginStatus.unlockPrimaryKeyError
    is PostLoginAccountSetup.Result.Error.UserCheckError -> PostLoginStatus.userCheckError
    is PostLoginAccountSetup.Result.Need.ChangePassword -> PostLoginStatus.needChangePassword
    is PostLoginAccountSetup.Result.Need.ChooseUsername -> PostLoginStatus.needChooseUsername
    is PostLoginAccountSetup.Result.Need.DeviceSecret -> PostLoginStatus.needDeviceSecret
    is PostLoginAccountSetup.Result.Need.SecondFactor -> PostLoginStatus.needSecondFactor
    is PostLoginAccountSetup.Result.Need.TwoPassMode -> PostLoginStatus.needTwoPassMode
}
