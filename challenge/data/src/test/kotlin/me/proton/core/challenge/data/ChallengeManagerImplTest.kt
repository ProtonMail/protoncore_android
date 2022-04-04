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

package me.proton.core.challenge.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.challenge.domain.ChallengeManager
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.challenge.domain.repository.ChallengeRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

public class ChallengeManagerImplTest {

    private val repository = mockk<ChallengeRepository>()

    private lateinit var manager: ChallengeManager

    @Before
    public fun beforeEveryTest() {
        manager = ChallengeManagerImpl(repository)
    }

    @Test
    public fun `reset flow calls repository delete`(): Unit = runBlockingTest {
        // Given
        val testFlow = "test-flow"
        coEvery { repository.deleteFrames(testFlow) } returns Unit
        // When
        manager.resetFlow(testFlow)
        // Then
        coVerify { repository.deleteFrames(testFlow) }
    }

    @Test
    public fun `get frames by flow returns correctly`(): Unit = runBlockingTest {
        // Given
        val testFlow = "test-flow"
        val testChallengeFrame = ChallengeFrameDetails(
            flow = testFlow,
            challengeFrame = "test-challenge-frame",
            focusTime = 0,
            clicks = 1,
            copy = emptyList(),
            paste = emptyList(),
            keys = emptyList()
        )
        coEvery { repository.getFramesByFlow(testFlow) } returns listOf(
            testChallengeFrame,
            testChallengeFrame.copy(challengeFrame = "test-challenge-frame2")
        )
        // When
        val result = manager.getFramesByFlowName(testFlow)
        // Then
        assertEquals(2, result.size)
        coVerify { repository.getFramesByFlow(testFlow) }
    }

    @Test
    public fun `get frames by flow and frame name returns correctly`(): Unit = runBlockingTest {
        // Given
        val testFlow = "test-flow"
        val testChallengeFrame = ChallengeFrameDetails(
            flow = testFlow,
            challengeFrame = "test-challenge-frame",
            focusTime = 0,
            clicks = 1,
            copy = emptyList(),
            paste = emptyList(),
            keys = emptyList()
        )
        coEvery { repository.getFramesByFlow(testFlow) } returns listOf(
            testChallengeFrame,
            testChallengeFrame.copy(challengeFrame = "test-challenge-frame2")
        )
        coEvery { repository.getFramesByFlowAndFrame(testFlow, "test-challenge-frame") } returns testChallengeFrame
        // When
        val result = manager.getFrameByFlowAndFrameName(testFlow, "test-challenge-frame")
        // Then
        assertEquals(testChallengeFrame, result)
        coVerify(exactly = 0) { repository.getFramesByFlow(testFlow) }
        coVerify { repository.getFramesByFlowAndFrame(testFlow, "test-challenge-frame") }
    }

    @Test
    public fun `insert new frame works correctly`(): Unit = runBlockingTest {
        // Given
        val testFlow = "test-flow"
        val challengeFrame = "test-frame"
        coEvery { repository.insertFrameDetails(any()) } returns Unit
        // When
        manager.addOrUpdateFrameToFlow(testFlow, challengeFrame, 0, 0, emptyList(), emptyList(), emptyList())
        // Then
        val frame = ChallengeFrameDetails(
            testFlow, challengeFrame, 0, 0, emptyList(), emptyList(), emptyList()
        )
        coVerify { repository.insertFrameDetails(frame) }
    }
}
