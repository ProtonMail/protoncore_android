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

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.compose.viewmodel.BaseViewModel
import me.proton.core.devicemigration.domain.usecase.DecodeEdmCode
import javax.inject.Inject

@HiltViewModel
public class ManualCodeInputViewModel @Inject constructor(
    private val decodeEdmCode: DecodeEdmCode
) : BaseViewModel<ManualCodeInputAction, ManualCodeInputState>(
    initialAction = ManualCodeInputAction.Load,
    initialState = ManualCodeInputState.Idle
) {
    override fun onAction(action: ManualCodeInputAction): Flow<ManualCodeInputState> = when (action) {
        is ManualCodeInputAction.Load -> flowOf(ManualCodeInputState.Idle)
        is ManualCodeInputAction.Submit -> onSubmit(action)
    }

    override suspend fun FlowCollector<ManualCodeInputState>.onError(throwable: Throwable) {
        emit(ManualCodeInputState.Error.Generic(throwable))
    }

    private fun onSubmit(action: ManualCodeInputAction.Submit) = flow {
        if (action.code.isBlank()) {
            emit(ManualCodeInputState.Error.EmptyCode)
        } else {
            emitAll(submitCode(code = action.code))
        }
    }

    private fun submitCode(code: String) = flow {
        emit(ManualCodeInputState.Loading)

        val edmParams = decodeEdmCode(code)
        if (edmParams == null) {
            emit(ManualCodeInputState.Error.InvalidCode)
        } else {
            TODO("Encrypt the passphrase and push the fork.")
            emit(ManualCodeInputState.SignedInSuccessfully)
        }
    }
}
