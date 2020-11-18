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

package me.proton.core.auth.data.repository

import me.proton.core.auth.data.api.AuthenticationApi
import me.proton.core.auth.data.entity.LoginInfoRequest
import me.proton.core.auth.data.entity.LoginRequest
import me.proton.core.auth.data.entity.SecondFactorRequest
import me.proton.core.auth.data.entity.UniversalTwoFactorRequest
import me.proton.core.auth.data.entity.request.AddressKeyEntity
import me.proton.core.auth.data.entity.request.AddressKeySetupRequest
import me.proton.core.auth.data.entity.request.AddressSetupRequest
import me.proton.core.auth.data.entity.request.AuthEntity
import me.proton.core.auth.data.entity.request.SetUsernameRequest
import me.proton.core.auth.data.entity.request.SetupKeysRequest
import me.proton.core.auth.data.entity.request.SignedKeyList
import me.proton.core.auth.domain.entity.Address
import me.proton.core.auth.domain.entity.AddressKey
import me.proton.core.auth.domain.entity.Addresses
import me.proton.core.auth.domain.entity.Auth
import me.proton.core.auth.domain.entity.Domain
import me.proton.core.auth.domain.entity.FullAddressKey
import me.proton.core.auth.domain.entity.KeySalts
import me.proton.core.auth.domain.entity.LoginInfo
import me.proton.core.auth.domain.entity.Modulus
import me.proton.core.auth.domain.entity.ScopeInfo
import me.proton.core.auth.domain.entity.SecondFactorProof
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.entity.User
import me.proton.core.auth.domain.entity.firstOrDefault
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.data.arch.toDataResponse
import me.proton.core.domain.arch.DataResult
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.ResponseCodes
import me.proton.core.network.domain.TimeoutOverride
import me.proton.core.network.domain.session.SessionId
import me.proton.core.util.kotlin.toInt

/**
 * Implementation of the [AuthRepository].
 * Provides implementation of the all auth related API routes.
 *
 * @author Dino Kadrikj.
 */
class AuthRepositoryImpl(
    private val provider: ApiProvider
) : AuthRepository {

    /**
     * Fetches the login info object for a particular [username] account.
     * The results are needed for further BL decisions on the login process.
     *
     * @param username the account's username trying to make a login request.
     * @param clientSecret client/app specific string.
     *
     * @return [LoginInfo] object containing meta-data for further login process operations.
     */
    override suspend fun getLoginInfo(
        username: String,
        clientSecret: String
    ): DataResult<LoginInfo> = provider.get<AuthenticationApi>().invoke {
        val request = LoginInfoRequest(username, clientSecret)
        getLoginInfo(request).toLoginInfo(username)
    }.toDataResponse()

    /**
     * Performs the login request to the API to try to get a valid Access Token and Session for the Account/username.
     *
     * @param username the account's username trying to make a login request.
     * @param clientSecret client/app specific string.
     * @param clientEphemeral Base64 encoded SrpProof generated client ephemeral.
     * @param clientProof Base64 encoded SrpProof generated proof.
     * @param srpSession the SRPSession returned from the [getLoginInfo] API result.
     *
     * @return [SessionInfo] login result containing the Access and Refresh tokens and additional meta-data.
     */
    override suspend fun performLogin(
        username: String,
        clientSecret: String,
        clientEphemeral: String,
        clientProof: String,
        srpSession: String
    ): DataResult<SessionInfo> = provider.get<AuthenticationApi>().invoke {
        val request = LoginRequest(username, clientSecret, clientEphemeral, clientProof, srpSession)
        performLogin(request).toSessionInfo(username)
    }.toDataResponse()

    /**
     * Performs the second factor request for the Accounts that have second factor enabled.
     *
     * @param sessionId the session Id for the current user making this request.
     * @param secondFactorProof the [SecondFactorProof] object containing the 2FA details.
     *
     * @return [ScopeInfo] object containing full list of available scopes for the user.
     */
    override suspend fun performSecondFactor(
        sessionId: SessionId,
        secondFactorProof: SecondFactorProof
    ): DataResult<ScopeInfo> = provider.get<AuthenticationApi>(sessionId).invoke {
        val request = when (secondFactorProof) {
            is SecondFactorProof.SecondFactorCode -> SecondFactorRequest(
                secondFactorCode = secondFactorProof.code
            )
            is SecondFactorProof.SecondFactorSignature -> SecondFactorRequest(
                universalTwoFactorRequest = UniversalTwoFactorRequest(
                    keyHandle = secondFactorProof.keyHandle,
                    clientData = secondFactorProof.clientData,
                    signatureData = secondFactorProof.signatureData
                )
            )
        }
        performSecondFactor(request).toScopeInfo()
    }.toDataResponse()

    /**
     * Revokes the session for the user. In particular this is practically logging out the user from the backend.
     *
     * @param sessionId the session Id for the current user making this request.
     *
     * @return boolean result of the logout/session revoking operation.
     */
    override suspend fun revokeSession(sessionId: SessionId): DataResult<Boolean> =
        provider.get<AuthenticationApi>(sessionId).invoke(true) {
            revokeSession(
                TimeoutOverride(
                    connectionTimeoutSeconds = 1,
                    readTimeoutSeconds = 1,
                    writeTimeoutSeconds = 1
                )
            ).code.isSuccessResponse()
        }.toDataResponse()

    /**
     * Perform check if the chosen username is available.
     */
    override suspend fun isUsernameAvailable(username: String): DataResult<Boolean> =
        provider.get<AuthenticationApi>().invoke {
            usernameAvailable(username).code.isSuccessResponse()
        }.toDataResponse()

    /**
     * Gets all available domains on the API.
     */
    override suspend fun getAvailableDomains(): DataResult<List<Domain>> =
        provider.get<AuthenticationApi>().invoke {
            getAvailableDomains().domains
        }.toDataResponse()

    /**
     * Fetches all addresses for the user.
     */
    override suspend fun getAddresses(sessionId: SessionId): DataResult<Addresses> =
        provider.get<AuthenticationApi>(sessionId).invoke {
            getAddresses().toAddresses()
        }.toDataResponse()

    /**
     * Sets a chosen username for a external address.
     */
    override suspend fun setUsername(sessionId: SessionId, username: String): DataResult<Boolean> =
        provider.get<AuthenticationApi>(sessionId).invoke {
            setUsername(SetUsernameRequest(username)).code.isSuccessResponse()
        }.toDataResponse()

    /**
     * Creates ProtonMail address.
     */
    override suspend fun createAddress(
        sessionId: SessionId,
        domain: String,
        displayName: String
    ): DataResult<Address> =
        provider.get<AuthenticationApi>(sessionId).invoke {
            createAddress(AddressSetupRequest(domain, displayName)).address.toAddress()
        }.toDataResponse()

    /**
     * Creates new address key for ProtonMail address.
     * Expects non-null values for [FullAddressKey] `token` and `signature`.
     */
    override suspend fun createAddressKey(
        sessionId: SessionId,
        addressId: String,
        privateKey: String,
        primary: Boolean,
        signedKeyListData: String,
        signedKeyListSignature: String
    ): DataResult<FullAddressKey> =
        provider.get<AuthenticationApi>(sessionId).invoke {
            val body = AddressKeySetupRequest(
                addressId = addressId,
                privateKey = privateKey,
                primary = primary.toInt(),
                signedKeyList = SignedKeyList(signedKeyListData, signedKeyListSignature)
            )
            createAddressKeyOld(body).key.toAddressKey()
        }.toDataResponse()

    /**
     * Returns new random modulus generated from the API.
     */
    override suspend fun randomModulus(): DataResult<Modulus> =
        provider.get<AuthenticationApi>().invoke {
            randomModulus().toModulus()
        }.toDataResponse()

    /**
     * Sets up the address primary key/
     */
    override suspend fun setupAddressKeys(
        sessionId: SessionId,
        primaryKey: String,
        keySalt: String,
        addressKeyList: List<AddressKey>,
        auth: Auth
    ): DataResult<User> =
        provider.get<AuthenticationApi>(sessionId).invoke {
            val setupKeysRequest = SetupKeysRequest(
                primaryKey = primaryKey,
                keySalt = keySalt,
                addressKeys = addressKeyList.map {
                    AddressKeyEntity.fromAddressKeySetup(it)
                },
                auth = AuthEntity(auth.version, auth.modulusId, auth.salt, auth.verifier)
            )
            setupAddressKeys(setupKeysRequest).user.toUser()
        }.toDataResponse()

    /**
     * Fetches the full user details from the API.
     *
     * @param sessionId the session Id for the current user making this request.
     *
     * @return [User] object with full user details.
     */
    override suspend fun getUser(sessionId: SessionId): DataResult<User> =
        provider.get<AuthenticationApi>(sessionId).invoke {
            getUser().user.toUser()
        }.toDataResponse()

    /**
     * Fetches the user-keys salts from the API.
     *
     * @param sessionId the session Id for the current user making this request.
     *
     * @return [KeySalts] containing salts for all user keys.
     */
    override suspend fun getSalts(sessionId: SessionId): DataResult<KeySalts> =
        provider.get<AuthenticationApi>(sessionId).invoke {
            getSalts().toKeySalts()
        }.toDataResponse()
}

internal fun Int.isSuccessResponse(): Boolean = this == ResponseCodes.OK
