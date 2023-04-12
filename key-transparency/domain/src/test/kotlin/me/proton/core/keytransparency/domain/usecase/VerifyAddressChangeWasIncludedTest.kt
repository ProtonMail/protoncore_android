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
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.keytransparency.domain.Constants
import me.proton.core.keytransparency.domain.entity.AddressChange
import me.proton.core.keytransparency.domain.entity.Epoch
import me.proton.core.keytransparency.domain.entity.ProofPair
import me.proton.core.keytransparency.domain.entity.ProofType
import me.proton.core.keytransparency.domain.entity.VerifiedState
import me.proton.core.keytransparency.domain.exception.KeyTransparencyException
import me.proton.core.keytransparency.domain.exception.UnverifiableSKLException
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.HttpResponseCodes
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertFailsWith

class VerifyAddressChangeWasIncludedTest {

    private lateinit var verifyAddressChangeWasIncluded: VerifyAddressChangeWasIncluded
    private val verifyProofInEpoch = mockk<VerifyProofInEpoch>()
    private val keyTransparencyRepository = mockk<KeyTransparencyRepository>()
    private val getCurrentTime = mockk<GetCurrentTime>()
    private val verifySignedKeyListSignature = mockk<VerifySignedKeyListSignature>()
    private val verifyObsolescenceInclusion = mockk<VerifyObsolescenceInclusion>()
    private val publicAddress = mockk<PublicAddress> {
        every { email } returns testEmail
        every { keys } returns emptyList()
    }
    private val publicAddressRepository: PublicAddressRepository = mockk()
    private val testUserId = UserId("test-user-id")
    private val testEmail = "kt.test@proton.me"
    private val sklData = "skl-data"
    private val sklSignature = "skl-signature"
    private val testEpochId = 100
    private val testCreationTimestamp = 10L
    private val addressChange = mockk<AddressChange> {
        every { creationTimestamp } returns testCreationTimestamp
        every { publicKeys } returns listOf("public key")
        every { epochId } returns testEpochId
        every { email } returns testEmail
        every { isObsolete } returns false
    }
    private val currentTime = 10_000L

    private val noSKLException = ApiException(
        ApiResult.Error.Http(httpCode = HttpResponseCodes.HTTP_UNPROCESSABLE, message = "test error")
    )

    @BeforeTest
    fun setUp() {
        coEvery { getCurrentTime() } returns currentTime
        coEvery { publicAddressRepository.getPublicAddress(testUserId, testEmail, any()) } returns publicAddress
        verifyAddressChangeWasIncluded = VerifyAddressChangeWasIncluded(
            verifyProofInEpoch,
            keyTransparencyRepository,
            publicAddressRepository,
            getCurrentTime,
            verifySignedKeyListSignature,
            verifyObsolescenceInclusion
        )
    }

    @Test
    fun `if no SKL is returned, and the change is very old (epoch has expired), blob is removed`() = runTest {
        // given
        val addressChange = mockk<AddressChange> {
            every { epochId } returns 10
            every { email } returns "email"
            every { creationTimestamp } returns currentTime - Constants.KT_EPOCH_VALIDITY_PERIOD_SECONDS - 1000
        }
        coEvery { publicAddressRepository.getSKLAtEpoch(testUserId, 10, "email") } throws noSKLException
        coJustRun { keyTransparencyRepository.removeAddressChange(addressChange) }
        // when
        verifyAddressChangeWasIncluded(testUserId, addressChange)
        // then
        coVerify {
            publicAddressRepository.getSKLAtEpoch(testUserId, 10, "email")
            keyTransparencyRepository.removeAddressChange(addressChange)
        }
    }

    @Test
    fun `if no SKL is returned, and the change is recent, verification stops`() = runTest {
        // given
        val addressChange = mockk<AddressChange> {
            every { epochId } returns 10
            every { email } returns "email"
            every { creationTimestamp } returns currentTime - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS + 1000
        }
        coEvery { publicAddressRepository.getSKLAtEpoch(testUserId, 10, "email") } throws noSKLException
        // when
        verifyAddressChangeWasIncluded(testUserId, addressChange)
        // then
        coVerify {
            publicAddressRepository.getSKLAtEpoch(testUserId, 10, "email")
        }
    }

    @Test
    fun `if no SKL is returned, and the change is too old, verification fails`() = runTest {
        // given
        val addressChange = mockk<AddressChange> {
            every { epochId } returns 10
            every { email } returns "email"
            every { creationTimestamp } returns currentTime - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS - 10
        }
        coEvery { publicAddressRepository.getSKLAtEpoch(testUserId, 10, "email") } throws noSKLException
        // when
        assertFailsWith<KeyTransparencyException> { verifyAddressChangeWasIncluded(testUserId, addressChange) }
        // then
        coVerify {
            publicAddressRepository.getSKLAtEpoch(testUserId, 10, "email")
        }
    }

    @Test
    fun `if the SKL is older than the one in LS, verification fails`() = runTest {
        // given
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns sklData
            every { signature } returns sklSignature
            every { minEpochId } returns testEpochId
        }
        coEvery {
            publicAddressRepository.getSKLAtEpoch(testUserId, testEpochId, testEmail)
        } returns skl
        val targetSKLTimestamp = testCreationTimestamp - 1
        every {
            verifySignedKeyListSignature(any<PublicKeyRing>(), skl)
        } returns targetSKLTimestamp
        // when
        assertFailsWith<KeyTransparencyException> { verifyAddressChangeWasIncluded(testUserId, addressChange) }
        // then
        coVerify {
            publicAddressRepository.getSKLAtEpoch(testUserId, testEpochId, testEmail)
            verifySignedKeyListSignature(any<PublicKeyRing>(), skl)
        }
    }

    @Test
    fun `if the SKL is too long after the one in LS, verification fails`() = runTest {
        // given
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns sklData
            every { signature } returns sklSignature
            every { minEpochId } returns testEpochId
        }
        coEvery {
            publicAddressRepository.getSKLAtEpoch(testUserId, testEpochId, testEmail)
        } returns skl
        val signatureTimestamp = testCreationTimestamp + Constants.KT_MAX_EPOCH_INTERVAL_SECONDS + 100
        every {
            verifySignedKeyListSignature(any<PublicKeyRing>(), skl)
        } returns signatureTimestamp
        // when
        assertFailsWith<KeyTransparencyException> { verifyAddressChangeWasIncluded(testUserId, addressChange) }
        // then
        coVerify {
            publicAddressRepository.getSKLAtEpoch(testUserId, testEpochId, testEmail)
            verifySignedKeyListSignature(any<PublicKeyRing>(), skl)
        }
    }

    @Test
    fun `if the SKL can't be verified with keys, verification fails with custom exception`() = runTest {
        // given
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns sklData
            every { signature } returns sklSignature
            every { minEpochId } returns testEpochId
        }
        coEvery {
            publicAddressRepository.getSKLAtEpoch(testUserId, testEpochId, testEmail)
        } returns skl
        every {
            verifySignedKeyListSignature(any<PublicKeyRing>(), skl)
        } throws KeyTransparencyException("test error: invalid sig")
        // when
        assertFailsWith<UnverifiableSKLException> { verifyAddressChangeWasIncluded(testUserId, addressChange) }
        // then
        coVerify {
            publicAddressRepository.getSKLAtEpoch(testUserId, testEpochId, testEmail)
            verifySignedKeyListSignature(any<PublicKeyRing>(), skl)
        }
    }

    @Test
    fun `if the SKL is in included has minEpochID greater than the one in LS, verification fails`() = runTest {
        // given
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns sklData
            every { signature } returns sklSignature
            every { minEpochId } returns testEpochId + 1
        }
        coEvery {
            publicAddressRepository.getSKLAtEpoch(testUserId, testEpochId, testEmail)
        } returns skl
        val targetSKLTimestamp = testCreationTimestamp + 1
        every {
            verifySignedKeyListSignature(any<PublicKeyRing>(), skl)
        } returns targetSKLTimestamp
        // when
        assertFailsWith<KeyTransparencyException> { verifyAddressChangeWasIncluded(testUserId, addressChange) }
        coVerify {
            publicAddressRepository.getSKLAtEpoch(testUserId, testEpochId, testEmail)
            verifySignedKeyListSignature(any<PublicKeyRing>(), skl)
        }
    }

    @Test
    fun `if the SKL correct, but proof fails, verification fails`() = runTest {
        // given
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns sklData
            every { signature } returns sklSignature
            every { minEpochId } returns testEpochId
        }
        coEvery {
            publicAddressRepository.getSKLAtEpoch(testUserId, testEpochId, testEmail)
        } returns skl
        val signatureTimestamp = testCreationTimestamp + 1
        every {
            verifySignedKeyListSignature(any<PublicKeyRing>(), skl)
        } returns signatureTimestamp
        val epoch = mockk<Epoch>()
        val proofs = mockk<ProofPair>()
        coEvery { keyTransparencyRepository.getEpoch(testUserId, testEpochId) } returns epoch
        coEvery { keyTransparencyRepository.getProof(testUserId, testEpochId, testEmail) } returns proofs
        coEvery {
            verifyProofInEpoch.invoke(testEmail, skl, epoch, proofs)
        } throws KeyTransparencyException("Something went wrong with proof verification")
        // when
        assertFailsWith<KeyTransparencyException> { verifyAddressChangeWasIncluded(testUserId, addressChange) }
        // then
        coVerify {
            publicAddressRepository.getSKLAtEpoch(testUserId, testEpochId, testEmail)
            verifySignedKeyListSignature(any<PublicKeyRing>(), skl)
            keyTransparencyRepository.getEpoch(testUserId, testEpochId)
            keyTransparencyRepository.getProof(testUserId, testEpochId, testEmail)
            verifyProofInEpoch.invoke(testEmail, skl, epoch, proofs)
        }
    }

    @Test
    fun `if the SKL has a proof of absence, verification fails`() = runTest {
        // given
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns sklData
            every { signature } returns sklSignature
            every { minEpochId } returns testEpochId
        }
        coEvery {
            publicAddressRepository.getSKLAtEpoch(testUserId, testEpochId, testEmail)
        } returns skl
        val signatureTimestamp = testCreationTimestamp + 1
        every {
            verifySignedKeyListSignature(any<PublicKeyRing>(), skl)
        } returns signatureTimestamp
        val epoch = mockk<Epoch>()
        val proofs = mockk<ProofPair>()
        coEvery { keyTransparencyRepository.getEpoch(testUserId, testEpochId) } returns epoch
        coEvery { keyTransparencyRepository.getProof(testUserId, testEpochId, testEmail) } returns proofs
        coEvery { verifyProofInEpoch.invoke(testEmail, skl, epoch, proofs) } returns VerifiedState.Absent(0)
        // when
        assertFailsWith<KeyTransparencyException> { verifyAddressChangeWasIncluded(testUserId, addressChange) }
        // then
        coVerify {
            publicAddressRepository.getSKLAtEpoch(testUserId, testEpochId, testEmail)
            verifySignedKeyListSignature(any<PublicKeyRing>(), skl)
            keyTransparencyRepository.getEpoch(testUserId, testEpochId)
            keyTransparencyRepository.getProof(testUserId, testEpochId, testEmail)
            verifyProofInEpoch.invoke(testEmail, skl, epoch, proofs)
        }
    }

    @Test
    fun `if the SKL is not yet included, verification fails`() = runTest {
        // given
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns sklData
            every { signature } returns sklSignature
            every { minEpochId } returns testEpochId
        }
        coEvery {
            publicAddressRepository.getSKLAtEpoch(testUserId, testEpochId, testEmail)
        } returns skl
        val signatureTimestamp = testCreationTimestamp + 1
        every {
            verifySignedKeyListSignature(any<PublicKeyRing>(), skl)
        } returns signatureTimestamp
        val epoch = mockk<Epoch>()
        val proofs = mockk<ProofPair>()
        coEvery { keyTransparencyRepository.getEpoch(testUserId, testEpochId) } returns epoch
        coEvery { keyTransparencyRepository.getProof(testUserId, testEpochId, testEmail) } returns proofs
        coEvery { verifyProofInEpoch.invoke(testEmail, skl, epoch, proofs) } returns VerifiedState.NotYetIncluded
        // when
        assertFailsWith<KeyTransparencyException> { verifyAddressChangeWasIncluded(testUserId, addressChange) }
        // then
        coVerify {
            publicAddressRepository.getSKLAtEpoch(testUserId, testEpochId, testEmail)
            verifySignedKeyListSignature(any<PublicKeyRing>(), skl)
            keyTransparencyRepository.getEpoch(testUserId, testEpochId)
            keyTransparencyRepository.getProof(testUserId, testEpochId, testEmail)
            verifyProofInEpoch.invoke(testEmail, skl, epoch, proofs)
        }
    }

    @Test
    fun `if the SKL has a correct proof, but epoch is too long after, verification fails`() = runTest {
        // given
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns sklData
            every { signature } returns sklSignature
            every { minEpochId } returns testEpochId
        }
        coEvery {
            publicAddressRepository.getSKLAtEpoch(testUserId, testEpochId, testEmail)
        } returns skl
        val signatureTimestamp = testCreationTimestamp + 1
        every {
            verifySignedKeyListSignature(any<PublicKeyRing>(), skl)
        } returns signatureTimestamp
        val epoch = mockk<Epoch>()
        val proofs = mockk<ProofPair>()
        coEvery { keyTransparencyRepository.getEpoch(testUserId, testEpochId) } returns epoch
        coEvery { keyTransparencyRepository.getProof(testUserId, testEpochId, testEmail) } returns proofs
        val notBefore = testCreationTimestamp + Constants.KT_MAX_EPOCH_INTERVAL_SECONDS + 1
        coEvery {
            verifyProofInEpoch.invoke(testEmail, skl, epoch, proofs)
        } returns VerifiedState.Existent(notBefore)
        // when
        assertFailsWith<KeyTransparencyException> { verifyAddressChangeWasIncluded(testUserId, addressChange) }
        // then
        coVerify {
            publicAddressRepository.getSKLAtEpoch(testUserId, testEpochId, testEmail)
            verifySignedKeyListSignature(any<PublicKeyRing>(), skl)
            keyTransparencyRepository.getEpoch(testUserId, testEpochId)
            keyTransparencyRepository.getProof(testUserId, testEpochId, testEmail)
            verifyProofInEpoch.invoke(testEmail, skl, epoch, proofs)
        }
    }

    @Test
    fun `if the SKL has a existence proof, it removes the LS blob and verification succeeds`() = runTest {
        // given
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns sklData
            every { signature } returns sklSignature
            every { minEpochId } returns testEpochId
        }
        coEvery {
            publicAddressRepository.getSKLAtEpoch(testUserId, testEpochId, testEmail)
        } returns skl
        val signatureTimestamp = testCreationTimestamp + 1
        every {
            verifySignedKeyListSignature(any<PublicKeyRing>(), skl)
        } returns signatureTimestamp
        val epoch = mockk<Epoch>()
        val proofs = mockk<ProofPair>()
        coEvery { keyTransparencyRepository.getEpoch(testUserId, testEpochId) } returns epoch
        coEvery { keyTransparencyRepository.getProof(testUserId, testEpochId, testEmail) } returns proofs
        val notBefore = testCreationTimestamp + 1
        coEvery {
            verifyProofInEpoch.invoke(testEmail, skl, epoch, proofs)
        } returns VerifiedState.Existent(notBefore)
        coJustRun { keyTransparencyRepository.removeAddressChange(addressChange) }
        // when
        verifyAddressChangeWasIncluded(testUserId, addressChange)
        // then
        coVerify {
            publicAddressRepository.getSKLAtEpoch(testUserId, testEpochId, testEmail)
            verifySignedKeyListSignature(any<PublicKeyRing>(), skl)
            keyTransparencyRepository.getEpoch(testUserId, testEpochId)
            keyTransparencyRepository.getProof(testUserId, testEpochId, testEmail)
            verifyProofInEpoch.invoke(testEmail, skl, epoch, proofs)
            keyTransparencyRepository.removeAddressChange(addressChange)
        }
    }

    @Test
    fun `if the change has isObsolete=true, special routine is called for checks`() = runTest {
        // given
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns null
            every { signature } returns null
            every { minEpochId } returns testEpochId
        }
        every { addressChange.isObsolete } returns true
        coEvery {
            publicAddressRepository.getSKLAtEpoch(testUserId, testEpochId, testEmail)
        } returns skl
        val epoch = mockk<Epoch>()
        val proofs = mockk<ProofPair> {
            every { proof.obsolescenceToken } returns "obsolescence-token"
            every { proof.type } returns ProofType.OBSOLESCENCE.getIntEnum()
            every { catchAllProof } returns null
        }
        coEvery { keyTransparencyRepository.getEpoch(testUserId, testEpochId) } returns epoch
        coEvery { keyTransparencyRepository.getProof(testUserId, testEpochId, testEmail) } returns proofs
        val notBefore = testCreationTimestamp + 1
        val verifiedState = VerifiedState.Obsolete(notBefore)
        coEvery {
            verifyProofInEpoch.invoke(testEmail, skl, epoch, proofs)
        } returns verifiedState
        coJustRun { keyTransparencyRepository.removeAddressChange(addressChange) }
        coJustRun { verifyObsolescenceInclusion(proofs, testCreationTimestamp, null) }
        // when
        verifyAddressChangeWasIncluded(testUserId, addressChange)
        // then
        coVerify {
            publicAddressRepository.getSKLAtEpoch(testUserId, testEpochId, testEmail)
            keyTransparencyRepository.getEpoch(testUserId, testEpochId)
            keyTransparencyRepository.getProof(testUserId, testEpochId, testEmail)
            verifyProofInEpoch.invoke(testEmail, skl, epoch, proofs)
            verifyObsolescenceInclusion(proofs, testCreationTimestamp, null)
            keyTransparencyRepository.removeAddressChange(addressChange)
        }
    }
}
