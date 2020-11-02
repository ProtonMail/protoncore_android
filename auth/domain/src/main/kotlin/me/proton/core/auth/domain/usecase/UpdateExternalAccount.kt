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

package me.proton.core.auth.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.core.auth.domain.crypto.CryptoProvider
import me.proton.core.auth.domain.entity.Addresses
import me.proton.core.auth.domain.entity.FullAddressKey
import me.proton.core.auth.domain.entity.KeySecurity
import me.proton.core.auth.domain.entity.KeyType
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.onFailure
import me.proton.core.domain.arch.onSuccess
import me.proton.core.network.domain.session.SessionId
import javax.inject.Inject

/**
 * Updates (associates) the chose username with an external account.
 * It will also generate new ProtonMail (internal) address and address keys for it.
 *
 * It is assumed that the chosen username is already available on the API, so the best is to invoke this use case
 * only after positive API response for the username availability.
 *
 * @author Dino Kadrikj.
 */
class UpdateExternalAccount @Inject constructor(
    private val authRepository: AuthRepository,
    private val cryptoProvider: CryptoProvider
) {

    /**
     * State sealed class with various (success, error) outcome state subclasses.
     */
    sealed class UpdateExternalAccountState {
        object Processing : UpdateExternalAccountState()
        data class Success(val address: Addresses) : UpdateExternalAccountState()
        sealed class Error : UpdateExternalAccountState() {
            data class Message(val message: String?) : Error()
            object EmptyCredentials : Error()
            object EmptyDomain : Error()
            object SetUsernameFailed : Error()
            data class GeneratingPrivateKeyFailed(val message: String? = null) : Error()
            data class GeneratingSignedKeyListFailed(val message: String? = null) : Error()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    // gopenpgp library can throw generic exception
    operator fun invoke(
        sessionId: SessionId,
        username: String,
        domain: String,
        passphrase: ByteArray
    ): Flow<UpdateExternalAccountState> = flow {
        if (username.isEmpty() || passphrase.isEmpty()) {
            emit(UpdateExternalAccountState.Error.EmptyCredentials)
            return@flow
        }
        if (domain.isEmpty()) {
            emit(UpdateExternalAccountState.Error.EmptyDomain)
            return@flow
        }
        emit(UpdateExternalAccountState.Processing)
        // step.1 set the username and create address with displayName equal to username
        val setUsernameResult = authRepository.setUsername(sessionId, username)
        val createAddressResult = authRepository.createAddress(sessionId, domain, username)

        setUsernameResult.onFailure { message, _, _ ->
            emit(UpdateExternalAccountState.Error.Message(message))
            return@flow
        }
        createAddressResult.onFailure { message, _, _ ->
            emit(UpdateExternalAccountState.Error.Message(message))
            return@flow
        }
        if (!(setUsernameResult as DataResult.Success).value) {
            emit(UpdateExternalAccountState.Error.SetUsernameFailed)
            return@flow
        }
        val address = (createAddressResult as DataResult.Success).value
        // step 2. generate new private key.
        val privateKey = try {
            cryptoProvider.generateNewPrivateKey(username, domain, passphrase, KeyType.RSA, KeySecurity.HIGH)
        } catch (privateKeyException: Exception) { // gopenpgp library throws generic exception
            emit(UpdateExternalAccountState.Error.GeneratingPrivateKeyFailed(privateKeyException.message))
            return@flow
        }
        // step 3. and generate signed key list for the newly generated private key.
        val signedKeyList = try {
            cryptoProvider.generateSignedKeyList(privateKey, passphrase)
        } catch (signedKeyListException: Exception) { // gopenpgp library throws generic exception
            emit(UpdateExternalAccountState.Error.GeneratingSignedKeyListFailed(signedKeyListException.message))
            return@flow
        }
        // step 4. at the end ask the API to create the address key for the new address.
        authRepository.createAddressKey(
            sessionId = sessionId, addressId = address.id, privateKey = privateKey, primary = true,
            signedKeyListData = signedKeyList.first, signedKeyListSignature = signedKeyList.second
        ).onFailure { message, _, _ ->
            emit(UpdateExternalAccountState.Error.Message(message))
        }.onSuccess {
            emit(UpdateExternalAccountState.Success(Addresses(listOf(address.copy(hasKeys = true, keys = listOf(it))))))
        }
    }
}
