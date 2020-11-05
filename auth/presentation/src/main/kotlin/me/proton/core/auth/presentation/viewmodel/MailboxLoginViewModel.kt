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
import me.proton.core.auth.domain.usecase.PerformUserSetup
import me.proton.core.auth.domain.usecase.onError
import me.proton.core.auth.domain.usecase.onProcessing
import me.proton.core.auth.domain.usecase.onSuccess
import me.proton.core.network.domain.session.SessionId
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope

/**
 * View model class for handling the mailbox login and passphrase generation.
 */
class MailboxLoginViewModel @ViewModelInject constructor(
    private val accountWorkflowHandler: AccountWorkflowHandler,
    private val performUserSetup: PerformUserSetup
) : ProtonViewModel(), ViewStateStoreScope {

    val mailboxLoginState = ViewStateStore<PerformUserSetup.State>().lock

    /**
     * Attempts the mailbox login flow. This includes whole procedure with passphrase generation and API handling.
     */
    fun startUserSetup(
        sessionId: SessionId,
        password: ByteArray
    ) {
        performUserSetup(sessionId, password)
            .onProcessing { mailboxLoginState.post(it) }
            .onSuccess {
                accountWorkflowHandler.handleTwoPassModeSuccess(sessionId)
                mailboxLoginState.post(it)
            }
            .onError { mailboxLoginState.post(it) }
            .launchIn(viewModelScope)
    }

    fun stopMailboxLoginFlow(
        sessionId: SessionId
    ): Job = viewModelScope.launch { accountWorkflowHandler.handleTwoPassModeFailed(sessionId) }
}
