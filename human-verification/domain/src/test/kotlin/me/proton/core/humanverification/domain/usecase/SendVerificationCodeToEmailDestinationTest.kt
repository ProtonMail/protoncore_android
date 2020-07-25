/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.humanverification.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.humanverification.domain.entity.VerificationResult
import me.proton.core.humanverification.domain.exception.EmptyDestinationException
import me.proton.core.humanverification.domain.repository.HumanVerificationRemoteRepository
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

/**
 * Proton Core
 *
 * @author Dino Kadrikj.
 */
@ExperimentalCoroutinesApi
class SendVerificationCodeToEmailDestinationTest {

    private val remoteRepository = mockk<HumanVerificationRemoteRepository>()

    @Rule
    @JvmField
    var thrown: ExpectedException = ExpectedException.none()

    private val testEmail = "test@protonmail.com"

    @Test
    fun `sends verification token to API email`() = runBlockingTest {
        val useCase = SendVerificationCodeToEmailDestination(remoteRepository)
        coEvery { remoteRepository.sendVerificationCodeEmailAddress(any()) } returns VerificationResult.Success

        val result = useCase.invoke(testEmail)
        assertEquals(VerificationResult.Success, result)
    }

    @Test
    fun `sends verification token to API email invalid`() = runBlockingTest {
        val useCase = SendVerificationCodeToEmailDestination(remoteRepository)

        thrown.expect(EmptyDestinationException::class.java)
        thrown.expectMessage("Provide valid email destination.")

        useCase.invoke("")
    }
}
