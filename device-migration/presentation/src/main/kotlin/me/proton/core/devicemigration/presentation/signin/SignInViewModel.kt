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
import me.proton.core.devicemigration.domain.usecase.ShouldIncludeEncryptionKey
import me.proton.core.devicemigration.presentation.BuildConfig
import me.proton.core.devicemigration.presentation.R
import me.proton.core.devicemigration.presentation.qr.QrBitmapGenerator
import me.proton.core.util.kotlin.CoreLogger
import java.util.Optional
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull

@HiltViewModel
public open class SignInViewModel @Inject constructor(
    private val accountType: AccountType,
    @ApplicationContext private val context: Context,
    private val createLoginSessionFromFork: CreateLoginSessionFromFork,
    private val observeEdmCode: ObserveEdmCode,
    private val postLoginAccountSetup: PostLoginAccountSetup,
    private val pullEdmSessionFork: PullEdmSessionFork,
    private val qrBitmapGenerator: QrBitmapGenerator,
    private val shouldIncludeEncryptionKey: Optional<ShouldIncludeEncryptionKey>
) : BaseViewModel<SignInAction, SignInState>(
    initialAction = SignInAction.Load(),
    initialState = SignInState.Loading
) {
    override fun onAction(action: SignInAction): Flow<SignInState> = when (action) {
        is SignInAction.Load -> onLoad()
        is SignInAction.SessionForkPulled -> onSessionForkPulled(action)
    }

    override suspend fun FlowCollector<SignInState>.onError(throwable: Throwable) {
        emit(stateWithUnrecoverableError(onRetry = { perform(SignInAction.Load()) }))
    }

    private fun onLoad(): Flow<SignInState> = observeEdmCode(
        sessionId = null,
        withEncryptionKey = shouldIncludeEncryptionKey.getOrNull()?.invoke() ?: ShouldIncludeEncryptionKey.DEFAULT
    ).flatMapLatest { edmCodeResult ->
        pullEdmSessionFork(edmCodeResult.edmParams.encryptionKey, edmCodeResult.selector).map { pullResult ->
            Pair(pullResult, edmCodeResult.qrCodeContent)
        }
    }.map { (pullResult, qrCodeContent) ->
        if (BuildConfig.DEBUG) {
            CoreLogger.d("GenerateEdmCode", "QR code: $qrCodeContent")
        }
        when (pullResult) {
            is PullEdmSessionFork.Result.Awaiting,
            is PullEdmSessionFork.Result.Loading -> SignInState.Idle(
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
    }.onStart { emit(SignInState.Loading) }

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
        )

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
