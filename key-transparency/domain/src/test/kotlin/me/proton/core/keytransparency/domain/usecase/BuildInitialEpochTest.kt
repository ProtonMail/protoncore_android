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

import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.keytransparency.domain.entity.VerifiedEpochData
import me.proton.core.keytransparency.domain.exception.KeyTransparencyException
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import me.proton.core.user.domain.entity.UserAddress
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BuildInitialEpochTest {

    private lateinit var buildInitialEpoch: BuildInitialEpoch
    private val keyTransparencyRepository = mockk<KeyTransparencyRepository>()
    private val bootstrapInitialEpoch = mockk<BootstrapInitialEpoch>()
    private val userId = UserId("test-user-id")

    @BeforeTest
    fun setUp() {
        buildInitialEpoch = BuildInitialEpoch(
            keyTransparencyRepository,
            bootstrapInitialEpoch
        )
    }

    @Test
    fun `If the verified epoch is null, we bootstrap the initial epoch`() = runTest {
        // given
        val verifiedEpoch: VerifiedEpochData? = null
        val newSKLs = emptyList<PublicSignedKeyList>()
        val userAddress = mockk<UserAddress>()
        val inputSKL = mockk<PublicSignedKeyList>()
        val bootstrappedEpoch = mockk<VerifiedEpochData>()
        coEvery { bootstrapInitialEpoch(userId, userAddress, inputSKL, newSKLs) } returns bootstrappedEpoch
        // when
        val initialEpoch = buildInitialEpoch(verifiedEpoch, newSKLs, userId, userAddress, inputSKL)
        // then
        assertEquals(bootstrappedEpoch, initialEpoch)
        coVerify {
            bootstrapInitialEpoch(userId, userAddress, inputSKL, newSKLs)
        }
    }

    @Test
    fun `If the verified epoch is not null & the new skls is empty, we use the verified epoch`() = runTest {
        // given
        val verifiedEpoch = mockk<VerifiedEpochData>()
        val newSKLs = emptyList<PublicSignedKeyList>()
        val userAddress = mockk<UserAddress>()
        val inputSKL = mockk<PublicSignedKeyList>()
        // when
        val initialEpoch = buildInitialEpoch(verifiedEpoch, newSKLs, userId, userAddress, inputSKL)
        // then
        assertEquals(verifiedEpoch, initialEpoch)
        coVerify {
            bootstrapInitialEpoch wasNot called
        }
    }

    @Test
    fun `If the verified epoch is not null & the 1st is not in KT, we use the verified epoch`() = runTest {
        // given
        val verifiedEpoch = mockk<VerifiedEpochData>()
        val newSKLs = listOf<PublicSignedKeyList>(
            mockk {
                every { minEpochId } returns null
            }
        )
        val userAddress = mockk<UserAddress>()
        val inputSKL = mockk<PublicSignedKeyList>()
        // when
        val initialEpoch = buildInitialEpoch(verifiedEpoch, newSKLs, userId, userAddress, inputSKL)
        // then
        assertEquals(verifiedEpoch, initialEpoch)
        coVerify {
            bootstrapInitialEpoch wasNot called
        }
    }

    @Test
    fun `If the verified epoch is not null & the firstSKL has no gap , we use the verified epoch`() = runTest {
        // given
        val verifiedEpoch = mockk<VerifiedEpochData> {
            every { revision } returns 10
        }
        val newSKLs = listOf<PublicSignedKeyList>(
            mockk {
                every { minEpochId } returns 100
            }
        )
        coEvery { keyTransparencyRepository.getProof(userId, 100, "email") } returns mockk {
            every { proof.revision } returns 11
        }
        val userAddress = mockk<UserAddress> {
            every { email } returns "email"
        }
        val inputSKL = mockk<PublicSignedKeyList>()
        // when
        val initialEpoch = buildInitialEpoch(verifiedEpoch, newSKLs, userId, userAddress, inputSKL)
        // then
        assertEquals(verifiedEpoch, initialEpoch)
        coVerify {
            keyTransparencyRepository.getProof(userId, 100, "email")
            bootstrapInitialEpoch wasNot called
        }
    }

    @Test
    fun `If the verified epoch is not null & the first revision == VE , we use the verified epoch`() = runTest {
        // given
        val verifiedEpoch = mockk<VerifiedEpochData> {
            every { revision } returns 10
        }
        val newSKLs = listOf<PublicSignedKeyList>(
            mockk {
                every { minEpochId } returns 100
            }
        )
        coEvery { keyTransparencyRepository.getProof(userId, 100, "email") } returns mockk {
            every { proof.revision } returns 10
        }
        val userAddress = mockk<UserAddress> {
            every { email } returns "email"
        }
        val inputSKL = mockk<PublicSignedKeyList>()
        // when
        val initialEpoch = buildInitialEpoch(verifiedEpoch, newSKLs, userId, userAddress, inputSKL)
        // then
        assertEquals(verifiedEpoch, initialEpoch)
        coVerify {
            keyTransparencyRepository.getProof(userId, 100, "email")
            bootstrapInitialEpoch wasNot called
        }
    }

    @Test
    fun `If the verified epoch is not null & the firstSKL has a gap in the revision, we bootstrap`() = runTest {
        // given
        val verifiedEpoch = mockk<VerifiedEpochData> {
            every { revision } returns 10
        }
        val newSKLs = listOf<PublicSignedKeyList>(
            mockk {
                every { minEpochId } returns 100
            }
        )
        coEvery { keyTransparencyRepository.getProof(userId, 100, "email") } returns mockk {
            every { proof.revision } returns 15
        }
        val userAddress = mockk<UserAddress> {
            every { email } returns "email"
        }
        val inputSKL = mockk<PublicSignedKeyList>()
        val bootstrappedEpoch = mockk<VerifiedEpochData> {}
        coEvery { bootstrapInitialEpoch(userId, userAddress, inputSKL, newSKLs) } returns bootstrappedEpoch
        // when
        val initialEpoch = buildInitialEpoch(verifiedEpoch, newSKLs, userId, userAddress, inputSKL)
        // then
        assertEquals(bootstrappedEpoch, initialEpoch)
        coVerify {
            keyTransparencyRepository.getProof(userId, 100, "email")
            bootstrapInitialEpoch(userId, userAddress, inputSKL, newSKLs)
        }
    }

    @Test
    fun `If the verified epoch is not null & the firstSKL has a revision lower than VE it fails`() = runTest {
        // given
        val verifiedEpoch = mockk<VerifiedEpochData> {
            every { revision } returns 10
        }
        val newSKLs = listOf<PublicSignedKeyList>(
            mockk {
                every { minEpochId } returns 100
            }
        )
        coEvery { keyTransparencyRepository.getProof(userId, 100, "email") } returns mockk {
            every { proof.revision } returns 4
        }
        val userAddress = mockk<UserAddress> {
            every { email } returns "email"
        }
        val inputSKL = mockk<PublicSignedKeyList>()
        val bootstrappedEpoch = mockk<VerifiedEpochData> {}
        coEvery { bootstrapInitialEpoch(userId, userAddress, inputSKL, newSKLs) } returns bootstrappedEpoch
        // when
        assertFailsWith<KeyTransparencyException> {
            buildInitialEpoch(verifiedEpoch, newSKLs, userId, userAddress, inputSKL)
        }
    }
}
