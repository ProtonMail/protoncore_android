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

package me.proton.core.auth.domain.repository

import me.proton.core.auth.domain.entity.Address
import me.proton.core.auth.domain.entity.AddressKey
import me.proton.core.auth.domain.entity.Addresses
import me.proton.core.auth.domain.entity.Auth
import me.proton.core.auth.domain.entity.FullAddressKey
import me.proton.core.auth.domain.entity.KeySalts
import me.proton.core.auth.domain.entity.LoginInfo
import me.proton.core.auth.domain.entity.Modulus
import me.proton.core.auth.domain.entity.ScopeInfo
import me.proton.core.auth.domain.entity.SecondFactorProof
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.entity.User
import me.proton.core.domain.arch.DataResult
import me.proton.core.network.domain.session.SessionId

@Suppress("LongParameterList")
interface AuthRepository {

    /**
     * Get Login Info needed to start the login process.
     */
    suspend fun getLoginInfo(
        username: String,
        clientSecret: String
    ): DataResult<LoginInfo>

    /**
     * Perform Login to create a session (accessToken, refreshToken, sessionId, ...).
     */
    suspend fun performLogin(
        username: String,
        clientSecret: String,
        clientEphemeral: String,
        clientProof: String,
        srpSession: String
    ): DataResult<SessionInfo>

    /**
     * Perform Two Factor for the Login process for a given [SessionId].
     */
    suspend fun performSecondFactor(
        sessionId: SessionId,
        secondFactorProof: SecondFactorProof
    ): DataResult<ScopeInfo>

    /**
     * Returns the basic user information for a given [SessionId].
     */
    suspend fun getUser(sessionId: SessionId): DataResult<User>

    /**
     * Returns the key salt information for a given [SessionId].
     */
    suspend fun getSalts(sessionId: SessionId): DataResult<KeySalts>

    /**
     * Revoke session for a given [SessionId].
     */
    suspend fun revokeSession(sessionId: SessionId): DataResult<Boolean>

    /**
     * Perform check if the chosen username is available.
     */
    suspend fun isUsernameAvailable(username: String): DataResult<Boolean>

    /**
     * Gets all available domains on the API.
     */
    suspend fun getAvailableDomains(): DataResult<List<String>>

    /**
     * Fetches all addresses for the user.
     */
    suspend fun getAddresses(sessionId: SessionId): DataResult<Addresses>

    /**
     * Sets a chosen username for a external address.
     */
    suspend fun setUsername(sessionId: SessionId, username: String): DataResult<Boolean>

    /**
     * Creates ProtonMail address.
     */
    suspend fun createAddress(
        sessionId: SessionId,
        domain: String,
        displayName: String
    ): DataResult<Address>

    /**
     * Creates new address key for ProtonMail address.
     * Expects non-null values for [FullAddressKey] `token` and `signature`.
     */
    suspend fun createAddressKey(
        sessionId: SessionId,
        addressId: String,
        privateKey: String,
        primary: Boolean,
        signedKeyListData: String,
        signedKeyListSignature: String
    ): DataResult<FullAddressKey>

    /**
     * Asks API to generate new random modulus.
     */
    suspend fun randomModulus(): DataResult<Modulus>

    /**
     * Sets up an address key.
     */
    suspend fun setupAddressKeys(
        primaryKey: String,
        keySalt: String,
        addressKeyList: List<AddressKey>,
        auth: Auth
    ): DataResult<User>
}
