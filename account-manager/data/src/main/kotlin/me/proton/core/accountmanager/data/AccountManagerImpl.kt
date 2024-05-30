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

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState.CreateAccountFailed
import me.proton.core.account.domain.entity.AccountState.CreateAccountNeeded
import me.proton.core.account.domain.entity.AccountState.CreateAccountSuccess
import me.proton.core.account.domain.entity.AccountState.CreateAddressFailed
import me.proton.core.account.domain.entity.AccountState.CreateAddressNeeded
import me.proton.core.account.domain.entity.AccountState.CreateAddressSuccess
import me.proton.core.account.domain.entity.AccountState.Disabled
import me.proton.core.account.domain.entity.AccountState.NotReady
import me.proton.core.account.domain.entity.AccountState.Ready
import me.proton.core.account.domain.entity.AccountState.Removed
import me.proton.core.account.domain.entity.AccountState.TwoPassModeFailed
import me.proton.core.account.domain.entity.AccountState.TwoPassModeNeeded
import me.proton.core.account.domain.entity.AccountState.TwoPassModeSuccess
import me.proton.core.account.domain.entity.AccountState.UnlockFailed
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.account.domain.entity.isReady
import me.proton.core.account.domain.entity.isSecondFactorNeeded
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.isCredentialLess
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountManagerImpl @Inject constructor(
    product: Product,
    private val scopeProvider: CoroutineScopeProvider,
    private val accountRepository: AccountRepository,
    private val authRepository: AuthRepository,
    private val userManager: UserManager,
    private val sessionListener: SessionListener,
) : AccountManager(product), AccountWorkflowHandler {

    private val removeSessionLock = Mutex()

    private suspend fun removeSession(sessionId: SessionId) {
        removeSessionLock.withLock {
            accountRepository.getAccountOrNull(sessionId)?.let { account ->
                if (account.sessionState == SessionState.Authenticated) {
                    authRepository.revokeSession(sessionId)
                }
            }
            accountRepository.deleteSession(sessionId)
        }
    }

    private fun removeAccount(account: Account): Job {
        return scopeProvider.GlobalDefaultSupervisedScope.launch {
            accountRepository.updateAccountState(account.userId, Removed)
            account.sessionId?.let { removeSession(it) }
            accountRepository.deleteAccount(account.userId)
        }
    }

    private fun disableAccount(account: Account, keepSession: Boolean): Job {
        return scopeProvider.GlobalDefaultSupervisedScope.launch {
            accountRepository.updateAccountState(account.userId, Disabled)
            account.sessionId?.takeUnless { keepSession }?.let { removeSession(it) }
            userManager.lock(account.userId)
        }
    }

    private suspend fun disableAccount(sessionId: SessionId, keepSession: Boolean) {
        accountRepository.getAccountOrNull(sessionId)?.let { disableAccount(it, keepSession).join() }
    }

    private suspend fun clearSessionDetails(userId: UserId) {
        accountRepository.getSessionIdOrNull(userId)?.let { accountRepository.clearSessionDetails(it) }
    }

    override suspend fun addAccount(account: Account, session: Session) {
        handleSession(account.copy(state = Ready), session)
    }

    override suspend fun removeAccount(userId: UserId, waitForCompletion: Boolean) {
        accountRepository.getAccountOrNull(userId)?.let {
            removeAccount(it).takeIf { waitForCompletion }?.join()
        }
    }

    override suspend fun disableAccount(userId: UserId, waitForCompletion: Boolean, keepSession: Boolean) {
        accountRepository.getAccountOrNull(userId)?.let {
            disableAccount(it, keepSession).takeIf { waitForCompletion }?.join()
        }
    }

    override fun getAccount(userId: UserId): Flow<Account?> =
        accountRepository.getAccount(userId)

    override fun getAccounts(): Flow<List<Account>> =
        accountRepository.getAccounts()

    override fun getSessions(): Flow<List<Session>> =
        accountRepository.getSessions()

    override fun onAccountStateChanged(initialState: Boolean): Flow<Account> =
        accountRepository.onAccountStateChanged(initialState)

    override fun onSessionStateChanged(initialState: Boolean): Flow<Account> =
        accountRepository.onSessionStateChanged(initialState)

    override fun getPrimaryUserId(): Flow<UserId?> =
        accountRepository.getPrimaryUserId()

    override suspend fun getPreviousPrimaryUserId(): UserId? =
        accountRepository.getPreviousPrimaryUserId()

    override suspend fun setAsPrimary(userId: UserId) =
        accountRepository.setAsPrimary(userId)

    // region AccountWorkflowHandler

    override suspend fun handleSession(account: Account, session: Session) {
        sessionListener.withLock(session.sessionId) {
            // Remove any existing Session.
            accountRepository.getSessionIdOrNull(account.userId)?.let { removeSession(it) }
            // Account state must be != Ready if SecondFactorNeeded.
            val state = if (account.isReady() && account.isSecondFactorNeeded()) NotReady else account.state
            accountRepository.createOrUpdateAccountSession(account.copy(state = state), session)
        }
    }

    override suspend fun handleTwoPassModeNeeded(userId: UserId) {
        accountRepository.updateAccountState(userId, TwoPassModeNeeded)
    }

    override suspend fun handleTwoPassModeSuccess(userId: UserId) {
        accountRepository.updateAccountState(userId, TwoPassModeSuccess)
    }

    override suspend fun handleTwoPassModeFailed(userId: UserId) {
        accountRepository.updateAccountState(userId, TwoPassModeFailed)
    }

    override suspend fun handleSecondFactorSuccess(sessionId: SessionId, updatedScopes: List<String>) {
        accountRepository.updateSessionScopes(sessionId, updatedScopes)
        accountRepository.updateSessionState(sessionId, SessionState.SecondFactorSuccess)
        accountRepository.updateSessionState(sessionId, SessionState.Authenticated)
    }

    override suspend fun handleSecondFactorFailed(sessionId: SessionId) {
        accountRepository.updateSessionState(sessionId, SessionState.SecondFactorFailed)
        disableAccount(sessionId, keepSession = false)
    }

    override suspend fun handleCreateAddressNeeded(userId: UserId) {
        accountRepository.updateAccountState(userId, CreateAddressNeeded)
    }

    override suspend fun handleCreateAddressSuccess(userId: UserId) {
        accountRepository.updateAccountState(userId, CreateAddressSuccess)
    }

    override suspend fun handleCreateAddressFailed(userId: UserId) {
        accountRepository.updateAccountState(userId, CreateAddressFailed)
    }

    override suspend fun handleCreateAccountNeeded(userId: UserId) {
        accountRepository.updateAccountState(userId, CreateAccountNeeded)
    }

    override suspend fun handleCreateAccountSuccess(userId: UserId) {
        accountRepository.updateAccountState(userId, CreateAccountSuccess)
        disableAccount(userId, keepSession = true)
    }

    override suspend fun handleCreateAccountFailed(userId: UserId) {
        accountRepository.updateAccountState(userId, CreateAccountFailed)
    }

    override suspend fun handleUnlockFailed(userId: UserId) {
        accountRepository.updateAccountState(userId, UnlockFailed)
        disableAccount(userId)
    }

    override suspend fun handleAccountReady(userId: UserId) {
        accountRepository.updateAccountState(userId, Ready)
        clearSessionDetails(userId)
        disableCredentialLessAccounts(userId)
    }

    override suspend fun handleAccountNotReady(userId: UserId) {
        accountRepository.updateAccountState(userId, NotReady)
    }

    override suspend fun handleAccountDisabled(userId: UserId) {
        disableAccount(userId)
    }

    // endregion

    private suspend fun disableCredentialLessAccounts(readyUserId: UserId) {
        val isReadyUserCredentialLess = userManager.getUser(readyUserId).isCredentialLess()
        accountRepository.getAccounts().first().filter { it.state == Ready }.forEach { account ->
            val user = userManager.getUser(account.userId)
            when {
                user.userId == readyUserId -> Unit // Ignore self.
                user.isCredentialLess() -> disableAccount(
                    userId = user.userId,
                    keepSession = !isReadyUserCredentialLess // Only 1 CredentialLess session at a time.
                )
            }
        }
    }
}
