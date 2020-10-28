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
package me.proton.core.accountmanager.data

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.humanverification.HumanVerificationHeaders
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId

class RepositoryMocks(
    private val session: Session,
    private val account: Account
) {

    @RelaxedMockK
    lateinit var accountRepository: AccountRepository

    @RelaxedMockK
    lateinit var authRepository: AuthRepository

    private val flowOfAccountLists = mutableListOf<List<Account>>()
    private val flowOfSessionLists = mutableListOf<List<Session>>()

    fun init() {
        MockKAnnotations.init(this)
    }

    @Suppress("LongMethod")
    fun setupAccountRepository() {
        val userIdSlot = slot<UserId>()
        val sessionIdSlot = slot<SessionId>()
        val accountStateSlot = slot<AccountState>()
        val sessionStateSlot = slot<SessionState>()
        val updatedScopesSlot = slot<List<String>>()
        val tokenTypeSlot = slot<String>()
        val tokenCodeSlot = slot<String>()
        val accessTokenSlot = slot<String>()
        val refreshTokenSlot = slot<String>()

        // Initial state.
        flowOfAccountLists.clear()
        flowOfSessionLists.clear()
        flowOfAccountLists.add(listOf(account))
        flowOfSessionLists.add(listOf(session))

        // For each updateAccountState -> emit a new updated List<Account> from getAccounts().
        coEvery { accountRepository.updateAccountState(capture(userIdSlot), capture(accountStateSlot)) } answers {
            flowOfAccountLists.add(
                listOf(
                    flowOfAccountLists.last().first { it.userId == userIdSlot.captured }.copy(
                        userId = userIdSlot.captured,
                        state = accountStateSlot.captured
                    )
                )
            )
        }
        coEvery { accountRepository.updateAccountState(capture(sessionIdSlot), capture(accountStateSlot)) } answers {
            flowOfAccountLists.add(
                listOf(
                    flowOfAccountLists.last().first { it.sessionId == sessionIdSlot.captured }.copy(
                        sessionId = sessionIdSlot.captured,
                        state = accountStateSlot.captured
                    )
                )
            )
        }

        // For each updateSessionState -> emit a new updated List<Account> from getAccounts().
        coEvery { accountRepository.updateSessionState(capture(sessionIdSlot), capture(sessionStateSlot)) } answers {
            flowOfAccountLists.add(
                listOf(
                    flowOfAccountLists.last().first { it.sessionId == sessionIdSlot.captured }.copy(
                        sessionId = sessionIdSlot.captured,
                        sessionState = sessionStateSlot.captured
                    )
                )
            )
        }

        // For each updateSessionScopes -> emit a new updated List<Session> from getSessions().
        coEvery { accountRepository.updateSessionScopes(capture(sessionIdSlot), capture(updatedScopesSlot)) } answers {
            flowOfSessionLists.add(
                listOf(
                    flowOfSessionLists.last().first { it.sessionId == sessionIdSlot.captured }.copy(
                        sessionId = sessionIdSlot.captured,
                        scopes = updatedScopesSlot.captured
                    )
                )
            )
        }

        // For each updateSessionHeaders -> emit a new updated List<Session> from getSessions().
        coEvery {
            accountRepository.updateSessionHeaders(
                capture(sessionIdSlot),
                capture(tokenTypeSlot),
                capture(tokenCodeSlot)
            )
        } answers {
            flowOfSessionLists.add(
                listOf(
                    flowOfSessionLists.last().first { it.sessionId == sessionIdSlot.captured }.copy(
                        sessionId = sessionIdSlot.captured,
                        headers = HumanVerificationHeaders(
                            tokenTypeSlot.captured,
                            tokenCodeSlot.captured
                        )
                    )
                )
            )
        }

        // For each updateSessionToken -> emit a new updated List<Session> from getSessions().
        coEvery {
            accountRepository.updateSessionToken(
                capture(sessionIdSlot),
                capture(accessTokenSlot),
                capture(refreshTokenSlot)
            )
        } answers {
            flowOfSessionLists.add(
                listOf(
                    session.copy(
                        sessionId = sessionIdSlot.captured,
                        accessToken = accessTokenSlot.captured,
                        refreshToken = refreshTokenSlot.captured
                    )
                )
            )
        }

        // Emit last state with same id if exist.
        coEvery { accountRepository.getAccountOrNull(capture(userIdSlot)) } answers {
            flowOfAccountLists.last().firstOrNull { it.userId == userIdSlot.captured }
        }
        coEvery { accountRepository.getAccountOrNull(capture(sessionIdSlot)) } answers {
            flowOfAccountLists.last().firstOrNull { it.sessionId == sessionIdSlot.captured }
        }

        // Emit all state with same id if exist.
        coEvery { accountRepository.getAccount(capture(sessionIdSlot)) } answers {
            val filteredLists = flowOfAccountLists.map { list ->
                list.firstOrNull { it.sessionId == sessionIdSlot.captured }
            }
            flowOf(*filteredLists.toTypedArray())
        }

        // Finally, emit all flow of Lists.
        every { accountRepository.getAccounts() } answers {
            flowOf(*flowOfAccountLists.toTypedArray())
        }
        every { accountRepository.getSessions() } answers {
            flowOf(*flowOfSessionLists.toTypedArray())
        }
    }

    fun setupAuthRepository() {
        val sessionIdSlot = slot<SessionId>()

        // Assume revokeSession is done successfully.
        coEvery { authRepository.revokeSession(capture(sessionIdSlot)) } answers {
            DataResult.Success(ResponseSource.Remote, true)
        }
    }
}
