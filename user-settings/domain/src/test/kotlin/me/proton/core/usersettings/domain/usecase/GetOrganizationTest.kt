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

package me.proton.core.usersettings.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.domain.entity.UserId
import me.proton.core.usersettings.domain.entity.Organization
import me.proton.core.usersettings.domain.repository.OrganizationRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GetOrganizationTest {
    // region mocks
    private val repository = mockk<OrganizationRepository>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testOrganization = Organization(
        userId = testUserId,
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
    // endregion
    private lateinit var useCase: GetOrganization

    @Before
    fun beforeEveryTest() {
        useCase = GetOrganization(repository)
    }

    @Test
    fun `get organization returns success`() = runBlockingTest {
        // GIVEN
        coEvery { repository.getOrganization(testUserId, any()) } returns testOrganization
        // WHEN
        val result = useCase.invoke(testUserId, refresh = true)
        // THEN
        assertEquals(testOrganization, result)
        assertNotNull(result)
        val email = result.email
        assertNotNull(email)
        assertEquals("test-email", email)
    }
}
