/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.plan.presentation.usecase

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.presentation.entity.DynamicUser
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveUserIdTest : CoroutinesTest by CoroutinesTest() {

    private val userId = UserId("userId")
    private val primaryUserId = UserId("primary")
    private val unknown = UserId("unknown")

    private val accountManager = mockk<AccountManager>(relaxed = true) {
        coEvery { getPrimaryUserId() } returns flowOf(primaryUserId)
        coEvery { getAccount(userId = any()) } answers { flowOf(mockk { every { userId } returns firstArg() }) }
        coEvery { getAccount(unknown) } answers { flowOf(null) }
    }

    private lateinit var tested: ObserveUserId

    @BeforeTest
    fun setUp() {
        tested = ObserveUserId(accountManager)
    }

    @Test
    fun returnEmptyFlowWhenUnspecified() = runTest {
        // When
        tested.invoke().test {
            // Then
            expectNoEvents()
        }
    }

    @Test
    fun returnNullWhenNone() = runTest {
        // Given
        tested.setUser(DynamicUser.None)
        // When
        tested.invoke().test {
            // Then
            assertEquals(expected = null, actual = awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun returnPrimaryUserIdWhenPrimary() = runTest {
        // Given
        tested.setUser(DynamicUser.Primary)
        // When
        tested.invoke().test {
            // Then
            assertEquals(expected = primaryUserId, actual = awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun returnUserIdFlowWhenByUserId() = runTest {
        // Given
        tested.setUser(DynamicUser.ByUserId(userId))
        // When
        tested.invoke().test {
            // Then
            assertEquals(expected = userId, actual = awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun returnNullFlowWhenByUserIdUnknown() = runTest {
        // Given
        tested.setUser(DynamicUser.ByUserId(unknown))
        // When
        tested.invoke().test {
            // Then
            assertEquals(expected = null, actual = awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
