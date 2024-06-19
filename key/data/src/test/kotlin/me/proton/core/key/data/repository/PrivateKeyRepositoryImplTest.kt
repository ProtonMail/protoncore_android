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

package me.proton.core.key.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.auth.data.api.response.SRPAuthenticationResponse
import me.proton.core.auth.domain.exception.InvalidServerAuthenticationException
import me.proton.core.auth.domain.usecase.ValidateServerProof
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.UserId
import me.proton.core.key.data.TestKeys
import me.proton.core.key.data.api.KeyApi
import me.proton.core.key.data.api.request.ReactivateKeysRequest
import me.proton.core.key.data.api.request.SignedKeyListRequest
import me.proton.core.key.data.api.response.CreateAddressKeyResponse
import me.proton.core.key.domain.entity.key.PrivateAddressKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.protonApi.GenericResponse
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PrivateKeyRepositoryImplTest {

    // region mocks
    private val keyApi = mockk<KeyApi>(relaxed = true)
    private val sessionProvider = mockk<SessionProvider>(relaxed = true)
    private val apiFactory = mockk<ApiManagerFactory>(relaxed = true)
    private lateinit var apiProvider: ApiProvider
    private lateinit var repository: PrivateKeyRepositoryImpl
    // endregion

    // region test data
    private val testSessionId = "test-session-id"
    private val testUserId = "test-user-id"
    private val testSrpProofs = SrpProofs(
        clientEphemeral = "test-client-ephemeral",
        clientProof = "test-client-proof",
        expectedServerProof = "test-server-proof"
    )

    // endregion

    private val validateServerProof = ValidateServerProof()

    private val dispatcherProvider = TestDispatcherProvider()

    @Before
    fun beforeEveryTest() {
        // GIVEN
        val apiManager = object : ApiManager<KeyApi> {
            override suspend fun <T> invoke(
                forceNoRetryOnConnectionErrors: Boolean,
                block: suspend KeyApi.() -> T
            ): ApiResult<T> = ApiResult.Success(block.invoke(keyApi))
        }
        coEvery { sessionProvider.getSessionId(any()) } returns SessionId(testSessionId)
        apiProvider = ApiProvider(apiFactory, sessionProvider, dispatcherProvider)
        every { apiFactory.create(any(), interfaceClass = KeyApi::class) } returns apiManager
        repository = PrivateKeyRepositoryImpl(apiProvider, validateServerProof)
    }

    @Test
    fun updatePrivateKeys_fails_wrong_srp_server_proof() = runTest(dispatcherProvider.Main) {
        // GIVEN
        coEvery { keyApi.updatePrivateKeys(any()) } returns SRPAuthenticationResponse(
            code = 1000,
            serverProof = testSrpProofs.expectedServerProof + "corrupted"
        )
        // WHEN & THEN
        assertFailsWith<InvalidServerAuthenticationException> {
            repository.updatePrivateKeys(
                sessionUserId = UserId(testUserId),
                keySalt = "test-key-salt",
                srpProofs = testSrpProofs,
                srpSession = "test-srp-session",
                secondFactorCode = "test-2fa-code",
                secondFactorFido = null,
                auth = mockk(relaxed = true),
                keys = listOf(mockk(relaxed = true)),
                userKeys = listOf(mockk(relaxed = true))
            )
        }
    }

    @Test
    fun `createAddressKey old`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        coEvery { keyApi.createAddressKey(any()) } returns CreateAddressKeyResponse(mockk(relaxed = true))
        coEvery { keyApi.createAddressKeyOld(any()) } returns CreateAddressKeyResponse(mockk(relaxed = true))
        // WHEN & THEN
        repository.createAddressKey(
            sessionUserId = UserId(testUserId),
            key = PrivateAddressKey(
                "test-address-id",
                PrivateKey(
                    TestKeys.Key1.privateKey,
                    isPrimary = true,
                    passphrase = EncryptedByteArray(TestKeys.Key1.passphrase)
                ),
                token = null,
                signature = "test-signature",
                signedKeyList = mockk(relaxed = true)
            )
        )
        coVerify(exactly = 1) { keyApi.createAddressKeyOld(any()) }
        coVerify(exactly = 0) { keyApi.createAddressKey(any()) }
    }

    @Test
    fun `createAddressKey old signature null`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        coEvery { keyApi.createAddressKey(any()) } returns CreateAddressKeyResponse(mockk(relaxed = true))
        coEvery { keyApi.createAddressKeyOld(any()) } returns CreateAddressKeyResponse(mockk(relaxed = true))
        // WHEN & THEN
        repository.createAddressKey(
            sessionUserId = UserId(testUserId),
            key = PrivateAddressKey(
                "test-address-id",
                PrivateKey(
                    TestKeys.Key1.privateKey,
                    isPrimary = true,
                    passphrase = EncryptedByteArray(TestKeys.Key1.passphrase)
                ),
                token = "test-token",
                signature = null,
                signedKeyList = mockk(relaxed = true)
            )
        )
        coVerify(exactly = 1) { keyApi.createAddressKeyOld(any()) }
        coVerify(exactly = 0) { keyApi.createAddressKey(any()) }
    }

    @Test
    fun `createAddressKey new`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        coEvery { keyApi.createAddressKey(any()) } returns CreateAddressKeyResponse(mockk(relaxed = true))
        coEvery { keyApi.createAddressKeyOld(any()) } returns CreateAddressKeyResponse(mockk(relaxed = true))
        // WHEN & THEN
        repository.createAddressKey(
            sessionUserId = UserId(testUserId),
            key = PrivateAddressKey(
                "test-address-id",
                PrivateKey(
                    TestKeys.Key1.privateKey,
                    isPrimary = true,
                    passphrase = EncryptedByteArray(TestKeys.Key1.passphrase)
                ),
                token = "test-token",
                signature = "test-signature",
                signedKeyList = mockk(relaxed = true)
            )
        )
        coVerify(exactly = 0) { keyApi.createAddressKeyOld(any()) }
        coVerify(exactly = 1) { keyApi.createAddressKey(any()) }
    }

    @Test
    fun `setup initial keys`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        coEvery { keyApi.createAddressKey(any()) } returns CreateAddressKeyResponse(mockk(relaxed = true))
        coEvery { keyApi.createAddressKeyOld(any()) } returns CreateAddressKeyResponse(mockk(relaxed = true))
        // WHEN & THEN
        repository.setupInitialKeys(
            sessionUserId = UserId(testUserId),
            primaryKey = "test-primary-key",
            primaryKeySalt = "test-primary-key-salt",
            addressKeys = listOf(
                PrivateAddressKey(
                    "test-address-id",
                    PrivateKey(
                        TestKeys.Key1.privateKey,
                        isPrimary = true,
                        passphrase = EncryptedByteArray(TestKeys.Key1.passphrase)
                    ),
                    token = "test-token",
                    signature = "test-signature",
                    signedKeyList = mockk(relaxed = true)
                )
            ),
            auth = Auth(1, "test-modulus", "test-salt", "test-verifier")
        )
        coVerify(exactly = 1) { keyApi.setupInitialKeys(any()) }
    }

    @Test
    fun `reactivate private key`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        val testPrivateKeyId = "test-private-key-id"
        val testPrivateKey = mockk<PrivateKey>(relaxed = true)
        val testAddressKeyFingerprint = "test-address-key-fingerprint"
        val testAddressKeyFingerprints = listOf(testAddressKeyFingerprint)
        val testAddressId = "test-address-id"
        val testPublicSKL = PublicSignedKeyList(
            data = "test-data",
            signature = "test-signature",
            minEpochId = null,
            maxEpochId = null,
            expectedMinEpochId = null
        )
        val testSignedKeyLists: Map<String, PublicSignedKeyList> = mapOf(testAddressId to testPublicSKL)
        coEvery { keyApi.reactivateKeys(any(), any()) } returns GenericResponse(1000)
        // WHEN
        val result = repository.reactivatePrivateKey(
            sessionUserId = UserId(testUserId),
            privateKeyId = testPrivateKeyId,
            privateKey = testPrivateKey,
            addressKeysFingerprints = testAddressKeyFingerprints,
            signedKeyLists =  testSignedKeyLists
        )
        // THEN
        assertNotNull(result)
        assertTrue(result)
        coVerify(exactly = 1) { keyApi.reactivateKeys(
            testPrivateKeyId,
            ReactivateKeysRequest(
                testPrivateKey.key,
                testAddressKeyFingerprints,
                mapOf(testAddressId to SignedKeyListRequest(
                    "test-data", "test-signature"
                ))
            )
        )
        }
    }
}
