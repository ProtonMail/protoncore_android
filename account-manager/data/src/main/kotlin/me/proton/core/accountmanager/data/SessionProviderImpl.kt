/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.accountmanager.data

import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import javax.inject.Inject

class SessionProviderImpl @Inject constructor(
    private val accountRepository: AccountRepository,
) : SessionProvider {

    private suspend fun getSessionOrNull(sessionId: SessionId): Session? =
        accountRepository.getSessionOrNull(sessionId)

    override suspend fun getSession(sessionId: SessionId?): Session? =
        (sessionId ?: getSessionId(userId = null))?.let { getSessionOrNull(it) }

    override suspend fun getSessions(): List<Session> =
        accountRepository.getSessions().firstOrNull().orEmpty()

    override suspend fun getSessionId(userId: UserId?): SessionId? =
        accountRepository.getSessionIdOrNull(userId)

    override suspend fun getUserId(sessionId: SessionId): UserId? =
        accountRepository.getAccountOrNull(sessionId)?.userId
}
