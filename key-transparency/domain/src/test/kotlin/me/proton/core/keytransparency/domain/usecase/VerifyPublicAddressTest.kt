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
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.keytransparency.domain.Constants
import me.proton.core.keytransparency.domain.entity.Epoch
import me.proton.core.keytransparency.domain.entity.ProofPair
import me.proton.core.keytransparency.domain.entity.VerifiedState
import me.proton.core.keytransparency.domain.exception.KeyTransparencyException
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class VerifyPublicAddressTest {
    private lateinit var verifyPublicAddress: VerifyPublicAddress
    private val verifyProofInEpoch = mockk<VerifyProofInEpoch>()
    private val checkAbsenceProof = mockk<CheckAbsenceProof>()
    private val keyTransparencyRepository = mockk<KeyTransparencyRepository>()
    private val checkSignedKeyListMatch = mockk<CheckSignedKeyListMatch>()
    private val storeAddressChange = mockk<StoreAddressChange>()
    private val getCurrentTime = mockk<GetCurrentTime>()
    private val verifySignedKeyListSignature = mockk<VerifySignedKeyListSignature>()

    private val currentTime = 10_000L

    @BeforeTest
    fun setUp() {
        coEvery { getCurrentTime() } returns currentTime
        verifyPublicAddress = VerifyPublicAddress(
            verifyProofInEpoch,
            checkAbsenceProof,
            keyTransparencyRepository,
            checkSignedKeyListMatch,
            verifySignedKeyListSignature,
            storeAddressChange,
            getCurrentTime
        )
    }

    @Test
    fun `if address doesn't have ignoreKT=0, verification is skipped`() = runTest {
        // given
        val userId = UserId("test-user-id")
        val testEmail = "kt.test@proton.me"
        val address = mockk<PublicAddress> {
            every { signedKeyList } returns null
            every { email } returns testEmail
            every { ignoreKT } returns 1
        }
        // when
        val result = verifyPublicAddress(userId, address)
        // then
        assertIs<PublicKeyVerificationResult.Success>(result)
        val state = result.state
        assertIs<VerifiedState.Absent>(state)
        assertEquals(currentTime, state.notBefore)
    }

    @Test
    fun `if address has no SKL, check that it has a valid absence proof`() = runTest {
        // given
        val userId = UserId("test-user-id")
        val testEmail = "kt.test@proton.me"
        val address = mockk<PublicAddress> {
            every { signedKeyList } returns null
            every { email } returns testEmail
            every { ignoreKT } returns 0
        }
        coEvery { checkAbsenceProof(userId, address) } throws KeyTransparencyException("test error")
        // when
        val result = verifyPublicAddress(userId, address)
        // then
        assertIs<PublicKeyVerificationResult.Failure>(result)
        coVerify {
            checkAbsenceProof(userId, address)
        }
    }

    @Test
    fun `if the max epoch id is null, store the SKL in local storage`() = runTest {
        // given
        val userId = UserId("test-user-id")
        val testEmail = "kt.test@proton.me"
        val skl = mockk<PublicSignedKeyList> {
            every { maxEpochId } returns null
            every { data } returns "data"
        }
        val address = mockk<PublicAddress> {
            every { signedKeyList } returns skl
            every { email } returns testEmail
            every { ignoreKT } returns 0
        }
        coEvery { verifySignedKeyListSignature(address, skl) } returns 1000
        coJustRun { storeAddressChange(userId, address, skl) }
        // when
        val result = verifyPublicAddress(userId, address)
        // then
        coVerify {
            storeAddressChange(userId, address, skl)
        }
        assertIs<PublicKeyVerificationResult.Success>(result)
        assertEquals(VerifiedState.NotYetIncluded, result.state)
    }

    @Test
    fun `if the skl signature is invalid, verification fails`() = runTest {
        // given
        val userId = UserId("test-user-id")
        val testEmail = "kt.test@proton.me"
        val skl = mockk<PublicSignedKeyList> {
            every { maxEpochId } returns 10
            every { data } returns "data"
            every { signature } returns "sig"
        }
        val address = mockk<PublicAddress> {
            every { signedKeyList } returns skl
            every { email } returns testEmail
            every { ignoreKT } returns 0
        }
        coEvery {
            verifySignedKeyListSignature(address, skl)
        } throws KeyTransparencyException("test error: invalid skl sig")
        // when
        val result = verifyPublicAddress(userId, address)
        // then
        assertIs<PublicKeyVerificationResult.Failure>(result)
        coVerify {
            verifySignedKeyListSignature(address, skl)
        }
    }

    @Test
    fun `if the skl doesn't match the address, verification fails`() = runTest {
        // given
        val userId = UserId("test-user-id")
        val testEmail = "kt.test@proton.me"
        val skl = mockk<PublicSignedKeyList> {
            every { maxEpochId } returns 10
            every { data } returns "data"
            every { signature } returns "sig"
        }
        val address = mockk<PublicAddress> {
            every { signedKeyList } returns skl
            every { email } returns testEmail
            every { ignoreKT } returns 0
        }
        coEvery {
            verifySignedKeyListSignature(address, skl)
        } returns 1000
        coEvery {
            checkSignedKeyListMatch(address, skl)
        } throws KeyTransparencyException("test error: skl data doesn't match")
        // when
        val result = verifyPublicAddress(userId, address)
        // then
        assertIs<PublicKeyVerificationResult.Failure>(result)
        coVerify {
            checkSignedKeyListMatch(address, skl)
        }
    }

    @Test
    fun `if the skl proof is invalid, verification fails`() = runTest {
        // given
        val userId = UserId("test-user-id")
        val testEmail = "kt.test@proton.me"
        val maxEpochIdVal = 10
        val skl = mockk<PublicSignedKeyList> {
            every { maxEpochId } returns maxEpochIdVal
            every { data } returns "data"
            every { signature } returns "sig"
        }
        val address = mockk<PublicAddress> {
            every { signedKeyList } returns skl
            every { email } returns testEmail
            every { ignoreKT } returns 0
        }
        coJustRun { checkSignedKeyListMatch(address, skl) }
        coEvery { verifySignedKeyListSignature(address, skl) } returns 0 // timestamp
        val epoch = mockk<Epoch> {
            every { epochId } returns maxEpochIdVal
        }
        val proofs = mockk<ProofPair>()
        coEvery { keyTransparencyRepository.getEpoch(userId, maxEpochIdVal) } returns epoch
        coEvery { keyTransparencyRepository.getProof(userId, maxEpochIdVal, testEmail) } returns proofs
        coEvery {
            verifyProofInEpoch(testEmail, skl, epoch, proofs)
        } throws KeyTransparencyException("test error: invalid proof")
        // when
        val result = verifyPublicAddress(userId, address)
        // then
        assertIs<PublicKeyVerificationResult.Failure>(result)
        coVerify {
            checkSignedKeyListMatch(address, skl)
            verifyProofInEpoch(testEmail, skl, epoch, proofs)
        }
    }

    @Test
    fun `if the epoch is too old, verification fails`() = runTest {
        // given
        val userId = UserId("test-user-id")
        val testEmail = "kt.test@proton.me"
        val maxEpochIdVal = 10
        val skl = mockk<PublicSignedKeyList> {
            every { maxEpochId } returns maxEpochIdVal
            every { data } returns "data"
            every { signature } returns "sig"
        }
        val address = mockk<PublicAddress> {
            every { signedKeyList } returns skl
            every { email } returns testEmail
            every { ignoreKT } returns 0
        }
        coJustRun { checkSignedKeyListMatch(address, skl) }
        coEvery { verifySignedKeyListSignature(address, skl) } returns 0 // timestamp
        val epoch = mockk<Epoch> {
            every { epochId } returns maxEpochIdVal
        }
        val proofs = mockk<ProofPair>()
        coEvery { keyTransparencyRepository.getEpoch(userId, maxEpochIdVal) } returns epoch
        coEvery { keyTransparencyRepository.getProof(userId, maxEpochIdVal, testEmail) } returns proofs
        val notBefore = currentTime - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS - 1000
        val addressState = VerifiedState.Existent(notBefore)
        coEvery {
            verifyProofInEpoch(testEmail, skl, epoch, proofs)
        } returns addressState
        // when
        val result = verifyPublicAddress(userId, address)
        // then
        assertIs<PublicKeyVerificationResult.Failure>(result)
        coVerify {
            checkSignedKeyListMatch(address, skl)
            verifyProofInEpoch(testEmail, skl, epoch, proofs)
        }
    }

    @Test
    fun `if the epoch is not too old, verification succeeds`() = runTest {
        // given
        val userId = UserId("test-user-id")
        val testEmail = "kt.test@proton.me"
        val maxEpochIdVal = 10
        val skl = mockk<PublicSignedKeyList> {
            every { maxEpochId } returns maxEpochIdVal
            every { data } returns "data"
            every { signature } returns "sig"
        }
        val address = mockk<PublicAddress> {
            every { signedKeyList } returns skl
            every { email } returns testEmail
            every { ignoreKT } returns 0
        }
        coJustRun { checkSignedKeyListMatch(address, skl) }
        coEvery { verifySignedKeyListSignature(address, skl) } returns 0 // timestamp
        val epoch = mockk<Epoch> {
            every { epochId } returns maxEpochIdVal
        }
        val proofs = mockk<ProofPair>()
        coEvery { keyTransparencyRepository.getEpoch(userId, maxEpochIdVal) } returns epoch
        coEvery { keyTransparencyRepository.getProof(userId, maxEpochIdVal, testEmail) } returns proofs
        val notBefore = currentTime - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS + 1000
        val addressState = VerifiedState.Existent(notBefore)
        coEvery {
            verifyProofInEpoch(testEmail, skl, epoch, proofs)
        } returns addressState
        // when
        val result = verifyPublicAddress(userId, address)
        // then
        coVerify {
            checkSignedKeyListMatch(address, skl)
            verifyProofInEpoch(testEmail, skl, epoch, proofs)
        }
        assertIs<PublicKeyVerificationResult.Success>(result)
        assertEquals(addressState, result.state)
    }
}
