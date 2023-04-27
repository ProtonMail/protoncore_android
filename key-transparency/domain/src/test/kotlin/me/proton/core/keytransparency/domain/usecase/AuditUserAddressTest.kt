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
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.keytransparency.domain.Constants
import me.proton.core.keytransparency.domain.entity.Epoch
import me.proton.core.keytransparency.domain.entity.ProofPair
import me.proton.core.keytransparency.domain.entity.UserAddressAuditResult
import me.proton.core.keytransparency.domain.entity.VerifiedEpochData
import me.proton.core.keytransparency.domain.entity.VerifiedState
import me.proton.core.keytransparency.domain.exception.KeyTransparencyException
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AuditUserAddressTest {
    private lateinit var auditUserAddress: AuditUserAddress
    private val checkAbsenceProof = mockk<CheckAbsenceProof>()
    private val checkSignedKeyListMatch = mockk<CheckSignedKeyListMatch>()
    private val verifySignedKeyListSignature = mockk<VerifySignedKeyListSignature>()
    private val keyTransparencyRepository = mockk<KeyTransparencyRepository>()
    private val verifyProofInEpoch = mockk<VerifyProofInEpoch>()
    private val uploadVerifiedEpoch = mockk<UploadVerifiedEpoch>()
    private val buildInitialEpoch = mockk<BuildInitialEpoch>()
    private val fetchVerifiedEpoch = mockk<FetchVerifiedEpoch>()
    private val getCurrentTime = mockk<GetCurrentTime>()
    private val testUserId = UserId("test-user-id")
    private val testAddressId = AddressId("test-address-id")
    private val publicAddressRepository = mockk<PublicAddressRepository>()

    private val testEmail = "kt.test@proton.me"
    private val userAddress = mockk<UserAddress> {
        every { addressId } returns testAddressId
        every { email } returns testEmail
        every { enabled } returns true
    }

    private val currentTime = 10_000L

    @BeforeTest
    fun setUp() {
        coEvery { getCurrentTime() } returns currentTime
        auditUserAddress = AuditUserAddress(
            checkAbsenceProof,
            checkSignedKeyListMatch,
            keyTransparencyRepository,
            verifyProofInEpoch,
            verifySignedKeyListSignature,
            uploadVerifiedEpoch,
            buildInitialEpoch,
            fetchVerifiedEpoch,
            getCurrentTime,
            publicAddressRepository
        )
    }

    @Test
    fun `Self audit skips disabled addresses`() = runTest {
        // given
        every { userAddress.enabled } returns false
        // when
        val result = auditUserAddress(testUserId, userAddress)
        // then
        assertTrue(result is UserAddressAuditResult.Warning.Disabled)
    }

    @Test
    fun `If the address has no SKL, and no absence proof, self audit fails`() = runTest {
        // given
        every { userAddress.signedKeyList } returns null
        coEvery { checkAbsenceProof(testUserId, userAddress) } throws KeyTransparencyException("Test failure")
        // when
        assertFailsWith<KeyTransparencyException> { auditUserAddress(testUserId, userAddress).unwrap() }
        // then
        coVerify {
            checkAbsenceProof(testUserId, userAddress)
        }
    }

    @Test
    fun `If the address has no SKL and a valid absent proof, self audit returns an absent warning`() = runTest {
        // given
        every { userAddress.signedKeyList } returns null
        coEvery { checkAbsenceProof(testUserId, userAddress) } returns VerifiedState.Absent(0)
        // when
        val result = auditUserAddress(testUserId, userAddress).unwrap()
        // then
        assertTrue(result is UserAddressAuditResult.Warning.AddressNotInKT)
        coVerify {
            checkAbsenceProof(testUserId, userAddress)
        }
    }

    @Test
    fun `If the initial epoch can't be built,  self audit returns a warning`() = runTest {
        // given
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { maxEpochId } returns 100
        }
        every { userAddress.signedKeyList } returns skl
        coEvery { fetchVerifiedEpoch(testUserId, userAddress) } returns null
        coEvery { publicAddressRepository.getSKLsAfterEpoch(testUserId, 0, testEmail) } returns emptyList()
        coEvery { buildInitialEpoch(null, emptyList(), testUserId, userAddress, skl) } returns null
        // when
        val result = auditUserAddress(testUserId, userAddress).unwrap()
        // then
        assertTrue(result is UserAddressAuditResult.Warning.CreationTooRecent)
        coVerify {
            buildInitialEpoch(null, emptyList(), testUserId, userAddress, skl)
        }
    }

    @Test
    fun `No changes - max epoch id can't be null`() = runTest {
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { maxEpochId } returns null
        }
        every { userAddress.signedKeyList } returns skl
        val verifiedEpoch = mockk<VerifiedEpochData> {
            every { epochId } returns 100
        }
        coEvery { fetchVerifiedEpoch(testUserId, userAddress) } returns verifiedEpoch
        coEvery { publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail) } returns emptyList()
        coEvery { buildInitialEpoch(verifiedEpoch, emptyList(), testUserId, userAddress, skl) } returns verifiedEpoch
        // when
        assertFailsWith<KeyTransparencyException> { auditUserAddress(testUserId, userAddress).unwrap() }
        // then
        coVerify {
            fetchVerifiedEpoch(testUserId, userAddress)
            publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail)
        }
    }

    @Test
    fun `No changes - proof for maxEpochID must verify`() = runTest {
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { maxEpochId } returns 101
        }
        every { userAddress.signedKeyList } returns skl
        val verifiedEpoch = mockk<VerifiedEpochData> {
            every { revision } returns 1
            every { epochId } returns 100
        }
        coEvery { fetchVerifiedEpoch(testUserId, userAddress) } returns verifiedEpoch
        coEvery { publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail) } returns emptyList()
        coEvery { buildInitialEpoch(verifiedEpoch, emptyList(), testUserId, userAddress, skl) } returns verifiedEpoch
        val proof = mockk<ProofPair>()
        val epoch = mockk<Epoch>()
        coEvery { keyTransparencyRepository.getProof(testUserId, 101, testEmail) } returns proof
        coEvery { keyTransparencyRepository.getEpoch(testUserId, 101) } returns epoch
        coEvery {
            verifyProofInEpoch(testEmail, skl, epoch, proof)
        } throws KeyTransparencyException("Test - proof failed")
        // when
        assertFailsWith<KeyTransparencyException> { auditUserAddress(testUserId, userAddress).unwrap() }
        // then
        coVerify {
            fetchVerifiedEpoch(testUserId, userAddress)
            publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail)
            verifyProofInEpoch(testEmail, skl, epoch, proof)
        }
    }

    @Test
    fun `No changes - max epoch must not be too old`() = runTest {
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { maxEpochId } returns 101
        }
        every { userAddress.signedKeyList } returns skl
        val verifiedEpoch = mockk<VerifiedEpochData> {
            every { revision } returns 1
            every { epochId } returns 100
        }
        coEvery { fetchVerifiedEpoch(testUserId, userAddress) } returns verifiedEpoch
        coEvery { publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail) } returns emptyList()
        coEvery { buildInitialEpoch(verifiedEpoch, emptyList(), testUserId, userAddress, skl) } returns verifiedEpoch
        val proof = mockk<ProofPair> { every { proof.revision } returns 1 }
        val epoch = mockk<Epoch>()
        coEvery { keyTransparencyRepository.getProof(testUserId, 101, testEmail) } returns proof
        coEvery { keyTransparencyRepository.getEpoch(testUserId, 101) } returns epoch
        val currentTime = 10_000L
        coEvery { getCurrentTime() } returns currentTime
        val certificateTime = currentTime - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS - 10
        val verifiedState = VerifiedState.Existent(certificateTime)
        coEvery { verifyProofInEpoch(testEmail, skl, epoch, proof) } returns verifiedState
        // when
        assertFailsWith<KeyTransparencyException> { auditUserAddress(testUserId, userAddress).unwrap() }
        // then
        coVerify {
            fetchVerifiedEpoch(testUserId, userAddress)
            publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail)
            verifyProofInEpoch(testEmail, skl, epoch, proof)
        }
    }

    @Test
    fun `No changes - revision must be the same as VE`() = runTest {
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { maxEpochId } returns 101
        }
        every { userAddress.signedKeyList } returns skl
        val verifiedEpoch = mockk<VerifiedEpochData> {
            every { revision } returns 1
            every { epochId } returns 100
        }
        coEvery { fetchVerifiedEpoch(testUserId, userAddress) } returns verifiedEpoch
        coEvery { publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail) } returns emptyList()
        coEvery { buildInitialEpoch(verifiedEpoch, emptyList(), testUserId, userAddress, skl) } returns verifiedEpoch
        val proof = mockk<ProofPair> { every { proof.revision } returns 2 }
        val epoch = mockk<Epoch>()
        coEvery { keyTransparencyRepository.getProof(testUserId, 101, testEmail) } returns proof
        coEvery { keyTransparencyRepository.getEpoch(testUserId, 101) } returns epoch
        val currentTime = 10_000L
        coEvery { getCurrentTime() } returns currentTime
        val certificateTime = currentTime - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS + 1000
        val verifiedState = VerifiedState.Existent(certificateTime)
        coEvery { verifyProofInEpoch(testEmail, skl, epoch, proof) } returns verifiedState
        // when
        assertFailsWith<KeyTransparencyException> { auditUserAddress(testUserId, userAddress).unwrap() }
        // then
        coVerify {
            fetchVerifiedEpoch(testUserId, userAddress)
            publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail)
            verifyProofInEpoch(testEmail, skl, epoch, proof)
        }
    }

    @Test
    fun `No changes - if maxEpochID has changed, upload new VE`() = runTest {
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { maxEpochId } returns 101
        }
        every { userAddress.signedKeyList } returns skl
        val lastVerifiedTimestamp = 1000L
        val verifiedEpoch = mockk<VerifiedEpochData> {
            every { revision } returns 1
            every { epochId } returns 100
            every { sklCreationTime } returns lastVerifiedTimestamp
        }
        coEvery { fetchVerifiedEpoch(testUserId, userAddress) } returns verifiedEpoch
        coEvery { publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail) } returns emptyList()
        coEvery { buildInitialEpoch(verifiedEpoch, emptyList(), testUserId, userAddress, skl) } returns verifiedEpoch
        val proof = mockk<ProofPair> { every { proof.revision } returns 1 }
        val epoch = mockk<Epoch>()
        coEvery { keyTransparencyRepository.getProof(testUserId, 101, testEmail) } returns proof
        coEvery { keyTransparencyRepository.getEpoch(testUserId, 101) } returns epoch
        val currentTime = 10_000L
        coEvery { getCurrentTime() } returns currentTime
        val certificateTime = currentTime - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS + 1000
        val verifiedState = VerifiedState.Existent(certificateTime)
        coEvery { verifyProofInEpoch(testEmail, skl, epoch, proof) } returns verifiedState
        val expectedVE = VerifiedEpochData(101, 1, lastVerifiedTimestamp)
        coJustRun { uploadVerifiedEpoch(testUserId, testAddressId, expectedVE) }
        // when
        auditUserAddress(testUserId, userAddress).unwrap()
        // then
        coVerify {
            fetchVerifiedEpoch(testUserId, userAddress)
            publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail)
            verifyProofInEpoch(testEmail, skl, epoch, proof)
            uploadVerifiedEpoch(testUserId, testAddressId, expectedVE)
        }
    }

    @Test
    fun `No changes - if maxEpochID has not changed, self audit succeeds`() = runTest {
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { maxEpochId } returns 100
        }
        every { userAddress.signedKeyList } returns skl
        val lastVerifiedTimestamp = 1000L
        val verifiedEpoch = mockk<VerifiedEpochData> {
            every { revision } returns 1
            every { epochId } returns 100
            every { sklCreationTime } returns lastVerifiedTimestamp
        }
        coEvery { fetchVerifiedEpoch(testUserId, userAddress) } returns verifiedEpoch
        coEvery { publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail) } returns emptyList()
        coEvery { buildInitialEpoch(verifiedEpoch, emptyList(), testUserId, userAddress, skl) } returns verifiedEpoch
        val proof = mockk<ProofPair> { every { proof.revision } returns 1 }
        val epoch = mockk<Epoch>()
        coEvery { keyTransparencyRepository.getProof(testUserId, 100, testEmail) } returns proof
        coEvery { keyTransparencyRepository.getEpoch(testUserId, 100) } returns epoch
        val currentTime = 10_000L
        coEvery { getCurrentTime() } returns currentTime
        val certificateTime = currentTime - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS + 1000
        val verifiedState = VerifiedState.Existent(certificateTime)
        coEvery { verifyProofInEpoch(testEmail, skl, epoch, proof) } returns verifiedState
        // when
        auditUserAddress(testUserId, userAddress).unwrap()
        // then
        coVerify {
            fetchVerifiedEpoch(testUserId, userAddress)
            publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail)
            uploadVerifiedEpoch wasNot called
        }
    }

    private fun UserAddressAuditResult.unwrap(): UserAddressAuditResult {
        if (this is UserAddressAuditResult.Failure) {
            throw reason
        }
        return this
    }

    @Test
    fun `New changes - Only last SKL can have MaxEpochID = null`() = runTest {
        // given
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { maxEpochId } returns 100
        }
        every { userAddress.signedKeyList } returns skl
        val lastVerifiedTimestamp = 1000L
        val verifiedEpoch = mockk<VerifiedEpochData> {
            every { revision } returns 1
            every { epochId } returns 100
            every { sklCreationTime } returns lastVerifiedTimestamp
        }
        coEvery { fetchVerifiedEpoch(testUserId, userAddress) } returns verifiedEpoch
        val newSKLs = listOf<PublicSignedKeyList>(
            mockk { every { maxEpochId } returns null },
            mockk()
        )
        coEvery { publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail) } returns newSKLs // when
        coEvery { buildInitialEpoch(verifiedEpoch, any(), testUserId, userAddress, skl) } returns verifiedEpoch
        assertFailsWith<KeyTransparencyException> { auditUserAddress(testUserId, userAddress).unwrap() }
        coVerify {
            fetchVerifiedEpoch(testUserId, userAddress)
            publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail)
        }
    }

    @Test
    fun `New changes - If the SKL signature is invalid, audit fails`() = runTest {
        // given
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns "data"
        }
        every { userAddress.signedKeyList } returns skl
        val lastVerifiedTimestamp = 1000L
        val verifiedEpoch = mockk<VerifiedEpochData> {
            every { revision } returns 1
            every { epochId } returns 100
            every { sklCreationTime } returns lastVerifiedTimestamp
        }
        coEvery { fetchVerifiedEpoch(testUserId, userAddress) } returns verifiedEpoch
        val newSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { maxEpochId } returns 101
        }
        coEvery { publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail) } returns listOf(newSKL)
        coEvery { buildInitialEpoch(verifiedEpoch, any(), testUserId, userAddress, skl) } returns verifiedEpoch
        coEvery {
            verifySignedKeyListSignature(userAddress, newSKL)
        } throws KeyTransparencyException("Test signature failed")
        // when
        assertFailsWith<KeyTransparencyException> { auditUserAddress(testUserId, userAddress).unwrap() }
        coVerify {
            fetchVerifiedEpoch(testUserId, userAddress)
            publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail)
            verifySignedKeyListSignature(userAddress, newSKL)
        }
    }

    @Test
    fun `New changes - If the SKL has an invalid proof, audit fails`() = runTest {
        // given
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns "data"
        }
        every { userAddress.signedKeyList } returns skl
        val lastVerifiedTimestamp = 1000L
        val verifiedEpoch = mockk<VerifiedEpochData> {
            every { revision } returns 1
            every { epochId } returns 100
            every { sklCreationTime } returns lastVerifiedTimestamp
        }
        coEvery { fetchVerifiedEpoch(testUserId, userAddress) } returns verifiedEpoch
        val newSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { maxEpochId } returns 110
        }
        coEvery { publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail) } returns listOf(newSKL)
        coEvery { buildInitialEpoch(verifiedEpoch, any(), testUserId, userAddress, skl) } returns verifiedEpoch
        coEvery { verifySignedKeyListSignature(userAddress, newSKL) } returns 10_000
        val epoch = mockk<Epoch>()
        coEvery { keyTransparencyRepository.getEpoch(testUserId, 110) } returns epoch
        val proof = mockk<ProofPair>()
        coEvery { keyTransparencyRepository.getProof(testUserId, 110, testEmail) } returns proof
        coEvery {
            verifyProofInEpoch(testEmail, newSKL, epoch, proof)
        } throws KeyTransparencyException("Test proof failed")
        // when
        assertFailsWith<KeyTransparencyException> { auditUserAddress(testUserId, userAddress).unwrap() }
        // then
        coVerify {
            fetchVerifiedEpoch(testUserId, userAddress)
            publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail)
            verifySignedKeyListSignature(userAddress, newSKL)
            keyTransparencyRepository.getEpoch(testUserId, 110)
            keyTransparencyRepository.getProof(testUserId, 110, testEmail)
            verifyProofInEpoch(testEmail, newSKL, epoch, proof)
        }
    }

    @Test
    fun `New changes - If the chain is inconsistent, audit fails`() = runTest {
        // given
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns "data"
        }
        every { userAddress.signedKeyList } returns skl
        val lastVerifiedTimestamp = 1000L
        val verifiedEpoch = mockk<VerifiedEpochData> {
            every { revision } returns 1
            every { epochId } returns 100
            every { sklCreationTime } returns lastVerifiedTimestamp
        }
        coEvery { fetchVerifiedEpoch(testUserId, userAddress) } returns verifiedEpoch
        val newSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { maxEpochId } returns 110
        }
        coEvery { publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail) } returns listOf(newSKL)
        coEvery { buildInitialEpoch(verifiedEpoch, any(), testUserId, userAddress, skl) } returns verifiedEpoch
        coEvery { verifySignedKeyListSignature(userAddress, newSKL) } returns 10_000
        val epoch = mockk<Epoch>()
        coEvery { keyTransparencyRepository.getEpoch(testUserId, 110) } returns epoch
        val proof = mockk<ProofPair> {
            every { proof.revision } returns 3 // missing revision 2
        }
        coEvery { keyTransparencyRepository.getProof(testUserId, 110, testEmail) } returns proof
        coEvery {
            verifyProofInEpoch(testEmail, newSKL, epoch, proof)
        } returns VerifiedState.Existent(1000)
        // when
        assertFailsWith<KeyTransparencyException> { auditUserAddress(testUserId, userAddress).unwrap() }
        // then
        coVerify {
            fetchVerifiedEpoch(testUserId, userAddress)
            publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail)
            verifySignedKeyListSignature(userAddress, newSKL)
            keyTransparencyRepository.getEpoch(testUserId, 110)
            keyTransparencyRepository.getProof(testUserId, 110, testEmail)
            verifyProofInEpoch(testEmail, newSKL, epoch, proof)
        }
    }

    @Test
    fun `New changes - If the last SKL doesn't have the same data as input, audit fails`() = runTest {
        // given
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns "data"
        }
        every { userAddress.signedKeyList } returns skl
        val lastVerifiedTimestamp = 1000L
        val verifiedEpoch = mockk<VerifiedEpochData> {
            every { revision } returns 1
            every { epochId } returns 100
            every { sklCreationTime } returns lastVerifiedTimestamp
        }
        coEvery { fetchVerifiedEpoch(testUserId, userAddress) } returns verifiedEpoch
        val newSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data1"
            every { maxEpochId } returns 110
        }
        coEvery { publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail) } returns listOf(newSKL)
        coEvery { buildInitialEpoch(verifiedEpoch, any(), testUserId, userAddress, skl) } returns verifiedEpoch
        coEvery { verifySignedKeyListSignature(userAddress, newSKL) } returns 10_000
        val epoch = mockk<Epoch>()
        coEvery { keyTransparencyRepository.getEpoch(testUserId, 110) } returns epoch
        val proof = mockk<ProofPair> {
            every { proof.revision } returns 2
        }
        coEvery { keyTransparencyRepository.getProof(testUserId, 110, testEmail) } returns proof
        val currentTime = 10_000L
        coEvery { getCurrentTime() } returns currentTime
        val certificateTime = currentTime - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS + 1000
        val verifiedState = VerifiedState.Existent(certificateTime)
        coEvery {
            verifyProofInEpoch(testEmail, newSKL, epoch, proof)
        } returns verifiedState
        // when
        assertFailsWith<KeyTransparencyException> { auditUserAddress(testUserId, userAddress).unwrap() }
        // then
        coVerify {
            fetchVerifiedEpoch(testUserId, userAddress)
            publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail)
            verifySignedKeyListSignature(userAddress, newSKL)
            keyTransparencyRepository.getEpoch(testUserId, 110)
            keyTransparencyRepository.getProof(testUserId, 110, testEmail)
            verifyProofInEpoch(testEmail, newSKL, epoch, proof)
        }
    }

    @Test
    fun `New changes - If the last SKL doesn't match the keys, audit fails`() = runTest {
        // given
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns "data"
        }
        every { userAddress.signedKeyList } returns skl
        val lastVerifiedTimestamp = 1000L
        val verifiedEpoch = mockk<VerifiedEpochData> {
            every { revision } returns 1
            every { epochId } returns 100
            every { sklCreationTime } returns lastVerifiedTimestamp
        }
        coEvery { fetchVerifiedEpoch(testUserId, userAddress) } returns verifiedEpoch
        val newSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { maxEpochId } returns 110
        }
        coEvery { publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail) } returns listOf(newSKL)
        coEvery { buildInitialEpoch(verifiedEpoch, any(), testUserId, userAddress, skl) } returns verifiedEpoch
        coEvery { verifySignedKeyListSignature(userAddress, newSKL) } returns 10_000
        val epoch = mockk<Epoch>()
        coEvery { keyTransparencyRepository.getEpoch(testUserId, 110) } returns epoch
        val proof = mockk<ProofPair> {
            every { proof.revision } returns 2
        }
        coEvery { keyTransparencyRepository.getProof(testUserId, 110, testEmail) } returns proof
        val currentTime = 10_000L
        coEvery { getCurrentTime() } returns currentTime
        val certificateTime = currentTime - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS + 1000
        val verifiedState = VerifiedState.Existent(certificateTime)
        coEvery {
            verifyProofInEpoch(testEmail, newSKL, epoch, proof)
        } returns verifiedState
        coEvery {
            checkSignedKeyListMatch(userAddress, skl)
        } throws KeyTransparencyException("Test - keys don't match skl")
        // when
        assertFailsWith<KeyTransparencyException> { auditUserAddress(testUserId, userAddress).unwrap() }
        // then
        coVerify {
            fetchVerifiedEpoch(testUserId, userAddress)
            publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail)
            verifySignedKeyListSignature(userAddress, newSKL)
            keyTransparencyRepository.getEpoch(testUserId, 110)
            keyTransparencyRepository.getProof(testUserId, 110, testEmail)
            verifyProofInEpoch(testEmail, newSKL, epoch, proof)
            checkSignedKeyListMatch(userAddress, skl)
        }
    }

    @Test
    fun `New changes - If the new verified epoch has an old certificate, audit fails`() = runTest {
        // given
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns "data"
        }
        every { userAddress.signedKeyList } returns skl
        val lastVerifiedTimestamp = 1000L
        val verifiedEpoch = mockk<VerifiedEpochData> {
            every { revision } returns 1
            every { epochId } returns 100
            every { sklCreationTime } returns lastVerifiedTimestamp
        }
        coEvery { fetchVerifiedEpoch(testUserId, userAddress) } returns verifiedEpoch
        val newSKL = mockk<PublicSignedKeyList> {
            every { maxEpochId } returns 110
            every { data } returns "data"
        }
        coEvery { publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail) } returns listOf(newSKL)
        coEvery { buildInitialEpoch(verifiedEpoch, any(), testUserId, userAddress, skl) } returns verifiedEpoch
        coEvery { verifySignedKeyListSignature(userAddress, newSKL) } returns 10_000
        val epoch = mockk<Epoch>()
        coEvery { keyTransparencyRepository.getEpoch(testUserId, 110) } returns epoch
        val proof = mockk<ProofPair> {
            every { proof.revision } returns 2
        }
        coEvery { keyTransparencyRepository.getProof(testUserId, 110, testEmail) } returns proof
        val certificateTime = currentTime - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS - 1000
        val verifiedState = VerifiedState.Existent(certificateTime)
        coEvery {
            verifyProofInEpoch(testEmail, newSKL, epoch, proof)
        } returns verifiedState
        coJustRun { checkSignedKeyListMatch(userAddress, skl) }
        // when
        assertFailsWith<KeyTransparencyException> { auditUserAddress(testUserId, userAddress).unwrap() }
        // then
        coVerify {
            fetchVerifiedEpoch(testUserId, userAddress)
            publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail)
            verifySignedKeyListSignature(userAddress, newSKL)
            keyTransparencyRepository.getEpoch(testUserId, 110)
            keyTransparencyRepository.getProof(testUserId, 110, testEmail)
            verifyProofInEpoch(testEmail, newSKL, epoch, proof)
            checkSignedKeyListMatch(userAddress, skl)
        }
    }

    @Test
    fun `New changes - If the creation time is decreasing, audit fails`() = runTest {
        // given
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns "data"
        }
        every { userAddress.signedKeyList } returns skl
        val lastVerifiedTimestamp = 1000L
        val verifiedEpoch = mockk<VerifiedEpochData> {
            every { revision } returns 1
            every { epochId } returns 100
            every { sklCreationTime } returns lastVerifiedTimestamp
        }
        coEvery { fetchVerifiedEpoch(testUserId, userAddress) } returns verifiedEpoch
        val newSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { maxEpochId } returns 110
        }
        coEvery { publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail) } returns listOf(newSKL)
        coEvery { buildInitialEpoch(verifiedEpoch, any(), testUserId, userAddress, skl) } returns verifiedEpoch
        val newSKLTimestamp = lastVerifiedTimestamp - 100 // SKL creation time is decreasing
        coEvery { verifySignedKeyListSignature(userAddress, newSKL) } returns newSKLTimestamp
        val epoch = mockk<Epoch>()
        coEvery { keyTransparencyRepository.getEpoch(testUserId, 110) } returns epoch
        val proof = mockk<ProofPair> {
            every { proof.revision } returns 2
        }
        coEvery { keyTransparencyRepository.getProof(testUserId, 110, testEmail) } returns proof
        val currentTime = 10_000L
        coEvery { getCurrentTime() } returns currentTime
        val certificateTime = currentTime - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS + 1000
        val verifiedState = VerifiedState.Existent(certificateTime)
        coEvery {
            verifyProofInEpoch(testEmail, newSKL, epoch, proof)
        } returns verifiedState
        coJustRun { checkSignedKeyListMatch(userAddress, skl) }
        val expectedNewVerifiedEpoch = mockk<VerifiedEpochData> {
            every { revision } returns 2
            every { epochId } returns 110
            every { sklCreationTime } returns newSKLTimestamp
        }
        coJustRun { uploadVerifiedEpoch(testUserId, testAddressId, expectedNewVerifiedEpoch) }
        // when
        assertFailsWith<KeyTransparencyException> {
            auditUserAddress(testUserId, userAddress).unwrap()
        }
        // then
        coVerify {
            fetchVerifiedEpoch(testUserId, userAddress)
            publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail)
            verifySignedKeyListSignature(userAddress, newSKL)
        }
    }

    @Test
    fun `New changes - If a new skl is included, the verified epoch is updated`() = runTest {
        // given
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns "data"
        }
        every { userAddress.signedKeyList } returns skl
        val lastVerifiedTimestamp = 1000L
        val verifiedEpoch = mockk<VerifiedEpochData> {
            every { revision } returns 1
            every { epochId } returns 100
            every { sklCreationTime } returns lastVerifiedTimestamp
        }
        coEvery { fetchVerifiedEpoch(testUserId, userAddress) } returns verifiedEpoch
        val newSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { maxEpochId } returns 110
        }
        coEvery { publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail) } returns listOf(newSKL)
        coEvery { buildInitialEpoch(verifiedEpoch, any(), testUserId, userAddress, skl) } returns verifiedEpoch
        val newSKLTimestamp = lastVerifiedTimestamp + 10
        coEvery { verifySignedKeyListSignature(userAddress, newSKL) } returns newSKLTimestamp
        val epoch = mockk<Epoch>()
        coEvery { keyTransparencyRepository.getEpoch(testUserId, 110) } returns epoch
        val proof = mockk<ProofPair> {
            every { proof.revision } returns 2
        }
        coEvery { keyTransparencyRepository.getProof(testUserId, 110, testEmail) } returns proof
        val currentTime = 10_000L
        coEvery { getCurrentTime() } returns currentTime
        val certificateTime = currentTime - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS + 1000
        val verifiedState = VerifiedState.Existent(certificateTime)
        coEvery {
            verifyProofInEpoch(testEmail, newSKL, epoch, proof)
        } returns verifiedState
        coJustRun { checkSignedKeyListMatch(userAddress, skl) }
        val expectedNewVerifiedEpoch = mockk<VerifiedEpochData> {
            every { revision } returns 2
            every { epochId } returns 110
            every { sklCreationTime } returns newSKLTimestamp
        }
        coJustRun { uploadVerifiedEpoch(testUserId, testAddressId, expectedNewVerifiedEpoch) }
        // when
        auditUserAddress(testUserId, userAddress).unwrap()
        // then
        coVerify {
            fetchVerifiedEpoch(testUserId, userAddress)
            publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail)
            verifySignedKeyListSignature(userAddress, newSKL)
            keyTransparencyRepository.getEpoch(testUserId, 110)
            keyTransparencyRepository.getProof(testUserId, 110, testEmail)
            verifyProofInEpoch(testEmail, newSKL, epoch, proof)
            checkSignedKeyListMatch(userAddress, skl)
            uploadVerifiedEpoch(testUserId, testAddressId, expectedNewVerifiedEpoch)
        }
    }

    @Test
    fun `New changes - If no skl was included, the verified epoch is not updated`() = runTest {
        // given
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns "data"
        }
        every { userAddress.signedKeyList } returns skl
        val signatureTimestamp = currentTime - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS + 1000
        val verifiedEpoch = mockk<VerifiedEpochData> {
            every { revision } returns 1
            every { epochId } returns 100
            every { sklCreationTime } returns signatureTimestamp - 1000
        }
        coEvery { fetchVerifiedEpoch(testUserId, userAddress) } returns verifiedEpoch
        val newSKL = mockk<PublicSignedKeyList> {
            every { data } returns "data"
            every { maxEpochId } returns null
        }
        coEvery { publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail) } returns listOf(newSKL)
        coEvery { buildInitialEpoch(verifiedEpoch, any(), testUserId, userAddress, skl) } returns verifiedEpoch
        val currentTime = 10_000L
        coEvery { getCurrentTime() } returns currentTime
        coEvery { verifySignedKeyListSignature(userAddress, newSKL) } returns signatureTimestamp
        coJustRun { checkSignedKeyListMatch(userAddress, skl) }
        // when
        auditUserAddress(testUserId, userAddress).unwrap()
        // then
        coVerify {
            fetchVerifiedEpoch(testUserId, userAddress)
            publicAddressRepository.getSKLsAfterEpoch(testUserId, 100, testEmail)
            verifySignedKeyListSignature(userAddress, newSKL)
            checkSignedKeyListMatch(userAddress, skl)
            uploadVerifiedEpoch wasNot called
        }
    }
}
