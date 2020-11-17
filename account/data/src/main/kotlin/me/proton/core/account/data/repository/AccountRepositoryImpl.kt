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

package me.proton.core.account.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import me.proton.core.account.data.db.AccountDatabase
import me.proton.core.account.data.entity.AccountMetadataEntity
import me.proton.core.account.data.extension.toAccountEntity
import me.proton.core.account.data.extension.toSessionEntity
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.data.crypto.StringCrypto
import me.proton.core.data.crypto.encrypt
import me.proton.core.data.db.CommonConverters
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.util.kotlin.exhaustive
import java.util.concurrent.ConcurrentHashMap

@Suppress("TooManyFunctions")
class AccountRepositoryImpl(
    private val product: Product,
    private val db: AccountDatabase,
    private val stringCrypto: StringCrypto
) : AccountRepository {

    private val accountDao = db.accountDao()
    private val sessionDao = db.sessionDao()
    private val accountMetadataDao = db.accountMetadataDao()

    private val humanVerificationDetails: ConcurrentHashMap<SessionId, HumanVerificationDetails?> = ConcurrentHashMap()

    // Accept 10 nested/concurrent state changes -> extraBufferCapacity.
    private val accountStateChanged = MutableSharedFlow<Account>(extraBufferCapacity = 10)
    private val sessionStateChanged = MutableSharedFlow<Account>(extraBufferCapacity = 10)

    private fun tryEmitAccountStateChanged(account: Account) {
        if (!accountStateChanged.tryEmit(account))
            throw IllegalStateException("Too many nested account state changes, extra buffer capacity exceeded.")
    }

    private fun tryEmitSessionStateChanged(account: Account) {
        if (!sessionStateChanged.tryEmit(account))
            throw IllegalStateException("Too many nested session state changes, extra buffer capacity exceeded.")
    }

    private suspend fun updateAccountMetadata(userId: UserId) =
        accountMetadataDao.insertOrUpdate(
            AccountMetadataEntity(
                userId = userId.id,
                product = product,
                primaryAtUtc = System.currentTimeMillis()
            )
        )

    private suspend fun deleteAccountMetadata(userId: UserId) =
        accountMetadataDao.delete(userId.id, product)

    override fun getAccount(userId: UserId): Flow<Account?> =
        accountDao.findByUserId(userId.id)
            .map { account -> account?.toAccount() }
            .distinctUntilChanged()

    override fun getAccount(sessionId: SessionId): Flow<Account?> =
        accountDao.findBySessionId(sessionId.id)
            .map { account -> account?.toAccount() }
            .distinctUntilChanged()

    override fun getAccounts(): Flow<List<Account>> =
        accountDao.findAll()
            .map { it.map { account -> account.toAccount() } }
            .distinctUntilChanged()

    override suspend fun getAccountOrNull(userId: UserId): Account? =
        accountDao.getByUserId(userId.id)?.toAccount()

    override suspend fun getAccountOrNull(sessionId: SessionId): Account? =
        accountDao.getBySessionId(sessionId.id)?.toAccount()

    override fun getSessions(): Flow<List<Session>> =
        sessionDao.findAll(product).map { list ->
            list.map { it.toSession(stringCrypto) }
        }.distinctUntilChanged()

    override fun getSession(sessionId: SessionId): Flow<Session?> =
        sessionDao.findBySessionId(sessionId.id)
            .map { it?.toSession(stringCrypto) }
            .distinctUntilChanged()

    override suspend fun getSessionOrNull(sessionId: SessionId): Session? =
        sessionDao.get(sessionId.id)?.toSession(stringCrypto)

    override suspend fun getSessionIdOrNull(userId: UserId): SessionId? =
        sessionDao.getSessionId(userId.id)?.let { SessionId(it) }

    override suspend fun createOrUpdateAccountSession(account: Account, session: Session) {
        require(session.isValid()) {
            "Session is not valid: $session\n.At least sessionId.id, accessToken and refreshToken must be valid."
        }
        // Update/raise Initializing state without session.
        db.inTransaction {
            accountDao.insertOrUpdate(
                account.copy(
                    state = AccountState.NotReady,
                    sessionId = null,
                    sessionState = null
                ).toAccountEntity()
            )
        }
        // Update/raise provided state with session.
        db.inTransaction {
            val sessionState = account.sessionState ?: SessionState.Authenticated
            sessionDao.insertOrUpdate(session.toSessionEntity(account.userId, product, stringCrypto))
            accountDao.addSession(account.userId.id, session.sessionId.id)
            updateAccountState(account.userId, account.state)
            updateSessionState(session.sessionId, sessionState)
        }
    }

    override suspend fun deleteAccount(userId: UserId) =
        accountDao.delete(userId.id)

    override suspend fun deleteSession(sessionId: SessionId) {
        db.inTransaction {
            accountDao.removeSession(sessionId.id)
            sessionDao.delete(sessionId.id)
        }
    }

    override fun onAccountStateChanged(): Flow<Account> =
        accountStateChanged.asSharedFlow().distinctUntilChanged()

    override fun onSessionStateChanged(): Flow<Account> =
        sessionStateChanged.asSharedFlow().distinctUntilChanged()

    override suspend fun updateAccountState(userId: UserId, state: AccountState) {
        db.inTransaction {
            when (state) {
                AccountState.Ready -> updateAccountMetadata(userId)
                AccountState.Disabled,
                AccountState.Removed,
                AccountState.NotReady,
                AccountState.TwoPassModeNeeded,
                AccountState.TwoPassModeSuccess,
                AccountState.TwoPassModeFailed -> deleteAccountMetadata(userId)
            }.exhaustive
            accountDao.updateAccountState(userId.id, state)
            getAccountOrNull(userId)?.let { tryEmitAccountStateChanged(it) }
        }
    }

    override suspend fun updateAccountState(sessionId: SessionId, state: AccountState) {
        getAccountOrNull(sessionId)?.let { updateAccountState(it.userId, state) }
    }

    override suspend fun updateSessionState(sessionId: SessionId, state: SessionState) {
        db.inTransaction {
            accountDao.updateSessionState(sessionId.id, state)
            getAccountOrNull(sessionId)?.let { tryEmitSessionStateChanged(it) }
        }
    }

    override suspend fun updateSessionScopes(sessionId: SessionId, scopes: List<String>) =
        sessionDao.updateScopes(sessionId.id, CommonConverters.fromListOfStringToString(scopes).orEmpty())

    override suspend fun updateSessionHeaders(sessionId: SessionId, tokenType: String?, tokenCode: String?) =
        sessionDao.updateHeaders(sessionId.id, tokenType?.encrypt(stringCrypto), tokenCode?.encrypt(stringCrypto))

    override suspend fun updateSessionToken(sessionId: SessionId, accessToken: String, refreshToken: String) =
        sessionDao.updateToken(sessionId.id, accessToken.encrypt(stringCrypto), refreshToken.encrypt(stringCrypto))

    override fun getPrimaryUserId(): Flow<UserId?> =
        accountMetadataDao.observeLatestPrimary(product).map { it?.let { UserId(it.userId) } }
            .distinctUntilChanged()

    override suspend fun setAsPrimary(userId: UserId) {
        db.inTransaction {
            val state = accountDao.findByUserId(userId.id).firstOrNull()?.state
            check(state == AccountState.Ready) {
                "Account is not ${AccountState.Ready}, it cannot be set as primary."
            }
            updateAccountMetadata(userId)
        }
    }

    override suspend fun getHumanVerificationDetails(id: SessionId): HumanVerificationDetails? =
        humanVerificationDetails[id]

    override suspend fun setHumanVerificationDetails(id: SessionId, details: HumanVerificationDetails?) {
        humanVerificationDetails[id] = details
    }
}
