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
import io.mockk.mockk
import me.proton.core.keytransparency.domain.Constants
import me.proton.core.keytransparency.domain.entity.ProofPair
import me.proton.core.keytransparency.domain.entity.ProofType
import me.proton.core.keytransparency.domain.exception.KeyTransparencyException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class VerifyObsolescenceInclusionTest {

    private lateinit var verifyObsolescenceInclusion: VerifyObsolescenceInclusion
    private val getObsolescenceTokenTimestamp: GetObsolescenceTokenTimestamp = mockk()

    @BeforeTest
    fun setUp() {
        verifyObsolescenceInclusion = VerifyObsolescenceInclusion(
            getObsolescenceTokenTimestamp
        )
    }

    @Test
    fun `If targetSKL exists, and is older than creation time stamp, check fails`() {
        // given
        val creationTimestamp = 1000L
        val proofs = mockk<ProofPair> {
            every { proof.type } returns ProofType.EXISTENCE.getIntEnum()
        }
        val targetSKLTimestamp = 500L
        // when
        assertFailsWith<KeyTransparencyException> {
            verifyObsolescenceInclusion(
                proofs,
                creationTimestamp,
                targetSKLTimestamp
            )
        }
    }

    @Test
    fun `If targetSKL exists, and is too long after creation time stamp, check fails`() {
        // given
        val creationTimestamp = 1000L
        val proofs = mockk<ProofPair> {
            every { proof.type } returns ProofType.EXISTENCE.getIntEnum()
        }
        val targetSKLTimestamp = creationTimestamp + Constants.KT_MAX_EPOCH_INTERVAL_SECONDS + 10
        // when
        assertFailsWith<KeyTransparencyException> {
            verifyObsolescenceInclusion(
                proofs,
                creationTimestamp,
                targetSKLTimestamp
            )
        }
    }

    @Test
    fun `If targetSKL exists, and is fresher than creation time stamp, check succeeds`() {
        // given
        val creationTimestamp = 1000L
        val proofs = mockk<ProofPair> {
            every { proof.type } returns ProofType.EXISTENCE.getIntEnum()
        }
        val targetSKLTimestamp = creationTimestamp + 10
        // when
        verifyObsolescenceInclusion(
            proofs,
            creationTimestamp,
            targetSKLTimestamp
        )
    }

    @Test
    fun `If targetSKL is obsolete, and is older than creation time stamp with epoch margin, check fails`() {
        // given
        val creationTimestamp = 1000L
        val proofs = mockk<ProofPair> {
            every { proof.type } returns ProofType.OBSOLESCENCE.getIntEnum()
            every { proof.obsolescenceToken } returns "obsolescenceToken"
        }
        val tokenTimestamp = creationTimestamp - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS - 10
        coEvery { getObsolescenceTokenTimestamp("obsolescenceToken") } returns tokenTimestamp
        // when
        assertFailsWith<KeyTransparencyException> {
            verifyObsolescenceInclusion(
                proofs,
                creationTimestamp,
                null
            )
        }
    }

    @Test
    fun `If targetSKL is obsolete, and is fresher than creation time stamp with epoch margin, check fails`() {
        // given
        val creationTimestamp = 1000L
        val proofs = mockk<ProofPair> {
            every { proof.type } returns ProofType.OBSOLESCENCE.getIntEnum()
            every { proof.obsolescenceToken } returns "obsolescenceToken"
        }
        val tokenTimestamp = creationTimestamp + Constants.KT_MAX_EPOCH_INTERVAL_SECONDS + 10
        coEvery { getObsolescenceTokenTimestamp("obsolescenceToken") } returns tokenTimestamp
        // when
        assertFailsWith<KeyTransparencyException> {
            verifyObsolescenceInclusion(
                proofs,
                creationTimestamp,
                null
            )
        }
    }

    @Test
    fun `If targetSKL is obsolete, and is within max epoch margin, check succeeds`() {
        // given
        val creationTimestamp = 1000L
        val proofs = mockk<ProofPair> {
            every { proof.type } returns ProofType.OBSOLESCENCE.getIntEnum()
            every { proof.obsolescenceToken } returns "obsolescenceToken"
        }
        val tokenTimestamp = creationTimestamp + Constants.KT_MAX_EPOCH_INTERVAL_SECONDS - 10
        coEvery { getObsolescenceTokenTimestamp("obsolescenceToken") } returns tokenTimestamp
        // when
        verifyObsolescenceInclusion(
            proofs,
            creationTimestamp,
            null
        )
    }
}
