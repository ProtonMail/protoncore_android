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

package me.proton.core.usersettings.data.repository

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.usersettings.data.api.OrganizationApi
import me.proton.core.usersettings.data.api.response.OrganizationKeysResponse
import me.proton.core.usersettings.data.db.OrganizationKeysDatabase
import me.proton.core.usersettings.data.db.dao.OrganizationKeysDao
import me.proton.core.usersettings.data.extension.fromResponse
import me.proton.core.usersettings.data.extension.toEntity
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OrganizationKeysRepositoryImplTest {
    // region mocks
    private val sessionProvider = mockk<SessionProvider>(relaxed = true)
    private val organizationApi = mockk<OrganizationApi>(relaxed = true)

    private val apiFactory = mockk<ApiManagerFactory>(relaxed = true)
    private lateinit var apiProvider: ApiProvider
    private lateinit var repository: OrganizationKeysRepositoryImpl

    private val db = mockk<OrganizationKeysDatabase>(relaxed = true)
    private val organizationKeysDao = mockk<OrganizationKeysDao>(relaxed = true)
    // endregion

    // region test data
    private val testSessionId = "test-session-id"
    private val testUserId = "test-user-id"
    // endregion

    @Before
    fun beforeEveryTest() {
        // GIVEN
        every { db.organizationKeysDao() } returns organizationKeysDao
        coEvery { sessionProvider.getSessionId(any()) } returns SessionId(testSessionId)
        apiProvider = ApiProvider(apiFactory, sessionProvider)
        every { apiFactory.create(any(), interfaceClass = OrganizationApi::class) } returns TestApiManager(
            organizationApi
        )

        repository = OrganizationKeysRepositoryImpl(db, apiProvider)
    }

    @Test
    fun `get organization keys returns success`() = runBlockingTest {
        // GIVEN
        val organizationKeysResponse = OrganizationKeysResponse(
            publicKey = "test-public-key",
            privateKey = "test-private-key"
        )
        coEvery { organizationApi.getOrganizationKeys() } returns organizationKeysResponse
        every { organizationKeysDao.observeByUserId(any()) } returns flowOf(
            organizationKeysResponse.fromResponse(UserId(testUserId)).toEntity()
        )

        // WHEN
        val response = repository.getOrganizationKeys(sessionUserId = UserId(testUserId))
        // THEN
        assertNotNull(response)
        assertEquals("test-public-key", response.publicKey)
        assertEquals("test-private-key", response.privateKey)
        verify { organizationKeysDao.observeByUserId(any()) }
    }
}