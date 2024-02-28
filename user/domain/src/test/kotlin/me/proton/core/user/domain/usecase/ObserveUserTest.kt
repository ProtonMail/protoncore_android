/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.user.domain.usecase

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.repository.UserRepository
import org.junit.Before
import org.junit.Test

class ObserveUserTest {

    private val userId = UserId("userId")

    private val userRepository = mockk<UserRepository>(relaxed = true)

    private lateinit var useCase: ObserveUser

    @Before
    fun setup() {
        useCase = ObserveUser(userRepository)
    }

    @Test
    fun observeUserCallUserRepository() = runTest {
        // When
        useCase.invoke(userId)
        // Then
        coVerify { userRepository.observeUser(userId, refresh = false) }
    }
}
