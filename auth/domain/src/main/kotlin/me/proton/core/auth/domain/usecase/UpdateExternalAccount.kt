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
import me.proton.core.auth.domain.entity.Address
import me.proton.core.auth.domain.entity.AddressType
import me.proton.core.auth.domain.entity.Addresses
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
    sealed class State {
        object Processing : State()
        data class Success(val address: Addresses) : State()
        sealed class Error : State() {
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
    ): Flow<State> = flow {
        if (username.isEmpty() || passphrase.isEmpty()) {
            emit(State.Error.EmptyCredentials)
            return@flow
        }
        if (domain.isEmpty()) {
            emit(State.Error.EmptyDomain)
            return@flow
        }
        emit(State.Processing)
        // step.1 set the username and create address with displayName equal to username
        val setUsernameResult = authRepository.setUsername(sessionId, username)
        setUsernameResult.onFailure { message, _, _ ->
            emit(State.Error.Message(message))
            return@flow
        }

        val existingAddress = getAddress(sessionId)
        val address = if (existingAddress == null) {
            // try to create address
            val createAddressResult = authRepository.createAddress(sessionId, domain, username)
            createAddressResult.onFailure { message, _, _ ->
                emit(State.Error.Message(message))
                return@flow
            }
            (createAddressResult as DataResult.Success).value
        } else existingAddress

        if (!(setUsernameResult as DataResult.Success).value) {
            emit(State.Error.SetUsernameFailed)
            return@flow
        }
        // step 2. generate new private key.
        val randomSalt = cryptoProvider.createNewKeySalt()
        val generatedPassphrase = cryptoProvider.generatePassphrase(passphrase, randomSalt)
        val privateKey = try {
            cryptoProvider.generateNewPrivateKey(username, domain, generatedPassphrase, KeyType.RSA, KeySecurity.HIGH)
        } catch (privateKeyException: Exception) { // gopenpgp library throws generic exception
            emit(State.Error.GeneratingPrivateKeyFailed(privateKeyException.message))
            return@flow
        }
        // step 3. and generate signed key list for the newly generated private key.
        val signedKeyList = try {
            cryptoProvider.generateSignedKeyList(privateKey, generatedPassphrase)
        } catch (signedKeyListException: Exception) { // gopenpgp library throws generic exception
            emit(State.Error.GeneratingSignedKeyListFailed(signedKeyListException.message))
            return@flow
        }
        // step 4. at the end ask the API to create the address key for the new address.
        authRepository.createAddressKey(
            sessionId = sessionId,
            addressId = address.id,
            privateKey = privateKey,
            primary = true,
            signedKeyListData = signedKeyList.first,
            signedKeyListSignature = signedKeyList.second
        ).onFailure { message, _, _ ->
            emit(State.Error.Message(message))
        }.onSuccess {
            emit(State.Success(Addresses(listOf(address.copy(hasKeys = true, keys = listOf(it))))))
        }
    }

    /**
     * Returns the address with type [AddressType.ORIGINAL]
     */
    private suspend fun getAddress(sessionId: SessionId): Address? {
        // try to get addresses
        val addressesResult = authRepository.getAddresses(sessionId)
        return if (addressesResult is DataResult.Success) {
            addressesResult.value.addresses.firstOrNull {
                it.type == AddressType.ORIGINAL
            }
        } else null
    }
}
