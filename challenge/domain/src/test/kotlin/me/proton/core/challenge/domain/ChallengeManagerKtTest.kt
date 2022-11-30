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

package me.proton.core.challenge.domain

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class ChallengeManagerKtTest {

    private val challengeManager = mockk<ChallengeManager>()

    private val flowName = "test-flow"
    private val testChallengeFrame = ChallengeFrameDetails(
        flow = flowName,
        challengeFrame = "test-challenge-frame",
        focusTime = listOf(0),
        clicks = 1,
        copy = emptyList(),
        paste = emptyList(),
        keys = emptyList()
    )

    @Test
    public fun `test use flow result is correct`(): Unit = runTest {
        val blockLambda: (List<ChallengeFrameDetails>) -> String = {
            "test-result"
        }

        coEvery { challengeManager.getFramesByFlowName(flowName) } returns listOf(testChallengeFrame)
        coEvery { challengeManager.resetFlow(flowName) } answers { }

        val result = challengeManager.useFlow(
            flowName,
            blockLambda
        )

        assertEquals("test-result", result)
    }

    @Test
    public fun `test use flow order is correct`(): Unit = runTest {
        val blockLambda = mockk<(List<ChallengeFrameDetails>) -> String>()

        every { blockLambda.invoke(any()) } returns "test-string"
        coEvery { challengeManager.getFramesByFlowName(flowName) } returns listOf(testChallengeFrame)
        coEvery { challengeManager.resetFlow(flowName) } answers { }

        challengeManager.useFlow(flowName, blockLambda)

        coVerify {
            challengeManager.getFramesByFlowName(flowName)
        }

        coVerify {
            challengeManager.resetFlow(flowName)
        }

        verify { blockLambda.invoke(listOf(testChallengeFrame)) }
    }
}
