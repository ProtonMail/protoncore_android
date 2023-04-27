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
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.keytransparency.domain.Constants
import me.proton.core.keytransparency.domain.entity.Epoch
import me.proton.core.keytransparency.domain.entity.ProofPair
import me.proton.core.keytransparency.domain.entity.VerifiedState
import me.proton.core.keytransparency.domain.exception.KeyTransparencyException
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import me.proton.core.user.domain.entity.UserAddress
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BootstrapInitialEpochTest {

    private lateinit var bootstrapInitialEpoch: BootstrapInitialEpoch
    private val verifySignedKeyListSignature = mockk<VerifySignedKeyListSignature>()
    private val getCurrentTime = mockk<GetCurrentTime>()
    private val keyTransparencyRepository = mockk<KeyTransparencyRepository>()
    private val verifyProofInEpoch = mockk<VerifyProofInEpoch>()

    private val userId = UserId("test-user-id")

    private val currentTime = 10_000L

    @Before
    fun setUp() {
        coEvery { getCurrentTime() } returns currentTime
        bootstrapInitialEpoch = BootstrapInitialEpoch(
            verifySignedKeyListSignature,
            getCurrentTime,
            keyTransparencyRepository,
            verifyProofInEpoch
        )
    }

    @Test
    fun `New SKLS can't be empty for bootstrapping`() = runTest {
        // given
        val userAddress = mockk<UserAddress>()
        val inputSKL = mockk<PublicSignedKeyList>()
        // when
        assertFailsWith<KeyTransparencyException> { bootstrapInitialEpoch(userId, userAddress, inputSKL, emptyList()) }
        // then
    }

    @Test
    fun `If min epoch id is null, skl must be the same as input`() = runTest {
        // given
        val userAddress = mockk<UserAddress>()
        val inputSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { signature } returns "signature"
        }
        val newSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data1"
            every { signature } returns "signature"
            every { minEpochId } returns null
        }
        // when
        assertFailsWith<KeyTransparencyException> {
            bootstrapInitialEpoch(userId, userAddress, inputSKL, listOf(newSKL))
        }
    }

    @Test
    fun `If min epoch id is null, verify the signature`() = runTest {
        // given
        val userAddress = mockk<UserAddress>()
        val inputSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { signature } returns "signature"
        }
        val newSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { signature } returns "signature"
            every { minEpochId } returns null
        }
        coEvery {
            verifySignedKeyListSignature(userAddress, inputSKL)
        } throws KeyTransparencyException("Test - sig is not verified")
        // when
        assertFailsWith<KeyTransparencyException> {
            bootstrapInitialEpoch(userId, userAddress, inputSKL, listOf(newSKL))
        }
        // then
        coVerify {
            verifySignedKeyListSignature(userAddress, inputSKL)
        }
    }

    @Test
    fun `If min epoch id is null, check it's not too old`() = runTest {
        // given
        val userAddress = mockk<UserAddress>()
        val inputSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { signature } returns "signature"
        }
        val newSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { signature } returns "signature"
            every { minEpochId } returns null
        }
        val signatureTimestamp = currentTime - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS - 1000
        coEvery { verifySignedKeyListSignature(userAddress, inputSKL) } returns signatureTimestamp
        // when
        assertFailsWith<KeyTransparencyException> {
            bootstrapInitialEpoch(userId, userAddress, inputSKL, listOf(newSKL))
        }
        // then
        coVerify {
            verifySignedKeyListSignature(userAddress, inputSKL)
        }
    }

    @Test
    fun `If min epoch id is null, it's too early`() = runTest {
        // given
        val userAddress = mockk<UserAddress>()
        val inputSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { signature } returns "signature"
        }
        val newSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { signature } returns "signature"
            every { minEpochId } returns null
        }
        val signatureTimestamp = currentTime - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS + 1000
        coEvery { verifySignedKeyListSignature(userAddress, inputSKL) } returns signatureTimestamp
        // when
        val bootstrapped = bootstrapInitialEpoch(userId, userAddress, inputSKL, listOf(newSKL))
        // then
        assertNull(bootstrapped)
        coVerify {
            verifySignedKeyListSignature(userAddress, inputSKL)
        }
    }

    @Test
    fun `If min epoch id is not null, verify proof`() = runTest {
        // given
        val userAddress = mockk<UserAddress> {
            every { email } returns "email"
        }
        val inputSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { signature } returns "signature"
        }
        val newSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { signature } returns "signature"
            every { minEpochId } returns 100
        }
        val epoch = mockk<Epoch>()
        val proof = mockk<ProofPair>()
        coEvery { keyTransparencyRepository.getEpoch(userId, 100) } returns epoch
        coEvery { keyTransparencyRepository.getProof(userId, 100, "email") } returns proof
        coEvery {
            verifyProofInEpoch("email", newSKL, epoch, proof)
        } throws KeyTransparencyException("Test - proof is invalid")
        // when
        assertFailsWith<KeyTransparencyException> {
            bootstrapInitialEpoch(userId, userAddress, inputSKL, listOf(newSKL))
        }
        // then
        coVerify {
            verifyProofInEpoch("email", newSKL, epoch, proof)
        }
    }

    @Test
    fun `Oldest epoch must be in range`() = runTest {
        // given
        val userAddress = mockk<UserAddress> {
            every { email } returns "email"
        }
        val inputSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { signature } returns "signature"
        }
        val newSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { signature } returns "signature"
            every { minEpochId } returns 100
        }
        val epoch = mockk<Epoch>()
        val proof = mockk<ProofPair> { every { proof.revision } returns 1 }
        coEvery { keyTransparencyRepository.getEpoch(userId, 100) } returns epoch
        coEvery { keyTransparencyRepository.getProof(userId, 100, "email") } returns proof
        val notBefore =
            currentTime - (Constants.KT_EPOCH_VALIDITY_PERIOD_SECONDS + Constants.KT_MAX_EPOCH_INTERVAL_SECONDS + 1)
        coEvery { verifyProofInEpoch("email", newSKL, epoch, proof) } returns VerifiedState.Existent(notBefore)
        // when
        assertFailsWith<KeyTransparencyException> {
            bootstrapInitialEpoch(userId, userAddress, inputSKL, listOf(newSKL))
        }
        // then
        coVerify {
            verifyProofInEpoch("email", newSKL, epoch, proof)
        }
    }

    @Test
    fun `Oldest epoch can be new if the address is at revision 0`() = runTest {
        // given
        val userAddress = mockk<UserAddress> {
            every { email } returns "email"
        }
        val inputSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { signature } returns "signature"
        }
        val newSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { signature } returns "signature"
            every { minEpochId } returns 100
        }
        val epoch = mockk<Epoch>()
        val proof = mockk<ProofPair> { every { proof.revision } returns 0 }
        coEvery { keyTransparencyRepository.getEpoch(userId, 100) } returns epoch
        coEvery { keyTransparencyRepository.getProof(userId, 100, "email") } returns proof
        val notBefore = currentTime - 1
        coEvery { verifyProofInEpoch("email", newSKL, epoch, proof) } returns VerifiedState.Existent(notBefore)
        // when
        val bootstrappedEpoch = bootstrapInitialEpoch(userId, userAddress, inputSKL, listOf(newSKL))
        // then
        coVerify {
            verifyProofInEpoch("email", newSKL, epoch, proof)
        }
        assertNotNull(bootstrappedEpoch)
        assertEquals(100, bootstrappedEpoch.epochId)
        assertEquals(0, bootstrappedEpoch.revision)
        assertEquals(0, bootstrappedEpoch.sklCreationTime)
    }

    @Test
    fun `Bootstrapped epoch is returned`() = runTest {
        // given
        val userAddress = mockk<UserAddress> {
            every { email } returns "email"
        }
        val inputSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { signature } returns "signature"
        }
        val newSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { signature } returns "signature"
            every { minEpochId } returns 100
        }
        val epoch = mockk<Epoch>()
        val proof = mockk<ProofPair> { every { proof.revision } returns 1 }
        coEvery { keyTransparencyRepository.getEpoch(userId, 100) } returns epoch
        coEvery { keyTransparencyRepository.getProof(userId, 100, "email") } returns proof
        val notBefore = currentTime - Constants.KT_EPOCH_VALIDITY_PERIOD_SECONDS - 10
        coEvery { verifyProofInEpoch("email", newSKL, epoch, proof) } returns VerifiedState.Existent(notBefore)
        // when
        val bootstrappedEpoch = bootstrapInitialEpoch(userId, userAddress, inputSKL, listOf(newSKL))
        // then
        coVerify {
            verifyProofInEpoch("email", newSKL, epoch, proof)
        }
        assertNotNull(bootstrappedEpoch)
        assertEquals(100, bootstrappedEpoch.epochId)
        assertEquals(1, bootstrappedEpoch.revision)
        assertEquals(0, bootstrappedEpoch.sklCreationTime)
    }
}
