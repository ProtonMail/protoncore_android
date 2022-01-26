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

package me.proton.core.user.data.repository

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.StoreRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import me.proton.core.auth.data.api.response.isSuccess
import me.proton.core.auth.domain.extension.requireValidProof
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.data.arch.buildProtonStore
import me.proton.core.data.arch.toDataResult
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.key.data.api.request.AuthRequest
import me.proton.core.key.domain.extension.updateIsActive
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.protonApi.isSuccess
import me.proton.core.user.data.api.UserApi
import me.proton.core.user.data.api.request.CreateExternalUserRequest
import me.proton.core.user.data.api.request.CreateUserRequest
import me.proton.core.user.data.api.request.UnlockPasswordRequest
import me.proton.core.user.data.api.request.UnlockRequest
import me.proton.core.user.data.db.UserDatabase
import me.proton.core.user.data.extension.toEntity
import me.proton.core.user.data.extension.toEntityList
import me.proton.core.user.data.extension.toUser
import me.proton.core.user.domain.entity.CreateUserType
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserKey
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.user.domain.repository.UserRepository
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl(
    private val db: UserDatabase,
    private val provider: ApiProvider,
    private val context: CryptoContext
) : UserRepository {

    private val onPassphraseChangedListeners = mutableSetOf<PassphraseRepository.OnPassphraseChangedListener>()

    private val userDao = db.userDao()
    private val userKeyDao = db.userKeyDao()
    private val userWithKeysDao = db.userWithKeysDao()

    private val store = StoreBuilder.from(
        fetcher = Fetcher.of { userId: UserId ->
            provider.get<UserApi>(userId).invoke {
                getUsers().user.toUser()
            }.valueOrThrow
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { userId -> observeUserLocal(userId) },
            writer = { _, input -> insertOrUpdate(input) },
            delete = null, // Not used.
            deleteAll = null // Not used.
        )
    ).buildProtonStore()

    private suspend fun invalidateMemCache(userId: UserId) =
        store.clear(userId)

    private fun List<UserKey>.updateIsActive(passphrase: EncryptedByteArray?): List<UserKey> =
        map { key -> key.copy(privateKey = key.privateKey.updateIsActive(context, passphrase)) }

    private suspend fun getUserLocal(userId: UserId): User? =
        userWithKeysDao.getByUserId(userId)?.toUser()

    private fun observeUserLocal(userId: UserId): Flow<User?> =
        userWithKeysDao.observeByUserId(userId).map { user -> user?.toUser() }

    private suspend fun insertOrUpdate(user: User) =
        db.inTransaction {
            // Get current passphrase -> don't overwrite passphrase.
            val passphrase = userDao.getPassphrase(user.userId)
            // Update isActive and passphrase.
            val userKeys = user.keys.updateIsActive(passphrase)
            // Insert in Database.
            userDao.insertOrUpdate(user.toEntity(passphrase))
            userKeyDao.insertOrUpdate(*userKeys.toEntityList().toTypedArray())
        }

    override suspend fun addUser(user: User) =
        insertOrUpdate(user)

    override suspend fun updateUser(user: User) =
        insertOrUpdate(user)

    override fun getUserFlow(sessionUserId: SessionUserId, refresh: Boolean): Flow<DataResult<User>> =
        store.stream(StoreRequest.cached(sessionUserId, refresh = refresh))
            .map { it.toDataResult() }
            .distinctUntilChanged()

    override suspend fun getUser(sessionUserId: SessionUserId, refresh: Boolean): User =
        if (refresh) store.fresh(sessionUserId) else store.get(sessionUserId)

    override suspend fun isUsernameAvailable(username: String): Boolean =
        provider.get<UserApi>().invoke {
            usernameAvailable(username).isSuccess()
        }.valueOrThrow

    /**
     * Create new [User]. Used during signup.
     */
    override suspend fun createUser(
        username: String,
        password: EncryptedString,
        recoveryEmail: String?,
        recoveryPhone: String?,
        referrer: String?,
        type: CreateUserType,
        auth: Auth
    ): User = provider.get<UserApi>().invoke {
        val request = CreateUserRequest(
            username,
            recoveryEmail,
            recoveryPhone,
            referrer,
            type.value,
            AuthRequest.from(auth)
        )
        createUser(request).user.toUser()
    }.valueOrThrow

    /**
     * Create new [User]. Used during signup.
     */
    override suspend fun createExternalEmailUser(
        email: String,
        password: EncryptedString,
        referrer: String?,
        type: CreateUserType,
        auth: Auth
    ): User = provider.get<UserApi>().invoke {
        val request = CreateExternalUserRequest(email, referrer, type.value, AuthRequest.from(auth))
        createExternalUser(request).user.toUser()
    }.valueOrThrow

    override suspend fun removeLockedAndPasswordScopes(sessionUserId: SessionUserId): Boolean =
        provider.get<UserApi>(sessionUserId).invoke {
            lockPasswordAndLockedScopes().isSuccess()
        }.valueOrThrow

    override suspend fun unlockUserForLockedScope(
        sessionUserId: SessionUserId,
        srpProofs: SrpProofs,
        srpSession: String
    ): Boolean =
        provider.get<UserApi>(sessionUserId).invoke {
            val request = UnlockRequest(srpProofs.clientEphemeral, srpProofs.clientProof, srpSession)
            val response = unlockLockedScope(request)
            response.serverProof.requireValidProof(srpProofs.expectedServerProof) { "getting locked scope failed" }
            response.isSuccess()
        }.valueOrThrow

    override suspend fun unlockUserForPasswordScope(
        sessionUserId: SessionUserId,
        srpProofs: SrpProofs,
        srpSession: String,
        twoFactorCode: String?
    ): Boolean =
        provider.get<UserApi>(sessionUserId).invoke {
            val request = UnlockPasswordRequest(
                srpProofs.clientEphemeral,
                srpProofs.clientProof,
                srpSession,
                twoFactorCode
            )
            val response = unlockPasswordScope(request)
            response.serverProof.requireValidProof(srpProofs.expectedServerProof) { "getting password scope failed" }
            response.isSuccess()
        }.valueOrThrow

    // region PassphraseRepository

    private suspend fun internalSetPassphrase(userId: UserId, passphrase: EncryptedByteArray?) {
        val passphraseChanged = db.inTransaction {
            if (passphrase == getPassphrase(userId)) {
                false
            } else {
                userDao.setPassphrase(userId, passphrase)
                insertOrUpdate(requireNotNull(getUserLocal(userId)))
                true
            }
        }
        if (passphraseChanged) {
            invalidateMemCache(userId)
            onPassphraseChangedListeners.forEach { it.onPassphraseChanged(userId) }
        }
    }

    override suspend fun setPassphrase(userId: UserId, passphrase: EncryptedByteArray) =
        internalSetPassphrase(userId, passphrase)

    override suspend fun getPassphrase(userId: UserId): EncryptedByteArray? =
        userDao.getPassphrase(userId)

    override suspend fun clearPassphrase(userId: UserId) =
        internalSetPassphrase(userId, null)

    override fun addOnPassphraseChangedListener(listener: PassphraseRepository.OnPassphraseChangedListener) {
        onPassphraseChangedListeners.add(listener)
    }

    //endregion
}
