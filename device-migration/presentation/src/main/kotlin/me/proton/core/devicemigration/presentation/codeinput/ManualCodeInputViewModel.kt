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

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import me.proton.core.compose.effect.Effect
import me.proton.core.compose.viewmodel.BaseViewModel
import me.proton.core.devicemigration.domain.usecase.DecodeEdmCode
import me.proton.core.devicemigration.domain.usecase.PushEdmSessionFork
import me.proton.core.devicemigration.presentation.DeviceMigrationRoutes.Arg.getUserId
import me.proton.core.devicemigration.presentation.util.toDecodeStatus
import me.proton.core.domain.arch.ErrorMessageContext
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.EdmDecodeQrCodeTotal
import me.proton.core.observability.domain.metrics.EdmForkPushTotal
import me.proton.core.util.kotlin.coroutine.flowWithResultContext
import javax.inject.Inject

@HiltViewModel
internal class ManualCodeInputViewModel @Inject constructor(
    private val decodeEdmCode: DecodeEdmCode,
    errorMessageContext: ErrorMessageContext,
    override val observabilityManager: ObservabilityManager,
    private val pushEdmSessionFork: PushEdmSessionFork,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ManualCodeInputAction, ManualCodeInputStateHolder>(
    initialAction = ManualCodeInputAction.Load,
    initialState = ManualCodeInputStateHolder(state = ManualCodeInputState.Loading)
), ObservabilityContext, ErrorMessageContext by errorMessageContext {
    private val userId: UserId by lazy { savedStateHandle.getUserId() }

    override fun onAction(action: ManualCodeInputAction): Flow<ManualCodeInputStateHolder> = when (action) {
        is ManualCodeInputAction.Load -> onLoad()
        is ManualCodeInputAction.Submit -> onSubmit(action)
    }

    override suspend fun FlowCollector<ManualCodeInputStateHolder>.onError(throwable: Throwable) {
        emit(
            ManualCodeInputStateHolder(
                effect = Effect.of(
                    ManualCodeInputEvent.ErrorMessage(getUserMessageOrDefault(throwable))
                ),
                state = ManualCodeInputState.Idle
            )
        )
    }

    // ACTION HANDLERS

    private fun onLoad() = flow {
        emit(ManualCodeInputStateHolder(state = ManualCodeInputState.Idle))
    }

    private fun onSubmit(action: ManualCodeInputAction.Submit) = flow {
        if (action.code.isBlank()) {
            emit(ManualCodeInputStateHolder(state = ManualCodeInputState.Error.EmptyCode))
        } else {
            emitAll(submitCode(code = action.code))
        }
    }

    private fun submitCode(code: String) = flowWithResultContext {
        onResultEnqueueObservability("decodeEdmCode") { EdmDecodeQrCodeTotal(toDecodeStatus()) }
        onResultEnqueueObservability("forkSession") { EdmForkPushTotal(this) }

        emit(ManualCodeInputStateHolder(state = ManualCodeInputState.Loading))

        val edmParams = decodeEdmCode(code)
        if (edmParams == null) {
            emit(ManualCodeInputStateHolder(state = ManualCodeInputState.Error.InvalidCode))
        } else {
            pushEdmSessionFork(userId = userId, params = edmParams)
            emit(
                ManualCodeInputStateHolder(
                    effect = consumableEffect(ManualCodeInputEvent.Success),
                    state = ManualCodeInputState.SignedInSuccessfully
                )
            )
        }
    }

    private fun consumableEffect(event: ManualCodeInputEvent): Effect<ManualCodeInputEvent> =
        Effect.of(event)
}
