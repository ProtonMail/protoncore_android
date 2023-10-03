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
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.key.data.api.KeyApi
import me.proton.core.key.data.api.response.KeySaltResponse
import me.proton.core.key.data.api.response.KeySaltsResponse
import me.proton.core.key.data.db.KeySaltDao
import me.proton.core.key.data.db.KeySaltDatabase
import me.proton.core.key.data.entity.KeySaltEntity
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import org.junit.Before
import org.junit.Test

class KeySaltRepositoryImplTest {
    // region mocks
    private val keySaltDao = mockk<KeySaltDao>(relaxed = true)
    private val keySaltDatabase = mockk<KeySaltDatabase>(relaxed = true)

    private val keyApi = mockk<KeyApi>()
    private val apiManagerFactory = mockk<ApiManagerFactory>(relaxed = true)
    private val sessionProvider = mockk<SessionProvider>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testSessionId = SessionId("session-id")
    private val dispatcherProvider = TestDispatcherProvider(UnconfinedTestDispatcher())

    // endregion
    private lateinit var repository: KeySaltRepositoryImpl
    private lateinit var apiProvider: ApiProvider

    @Before
    fun beforeEveryTest() {
        every { keySaltDatabase.keySaltDao() } returns keySaltDao

        coEvery { sessionProvider.getSessionId(any()) } returns testSessionId
        val apiManager = object : ApiManager<KeyApi> {
            override suspend fun <T> invoke(
                forceNoRetryOnConnectionErrors: Boolean,
                block: suspend KeyApi.() -> T
            ): ApiResult<T> = ApiResult.Success(block.invoke(keyApi))
        }
        apiProvider = ApiProvider(apiManagerFactory, sessionProvider, dispatcherProvider)
        every { apiManagerFactory.create(any(), interfaceClass = KeyApi::class) } returns apiManager

        repository =
            KeySaltRepositoryImpl(keySaltDatabase, apiProvider, TestCoroutineScopeProvider(dispatcherProvider))
    }

    @Test
    fun `clear by user id calls dao delete`() = runTest {
        repository.clear(testUserId)
        coVerify(exactly = 1) { keySaltDao.deleteByUserId(testUserId) }
    }

    @Test
    fun `clear all calls dao delete all`() = runTest {
        repository.clearAll()
        coVerify(exactly = 1) { keySaltDao.deleteAll() }
    }

    @Test
    fun `get salts from cache`() = runTest {
        val keySaltEntity = KeySaltEntity(testUserId, KeyId("test-key-id"), "test-key-salt")
        val keySaltEntityList = listOf(keySaltEntity)
        every { keySaltDao.findAllByUserId(testUserId) } returns flowOf(keySaltEntityList)
        repository.getKeySalts(testUserId, false)
        verify(exactly = 1) { keySaltDao.findAllByUserId(testUserId) }
    }

    @Test
    fun `get salts online`() = runTest {
        val keySaltEntity = KeySaltEntity(testUserId, KeyId("test-key-id"), "test-key-salt")
        val keySaltEntityList = listOf(keySaltEntity)
        every { keySaltDao.findAllByUserId(testUserId) } returns flowOf(keySaltEntityList)

        val keySaltResponse = KeySaltsResponse(
            listOf(KeySaltResponse("test-key-id", "test-key-salt"))
        )
        coEvery { keyApi.getSalts() } returns keySaltResponse
        repository.getKeySalts(testUserId)
        coVerify(exactly = 1) { keyApi.getSalts() }
    }
}