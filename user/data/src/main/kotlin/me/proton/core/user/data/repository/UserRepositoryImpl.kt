/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.user.data.repository

import android.content.Context
import android.util.Base64
import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.StoreRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import me.proton.core.auth.data.api.fido2.AuthenticationOptionsData
import me.proton.core.auth.data.api.fido2.PublicKeyCredentialDescriptorData
import me.proton.core.auth.data.api.fido2.PublicKeyCredentialRequestOptionsResponse
import me.proton.core.auth.data.api.request.Fido2Request
import me.proton.core.auth.data.api.response.isSuccess
import me.proton.core.auth.domain.usecase.ValidateServerProof
import me.proton.core.auth.fido.domain.ext.toJson
import me.proton.core.challenge.data.frame.ChallengeFrame
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.challenge.domain.framePrefix
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.data.arch.buildProtonStore
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.key.data.api.request.AuthRequest
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.protonApi.isSuccess
import me.proton.core.user.data.api.UserApi
import me.proton.core.user.data.api.request.CreateExternalUserRequest
import me.proton.core.user.data.api.request.CreateUserRequest
import me.proton.core.user.data.api.request.UnlockPasswordRequest
import me.proton.core.user.data.api.request.UnlockRequest
import me.proton.core.user.data.extension.toUser
import me.proton.core.user.domain.entity.CreateUserType
import me.proton.core.user.domain.entity.SecondFactorFido
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.user.domain.repository.UserLocalDataSource
import me.proton.core.user.domain.repository.UserRemoteDataSource
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.util.kotlin.CoroutineScopeProvider
import me.proton.core.util.kotlin.coroutine.result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("TooManyFunctions")
class UserRepositoryImpl @Inject constructor(
    private val provider: ApiProvider,
    @ApplicationContext private val context: Context,
    private val product: Product,
    private val validateServerProof: ValidateServerProof,
    scopeProvider: CoroutineScopeProvider,
    private val userLocalDataSource: UserLocalDataSource,
    private val userRemoteDataSource: UserRemoteDataSource
) : UserRepository {

    private val onPassphraseChangedListeners = mutableSetOf<PassphraseRepository.OnPassphraseChangedListener>()

    private val store = StoreBuilder.from(
        fetcher = Fetcher.of { userId: UserId ->
            userRemoteDataSource.fetch(userId)
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { userId -> userLocalDataSource.observe(userId) },
            writer = { _, input -> insertOrUpdate(input) },
            delete = null, // Not used.
            deleteAll = null // Not used.
        )
    ).buildProtonStore(scopeProvider)

    private suspend fun invalidateMemCache(userId: UserId) =
        store.clear(userId)

    private suspend fun insertOrUpdate(user: User) {
        userLocalDataSource.upsert(user) {
            invalidateMemCache(user.userId)
        }
    }

    override suspend fun addUser(user: User): Unit =
        insertOrUpdate(user)

    override suspend fun updateUser(user: User): Unit =
        insertOrUpdate(user)

    override suspend fun updateUserUsedSpace(userId: UserId, usedSpace: Long) =
        userLocalDataSource.updateUserUsedBaseSpace(userId, usedSpace)

    override suspend fun updateUserUsedBaseSpace(userId: UserId, usedBaseSpace: Long) {
        userLocalDataSource.updateUserUsedBaseSpace(userId, usedBaseSpace)
    }

    override suspend fun updateUserUsedDriveSpace(userId: UserId, usedDriveSpace: Long) {
        userLocalDataSource.updateUserUsedDriveSpace(userId, usedDriveSpace)
    }

    override fun observeUser(sessionUserId: SessionUserId, refresh: Boolean): Flow<User?> =
        store.stream(StoreRequest.cached(sessionUserId, refresh = refresh))
            .map { it.dataOrNull() }
            .distinctUntilChanged()

    override suspend fun getUser(sessionUserId: SessionUserId, refresh: Boolean): User =
        if (refresh) store.fresh(sessionUserId) else store.get(sessionUserId)

    /**
     * Create new [User]. Used during signup.
     */
    override suspend fun createUser(
        username: String,
        domain: String?,
        password: EncryptedString,
        recoveryEmail: String?,
        recoveryPhone: String?,
        referrer: String?,
        type: CreateUserType,
        auth: Auth,
        frames: List<ChallengeFrameDetails>,
        sessionUserId: SessionUserId?
    ): User = result("createUser") {
        provider.get<UserApi>(sessionUserId).invoke {
            val request = CreateUserRequest(
                username,
                recoveryEmail,
                recoveryPhone,
                referrer,
                type.value,
                AuthRequest.from(auth),
                domain,
                getUserSignUpFrameMap(frames)
            )
            createUser(request).user.toUser()
        }.valueOrThrow
    }

    /**
     * Create new [User]. Used during signup.
     */
    override suspend fun createExternalEmailUser(
        email: String,
        password: EncryptedString,
        referrer: String?,
        type: CreateUserType,
        auth: Auth,
        frames: List<ChallengeFrameDetails>,
        sessionUserId: SessionUserId?
    ): User = result("createExternalEmailUser") {
        provider.get<UserApi>(sessionUserId).invoke {
            val request = CreateExternalUserRequest(
                email,
                referrer,
                type.value,
                AuthRequest.from(auth),
                getUserSignUpFrameMap(frames)
            )
            createExternalUser(request).user.toUser()
        }.valueOrThrow
    }

    override suspend fun removeLockedAndPasswordScopes(
        sessionUserId: SessionUserId
    ): Boolean = provider.get<UserApi>(sessionUserId).invoke {
        lockPasswordAndLockedScopes().isSuccess()
    }.valueOrThrow

    override suspend fun unlockUserForLockedScope(
        sessionUserId: SessionUserId,
        srpProofs: SrpProofs,
        srpSession: String
    ): Boolean = provider.get<UserApi>(sessionUserId).invoke {
        val request = UnlockRequest(srpProofs.clientEphemeral, srpProofs.clientProof, srpSession)
        val response = unlockLockedScope(request)
        validateServerProof(
            response.serverProof,
            srpProofs.expectedServerProof
        ) { "getting locked scope failed" }
        response.isSuccess()
    }.valueOrThrow

    override suspend fun unlockUserForPasswordScope(
        sessionUserId: SessionUserId,
        srpProofs: SrpProofs,
        srpSession: String,
        secondFactorCode: String?,
        secondFactorFido: SecondFactorFido?
    ): Boolean = result("unlockUserForPasswordScope") {
        provider.get<UserApi>(sessionUserId).invoke {
            val request = UnlockPasswordRequest(
                srpProofs.clientEphemeral,
                srpProofs.clientProof,
                srpSession,
                secondFactorCode,
                secondFactorFido?.toFido2Request()
            )
            val response = unlockPasswordScope(request)
            validateServerProof(
                response.serverProof,
                srpProofs.expectedServerProof
            ) { "getting password scope failed" }
            response.isSuccess()
        }.valueOrThrow
    }

    override suspend fun checkUsernameAvailable(
        sessionUserId: SessionUserId?,
        username: String
    ) = result("checkUsernameAvailable") {
        provider.get<UserApi>(sessionUserId).invoke {
            usernameAvailable(username)
        }.throwIfError()
    }

    override suspend fun checkExternalEmailAvailable(
        sessionUserId: SessionUserId?,
        email: String
    ) = result("checkExternalEmailAvailable") {
        provider.get<UserApi>(sessionUserId).invoke {
            externalEmailAvailable(email)
        }.throwIfError()
    }

    // region PassphraseRepository

    private suspend fun internalSetPassphrase(
        userId: UserId,
        passphrase: EncryptedByteArray?
    ) {
        userLocalDataSource.setPassphrase(userId, passphrase) {
            invalidateMemCache(userId)
            onPassphraseChangedListeners.forEach { it.onPassphraseChanged(userId) }
        }
    }

    override suspend fun setPassphrase(userId: UserId, passphrase: EncryptedByteArray) =
        internalSetPassphrase(userId, passphrase)

    override suspend fun getPassphrase(userId: UserId): EncryptedByteArray? =
        userLocalDataSource.getPassphrase(userId)

    override suspend fun clearPassphrase(userId: UserId) =
        internalSetPassphrase(userId, null)

    override fun addOnPassphraseChangedListener(listener: PassphraseRepository.OnPassphraseChangedListener) {
        onPassphraseChangedListeners.add(listener)
    }

    // endregion

    // region Challenge frame

    private suspend fun getUserSignUpFrameMap(frames: List<ChallengeFrameDetails>): Map<String, ChallengeFrame?> {
        val prefix = product.framePrefix()
        val usernameFrame = frames.find { it.challengeFrame == "username" && it.flow == "signup" }
        val recoveryFrame = frames.find { it.challengeFrame == "recovery" && it.flow == "signup" }
        requireNotNull(usernameFrame)
        // recoveryFrame is optional.
        return mapOf(
            "$prefix-0" to ChallengeFrame.Username.from(context, usernameFrame),
            "$prefix-1" to ChallengeFrame.Recovery.from(context, recoveryFrame)
        )
    }

    // endregion
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun SecondFactorFido.toFido2Request(): Fido2Request {
    val optionsData = AuthenticationOptionsData(
        PublicKeyCredentialRequestOptionsResponse(
            challenge = publicKeyOptions.challenge,
            timeout = publicKeyOptions.timeout,
            rpId = publicKeyOptions.rpId,
            allowCredentials = publicKeyOptions.allowCredentials?.map {
                PublicKeyCredentialDescriptorData(
                    type = it.type,
                    id = it.id,
                    transports = it.transports
                )
            },
            userVerification = publicKeyOptions.userVerification,
            extensions = publicKeyOptions.extensions?.toJson()
        )
    )
    return Fido2Request(
        authenticationOptions = optionsData,
        clientData = clientData.toBase64(),
        authenticatorData = authenticatorData.toBase64(),
        signature = signature.toBase64(),
        credentialID = credentialID.toUByteArray()
    )
}

private fun ByteArray.toBase64(): String = Base64.encodeToString(this, Base64.NO_WRAP)
