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

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import me.proton.core.compose.effect.Effect
import me.proton.core.compose.viewmodel.BaseViewModel
import me.proton.core.devicemigration.domain.usecase.ObserveEdmCode
import me.proton.core.devicemigration.domain.usecase.PullEdmSessionFork
import me.proton.core.devicemigration.presentation.qr.QrBitmapGenerator
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
internal class SignInViewModel @Inject constructor(
    private val observeEdmCode: ObserveEdmCode,
    private val pullEdmSessionFork: PullEdmSessionFork,
    private val qrBitmapGenerator: QrBitmapGenerator,
) : BaseViewModel<SignInAction, SignInStateHolder>(
    initialAction = SignInAction.Load(),
    initialState = SignInStateHolder(state = SignInState.Loading),
) {
    override fun onAction(action: SignInAction): Flow<SignInStateHolder> = when (action) {
        is SignInAction.Load -> onLoad()
        is SignInAction.SessionForkPulled -> onSessionForkPulled(action)
    }

    override suspend fun FlowCollector<SignInStateHolder>.onError(throwable: Throwable) {
        emit(stateWithUnrecoverableError())
    }

    private fun onLoad(): Flow<SignInStateHolder> = observeEdmCode(sessionId = null).flatMapLatest { edmCodeResult ->
        pullEdmSessionFork(edmCodeResult.edmParams.encryptionKey, edmCodeResult.selector).map { pullResult ->
            Pair(pullResult, edmCodeResult.qrCodeContent)
        }
    }.map { (pullResult, qrCodeContent) ->
        when (pullResult) {
            is PullEdmSessionFork.Result.Awaiting,
            is PullEdmSessionFork.Result.Loading -> SignInStateHolder(
                state = SignInState.Idle(
                    qrCode = qrCodeContent,
                    generateBitmap = qrBitmapGenerator::invoke
                )
            )

            is PullEdmSessionFork.Result.Success -> {
                perform(SignInAction.SessionForkPulled(pullResult.passphrase, pullResult.session))
                SignInStateHolder(state = SignInState.Loading)
            }

            is PullEdmSessionFork.Result.UnrecoverableError -> stateWithUnrecoverableError()
        }
    }.onStart { emit(SignInStateHolder(state = SignInState.Loading)) }

    private fun onSessionForkPulled(action: SignInAction.SessionForkPulled) = flow {
        emit(SignInStateHolder(state = SignInState.Loading))
        delay(3.seconds) // TODO remove
        TODO("perform post-login actions")
        SignInStateHolder(
            effect = Effect.of(SignInEvent.SignedIn(action.session.userId)),
            state = SignInState.SuccessfullySignedIn
        )
    }

    private fun stateWithUnrecoverableError() = SignInStateHolder(
        state = SignInState.UnrecoverableError(
            onRetry = { perform(SignInAction.Load()) }
        )
    )
}
