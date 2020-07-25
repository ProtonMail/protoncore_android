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
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.repository.HumanVerificationRemoteRepository
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Dino Kadrikj.
 */
@ExperimentalCoroutinesApi
class VerifyCodeTest {

    private val remoteRepository = mockk<HumanVerificationRemoteRepository>()

    @Test
    fun `code verification success`() = runBlockingTest {
        val useCase = VerifyCode(remoteRepository)
        coEvery { remoteRepository.verifyCode(any(), any()) } returns VerificationResult.Success
        val result = useCase.invoke(TokenType.EMAIL.tokenTypeValue, "testCode")

        assertEquals(VerificationResult.Success, result)
    }

    @Test
    fun `code verification error`() = runBlockingTest {
        val useCase = VerifyCode(remoteRepository)
        coEvery { remoteRepository.verifyCode(any(), any()) } returns VerificationResult.Error("test error")
        val result = useCase.invoke(TokenType.EMAIL.tokenTypeValue, "testCode")

        assertTrue(result is VerificationResult.Error)
    }
}
