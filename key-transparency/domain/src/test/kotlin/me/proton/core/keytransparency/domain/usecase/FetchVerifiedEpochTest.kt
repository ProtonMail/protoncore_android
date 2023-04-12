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

package me.proton.core.keytransparency.domain.usecase

import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.keytransparency.domain.entity.VerifiedEpoch
import me.proton.core.keytransparency.domain.entity.VerifiedEpochData
import me.proton.core.keytransparency.domain.exception.KeyTransparencyException
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.HttpResponseCodes
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class FetchVerifiedEpochTest {
    private lateinit var fetchVerifiedEpoch: FetchVerifiedEpoch
    private val keyTransparencyRepository = mockk<KeyTransparencyRepository>()
    private val checkVerifiedEpochSignature = mockk<CheckVerifiedEpochSignature>()

    private val userId = UserId("test-user-id")
    private val testAddressId = AddressId("test-address-id")
    private val userAddress = mockk<UserAddress> {
        every { email } returns "email"
        every { addressId } returns testAddressId
    }

    private val noVEException = ApiException(
        ApiResult.Error.Http(httpCode = HttpResponseCodes.HTTP_UNPROCESSABLE, message = "test error")
    )

    @BeforeTest
    fun setUp() {
        fetchVerifiedEpoch = FetchVerifiedEpoch(
            keyTransparencyRepository,
            checkVerifiedEpochSignature
        )
    }

    @Test
    fun `if the signature fails, returns null`() = runTest {
        // given
        val verifiedEpoch = mockk<VerifiedEpoch>()
        coEvery { keyTransparencyRepository.getVerifiedEpoch(userId, testAddressId) } returns verifiedEpoch
        coEvery {
            checkVerifiedEpochSignature(userId, verifiedEpoch)
        } throws KeyTransparencyException("Test - verification failed")
        // when
        val actual = fetchVerifiedEpoch(userId, userAddress)
        // then
        assertNull(actual)
        coVerify {
            keyTransparencyRepository.getVerifiedEpoch(userId, testAddressId)
            checkVerifiedEpochSignature(userId, verifiedEpoch)
        }
    }

    @Test
    fun `if the epoch can be parsed, returns the parsed data`() = runTest {
        // given
        val verifiedEpochData = VerifiedEpochData(
            10,
            2
        )
        val verifiedEpoch = mockk<VerifiedEpoch> {
            every { data } returns "data"
            every { signature } returns "signature"
            every { getVerifiedEpoch() } returns verifiedEpochData
        }
        coEvery { keyTransparencyRepository.getVerifiedEpoch(userId, testAddressId) } returns verifiedEpoch
        coJustRun {
            checkVerifiedEpochSignature(userId, verifiedEpoch)
        }
        // when
        val actual = fetchVerifiedEpoch(userId, userAddress)
        // then
        assertEquals(verifiedEpochData, actual)
        coVerify {
            keyTransparencyRepository.getVerifiedEpoch(userId, testAddressId)
            checkVerifiedEpochSignature(userId, verifiedEpoch)
        }
    }

    @Test
    fun `if the epoch doesn't exist, returns null`() = runTest {
        // given
        coEvery { keyTransparencyRepository.getVerifiedEpoch(userId, testAddressId) } throws noVEException
        // when
        val actual = fetchVerifiedEpoch(userId, userAddress)
        // then
        assertNull(actual)
        coVerify {
            keyTransparencyRepository.getVerifiedEpoch(userId, testAddressId)
        }
    }

    @Test
    fun `if fetching fails with an exception, it is not caught`() = runTest {
        // given
        coEvery {
            keyTransparencyRepository.getVerifiedEpoch(userId, testAddressId)
        } throws Exception("Test - Unknown error")
        // when
        assertFailsWith<Exception> { fetchVerifiedEpoch(userId, userAddress) }
        // then
        coVerify {
            keyTransparencyRepository.getVerifiedEpoch(userId, testAddressId)
        }
    }
}
