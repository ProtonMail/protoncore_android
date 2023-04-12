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
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.keytransparency.domain.entity.Epoch
import me.proton.core.keytransparency.domain.entity.Proof
import me.proton.core.keytransparency.domain.entity.ProofPair
import me.proton.core.keytransparency.domain.entity.ProofType
import me.proton.core.keytransparency.domain.entity.VerifiedState
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class VerifyProofInEpochTest {

    private lateinit var verifyProofInEpoch: VerifyProofInEpoch

    private val verifyProof = mockk<VerifyProof>()

    private val verifyEpoch = mockk<VerifyEpoch>()

    private val normalizeEmail = NormalizeEmail()

    @Before
    fun setUp() {
        verifyProofInEpoch = VerifyProofInEpoch(verifyProof, verifyEpoch, normalizeEmail)
    }

    @Test
    fun `absent + null = absent`() = runTest {
        // GIVEN
        val email = "kt.test@proton.black"
        val signedKeyList: PublicSignedKeyList? = null
        val treeHashValue = "treeHash"
        val epoch = mockk<Epoch>(relaxed = true) {
            every { treeHash } returns treeHashValue
        }
        val regularProof = mockk<Proof>(relaxed = true) {
            every { type } returns ProofType.ABSENCE.getIntEnum()
        }
        val proofs = ProofPair(regularProof, null)
        coEvery { verifyEpoch(epoch) } returns 0
        justRun {
            verifyProof(
                email = "kttest@proton.black",
                signedKeyList = null,
                proof = regularProof,
                rootHash = treeHashValue
            )
        }
        // WHEN
        val result = verifyProofInEpoch.invoke(email, signedKeyList, epoch, proofs)
        // THEN
        assertTrue(result is VerifiedState.Absent)
        verify {
            verifyProof(
                email = "kttest@proton.black",
                signedKeyList = null,
                proof = regularProof,
                rootHash = treeHashValue
            )
        }
    }

    @Test
    fun `present + null = present`() = runTest {
        // GIVEN
        val email = "kt.test@proton.black"
        val signedKeyList: PublicSignedKeyList = mockk {
            every { data } returns "skl.data"
        }
        val treeHashValue = "treeHash"
        val epoch = mockk<Epoch>(relaxed = true) {
            every { treeHash } returns treeHashValue
        }
        val regularProof = mockk<Proof>(relaxed = true) {
            every { type } returns ProofType.EXISTENCE.getIntEnum()
        }
        val proofs = ProofPair(regularProof, null)
        coEvery { verifyEpoch(epoch) } returns 0
        justRun {
            verifyProof(
                email = "kttest@proton.black",
                signedKeyList = "skl.data",
                proof = regularProof,
                rootHash = treeHashValue
            )
        }
        // WHEN
        val result = verifyProofInEpoch.invoke(email, signedKeyList, epoch, proofs)
        // THEN
        assertTrue(result is VerifiedState.Existent)
        verify {
            verifyProof(
                email = "kttest@proton.black",
                signedKeyList = "skl.data",
                proof = regularProof,
                rootHash = treeHashValue
            )
        }
    }

    @Test
    fun `obsolete + null = obsolete`() = runTest {
        // GIVEN
        val email = "kt.test@proton.black"
        val signedKeyList: PublicSignedKeyList = mockk {
            every { data } returns null
        }
        val treeHashValue = "treeHash"
        val epoch = mockk<Epoch>(relaxed = true) {
            every { treeHash } returns treeHashValue
        }
        val regularProof = mockk<Proof>(relaxed = true) {
            every { obsolescenceToken } returns "deadbeef"
            every { type } returns ProofType.OBSOLESCENCE.getIntEnum()
        }
        val proofs = ProofPair(regularProof, null)
        coEvery { verifyEpoch(epoch) } returns 0
        justRun {
            verifyProof(
                email = "kttest@proton.black",
                signedKeyList = "deadbeef",
                proof = regularProof,
                rootHash = treeHashValue
            )
        }
        // WHEN
        val result = verifyProofInEpoch.invoke(email, signedKeyList, epoch, proofs)
        // THEN
        assertTrue(result is VerifiedState.Obsolete)
        verify {
            verifyProof(
                email = "kttest@proton.black",
                signedKeyList = "deadbeef",
                proof = regularProof,
                rootHash = treeHashValue
            )
        }
    }

    @Test
    fun `obsolescence verification fails with null token`() = runTest {
        // GIVEN
        val email = "kt.test@proton.black"
        val signedKeyList: PublicSignedKeyList? = null
        val treeHashValue = "treeHash"
        val epoch = mockk<Epoch>(relaxed = true) {
            every { treeHash } returns treeHashValue
        }
        val regularProof = mockk<Proof>(relaxed = true) {
            every { type } returns ProofType.OBSOLESCENCE.getIntEnum()
            every { obsolescenceToken } returns null
        }
        val catchAllProof = mockk<Proof>(relaxed = true) {
            every { type } returns ProofType.ABSENCE.getIntEnum()
            every { obsolescenceToken } returns null
        }
        val proofs = ProofPair(regularProof, catchAllProof)
        coEvery { verifyEpoch(epoch) } returns 0
        // WHEN
        assertFails { verifyProofInEpoch.invoke(email, signedKeyList, epoch, proofs) }
    }

    @Test
    fun `obsolescence verification fails with non-hexa token`() = runTest {
        // GIVEN
        val email = "kt.test@proton.black"
        val signedKeyList: PublicSignedKeyList = mockk {
            every { data } returns null
        }
        val treeHashValue = "treeHash"
        val epoch = mockk<Epoch>(relaxed = true) {
            every { treeHash } returns treeHashValue
        }
        val regularProof = mockk<Proof>(relaxed = true) {
            every { obsolescenceToken } returns "NON-HEXA-TOKEN;"
            every { type } returns ProofType.OBSOLESCENCE.getIntEnum()
        }
        val catchAllProof = mockk<Proof>(relaxed = true) {
            every { type } returns ProofType.ABSENCE.getIntEnum()
        }
        val proofs = ProofPair(regularProof, catchAllProof)
        coEvery { verifyEpoch(epoch) } returns 0
        // WHEN
        assertFails { verifyProofInEpoch.invoke(email, signedKeyList, epoch, proofs) }
    }

    @Test
    fun `obsolescence verification fails with null revision`() = runTest {
        // GIVEN
        val email = "kt.test@proton.black"
        val signedKeyList: PublicSignedKeyList = mockk {
            every { data } returns null
        }
        val treeHashValue = "treeHash"
        val epoch = mockk<Epoch>(relaxed = true) {
            every { treeHash } returns treeHashValue
        }
        val regularProof = mockk<Proof>(relaxed = true) {
            every { type } returns ProofType.OBSOLESCENCE.getIntEnum()
            every { revision } returns null
            every { obsolescenceToken } returns "deadbeef"
        }
        val catchAllProof = mockk<Proof>(relaxed = true) {
            every { type } returns ProofType.ABSENCE.getIntEnum()
        }
        val proofs = ProofPair(regularProof, catchAllProof)
        coEvery { verifyEpoch(epoch) } returns 0
        // WHEN
        assertFails { verifyProofInEpoch.invoke(email, signedKeyList, epoch, proofs) }
    }

    @Test
    fun `presence proof fails with null skl data`() = runTest {
        // GIVEN
        val email = "kt.test@proton.black"
        val signedKeyList: PublicSignedKeyList = mockk {
            every { data } returns null
        }
        val treeHashValue = "treeHash"
        val epoch = mockk<Epoch>(relaxed = true) {
            every { treeHash } returns treeHashValue
        }
        val regularProof = mockk<Proof>(relaxed = true) {
            every { type } returns ProofType.EXISTENCE.getIntEnum()
        }
        val proofs = ProofPair(regularProof, null)
        coEvery { verifyEpoch(epoch) } returns 0
        // WHEN
        assertFails { verifyProofInEpoch.invoke(email, signedKeyList, epoch, proofs) }
    }

    @Test
    fun `presence proof fails with null revision`() = runTest {
        // GIVEN
        val email = "kt.test@proton.black"
        val signedKeyList: PublicSignedKeyList = mockk {
            every { data } returns "skl.data"
        }
        val treeHashValue = "treeHash"
        val epoch = mockk<Epoch>(relaxed = true) {
            every { treeHash } returns treeHashValue
        }
        val regularProof = mockk<Proof>(relaxed = true) {
            every { type } returns ProofType.EXISTENCE.getIntEnum()
            every { revision } returns null
        }
        val proofs = ProofPair(regularProof, null)
        coEvery { verifyEpoch(epoch) } returns 0
        // WHEN
        assertFails { verifyProofInEpoch.invoke(email, signedKeyList, epoch, proofs) }
    }
}
