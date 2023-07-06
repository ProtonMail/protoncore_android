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

package me.proton.core.auth.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.SignupUsernameAvailabilityTotal
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus
import me.proton.core.user.domain.repository.DomainRepository
import me.proton.core.user.domain.repository.UserRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

internal class AccountAvailabilityTest {

    private lateinit var tested: AccountAvailability

    @MockK
    private lateinit var domainRepository: DomainRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = AccountAvailability(userRepository, domainRepository)
    }

    @Test
    fun `checkUsername observability success`() = runTest {
        // GIVEN
        val userId = UserId("123")
        val username = "test-user"
        coEvery { userRepository.getUser(any()) } returns mockk {
            every { name } returns null
        }
        coEvery { userRepository.checkUsernameAvailable(any(), any()) } just runs

        // WHEN
        tested.checkUsernameAuthenticated(
            userId = userId,
            username = username
        )

        // THEN
        coVerify { userRepository.checkUsernameAvailable(userId, username) }
    }

    @Test
    fun `checkUsername failure`() = runTest {
        // GIVEN
        val userId = UserId("123")
        val username = "test-user"
        coEvery { userRepository.getUser(any()) } returns mockk {
            every { name } returns null
        }
        coEvery { userRepository.checkUsernameAvailable(any(), any()) } throws ApiException(
            ApiResult.Error.Connection()
        )

        // WHEN
        assertFailsWith<ApiException> {
            tested.checkUsernameAuthenticated(
                userId = userId,
                username = username,
            )
        }

        // THEN
        coVerify { userRepository.checkUsernameAvailable(userId, username) }
    }
}
