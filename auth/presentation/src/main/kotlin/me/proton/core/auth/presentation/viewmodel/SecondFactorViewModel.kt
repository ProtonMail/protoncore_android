/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.auth.presentation.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import me.proton.android.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.ScopeInfo
import me.proton.core.auth.domain.usecase.PerformSecondFactor
import me.proton.core.auth.domain.usecase.PerformUserSetup
import me.proton.core.auth.domain.usecase.onError
import me.proton.core.auth.domain.usecase.onProcessing
import me.proton.core.auth.domain.usecase.onSecondFactorSuccess
import me.proton.core.auth.domain.usecase.onSuccess
import me.proton.core.network.domain.session.SessionId
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope

/**
 * View Model that serves the Second Factor authentication.
 */
class SecondFactorViewModel @ViewModelInject constructor(
    private val accountWorkflowHandler: AccountWorkflowHandler,
    private val performSecondFactor: PerformSecondFactor,
    private val performUserSetup: PerformUserSetup
) : ProtonViewModel(), ViewStateStoreScope {

    val secondFactorState = ViewStateStore<PerformSecondFactor.State>().lock

    fun startSecondFactorFlow(
        sessionId: SessionId,
        password: ByteArray,
        secondFactorCode: String,
        isTwoPassModeNeeded: Boolean
    ) {
        performSecondFactor(sessionId, secondFactorCode)
            .onProcessing { secondFactorState.post(it) }
            .onSecondFactorSuccess { success ->
                accountWorkflowHandler.handleSecondFactorSuccess(sessionId, success.scopeInfo.scopes)
                // No more steps -> directly setup user.
                if (!isTwoPassModeNeeded) {
                    // Raise Success.UserSetup.
                    setupUser(password, success.sessionId, success.scopeInfo)
                } else {
                    // Raise Success.SecondFactor.
                    secondFactorState.post(
                        PerformSecondFactor.State.Success.SecondFactor(
                            success.sessionId,
                            success.scopeInfo
                        )
                    )
                }
            }
            .onError { secondFactorState.post(it) }
            .launchIn(viewModelScope)
    }

    private fun setupUser(password: ByteArray, sessionId: SessionId, scopeInfo: ScopeInfo) {
        performUserSetup(sessionId, password)
            .onSuccess { success ->
                secondFactorState.post(PerformSecondFactor.State.Success.UserSetup(sessionId, scopeInfo, success.user))
            }
            .onError { error ->
                accountWorkflowHandler.handleSecondFactorFailed(sessionId)
                secondFactorState.post(PerformSecondFactor.State.Error.UserSetup(error))
            }
            .launchIn(viewModelScope)
    }

    fun stopSecondFactorFlow(
        sessionId: SessionId
    ): Job = viewModelScope.launch { accountWorkflowHandler.handleSecondFactorFailed(sessionId) }
}
