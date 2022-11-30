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
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.android.api.TestApiManager
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import me.proton.core.usersettings.data.api.OrganizationApi
import me.proton.core.usersettings.data.api.response.OrganizationResponse
import me.proton.core.usersettings.data.api.response.SingleOrganizationResponse
import me.proton.core.usersettings.data.db.OrganizationDatabase
import me.proton.core.usersettings.data.db.dao.OrganizationDao
import me.proton.core.usersettings.data.extension.fromResponse
import me.proton.core.usersettings.data.extension.toEntity
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OrganizationRepositoryImplTest {
    // region mocks
    private val sessionProvider = mockk<SessionProvider>(relaxed = true)
    private val organizationApi = mockk<OrganizationApi>(relaxed = true)

    private val apiFactory = mockk<ApiManagerFactory>(relaxed = true)
    private lateinit var apiProvider: ApiProvider
    private lateinit var repository: OrganizationRepositoryImpl

    private val db = mockk<OrganizationDatabase>(relaxed = true)
    private val organizationDao = mockk<OrganizationDao>(relaxed = true)
    // endregion

    // region test data
    private val testSessionId = "test-session-id"
    private val testUserId = "test-user-id"
    // endregion

    private val dispatcherProvider = TestDispatcherProvider()

    @Before
    fun beforeEveryTest() {
        // GIVEN
        every { db.organizationDao() } returns organizationDao
        coEvery { sessionProvider.getSessionId(any()) } returns SessionId(testSessionId)
        apiProvider = ApiProvider(apiFactory, sessionProvider, dispatcherProvider)
        every { apiFactory.create(any(), interfaceClass = OrganizationApi::class) } returns TestApiManager(
            organizationApi
        )

        repository = OrganizationRepositoryImpl(db, apiProvider, TestCoroutineScopeProvider(dispatcherProvider))
    }

    @Test
    fun `get organization returns success`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        val organization = OrganizationResponse(
            email = "test-email",
            name = "test-name",
            theme = "test-theme",
            flags = 1,
            displayName = "test-display-name",
            planName = "test-plan-name",
            vpnPlanName = null,
            twoFactorGracePeriod = null,
            maxDomains = 1,
            maxAddresses = 10,
            maxSpace = 100,
            maxMembers = 2,
            maxVPN = null,
            features = 2,
            usedDomains = 1,
            usedAddresses = 2,
            usedSpace = 2,
            assignedSpace = 5,
            usedMembers = 2,
            usedVPN = 0,
            hasKeys = 0,
            toMigrate = 1,
            maxCalendars = 0,
            usedCalendars = 0
        )
        coEvery { organizationApi.getOrganization() } returns SingleOrganizationResponse(organization)
        every { organizationDao.observeByUserId(any()) } returns flowOf(
            organization.fromResponse(UserId(testUserId)).toEntity()
        )

        // WHEN
        val response = repository.getOrganization(sessionUserId = UserId(testUserId))
        // THEN
        assertNotNull(response)
        assertEquals("test-email", response.email)
        verify { organizationDao.observeByUserId(any()) }
    }
}
