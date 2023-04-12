/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.key.data.repository

import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.key.data.api.KeyApi
import me.proton.core.key.data.db.PublicAddressDao
import me.proton.core.key.data.db.PublicAddressDatabase
import me.proton.core.key.data.db.PublicAddressKeyDao
import me.proton.core.key.data.db.PublicAddressWithKeysDao
import me.proton.core.key.data.entity.PublicAddressKeyEntity
import me.proton.core.key.domain.repository.PublicAddressVerifier
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Optional

class PublicAddressRepositoryImplTest {

    private lateinit var repositoryImpl: PublicAddressRepositoryImpl
    private val db: PublicAddressDatabase = mockk()
    private val sessionProvider = mockk<SessionProvider>()
    private val keyApi = mockk<KeyApi>()
    private val apiFactory = mockk<ApiManagerFactory>()
    private lateinit var apiProvider: ApiProvider

    private val testSessionId = SessionId("test-session-id")
    private val testUserId = UserId("test-user-id")

    private val dispatcherProvider = TestDispatcherProvider(UnconfinedTestDispatcher())

    private val publicAddressDao = mockk<PublicAddressDao>()
    private val publicAddressWithKeysDao = mockk<PublicAddressWithKeysDao>()
    private val publicAddressKeyDao = mockk<PublicAddressKeyDao>()

    private val publicAddressVerifier = mockk<PublicAddressVerifier>()

    @Before
    fun setUp() {
        val apiManager = object : ApiManager<KeyApi> {
            override suspend fun <T> invoke(
                forceNoRetryOnConnectionErrors: Boolean,
                block: suspend KeyApi.() -> T
            ): ApiResult<T> = ApiResult.Success(block.invoke(keyApi))
        }

        coEvery { sessionProvider.getSessionId(testUserId) } returns testSessionId
        apiProvider = ApiProvider(apiFactory, sessionProvider, dispatcherProvider)
        every { apiFactory.create(testSessionId, interfaceClass = KeyApi::class) } returns apiManager
        coEvery { db.publicAddressDao() } returns publicAddressDao
        coEvery { db.publicAddressKeyDao() } returns publicAddressKeyDao
        coEvery { db.publicAddressWithKeysDao() } returns publicAddressWithKeysDao
        val transactionLambda = slot<suspend () -> Unit>()
        coEvery { db.inTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }
        coJustRun {
            publicAddressVerifier.verifyPublicAddress(any(), any())
        }
        repositoryImpl = PublicAddressRepositoryImpl(
            db,
            apiProvider,
            TestCoroutineScopeProvider(dispatcherProvider),
            Optional.of(publicAddressVerifier)
        )
    }

    @Test
    fun `Old public keys are removed from local db`() = runTest {
        // given
        val testEmail = "email"
        val storedKeys = mutableListOf("key1", "key2")
        coJustRun { publicAddressDao.insertOrUpdate(any()) }
        coEvery { publicAddressKeyDao.deleteByEmail(testEmail) } coAnswers {
            storedKeys.clear()
        }
        val insertKeys = mutableListOf<PublicAddressKeyEntity>()
        coEvery { publicAddressKeyDao.insertOrUpdate(*varargAll { insertKeys.add(it) }) } coAnswers {
            insertKeys.forEach { key ->
                if (key.email == testEmail) {
                    storedKeys.add(key.publicKey)
                }
            }
            insertKeys.clear()
        }
        coEvery {
            keyApi.getPublicAddressKeys(testEmail, any())
        } returns mockk {
            every { toPublicAddress(testEmail) } returns mockk(relaxed = true) {
                every { email } returns testEmail
                every { keys } returns listOf(
                    mockk(relaxed = true) {
                        every { email } returns testEmail
                        every { publicKey.key } returns "key2"
                    },
                    mockk(relaxed = true) {
                        every { email } returns testEmail
                        every { publicKey.key } returns "key3"
                    }
                )
            }
        }
        coEvery { publicAddressWithKeysDao.findWithKeysByEmail(testEmail) } answers {
            flowOf(
                mockk(relaxed = true) {
                    every { entity.email } returns testEmail
                    every { keys } returns storedKeys.map { key ->
                        mockk(relaxed = true) {
                            every { publicKey } returns key
                        }
                    }
                }
            )
        }
        val expectedKeys = listOf("key2", "key3")
        // when
        val address = repositoryImpl.getPublicAddress(testUserId, testEmail)
        // then
        assertEquals(expectedKeys, storedKeys)
        assertEquals(expectedKeys, address.keys.map { it.publicKey.key })
        assertEquals(testEmail, address.email)
        coVerify {
            publicAddressVerifier.verifyPublicAddress(
                testUserId,
                any()
            )
            keyApi.getPublicAddressKeys(testEmail, any())
            publicAddressDao.insertOrUpdate(match { it.email == testEmail })
            publicAddressKeyDao.deleteByEmail(testEmail)
            publicAddressKeyDao.insertOrUpdate(*varargAll { it.publicKey in listOf("key2", "key3") })
        }
    }
}
