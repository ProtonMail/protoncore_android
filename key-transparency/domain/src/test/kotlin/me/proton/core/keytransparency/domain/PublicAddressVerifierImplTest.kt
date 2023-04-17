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

package me.proton.core.keytransparency.domain

import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.keytransparency.domain.entity.VerifiedState
import me.proton.core.keytransparency.domain.usecase.IsKeyTransparencyEnabled
import me.proton.core.keytransparency.domain.usecase.LogKeyTransparency
import me.proton.core.keytransparency.domain.usecase.PublicKeyVerificationResult
import me.proton.core.keytransparency.domain.usecase.VerifyPublicAddress
import org.junit.Test
import kotlin.test.BeforeTest

class PublicAddressVerifierImplTest {

    private lateinit var publicAddressVerifierImpl: PublicAddressVerifierImpl
    private val verifyPublicAddress = mockk<VerifyPublicAddress>()
    private val logKeyTransparency = mockk<LogKeyTransparency>()
    private val isKeyTransparencyEnabled = mockk<IsKeyTransparencyEnabled>()

    @BeforeTest
    fun setUp() {
        publicAddressVerifierImpl = PublicAddressVerifierImpl(
            verifyPublicAddress,
            logKeyTransparency,
            isKeyTransparencyEnabled
        )
    }

    @Test
    fun `when KT is deactivated, do nothing`() = runTest {
        // given
        val userId = UserId("test-user-id")
        val publicAddress = mockk<PublicAddress>()
        coEvery { isKeyTransparencyEnabled(userId) } returns false
        // when
        publicAddressVerifierImpl.verifyPublicAddress(userId, publicAddress)
        // then
        coVerify(exactly = 0) {
            verifyPublicAddress(any(), any())
        }
    }

    @Test
    fun `when KT is activated, verify and log`() = runTest {
        // given
        val userId = UserId("test-user-id")
        val testEmail = "email"
        val publicAddress = mockk<PublicAddress> {
            every { email } returns testEmail
        }
        coEvery { isKeyTransparencyEnabled(userId) } returns true
        val result = PublicKeyVerificationResult.Success(VerifiedState.Existent(0))
        coEvery { verifyPublicAddress(userId, publicAddress) } returns result
        coJustRun { logKeyTransparency.logPublicAddressVerification(result) }
        // when
        publicAddressVerifierImpl.verifyPublicAddress(userId, publicAddress)
        // then
        coVerify(exactly = 1) {
            verifyPublicAddress(userId, publicAddress)
            logKeyTransparency.logPublicAddressVerification(result)
        }
    }
}
