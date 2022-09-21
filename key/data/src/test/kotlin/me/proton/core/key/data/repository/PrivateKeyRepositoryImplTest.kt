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
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.auth.data.api.response.SRPAuthenticationResponse
import me.proton.core.auth.domain.exception.InvalidServerAuthenticationException
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.UserId
import me.proton.core.key.data.api.KeyApi
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertFailsWith

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

    private val dispatcherProvider = TestDispatcherProvider

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
        repository = PrivateKeyRepositoryImpl(apiProvider)
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
                auth = mockk(relaxed = true),
                keys = listOf(mockk(relaxed = true)),
                userKeys = listOf(mockk(relaxed = true)),
                organizationKey = "test-org-key"
            )
        }
    }

}
