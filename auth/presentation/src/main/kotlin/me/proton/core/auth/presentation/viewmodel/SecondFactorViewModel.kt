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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.android.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.usecase.PerformSecondFactor
import me.proton.core.network.domain.session.SessionId
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope

/**
 * View Model that serves the Second Factor authentication.
 */
class SecondFactorViewModel @ViewModelInject constructor(
    private val accountWorkflowHandler: AccountWorkflowHandler,
    private val performSecondFactor: PerformSecondFactor
) : ProtonViewModel(), ViewStateStoreScope {

    val secondFactorState = ViewStateStore<PerformSecondFactor.SecondFactorState>().lock

    fun startSecondFactorFlow(
        sessionId: SessionId,
        secondFactorCode: String,
        isTwoPassModeNeeded: Boolean = false
    ) {
        performSecondFactor(sessionId, secondFactorCode, isTwoPassModeNeeded).onEach {
            if (it is PerformSecondFactor.SecondFactorState.Success) {
                accountWorkflowHandler.handleSecondFactorSuccess(
                    sessionId = sessionId,
                    updatedScopes = it.scopeInfo.scopes
                )
            }
            secondFactorState.post(it)
        }.launchIn(viewModelScope)
    }

    fun stopSecondFactorFlow(
        sessionId: SessionId
    ): Job = viewModelScope.launch { accountWorkflowHandler.handleSecondFactorFailed(sessionId) }
}
