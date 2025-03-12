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

package me.proton.core.plan.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.plan.domain.entity.dynamicSubscription
import me.proton.core.plan.domain.repository.PlansRepository
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class GetDynamicSubscriptionTest {
    // region mocks
    @MockK(relaxed = true)
    private lateinit var repository: PlansRepository

    @MockK
    private lateinit var userManager: UserManager
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testSubscription = dynamicSubscription
    private val testSubscriptions = listOf(testSubscription)
    // endregion

    private lateinit var useCase: GetDynamicSubscription

    @Before
    fun beforeEveryTest() {
        MockKAnnotations.init(this)
        useCase = GetDynamicSubscription(repository, userManager)
    }

    @Test
    fun `get subscription returns success, enqueue getsubscription observability success`() = runTest {
        // GIVEN
        coEvery { repository.getDynamicSubscriptions(testUserId) } returns testSubscriptions
        coEvery { userManager.getUser(testUserId) } returns mockk { every { role } returns Role.NoOrganization }
        // WHEN
        val result = useCase.invoke(testUserId)
        // THEN
        assertNotNull(result)
        assertEquals(testSubscription, result)
        assertEquals(0L, result?.amount)
    }

    @Test
    fun `get subscription returns error`() = runTest {
        // GIVEN
        coEvery { repository.getDynamicSubscriptions(testUserId) } throws ApiException(
            ApiResult.Error.Connection(
                false,
                RuntimeException("Test error")
            )
        )
        coEvery { userManager.getUser(testUserId) } returns mockk { every { role } returns Role.OrganizationAdmin }
        // WHEN
        val result = useCase.invoke(testUserId)
        // THEN
        assertNull(result)
    }

    @Test
    fun `get dynamic subscription returns no active subscription`() = runTest {
        // GIVEN
        coEvery { repository.getDynamicSubscriptions(testUserId) } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123,
                "http error",
                ApiResult.Error.ProtonData(
                    code = ResponseCodes.PAYMENTS_SUBSCRIPTION_NOT_EXISTS,
                    error = "no active subscription"
                )
            )
        )
        coEvery { userManager.getUser(testUserId) } returns mockk { every { role } returns Role.NoOrganization }
        // WHEN
        val result = useCase.invoke(testUserId)
        assertNull(result)
    }

    @Test
    fun `no subscription if user is member of organization`() = runTest {
        // GIVEN
        coEvery { userManager.getUser(testUserId) } returns mockk { every { role } returns Role.OrganizationMember }

        // WHEN
        val result = useCase.invoke(testUserId)
        assertNull(result)
    }

    @Test
    fun `exception is rethrown for runtime exception`() = runTest {
        // GIVEN
        coEvery { userManager.getUser(testUserId) } throws CancellationException("Error")

        // WHEN
        assertFailsWith<CancellationException> { useCase.invoke(testUserId) }
    }
}
